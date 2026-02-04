package com.cts.vis.controller;

import com.cts.vis.model.Vehicle;
import com.cts.vis.model.VehicleType;
import com.cts.vis.service.ClaimService;
import com.cts.vis.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class VehicleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VehicleService vehicleService;

    @Mock
    private ClaimService claimService;

    @InjectMocks
    private VehicleController vehicleController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(vehicleController).build();
    }

    @Test
    public void testVehiclesListPage() throws Exception {
        Vehicle v = new Vehicle();
        v.setVehicleId(1L);
        List<Vehicle> vehicleList = new ArrayList<>();
        vehicleList.add(v);

        when(vehicleService.myVehicles()).thenReturn(vehicleList);
        when(claimService.hasApprovedClaimForVehicle(anyLong())).thenReturn(false);

        mockMvc.perform(get("/customer/vehicles"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/vehicles"))
                .andExpect(model().attributeExists("vehicles"))
                .andExpect(model().attributeExists("editDisabled"))
                .andExpect(model().attributeExists("types"));
    }

    @Test
    public void testAddVehicleSuccess() throws Exception {
        // Fix: Removed hyphens to satisfy ^[A-Z0-9]{5,12}$
        mockMvc.perform(post("/customer/vehicles/add")
                        .param("registrationNumber", "TN01AB1234")
                        .param("make", "Toyota")
                        .param("model", "Camry")
                        .param("yearOfManufacture", "2022")
                        .param("vehicleType", "CAR"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customer/vehicles?added=true"));

        verify(vehicleService).addVehicle(eq("TN01AB1234"), anyString(), anyString(), anyInt(), any(VehicleType.class));
    }

    @Test
    public void testAddVehicleDuplicateError() throws Exception {
        doThrow(new RuntimeException("Duplicate Reg No"))
                .when(vehicleService).addVehicle(anyString(), anyString(), anyString(), anyInt(), any(VehicleType.class));

        // Use valid format even for error testing to ensure we hit the Service, not the Validation layer
        mockMvc.perform(post("/customer/vehicles/add")
                        .param("registrationNumber", "DUPLICATE12")
                        .param("make", "Honda")
                        .param("model", "Civic")
                        .param("yearOfManufacture", "2021")
                        .param("vehicleType", "CAR"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/vehicles"))
                .andExpect(model().hasErrors());
    }

    @Test
    public void testEditLockedByApprovedClaim() throws Exception {
        when(claimService.hasApprovedClaimForVehicle(1L)).thenReturn(true);

        mockMvc.perform(get("/customer/vehicles/1/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customer/vehicles?editLocked=true"));
    }

    @Test
    public void testUpdateVehicleSuccess() throws Exception {
        when(claimService.hasApprovedClaimForVehicle(1L)).thenReturn(false);

        // Fix: Changed "NEW-REG" to "NEWREG123" to match ^[A-Z0-9]{5,12}$
        mockMvc.perform(post("/customer/vehicles/1/edit")
                        .param("registrationNumber", "NEWREG123")
                        .param("make", "Ford")
                        .param("model", "Mustang")
                        .param("yearOfManufacture", "2023")
                        .param("vehicleType", "CAR"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customer/vehicles?updated=true"));

        verify(vehicleService).updateVehicle(eq(1L), eq("NEWREG123"), anyString(), anyString(), anyInt(), any(VehicleType.class));
    }
}
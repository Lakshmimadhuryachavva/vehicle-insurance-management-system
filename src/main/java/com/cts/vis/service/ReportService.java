package com.cts.vis.service;

import java.util.Map;

public interface ReportService {
    Map<String, Object> customerDashboardStats();
    Map<String, Object> customerPolicyReport();
    Map<String, Object> customerClaimReport();

    Map<String, Object> adminDashboardStats();
}
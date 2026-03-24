package com.example.Library_backend.controller;

import com.example.Library_backend.dto.response.ApiResponse;
import com.example.Library_backend.dto.response.dashbordresponse.BorrowingTrendResponse;
import com.example.Library_backend.dto.response.dashbordresponse.BranchComparisonResponse;
import com.example.Library_backend.dto.response.dashbordresponse.DashboardResponse;
import com.example.Library_backend.dto.response.dashbordresponse.PopularBookResponse;
import com.example.Library_backend.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // GET /api/analytics/dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse> getDashboard() {
        DashboardResponse dashboard =
                analyticsService.getDashboard();
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Dashboard data fetched!", dashboard));
    }

    // GET /api/analytics/borrowing-trends?year=2024
    @GetMapping("/borrowing-trends")
    public ResponseEntity<ApiResponse>
    getBorrowingTrends(
            @RequestParam(defaultValue = "0") int year) {
        if (year == 0) {
            year = LocalDate.now().getYear();
        }
        BorrowingTrendResponse trends =
                analyticsService.getBorrowingTrends(year);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Borrowing trends fetched!", trends));
    }

    // GET /api/analytics/popular-books?limit=10
    @GetMapping("/popular-books")
    public ResponseEntity<ApiResponse> getPopularBooks(
            @RequestParam(defaultValue = "10")
            int limit) {
        List<PopularBookResponse> books =
                analyticsService.getPopularBooks(limit);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Popular books fetched!", books));
    }

    // GET /api/analytics/branch-comparison
    @GetMapping("/branch-comparison")
    public ResponseEntity<ApiResponse>
    getBranchComparison() {
        List<BranchComparisonResponse> branches =
                analyticsService.getBranchComparison();
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Branch comparison fetched!", branches));
    }

    // GET /api/analytics/overdue-summary
    @GetMapping("/overdue-summary")
    @PreAuthorize(
            "hasAnyRole('LIBRARIAN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse>
    getOverdueSummary() {
        Map<String, Object> summary =
                analyticsService.getOverdueSummary();
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Overdue summary fetched!", summary));
    }

    // GET /api/analytics/fine-collection?year=2024
    @GetMapping("/fine-collection")
    public ResponseEntity<ApiResponse>
    getFineCollection(
            @RequestParam(defaultValue = "0") int year) {
        if (year == 0) {
            year = LocalDate.now().getYear();
        }
        Map<String, Object> fines =
                analyticsService.getFineCollection(year);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Fine collection data fetched!", fines));
    }

    // GET /api/analytics/active-users?limit=10
    @GetMapping("/active-users")
    public ResponseEntity<ApiResponse> getActiveUsers(
            @RequestParam(defaultValue = "10")
            int limit) {
        List<Map<String, Object>> users =
                analyticsService.getActiveUsers(limit);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Active users fetched!", users));
    }

    // GET /api/analytics/category-breakdown
    @GetMapping("/category-breakdown")
    public ResponseEntity<ApiResponse>
    getCategoryBreakdown() {
        List<Map<String, Object>> breakdown =
                analyticsService.getCategoryBreakdown();
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Category breakdown fetched!",
                        breakdown));
    }
}
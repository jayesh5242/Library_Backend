package com.example.Library_backend.dto.response.dashbordresponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponse {

    // ── User Stats ────────────────────────────────
    private Long totalUsers;
    private Long totalStudents;
    private Long totalFaculty;
    private Long totalLibrarians;
    private Long activeUsers;
    private Long blockedUsers;

    // ── Book Stats ────────────────────────────────
    private Long totalBooks;
    private Long totalAvailableBooks;
    private Long totalBorrowedBooks;

    // ── Branch Stats ──────────────────────────────
    private Long totalBranches;

    // ── Borrowing Stats ───────────────────────────
    private Long totalBorrowings;
    private Long activeBorrowings;
    private Long overdueBooks;
    private Long returnedToday;

    // ── Fine Stats ────────────────────────────────
    private Double totalPendingFines;
    private Double totalCollectedFines;
    private Long pendingFineCount;

    // ── Reservation Stats ─────────────────────────
    private Long pendingReservations;
    private Long approvedReservations;

    // ── Quick Alerts (things needing attention) ───
    private List<String> alerts;
}
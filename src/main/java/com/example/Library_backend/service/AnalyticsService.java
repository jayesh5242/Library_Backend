package com.example.Library_backend.service;

import com.example.Library_backend.dto.response.dashbordresponse.BorrowingTrendResponse;
import com.example.Library_backend.dto.response.dashbordresponse.BranchComparisonResponse;
import com.example.Library_backend.dto.response.dashbordresponse.DashboardResponse;
import com.example.Library_backend.dto.response.dashbordresponse.PopularBookResponse;
import com.example.Library_backend.entity.Book;
import com.example.Library_backend.entity.BorrowTransaction;
import com.example.Library_backend.entity.User;
import com.example.Library_backend.enums.FineStatus;
import com.example.Library_backend.enums.ReservationStatus;
import com.example.Library_backend.enums.Role;
import com.example.Library_backend.enums.TransactionStatus;
import com.example.Library_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final BorrowTransactionRepository borrowRepo;
    private final FineRepository fineRepository;
    private final BookInventoryRepository inventoryRepo;
    private final ReservationRepository reservationRepo;

    // ─── API 1: MAIN DASHBOARD ────────────────────────
    public DashboardResponse getDashboard() {

        DashboardResponse dashboard =
                new DashboardResponse();

        // ── User Stats ──────────────────────────────
        dashboard.setTotalUsers(
                userRepository.count());
        dashboard.setTotalStudents(
                userRepository.countByRole(Role.STUDENT));
        dashboard.setTotalFaculty(
                userRepository.countByRole(Role.FACULTY));
        dashboard.setTotalLibrarians(
                userRepository.countByRole(Role.LIBRARIAN));
        dashboard.setActiveUsers(
                userRepository.countByIsActive(true));
        dashboard.setBlockedUsers(
                userRepository.countByIsActive(false));

        // ── Book Stats ──────────────────────────────
        Long totalBooks =
                inventoryRepo.getTotalBooks();
        Long availableBooks =
                inventoryRepo.getTotalAvailableBooks();
        dashboard.setTotalBooks(
                totalBooks != null ? totalBooks : 0L);
        dashboard.setTotalAvailableBooks(
                availableBooks != null ? availableBooks : 0L);

        // ── Branch Stats ────────────────────────────
        dashboard.setTotalBranches(
                (long) branchRepository
                        .findByIsActiveTrue().size());

        // ── Borrowing Stats ─────────────────────────
        dashboard.setTotalBorrowings(
                borrowRepo.count());
        dashboard.setActiveBorrowings(
                borrowRepo.countByStatus(
                        String.valueOf(TransactionStatus.BORROWED)));
        dashboard.setOverdueBooks(
                borrowRepo.countByStatus(
                        String.valueOf(TransactionStatus.OVERDUE)));

        // ── Fine Stats ──────────────────────────────
        Double pendingFines =
                fineRepository.getTotalPendingFinesAmount();
        Double collectedFines =
                fineRepository.getTotalCollectedAmount();
        dashboard.setTotalPendingFines(
                pendingFines != null ? pendingFines : 0.0);
        dashboard.setTotalCollectedFines(
                collectedFines != null ? collectedFines : 0.0);
        dashboard.setPendingFineCount(
                fineRepository.countByStatus(
                        String.valueOf(FineStatus.PENDING)));

        // ── Reservation Stats ───────────────────────
        dashboard.setPendingReservations(
                reservationRepo.countByStatus(
                        String.valueOf(ReservationStatus.PENDING)));
        dashboard.setApprovedReservations(
                reservationRepo.countByStatus(
                        String.valueOf(ReservationStatus.APPROVED)));

        // ── Alerts (things needing attention) ───────
        List<String> alerts = new ArrayList<>();

        if (dashboard.getOverdueBooks() > 0) {
            alerts.add("⚠️ " + dashboard.getOverdueBooks()
                    + " books are overdue!");
        }
        if (dashboard.getTotalPendingFines() > 0) {
            alerts.add("💰 Rs."
                    + dashboard.getTotalPendingFines()
                    + " in pending fines!");
        }
        if (dashboard.getPendingReservations() > 0) {
            alerts.add("📋 "
                    + dashboard.getPendingReservations()
                    + " reservations pending approval!");
        }
        if (alerts.isEmpty()) {
            alerts.add("✅ All systems running smoothly!");
        }
        dashboard.setAlerts(alerts);

        return dashboard;
    }

    // ─── API 2: BORROWING TRENDS ──────────────────────
    public BorrowingTrendResponse getBorrowingTrends(
            int year) {

        List<Object[]> results =
                borrowRepo.getMonthlyBorrowingCounts(year);

        // Month names array
        String[] monthNames = {
                "Jan","Feb","Mar","Apr","May","Jun",
                "Jul","Aug","Sep","Oct","Nov","Dec"
        };

        // Initialize all months with 0
        Long[] counts = new Long[12];
        Arrays.fill(counts, 0L);

        // Fill in actual data
        for (Object[] row : results) {
            int month = ((Number) row[0]).intValue() - 1;
            Long count = ((Number) row[1]).longValue();
            if (month >= 0 && month < 12) {
                counts[month] = count;
            }
        }

        Long yearTotal = Arrays.stream(counts)
                .mapToLong(Long::longValue).sum();

        return new BorrowingTrendResponse(
                year,
                Arrays.asList(monthNames),
                Arrays.asList(counts),
                yearTotal
        );
    }

    // ─── API 3: POPULAR BOOKS ─────────────────────────
    public List<PopularBookResponse> getPopularBooks(
            int limit) {

        List<Object[]> results =
                borrowRepo.getTopBorrowedBooks(
                        PageRequest.of(0, limit));

        List<PopularBookResponse> popularBooks =
                new ArrayList<>();

        for (int i = 0; i < results.size(); i++) {
            Object[] row = results.get(i);
            var book =
                    (Book) row[0];
            Long count = ((Number) row[1]).longValue();

            PopularBookResponse response =
                    new PopularBookResponse();
            response.setBookId(book.getId());
            response.setTitle(book.getTitle());
            response.setAuthor(book.getAuthor());
            response.setCategory(book.getCategory());
            response.setBorrowCount(count);
            response.setRank(i + 1);

            popularBooks.add(response);
        }

        return popularBooks;
    }

    // ─── API 4: BRANCH COMPARISON ─────────────────────
    public List<BranchComparisonResponse>
    getBranchComparison() {

        List<Object[]> borrowings =
                borrowRepo.getBranchWiseBorrowings();

        // Get total borrowings for percentage calc
        long totalBorrowings = borrowRepo.count();

        // Build map of branch name → count
        Map<String, Long> borrowMap = new HashMap<>();
        for (Object[] row : borrowings) {
            String branchName = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            borrowMap.put(branchName, count);
        }

        List<BranchComparisonResponse> responses =
                new ArrayList<>();

        branchRepository.findByIsActiveTrue()
                .forEach(branch -> {
                    BranchComparisonResponse response =
                            new BranchComparisonResponse();
                    response.setBranchId(branch.getId());
                    response.setBranchName(branch.getName());
                    response.setDepartment(
                            branch.getDepartment());

                    Long count = borrowMap.getOrDefault(
                            branch.getName(), 0L);
                    response.setTotalBorrowings(count);

                    // Calculate percentage
                    double pct = totalBorrowings > 0
                            ? (count * 100.0) / totalBorrowings
                            : 0.0;
                    response.setBorrowingPercentage(
                            Math.round(pct * 10.0) / 10.0);

                    // Librarian info
                    if (branch.getLibrarian() != null) {
                        response.setLibrarianName(
                                branch.getLibrarian()
                                        .getFullName());
                    }

                    // Book stats
                    Long total =
                            inventoryRepo.getTotalBooks();
                    Long available =
                            inventoryRepo.getTotalAvailableBooks();
                    response.setTotalBooks(
                            total != null ? total : 0L);
                    response.setAvailableBooks(
                            available != null ? available : 0L);

                    responses.add(response);
                });

        return responses;
    }

    // ─── API 5: OVERDUE SUMMARY ───────────────────────
    public Map<String, Object> getOverdueSummary() {

        Map<String, Object> summary = new HashMap<>();

        List<BorrowTransaction>
                overdueList = borrowRepo.getOverdueTransactions(
                LocalDate.now());

        summary.put("totalOverdue", overdueList.size());

        // Group by branch
        Map<String, Long> byBranch = overdueList.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getBranch().getName(),
                        Collectors.counting()
                ));
        summary.put("overdueByBranch", byBranch);

        // Average days overdue
        double avgDays = overdueList.stream()
                .mapToLong(t ->
                        LocalDate.now().toEpochDay()
                                - t.getDueDate().toEpochDay())
                .average()
                .orElse(0.0);
        summary.put("averageDaysOverdue",
                Math.round(avgDays * 10.0) / 10.0);

        // Most overdue book
        overdueList.stream()
                .max(Comparator.comparingLong(t ->
                        LocalDate.now().toEpochDay()
                                - t.getDueDate().toEpochDay()))
                .ifPresent(t -> {
                    summary.put("mostOverdueBook",
                            t.getBook().getTitle());
                    summary.put("mostOverdueDays",
                            LocalDate.now().toEpochDay()
                                    - t.getDueDate().toEpochDay());
                });

        return summary;
    }

    // ─── API 6: FINE COLLECTION ───────────────────────
    public Map<String, Object> getFineCollection(
            int year) {

        Map<String, Object> result = new HashMap<>();

        List<Object[]> monthly =
                fineRepository.getMonthlyFineCollection(year);

        String[] monthNames = {
                "Jan","Feb","Mar","Apr","May","Jun",
                "Jul","Aug","Sep","Oct","Nov","Dec"
        };

        Double[] amounts = new Double[12];
        Arrays.fill(amounts, 0.0);

        for (Object[] row : monthly) {
            int month = ((Number) row[0]).intValue() - 1;
            Double amount = ((Number) row[1]).doubleValue();
            if (month >= 0 && month < 12) {
                amounts[month] = amount;
            }
        }

        double yearTotal = Arrays.stream(amounts)
                .mapToDouble(Double::doubleValue).sum();

        result.put("year", year);
        result.put("months", Arrays.asList(monthNames));
        result.put("amounts", Arrays.asList(amounts));
        result.put("yearTotal",
                Math.round(yearTotal * 100.0) / 100.0);
        result.put("pendingAmount",
                fineRepository
                        .getTotalPendingFinesAmount());
        result.put("totalCollected",
                fineRepository.getTotalCollectedAmount());

        return result;
    }

    // ─── API 7: ACTIVE USERS ──────────────────────────
    public List<Map<String, Object>> getActiveUsers(
            int limit) {

        List<Object[]> results =
                borrowRepo.getMostActiveUsers(
                        PageRequest.of(0, limit));

        List<Map<String, Object>> activeUsers =
                new ArrayList<>();

        for (int i = 0; i < results.size(); i++) {
            Object[] row = results.get(i);
            var user =
                    (User) row[0];
            Long count = ((Number) row[1]).longValue();

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("rank", i + 1);
            userMap.put("userId", user.getId());
            userMap.put("fullName", user.getFullName());
            userMap.put("email", user.getEmail());
            userMap.put("role", user.getRole().name());
            userMap.put("department",
                    user.getDepartment());
            userMap.put("totalBorrowings", count);
            activeUsers.add(userMap);
        }

        return activeUsers;
    }

    // ─── API 8: CATEGORY BREAKDOWN ────────────────────
    public List<Map<String, Object>>
    getCategoryBreakdown() {

        List<Object[]> results =
                borrowRepo.getCategoryWiseBorrowings();

        long total = borrowRepo.count();

        List<Map<String, Object>> breakdown =
                new ArrayList<>();

        for (Object[] row : results) {
            String category = (String) row[0];
            Long count = ((Number) row[1]).longValue();

            Map<String, Object> item = new HashMap<>();
            item.put("category", category);
            item.put("borrowCount", count);

            double pct = total > 0
                    ? (count * 100.0) / total : 0.0;
            item.put("percentage",
                    Math.round(pct * 10.0) / 10.0);

            breakdown.add(item);
        }

        return breakdown;
    }
}
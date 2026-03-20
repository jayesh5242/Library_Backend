package com.example.Library_backend.service;

import com.example.Library_backend.entity.BorrowTransaction;
import com.example.Library_backend.enums.FineStatus;
import com.example.Library_backend.enums.Role;
import com.example.Library_backend.enums.TransactionStatus;
import com.example.Library_backend.repository.BookInventoryRepository;
import com.example.Library_backend.repository.BorrowTransactionRepository;
import com.example.Library_backend.repository.FineRepository;
import com.example.Library_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final BorrowTransactionRepository borrowRepo;
    private final FineRepository fineRepository;
    private final BookInventoryRepository inventoryRepo;
    private final UserRepository userRepository;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // ─── Helper: Build HTML Report ────────────────────
    // We generate HTML report (simpler than PDF library)
    // Frontend can print this as PDF

    // ─── API 1: INVENTORY REPORT ──────────────────────
    public String generateInventoryReport() {

        Long total =
                inventoryRepo.getTotalBooks();
        Long available =
                inventoryRepo.getTotalAvailableBooks();
        Long borrowed = total != null && available != null
                ? (total - available) : 0L;

        StringBuilder html = new StringBuilder();
        html.append(getHtmlHeader(
                "Library Inventory Report"));
        html.append("<h2>📚 Inventory Report</h2>");
        html.append("<p>Generated: ")
                .append(LocalDate.now().format(FMT))
                .append("</p>");
        html.append("<table>");
        html.append("<tr><th>Metric</th>")
                .append("<th>Count</th></tr>");
        html.append("<tr><td>Total Books</td><td>")
                .append(total != null ? total : 0)
                .append("</td></tr>");
        html.append("<tr><td>Available Books</td><td>")
                .append(available != null ? available : 0)
                .append("</td></tr>");
        html.append("<tr><td>Currently Borrowed</td><td>")
                .append(borrowed)
                .append("</td></tr>");
        html.append("<tr><td>Out of Stock Books</td><td>")
                .append(inventoryRepo
                        .getOutOfStockBooks().size())
                .append("</td></tr>");
        html.append("</table>");
        html.append(getHtmlFooter());

        return html.toString();
    }

    // ─── API 2: BORROWING REPORT ──────────────────────
    public String generateBorrowingReport() {

        List<BorrowTransaction> overdue =
                borrowRepo.getOverdueTransactions(
                        LocalDate.now());

        long totalBorrowings = borrowRepo.count();
        long activeBorrowings = borrowRepo
                .countByStatus(String.valueOf(TransactionStatus.BORROWED));
        long overdueCount = overdue.size();

        StringBuilder html = new StringBuilder();
        html.append(getHtmlHeader(
                "Borrowing Report"));
        html.append("<h2>📖 Borrowing Report</h2>");
        html.append("<p>Generated: ")
                .append(LocalDate.now().format(FMT))
                .append("</p>");

        html.append("<table>");
        html.append("<tr><th>Metric</th>")
                .append("<th>Count</th></tr>");
        html.append("<tr><td>Total Borrowings</td><td>")
                .append(totalBorrowings)
                .append("</td></tr>");
        html.append("<tr><td>Currently Borrowed</td><td>")
                .append(activeBorrowings)
                .append("</td></tr>");
        html.append("<tr><td>Overdue Books</td><td>")
                .append(overdueCount)
                .append("</td></tr>");
        html.append("</table>");

        if (!overdue.isEmpty()) {
            html.append("<h3>⚠️ Overdue Books List</h3>");
            html.append("<table>");
            html.append("<tr>")
                    .append("<th>Student</th>")
                    .append("<th>Book</th>")
                    .append("<th>Due Date</th>")
                    .append("<th>Days Overdue</th>")
                    .append("</tr>");

            for (BorrowTransaction t : overdue) {
                long daysOverdue =
                        LocalDate.now().toEpochDay()
                                - t.getDueDate().toEpochDay();
                html.append("<tr>")
                        .append("<td>")
                        .append(t.getUser().getFullName())
                        .append("</td>")
                        .append("<td>")
                        .append(t.getBook().getTitle())
                        .append("</td>")
                        .append("<td>")
                        .append(t.getDueDate().format(FMT))
                        .append("</td>")
                        .append("<td>")
                        .append(daysOverdue)
                        .append(" days</td>")
                        .append("</tr>");
            }
            html.append("</table>");
        }

        html.append(getHtmlFooter());
        return html.toString();
    }

    // ─── API 3: FINE REPORT ───────────────────────────
    public String generateFineReport() {

        Double pending =
                fineRepository.getTotalPendingFinesAmount();
        Double collected =
                fineRepository.getTotalCollectedAmount();
        long pendingCount = fineRepository
                .countByStatus(String.valueOf(FineStatus.PENDING));

        StringBuilder html = new StringBuilder();
        html.append(getHtmlHeader("Fine Report"));
        html.append("<h2>💰 Fine Collection Report</h2>");
        html.append("<p>Generated: ")
                .append(LocalDate.now().format(FMT))
                .append("</p>");

        html.append("<table>");
        html.append("<tr><th>Metric</th>")
                .append("<th>Amount</th></tr>");
        html.append("<tr><td>Total Pending Fines</td>")
                .append("<td>Rs.")
                .append(pending != null ? pending : 0.0)
                .append("</td></tr>");
        html.append("<tr><td>Total Collected</td>")
                .append("<td>Rs.")
                .append(collected != null ? collected : 0.0)
                .append("</td></tr>");
        html.append("<tr><td>Pending Fine Count</td>")
                .append("<td>")
                .append(pendingCount)
                .append(" students</td></tr>");
        html.append("</table>");

        html.append(getHtmlFooter());
        return html.toString();
    }

    // ─── API 4: SEMESTER REPORT ───────────────────────
    public String generateSemesterReport() {

        int year = LocalDate.now().getYear();

        StringBuilder html = new StringBuilder();
        html.append(getHtmlHeader(
                "Semester Report " + year));
        html.append("<h2>🎓 Semester Report — ")
                .append(year).append("</h2>");
        html.append("<p>Generated: ")
                .append(LocalDate.now().format(FMT))
                .append("</p>");

        html.append("<h3>User Summary</h3>");
        html.append("<table>");
        html.append("<tr><th>Role</th>")
                .append("<th>Count</th></tr>");
        html.append("<tr><td>Students</td><td>")
                .append(userRepository.countByRole(
                    Role.STUDENT))
                .append("</td></tr>");
        html.append("<tr><td>Faculty</td><td>")
                .append(userRepository.countByRole(
                     Role.FACULTY))
                .append("</td></tr>");
        html.append("<tr><td>Librarians</td><td>")
                .append(userRepository.countByRole(
                        Role.LIBRARIAN))
                .append("</td></tr>");
        html.append("</table>");

        html.append("<h3>Library Summary</h3>");
        html.append("<table>");
        html.append("<tr><th>Metric</th>")
                .append("<th>Value</th></tr>");
        html.append("<tr><td>Total Borrowings</td><td>")
                .append(borrowRepo.count())
                .append("</td></tr>");
        html.append("<tr><td>Overdue Books</td><td>")
                .append(borrowRepo.countByStatus(
                        String.valueOf(TransactionStatus.OVERDUE)))
                .append("</td></tr>");
        html.append("<tr><td>Fines Collected</td>")
                .append("<td>Rs.")
                .append(fineRepository
                        .getTotalCollectedAmount())
                .append("</td></tr>");
        html.append("</table>");

        html.append(getHtmlFooter());
        return html.toString();
    }

    // ─── API 5: OVERDUE REPORT ────────────────────────
    public String generateOverdueReport() {

        List<BorrowTransaction> overdue =
                borrowRepo.getOverdueTransactions(
                        LocalDate.now());

        StringBuilder html = new StringBuilder();
        html.append(getHtmlHeader("Overdue Report"));
        html.append("<h2>⚠️ Overdue Books Report</h2>");
        html.append("<p>Generated: ")
                .append(LocalDate.now().format(FMT))
                .append("</p>");
        html.append("<p><strong>Total Overdue: ")
                .append(overdue.size())
                .append("</strong></p>");

        if (overdue.isEmpty()) {
            html.append(
                    "<p>✅ No overdue books!</p>");
        } else {
            html.append("<table>");
            html.append("<tr>")
                    .append("<th>Student</th>")
                    .append("<th>Email</th>")
                    .append("<th>Book</th>")
                    .append("<th>Branch</th>")
                    .append("<th>Due Date</th>")
                    .append("<th>Days Overdue</th>")
                    .append("</tr>");

            for (BorrowTransaction t : overdue) {
                long days =
                        LocalDate.now().toEpochDay()
                                - t.getDueDate().toEpochDay();
                html.append("<tr>")
                        .append("<td>")
                        .append(t.getUser().getFullName())
                        .append("</td>")
                        .append("<td>")
                        .append(t.getUser().getEmail())
                        .append("</td>")
                        .append("<td>")
                        .append(t.getBook().getTitle())
                        .append("</td>")
                        .append("<td>")
                        .append(t.getBranch().getName())
                        .append("</td>")
                        .append("<td>")
                        .append(t.getDueDate().format(FMT))
                        .append("</td>")
                        .append("<td style='color:red'>")
                        .append(days)
                        .append(" days</td>")
                        .append("</tr>");
            }
            html.append("</table>");
        }

        html.append(getHtmlFooter());
        return html.toString();
    }

    // ─── HTML Helpers ──────────────────────────────────
    private String getHtmlHeader(String title) {
        return "<!DOCTYPE html><html><head>"
                + "<title>" + title + "</title>"
                + "<style>"
                + "body{font-family:Arial;margin:20px;"
                + "color:#333}"
                + "h2{color:#1A237E;"
                + "border-bottom:2px solid #1565C0;"
                + "padding-bottom:8px}"
                + "h3{color:#1565C0}"
                + "table{width:100%;border-collapse:collapse;"
                + "margin:15px 0}"
                + "th{background:#1A237E;color:white;"
                + "padding:10px;text-align:left}"
                + "td{padding:8px;border:1px solid #ddd}"
                + "tr:nth-child(even){background:#f5f5f5}"
                + ".footer{margin-top:30px;"
                + "text-align:center;color:#666;"
                + "font-size:12px}"
                + "</style></head><body>"
                + "<div style='text-align:center;"
                + "background:#1A237E;color:white;"
                + "padding:20px;margin-bottom:20px'>"
                + "<h1>📚 College Library Network</h1>"
                + "<p>" + title + "</p>"
                + "</div>";
    }

    private String getHtmlFooter() {
        return "<div class='footer'>"
                + "<p>Generated by College Library "
                + "Network System</p>"
                + "<p>"
                + LocalDate.now().format(FMT)
                + "</p></div>"
                + "</body></html>";
    }
}
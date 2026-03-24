package com.example.Library_backend.controller;

import com.example.Library_backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // GET /api/reports/inventory
    @GetMapping("/inventory")
    @PreAuthorize(
            "hasAnyRole('LIBRARIAN','SUPER_ADMIN')")
    public ResponseEntity<String> inventoryReport() {
        String html =
                reportService.generateInventoryReport();
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    // GET /api/reports/borrowing
    @GetMapping("/borrowing")
    @PreAuthorize(
            "hasAnyRole('LIBRARIAN','SUPER_ADMIN')")
    public ResponseEntity<String> borrowingReport() {
        String html =
                reportService.generateBorrowingReport();
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    // GET /api/reports/fines
    @GetMapping("/fines")
    @PreAuthorize(
            "hasAnyRole('LIBRARIAN','SUPER_ADMIN')")
    public ResponseEntity<String> fineReport() {
        String html =
                reportService.generateFineReport();
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    // GET /api/reports/semester
    @GetMapping("/semester")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> semesterReport() {
        String html =
                reportService.generateSemesterReport();
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    // GET /api/reports/overdue
    @GetMapping("/overdue")
    @PreAuthorize(
            "hasAnyRole('LIBRARIAN','SUPER_ADMIN')")
    public ResponseEntity<String> overdueReport() {
        String html =
                reportService.generateOverdueReport();
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }
}

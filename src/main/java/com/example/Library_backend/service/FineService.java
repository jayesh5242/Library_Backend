package com.example.Library_backend.service;

import com.example.Library_backend.dto.response.ApiResponse;
import com.example.Library_backend.dto.response.FineResponse;
import com.example.Library_backend.dto.response.PageResponse;
import com.example.Library_backend.entity.Fine;
import com.example.Library_backend.enums.FineStatus;
import com.example.Library_backend.repository.FineRepository;
import com.example.Library_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FineService {

    private final FineRepository repo;
    private final UserRepository userRepo;
    private final HelperService helperService;

    public ApiResponse<PageResponse<FineResponse>> my(Pageable pageable, Long userId) {
        try {

            Page<FineResponse> data = repo.findByUserIdWithPagination(userId, pageable)
                    .map(this::map);

            return new ApiResponse<>(true, "Fetched fines", helperService.toPageResponse(data));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to fetch fines", null);
        }
    }

    public ApiResponse<Double> myTotal(Long userId) {
        try {
            List<Fine> fines = repo.findByUserIdAndStatus(userId, FineStatus.PENDING);

            double total = fines.stream()
                    .mapToDouble(f -> f.getTotalAmount() - f.getPaidAmount())
                    .sum();

            return new ApiResponse<>(true, "Total calculated", total);

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to calculate total", null);
        }
    }

    public ApiResponse<PageResponse<FineResponse>> all(Pageable pageable) {
        try {
            Page<FineResponse> data = repo.findAll(pageable)
                    .map(this::map);

            return new ApiResponse<>(true, "All fines",  helperService.toPageResponse(data));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed", null);
        }
    }

    public ApiResponse<PageResponse<FineResponse>> pending(Pageable pageable) {
        try {
            Page<FineResponse> data = repo.findByStatus(FineStatus.PENDING, pageable)
                    .map(this::map);

            return new ApiResponse<>(true, "Pending fines",  helperService.toPageResponse(data));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed", null);
        }
    }

    public ApiResponse<PageResponse<FineResponse>> byBranch(Long branchId, Pageable pageable) {
        try {
            Page<FineResponse> data = repo.findByBranch(branchId, pageable)
                    .map(this::map);

            return new ApiResponse<>(true, "Branch fines",  helperService.toPageResponse(data));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed", null);
        }
    }

    public ApiResponse<FineResponse> pay(Long id) {
        try {

            if (!repo.existsById(id)) {
                return new ApiResponse<>(false, "Fine not found", null);
            }

            Fine f = repo.findById(id).get();

            f.setPaidAmount(f.getTotalAmount());
            f.setStatus(FineStatus.PAID);
            f.setPaidAt(LocalDateTime.now());

            repo.save(f);

            return new ApiResponse<>(true, "Fine paid", map(f));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to pay", null);
        }
    }

    public ApiResponse<FineResponse> waive(Long id, String reason,Long userId) {
        try {

            if (!repo.existsById(id)) {
                return new ApiResponse<>(false, "Fine not found", null);
            }

            if (!userRepo.existsById(userId)) {
                return new ApiResponse<>(false, "User not found", null);
            }

            Fine f = repo.findById(id).get();

            f.setStatus(FineStatus.WAIVED);
            f.setWaivedBy(userRepo.findById(userId).get());

            repo.save(f);

            return new ApiResponse<>(true, "Fine waived", map(f));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to waive", null);
        }
    }

    public ApiResponse<FineResponse> partialPay(Long id, Double amount) {
        try {

            if (!repo.existsById(id)) {
                return new ApiResponse<>(false, "Fine not found", null);
            }

            Fine f = repo.findById(id).get();

            double newPaid = f.getPaidAmount() + amount;

            if (newPaid >= f.getTotalAmount()) {
                f.setPaidAmount(f.getTotalAmount());
                f.setStatus(FineStatus.PAID);
                f.setPaidAt(LocalDateTime.now());
            } else {
                f.setPaidAmount(newPaid);
                f.setStatus(FineStatus.PARTIAL);
            }

            repo.save(f);

            return new ApiResponse<>(true, "Partial payment recorded", map(f));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed partial payment", null);
        }
    }

    public ApiResponse<PageResponse<FineResponse>> byUser(Long userId, Pageable pageable) {
        try {
            Page<FineResponse> data = repo.findByUserIdWithPagination(userId, pageable)
                    .map(this::map);

            return new ApiResponse<>(true, "User fines",  helperService.toPageResponse(data));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed", null);
        }
    }

    public ApiResponse<Map<String, Double>> summary() {
        try {

            List<Fine> fines = repo.findAll();

            double total = fines.stream().mapToDouble(Fine::getTotalAmount).sum();
            double collected = fines.stream().mapToDouble(Fine::getPaidAmount).sum();

            Map<String, Double> map = new HashMap<>();
            map.put("total_amount", total);
            map.put("collected_amount", collected);
            map.put("pending_amount", total - collected);

            return new ApiResponse<>(true, "Summary", map);

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed summary", null);
        }
    }

    private FineResponse map(Fine f) {
        return FineResponse.builder()
                .id(f.getId())
                .userId(f.getUser().getId())
                .transactionId(f.getTransaction().getId())
                .daysOverdue(f.getDaysOverdue())
                .amountPerDay(f.getAmountPerDay())
                .totalAmount(f.getTotalAmount())
                .paidAmount(f.getPaidAmount())
                .status(f.getStatus())
                .paidAt(f.getPaidAt())
                .createdAt(f.getCreatedAt())
                .build();
    }
}

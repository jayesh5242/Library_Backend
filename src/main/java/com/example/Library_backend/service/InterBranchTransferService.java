package com.example.Library_backend.service;

import com.example.Library_backend.dto.request.CreateTransferRequest;
import com.example.Library_backend.dto.response.ApiResponse;
import com.example.Library_backend.dto.response.TransferResponse;
import com.example.Library_backend.dto.response.authresponse.PagedResponse;
import com.example.Library_backend.entity.Book;
import com.example.Library_backend.entity.Branch;
import com.example.Library_backend.entity.InterBranchTransfer;
import com.example.Library_backend.entity.User;
import com.example.Library_backend.enums.TransferStatus;
import com.example.Library_backend.repository.BookRepository;
import com.example.Library_backend.repository.BranchRepository;
import com.example.Library_backend.repository.InterBranchTransferRepository;
import com.example.Library_backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class InterBranchTransferService {

    private final InterBranchTransferRepository repo;
    private final BookRepository bookRepo;
    private final BranchRepository branchRepo;
    private final UserRepository userRepo;
    private final HelperService helperService;

    public ApiResponse<TransferResponse> create(CreateTransferRequest req) {
        try {

            if (!bookRepo.existsById(req.getBookId())) {
                return new ApiResponse<>(false, "Book not found", null);
            }

            if (!branchRepo.existsById(req.getFromBranchId())) {
                return new ApiResponse<>(false, "From branch not found", null);
            }

            if (!branchRepo.existsById(req.getToBranchId())) {
                return new ApiResponse<>(false, "To branch not found", null);
            }

            if (!userRepo.existsById(req.getRequestedBy())) {
                return new ApiResponse<>(false, "User not found", null);
            }

            Book book = bookRepo.findById(req.getBookId()).get();
            Branch from = branchRepo.findById(req.getFromBranchId()).get();
            Branch to = branchRepo.findById(req.getToBranchId()).get();
            User user = userRepo.findById(req.getRequestedBy()).get();

            InterBranchTransfer t = InterBranchTransfer.builder()
                    .book(book)
                    .fromBranch(from)
                    .toBranch(to)
                    .requestedBy(user)
                    .status(TransferStatus.REQUESTED)
                    .notes(req.getNotes())
                    .build();

            repo.save(t);

            return new ApiResponse<>(true, "Transfer requested successfully", map(t));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to create transfer", null);
        }
    }

    public ApiResponse<PagedResponse<TransferResponse>> my(Pageable pageable, Long userId) {
        try {

            PagedResponse<TransferResponse> data = helperService.toPagedResponse(
                    repo.findByRequestedById(userId, pageable)
                            .map(this::map),
                    "Fetched transfers"
            );

            return new ApiResponse<>(true, "Fetched transfers", data);

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to fetch transfers", null);
        }
    }

    public ApiResponse<PagedResponse<TransferResponse>> outgoing(Long branchId, Pageable pageable) {
        try {

            PagedResponse<TransferResponse> data = helperService.toPagedResponse(
                    repo.findByFromBranchId(branchId, pageable)
                            .map(this::map),
                    "Outgoing transfers fetched"
            );

            return new ApiResponse<>(true, "Outgoing transfers fetched", data);

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to fetch outgoing", null);
        }
    }

    public ApiResponse<PagedResponse<TransferResponse>> incoming(Long branchId, Pageable pageable) {
        try {

            PagedResponse<TransferResponse> data = helperService.toPagedResponse(
                    repo.findByToBranchId(branchId, pageable)
                            .map(this::map),
                    "Incoming transfers fetched"
            );

            return new ApiResponse<>(true, "Incoming transfers fetched", data);

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to fetch incoming", null);
        }
    }

    public ApiResponse<TransferResponse> approve(Long id,Long userId) {
        try {

            if (!repo.existsById(id)) {
                return new ApiResponse<>(false, "Transfer not found", null);
            }

            InterBranchTransfer t = repo.findById(id).get();

            if (!userRepo.existsById(userId)) {
                return new ApiResponse<>(false, "Approver not found", null);
            }

            t.setStatus(TransferStatus.APPROVED);
            t.setApprovedBy(userRepo.findById(userId).get());

            repo.save(t);

            return new ApiResponse<>(true, "Approved", map(t));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to approve", null);
        }
    }

    public ApiResponse<TransferResponse> dispatch(Long id) {
        try {

            if (!repo.existsById(id)) {
                return new ApiResponse<>(false, "Transfer not found", null);
            }

            InterBranchTransfer t = repo.findById(id).get();

            t.setStatus(TransferStatus.DISPATCHED);

            repo.save(t);

            return new ApiResponse<>(true, "Dispatched", map(t));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to dispatch", null);
        }
    }

    public ApiResponse<TransferResponse> receive(Long id) {
        try {

            if (!repo.existsById(id)) {
                return new ApiResponse<>(false, "Transfer not found", null);
            }

            InterBranchTransfer t = repo.findById(id).get();

            t.setStatus(TransferStatus.RECEIVED);
            t.setCompletionDate(LocalDateTime.now());

            repo.save(t);

            return new ApiResponse<>(true, "Received", map(t));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to receive", null);
        }
    }

    public ApiResponse<TransferResponse> reject(Long id, String reason) {
        try {

            if (!repo.existsById(id)) {
                return new ApiResponse<>(false, "Transfer not found", null);
            }

            InterBranchTransfer t = repo.findById(id).get();

            t.setStatus(TransferStatus.REJECTED);
            t.setNotes(reason);

            repo.save(t);

            return new ApiResponse<>(true, "Transfer Rejected Successfully", map(t));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to reject", null);
        }
    }

    public ApiResponse<PagedResponse<TransferResponse>> all(Pageable pageable) {
        try {

            PagedResponse<TransferResponse> data = helperService.toPagedResponse(
                    repo.findAll(pageable)
                            .map(this::map),
                    "All transfers"
            );

            return new ApiResponse<>(true, "All transfers", data);

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to fetch", null);
        }
    }

    public ApiResponse<TransferResponse> get(Long id) {
        try {

            if (!repo.existsById(id)) {
                return new ApiResponse<>(false, "Transfer not found", null);
            }

            InterBranchTransfer t = repo.findById(id).get();

            return new ApiResponse<>(true, "Fetched", map(t));

        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(false, "Failed to fetch", null);
        }
    }

    private TransferResponse map(InterBranchTransfer t) {
        return TransferResponse.builder()
                .id(t.getId())
                .bookId(t.getBook().getId())
                .bookName(t.getBook().getTitle())
                .fromBranchId(t.getFromBranch().getId())
                .fromBranchName(t.getFromBranch().getName())
                .toBranchId(t.getToBranch().getId())
                .toBranchName(t.getToBranch().getName())
                .requestedBy(t.getRequestedBy().getId())
                .approvedBy(t.getApprovedBy() != null ? t.getApprovedBy().getId() : null)
                .status(t.getStatus())
                .requestDate(t.getRequestDate())
                .completionDate(t.getCompletionDate())
                .notes(t.getNotes())
                .build();
    }
}

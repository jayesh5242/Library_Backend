package com.example.Library_backend.repository;

import com.example.Library_backend.entity.InterBranchTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterBranchTransferRepository extends JpaRepository<InterBranchTransfer,Long> {


    Page<InterBranchTransfer> findByRequestedById(Long userId, Pageable pageable);

    Page<InterBranchTransfer> findByFromBranchId(Long branchId, Pageable pageable);

    Page<InterBranchTransfer> findByToBranchId(Long branchId, Pageable pageable);
}

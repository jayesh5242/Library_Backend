package com.example.Library_backend.repository;


import com.example.Library_backend.entity.User;
import com.example.Library_backend.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByEmailVerifyToken(String token);
    Optional<User> findByPasswordResetToken(String token);


    // ── NEW METHODS for User Management ──────────────────

    // Get all users with pagination
    Page<User> findAll(Pageable pageable);

    // Get users by role
    Page<User> findByRole(Role role, Pageable pageable);

    // Get active/inactive users
    Page<User> findByIsActive(
            Boolean isActive, Pageable pageable);

    // Search users by name OR email OR enrollmentNo
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.fullName) LIKE LOWER(CONCAT('%',:query,'%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%',:query,'%')) OR " +
            "LOWER(u.enrollmentNo) LIKE LOWER(CONCAT('%',:query,'%')) OR " +
            "LOWER(u.employeeId) LIKE LOWER(CONCAT('%',:query,'%')) OR " +
            "LOWER(u.department) LIKE LOWER(CONCAT('%',:query,'%'))")
    Page<User> searchUsers(
            @Param("query") String query, Pageable pageable);

    // Count users by role
    long countByRole(Role role);

    // Count active users
    long countByIsActive(Boolean isActive);

    // Check if enrollment number exists (excluding current user)
    boolean existsByEnrollmentNoAndIdNot(
            String enrollmentNo, Long id);

    // Check if employee ID exists (excluding current user)
    boolean existsByEmployeeIdAndIdNot(
            String employeeId, Long id);

    // Count users assigned to a branch
    @Query(value = "SELECT COUNT(*) FROM users WHERE branch_id = :branchId",
            nativeQuery = true)
    Integer countUsersByBranchId(@Param("branchId") Long branchId);
}

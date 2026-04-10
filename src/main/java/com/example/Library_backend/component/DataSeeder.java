package com.example.Library_backend.component;

import com.example.Library_backend.entity.Branch;
import com.example.Library_backend.entity.User;
import com.example.Library_backend.enums.Role;
import com.example.Library_backend.repository.BranchRepository;
import com.example.Library_backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
            BranchRepository branchRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        seedBranchIfEmpty();
        seedAdminIfEmpty();
    }

    private void seedBranchIfEmpty() {
        if (branchRepository.count() == 0) {
            log.info("Seeding initial branch...");
            Branch branch = new Branch();
            branch.setName("Main University Library");
            branch.setDepartment("Central");
            branch.setLocation("Ground Floor, Academic Block");
            branch.setPhone("9000000001");
            branch.setEmail("library@univ.edu");
            branch.setOperatingHours("Mon-Sat 8am-8pm");
            branch.setMaxBorrowDays(14);
            branch.setFinePerDay(BigDecimal.valueOf(2.0));
            branch.setIsActive(true);
            branch.setCreatedAt(LocalDateTime.now());
            branchRepository.save(branch);
            log.info("Initial branch seeded.");
        }
    }

    private void seedAdminIfEmpty() {
        User admin = userRepository.findByEmail("admin@library.com").orElse(null);

        if (admin == null) {
            log.info("Seeding Super Admin account...");
            admin = new User();
            admin.setFullName("Super Admin");
            admin.setEmail("admin@library.com");
            admin.setRole(Role.SUPER_ADMIN);
            admin.setIsActive(true);
            admin.setIsEmailVerified(true);
            admin.setCreatedAt(LocalDateTime.now());

            // Assign to the first branch
            branchRepository.findAll().stream().findFirst().ifPresent(admin::setBranch);

            admin.setPassword(passwordEncoder.encode("Admin@5979"));
            admin.setUpdatedAt(LocalDateTime.now());
            userRepository.save(admin);
            log.info("Super Admin account created (admin@library.com / Admin@123).");
        } else {
            log.info("Admin account already exists. Skipping seeding.");
        }
    }
}

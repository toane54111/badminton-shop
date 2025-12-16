package com.badmintonshop.config;

import com.badmintonshop.entity.Staff;
import com.badmintonshop.entity.enums.StaffRole;
import com.badmintonshop.entity.enums.StaffStatus;
import com.badmintonshop.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Data seeder to create default super admin on application startup
 * Credentials are read from application-secrets.properties
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    // Super admin credentials from properties
    @Value("${app.admin.email}")
    private String superAdminEmail;

    @Value("${app.admin.password}")
    private String superAdminPassword;

    @Value("${app.admin.name}")
    private String superAdminName;

    @Value("${app.admin.phone}")
    private String superAdminPhone;

    @Override
    public void run(String... args) {
        createSuperAdminIfNotExists();
    }

    /**
     * Create super admin if not exists, or reset password if exists
     */
    private void createSuperAdminIfNotExists() {
        // Check if this specific email already exists
        var existingStaff = staffRepository.findByEmail(superAdminEmail);

        if (existingStaff.isEmpty()) {
            log.info("Super admin email not found. Creating default super admin...");

            Staff superAdmin = Staff.builder()
                    .email(superAdminEmail)
                    .passwordHash(passwordEncoder.encode(superAdminPassword))
                    .fullName(superAdminName)
                    .phone(superAdminPhone)
                    .role(StaffRole.SUPER_ADMIN)
                    .status(StaffStatus.ACTIVE)
                    .build();

            staffRepository.save(superAdmin);

            log.info("==============================================");
            log.info("DEFAULT SUPER ADMIN CREATED SUCCESSFULLY!");
            log.info("Email: {}", superAdminEmail);
            log.info("Password: [HIDDEN]");
            log.info("Please change the password after first login!");
            log.info("==============================================");
        } else {
            // Reset password and ensure account is active
            Staff staff = existingStaff.get();
            staff.setPasswordHash(passwordEncoder.encode(superAdminPassword));
            staff.setStatus(StaffStatus.ACTIVE);
            staff.setRole(StaffRole.SUPER_ADMIN);
            staffRepository.save(staff);

            log.info("==============================================");
            log.info("SUPER ADMIN PASSWORD RESET!");
            log.info("Email: {}", superAdminEmail);
            log.info("Password: [HIDDEN]");
            log.info("==============================================");
        }
    }
}

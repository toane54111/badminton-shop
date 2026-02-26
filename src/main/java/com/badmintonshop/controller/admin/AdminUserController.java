package com.badmintonshop.controller.admin;

import com.badmintonshop.dto.request.UserStatusRequest;
import com.badmintonshop.dto.response.ApiResponse;
import com.badmintonshop.dto.response.UserResponse;
import com.badmintonshop.dto.response.UserStatisticsResponse;
import com.badmintonshop.entity.Staff;
import com.badmintonshop.entity.enums.ActivityAction;
import com.badmintonshop.entity.enums.UserStatus;
import com.badmintonshop.repository.StaffRepository;
import com.badmintonshop.service.AuditService;
import com.badmintonshop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Admin API controller for User (Customer) management
 */
@RestController
@RequestMapping("/admin/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - User Management", description = "APIs for managing customers/users")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'SALE_STAFF')")
public class AdminUserController {

    private final UserService userService;
    private final StaffRepository staffRepository;
    private final AuditService auditService;

    /**
     * Get all users with filters and pagination
     * GET /admin/api/users?page=0&size=10&search=nguyen&status=ACTIVE&verified=true
     */
    @GetMapping
    @Operation(summary = "List users", description = "Get all users with search, filter and pagination")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) Boolean verified,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("Fetching users list - search: {}, status: {}, verified: {}, page: {}, size: {}",
                search, status, verified, page, size);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<UserResponse> userPage = userService.getAll(search, status, verified, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(userPage));
    }

    /**
     * Get user by ID
     * GET /admin/api/users/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Get user details with stats")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable Long id) {
        log.info("Fetching user by id: {}", id);
        
        UserResponse user = userService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * Update user status (ban/unban/lock)
     * PUT /admin/api/users/{id}/status
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "Update user status", description = "Ban, unban, or lock a user")
    public ResponseEntity<ApiResponse<UserResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        log.info("Updating status for user {} to {}", id, request.getStatus());
        
        // Get old status for audit
        UserResponse oldUser = userService.getById(id);
        UserResponse updated = userService.updateStatus(id, request.getStatus());
        
        // Log audit
        Staff currentStaff = getStaffFromAuth(authentication);
        String description = String.format("Updated user %s status from %s to %s. Reason: %s",
                oldUser.getEmail(), oldUser.getStatus(), request.getStatus(), 
                request.getReason() != null ? request.getReason() : "N/A");
        
        auditService.logActivity(
                currentStaff,
                ActivityAction.UPDATE,
                "User",
                id,
                description,
                oldUser.getStatus(),
                request.getStatus(),
                httpRequest
        );
        
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", updated));
    }

    /**
     * Get user statistics
     * GET /admin/api/users/statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get user statistics", description = "Get comprehensive user statistics")
    public ResponseEntity<ApiResponse<UserStatisticsResponse>> getStatistics() {
        log.info("Fetching user statistics");
        
        UserStatisticsResponse stats = userService.getStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * Export users to CSV
     * GET /admin/api/users/export?search=...&status=...
     */
    @GetMapping("/export")
    @Operation(summary = "Export users", description = "Export users to CSV file")
    public ResponseEntity<byte[]> exportToCsv(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserStatus status,
            Authentication authentication,
            HttpServletRequest httpRequest) {
        
        log.info("Exporting users to CSV - search: {}, status: {}", search, status);
        
        byte[] csvData = userService.exportToCsv(search, status);
        
        // Generate filename with timestamp
        String filename = "users_export_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        
        // Log audit
        Staff currentStaff = getStaffFromAuth(authentication);
        auditService.logActivity(
                currentStaff,
                ActivityAction.EXPORT,
                "User",
                null,
                "Exported users to CSV. Filters - search: " + search + ", status: " + status,
                null,
                null,
                httpRequest
        );
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }

    /**
     * Get current staff from authentication
     */
    private Staff getStaffFromAuth(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        String email = authentication.getName();
        return staffRepository.findByEmail(email).orElse(null);
    }
}

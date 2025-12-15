package com.badmintonshop.controller.admin;

import com.badmintonshop.dto.request.PermissionRequest;
import com.badmintonshop.dto.request.StaffRequest;
import com.badmintonshop.dto.request.StatusRequest;
import com.badmintonshop.dto.response.ApiResponse;
import com.badmintonshop.dto.response.StaffResponse;
import com.badmintonshop.entity.Staff;
import com.badmintonshop.entity.enums.ActivityAction;
import com.badmintonshop.entity.enums.StaffRole;
import com.badmintonshop.entity.enums.StaffStatus;
import com.badmintonshop.repository.StaffRepository;
import com.badmintonshop.service.AuditService;
import com.badmintonshop.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Admin API controller for Staff management
 */
@RestController
@RequestMapping("/admin/api/staff")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin - Staff Management", description = "APIs for managing staff members")
@PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('staff.view')")
public class AdminStaffController {

    private final StaffService staffService;
    private final StaffRepository staffRepository;
    private final AuditService auditService;

    /**
     * Get all staff with filters and pagination
     * GET
     * /admin/api/staff?page=0&size=10&search=nguyen&role=SALE_STAFF&status=ACTIVE
     */
    @GetMapping
    @Operation(summary = "List staff", description = "Get all staff with search, filter and pagination")
    public ResponseEntity<ApiResponse<Page<StaffResponse>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) StaffRole role,
            @RequestParam(required = false) StaffStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Fetching staff list - search: {}, role: {}, status: {}, page: {}, size: {}",
                search, role, status, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<StaffResponse> staffPage = staffService.getAll(search, role, status, pageable);

        return ResponseEntity.ok(ApiResponse.success(staffPage));
    }

    /**
     * Get staff by ID
     * GET /admin/api/staff/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get staff by ID", description = "Get staff details including permissions")
    public ResponseEntity<ApiResponse<StaffResponse>> getById(@PathVariable Long id) {
        log.info("Fetching staff by id: {}", id);

        StaffResponse staff = staffService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(staff));
    }

    /**
     * Create new staff
     * POST /admin/api/staff
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('staff.create')")
    @Operation(summary = "Create staff", description = "Create a new staff member")
    public ResponseEntity<ApiResponse<StaffResponse>> create(
            @Valid @RequestBody StaffRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        log.info("Creating new staff: {}", request.getEmail());

        StaffResponse created = staffService.create(request);

        // Log audit
        Staff currentStaff = getStaffFromAuth(authentication);
        auditService.logActivity(
                currentStaff,
                ActivityAction.CREATE,
                "Staff",
                created.getStaffId(),
                "Created staff: " + created.getEmail(),
                null,
                request,
                httpRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo nhân viên thành công", created));
    }

    /**
     * Update staff
     * PUT /admin/api/staff/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('staff.update')")
    @Operation(summary = "Update staff", description = "Update staff information")
    public ResponseEntity<ApiResponse<StaffResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody StaffRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        log.info("Updating staff: {}", id);

        // Get old value for audit
        StaffResponse oldStaff = staffService.getById(id);
        StaffResponse updated = staffService.update(id, request);

        // Log audit
        Staff currentStaff = getStaffFromAuth(authentication);
        auditService.logActivity(
                currentStaff,
                ActivityAction.UPDATE,
                "Staff",
                id,
                "Updated staff: " + updated.getEmail(),
                oldStaff,
                request,
                httpRequest);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật thành công", updated));
    }

    /**
     * Soft delete staff
     * DELETE /admin/api/staff/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('staff.delete')")
    @Operation(summary = "Delete staff", description = "Soft delete a staff member")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        log.info("Soft deleting staff: {}", id);

        // Get staff info for audit before deletion
        StaffResponse staff = staffService.getById(id);
        staffService.softDelete(id);

        // Log audit
        Staff currentStaff = getStaffFromAuth(authentication);
        auditService.logActivity(
                currentStaff,
                ActivityAction.DELETE,
                "Staff",
                id,
                "Deleted staff: " + staff.getEmail(),
                staff,
                null,
                httpRequest);

        return ResponseEntity.ok(ApiResponse.success("Đã xóa nhân viên"));
    }

    /**
     * Update staff permissions
     * PUT /admin/api/staff/{id}/permissions
     */
    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Update permissions", description = "Update staff permissions")
    public ResponseEntity<ApiResponse<StaffResponse>> updatePermissions(
            @PathVariable Long id,
            @Valid @RequestBody PermissionRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        log.info("Updating permissions for staff: {}", id);

        // Get old permissions for audit
        StaffResponse oldStaff = staffService.getById(id);
        StaffResponse updated = staffService.updatePermissions(id, request.getPermissions());

        // Log audit
        Staff currentStaff = getStaffFromAuth(authentication);
        auditService.logActivity(
                currentStaff,
                ActivityAction.UPDATE,
                "Staff",
                id,
                "Updated permissions for: " + updated.getEmail(),
                oldStaff.getPermissions(),
                request.getPermissions(),
                httpRequest);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật quyền thành công", updated));
    }

    /**
     * Update staff status
     * PUT /admin/api/staff/{id}/status
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('staff.update')")
    @Operation(summary = "Update status", description = "Update staff status (ACTIVE/INACTIVE/RESIGNED/BANNED)")
    public ResponseEntity<ApiResponse<StaffResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        log.info("Updating status for staff {} to {}", id, request.getStatus());

        // Get old status for audit
        StaffResponse oldStaff = staffService.getById(id);
        StaffResponse updated = staffService.updateStatus(id, request.getStatus());

        // Log audit
        Staff currentStaff = getStaffFromAuth(authentication);
        auditService.logActivity(
                currentStaff,
                ActivityAction.UPDATE,
                "Staff",
                id,
                "Updated status for " + updated.getEmail() + " to " + request.getStatus(),
                oldStaff.getStatus(),
                request.getStatus(),
                httpRequest);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công", updated));
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
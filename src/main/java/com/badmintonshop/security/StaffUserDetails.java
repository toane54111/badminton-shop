package com.badmintonshop.security;

import com.badmintonshop.entity.Staff;
import com.badmintonshop.entity.StaffPermission;
import com.badmintonshop.entity.enums.StaffRole;
import com.badmintonshop.entity.enums.StaffStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Custom UserDetails for Staff (Admin/Staff users)
 * Includes both role-based and permission-based authorities
 */
@Getter
public class StaffUserDetails implements UserDetails {

    private final Staff staff;
    private final List<GrantedAuthority> authorities;

    public StaffUserDetails(Staff staff) {
        this.staff = staff;
        this.authorities = buildAuthorities();
    }

    private List<GrantedAuthority> buildAuthorities() {
        List<GrantedAuthority> auths = new ArrayList<>();

        // Add role as authority (e.g., ROLE_SALE_STAFF)
        String role = "ROLE_" + staff.getRole().name();
        auths.add(new SimpleGrantedAuthority(role));

        // SUPER_ADMIN has all permissions automatically
        if (staff.getRole() == StaffRole.SUPER_ADMIN) {
            auths.add(new SimpleGrantedAuthority("products.view"));
            auths.add(new SimpleGrantedAuthority("products.create"));
            auths.add(new SimpleGrantedAuthority("products.update"));
            auths.add(new SimpleGrantedAuthority("products.delete"));
            auths.add(new SimpleGrantedAuthority("orders.view"));
            auths.add(new SimpleGrantedAuthority("orders.update"));
            auths.add(new SimpleGrantedAuthority("orders.cancel"));
            auths.add(new SimpleGrantedAuthority("stringing.view"));
            auths.add(new SimpleGrantedAuthority("stringing.update"));
            auths.add(new SimpleGrantedAuthority("stringing.assign"));
            auths.add(new SimpleGrantedAuthority("customers.view"));
            auths.add(new SimpleGrantedAuthority("customers.update"));
            auths.add(new SimpleGrantedAuthority("staff.view"));
            auths.add(new SimpleGrantedAuthority("staff.create"));
            auths.add(new SimpleGrantedAuthority("staff.update"));
            auths.add(new SimpleGrantedAuthority("staff.delete"));
            auths.add(new SimpleGrantedAuthority("inventory.view"));
            auths.add(new SimpleGrantedAuthority("inventory.import"));
            auths.add(new SimpleGrantedAuthority("inventory.export"));
        } else {
            // Add individual permissions from database
            if (staff.getPermissions() != null) {
                for (StaffPermission permission : staff.getPermissions()) {
                    auths.add(new SimpleGrantedAuthority(permission.getPermissionKey()));
                }
            }
        }

        return auths;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return staff.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return staff.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return staff.getStatus() != StaffStatus.BANNED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return staff.getStatus() == StaffStatus.ACTIVE;
    }

    // Helper methods
    public Long getStaffId() {
        return staff.getStaffId();
    }

    public String getFullName() {
        return staff.getFullName();
    }

    public String getEmail() {
        return staff.getEmail();
    }

    public String getAvatarUrl() {
        return staff.getAvatarUrl();
    }

    public String getRole() {
        return staff.getRole().name();
    }

    public boolean hasPermission(String permissionKey) {
        return authorities.stream()
                .anyMatch(a -> a.getAuthority().equals(permissionKey));
    }
}

package com.badmintonshop.controller;

import com.badmintonshop.dto.profile.*;
import com.badmintonshop.entity.User;
import com.badmintonshop.repository.UserRepository;
import com.badmintonshop.service.FileStorageService;
import com.badmintonshop.service.UserAddressService;
import com.badmintonshop.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final UserProfileService userProfileService;
    private final UserAddressService userAddressService;
    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    @GetMapping("/users/profile")
    public String profilePage(Model model) {
        try {
            UserProfileResponse profile = userProfileService.getCurrentUserProfile();
            model.addAttribute("profile", profile);

            model.addAttribute("changePasswordRequest", new ChangePasswordRequest());

            model.addAttribute("addresses", userAddressService.getUserAddresses());
            
            return "profile/index";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/login";
        }
    }

    @GetMapping("/api/users/profile")
    @ResponseBody
    public ResponseEntity<UserProfileResponse> getProfile() {
        return ResponseEntity.ok(userProfileService.getCurrentUserProfile());
    }

    @PutMapping("/api/users/profile")
    @ResponseBody
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UserProfileRequest request) {
        log.info("Received profile update request: {}", request);
        userProfileService.updateUserProfile(request);
        return ResponseEntity.ok().body("Cập nhật thông tin thành công");
    }

    @PutMapping("/api/users/change-password")
    @ResponseBody
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userProfileService.changePassword(request);
        return ResponseEntity.ok().body("Đổi mật khẩu thành công");
    }

    @PutMapping("/api/users/playing-profile")
    @ResponseBody
    public ResponseEntity<?> updatePlayingProfile(@RequestBody PlayingProfileRequest request) {
        userProfileService.updatePlayingProfile(request);
        return ResponseEntity.ok().body("Cập nhật hồ sơ chơi cầu thành công");
    }

    @GetMapping("/api/users/addresses")
    @ResponseBody
    public ResponseEntity<List<AddressResponse>> getAddresses() {
        return ResponseEntity.ok(userAddressService.getUserAddresses());
    }

    @PostMapping("/api/users/addresses")
    @ResponseBody
    public ResponseEntity<AddressResponse> addAddress(@Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(userAddressService.addAddress(request));
    }

    @PutMapping("/api/users/addresses/{id}")
    @ResponseBody
    public ResponseEntity<AddressResponse> updateAddress(@PathVariable Long id, @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(userAddressService.updateAddress(id, request));
    }

    @DeleteMapping("/api/users/addresses/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteAddress(@PathVariable Long id) {
        userAddressService.deleteAddress(id);
        return ResponseEntity.ok().body("Xóa địa chỉ thành công");
    }

    @PutMapping("/api/users/addresses/{id}/default")
    @ResponseBody
    public ResponseEntity<?> setDefaultAddress(@PathVariable Long id) {
        userAddressService.setDefaultAddress(id);
        return ResponseEntity.ok().body("Đặt địa chỉ mặc định thành công");
    }

    /**
     * Upload user avatar
     */
    @PostMapping("/api/users/avatar")
    @ResponseBody
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        UserProfileResponse profile = userProfileService.getCurrentUserProfile();
        if (profile == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
        }

        try {
            // Store avatar file
            String avatarUrl = fileStorageService.storeUserAvatar(file, profile.getUserId());
            
            // Update user avatar URL
            User user = userRepository.findById(profile.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Delete old avatar file if exists and is local
            String oldAvatar = user.getAvatarUrl();
            if (oldAvatar != null && oldAvatar.startsWith("/uploads/")) {
                fileStorageService.deleteFile(oldAvatar);
            }
            
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);

            log.info("Avatar updated for user {}: {}", profile.getUserId(), avatarUrl);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "avatarUrl", avatarUrl,
                    "message", "Cập nhật ảnh đại diện thành công"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            log.error("Error uploading avatar: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "Lỗi upload file"));
        }
    }
}
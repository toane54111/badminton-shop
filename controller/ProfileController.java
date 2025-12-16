package com.badmintonshop.controller;

import com.badmintonshop.dto.profile.*;
import com.badmintonshop.service.UserAddressService;
import com.badmintonshop.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.naming.AuthenticationException;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final UserProfileService userProfileService;
    private final UserAddressService userAddressService;

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
}
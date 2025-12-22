package com.badmintonshop.service;

import com.badmintonshop.dto.profile.ChangePasswordRequest;
import com.badmintonshop.dto.profile.PlayingProfileRequest;
import com.badmintonshop.dto.profile.UserProfileRequest;
import com.badmintonshop.dto.profile.UserProfileResponse;
import com.badmintonshop.entity.User;
import com.badmintonshop.exception.BadRequestException;
import com.badmintonshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile() {
        User user = authService.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("Người dùng chưa đăng nhập"));

        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .playingStyle(user.getPlayingStyle())
                .skillLevel(user.getSkillLevel())
                .preferredRacketWeight(user.getPreferredRacketWeight())
                .preferredTensionMin(user.getPreferredTensionMin())
                .preferredTensionMax(user.getPreferredTensionMax())
                .preferredStringType(user.getPreferredStringType())
                .build();
    }

    @Transactional
    public void updateUserProfile(UserProfileRequest request) {
        User user = authService.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("Người dùng chưa đăng nhập"));

        // Validate Validation Service or manual check?
        // Check uniqueness of phone if changed
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
             if (userRepository.existsByPhoneAndDeletedAtIsNull(request.getPhone())) {
                 throw new BadRequestException("Số điện thoại đã được sử dụng");
             }
        }

        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());

        userRepository.save(user);
        log.info("Cập nhật thông tin cá nhân cho user: {}", user.getEmail());
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = authService.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("Người dùng chưa đăng nhập"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Mật khẩu hiện tại không đúng");
        }

        if (passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Mật khẩu mới không được trùng với mật khẩu cũ");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        log.info("Đổi mật khẩu thành công cho user: {}", user.getEmail());
    }

    @Transactional
    public void updatePlayingProfile(PlayingProfileRequest request) {
        User user = authService.getCurrentUser()
                .orElseThrow(() -> new BadRequestException("Người dùng chưa đăng nhập"));

        user.setPlayingStyle(request.getPlayingStyle());
        user.setSkillLevel(request.getSkillLevel());
        user.setPreferredRacketWeight(request.getPreferredRacketWeight());
        user.setPreferredTensionMin(request.getPreferredTensionMin());
        user.setPreferredTensionMax(request.getPreferredTensionMax());
        user.setPreferredStringType(request.getPreferredStringType());

        userRepository.save(user);
        log.info("Cập nhật hồ sơ chơi cầu cho user: {}", user.getEmail());
    }
}

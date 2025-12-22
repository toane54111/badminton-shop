package com.badmintonshop.security;

import com.badmintonshop.entity.User;
import com.badmintonshop.entity.enums.UserStatus;
import com.badmintonshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Custom UserDetailsService for loading user from database
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản với email: " + email));

        if (user.getStatus() == UserStatus.BANNED) {
            throw new UsernameNotFoundException("Tài khoản đã bị khóa vĩnh viễn");
        }

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new UsernameNotFoundException("Tài khoản đang bị tạm khóa");
        }

        // Check email verification
        if (!Boolean.TRUE.equals(user.getIsEmailVerified())) {
            throw new EmailNotVerifiedException("Email chưa được xác thực. Vui lòng kiểm tra hộp thư.", email);
        }

        return new CustomUserDetails(user);
    }
}

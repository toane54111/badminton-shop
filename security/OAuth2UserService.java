package com.badmintonshop.security;

import com.badmintonshop.entity.User;
import com.badmintonshop.entity.enums.UserStatus;
import com.badmintonshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * OAuth2 User Service for Google Login
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String googleId = oAuth2User.getAttribute("sub");
        String picture = oAuth2User.getAttribute("picture");
        
        log.info("OAuth2 login attempt for email: {}", email);
        
        // Find or create user
        Optional<User> existingUser = userRepository.findByEmailAndDeletedAtIsNull(email);
        
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            // Update Google OAuth ID if not set
            if (user.getGoogleOauthId() == null) {
                user.setGoogleOauthId(googleId);
            }
            // Update avatar if not set
            if (user.getAvatarUrl() == null && picture != null) {
                user.setAvatarUrl(picture);
            }
        } else {
            // Create new user
            user = User.builder()
                    .email(email)
                    .fullName(name)
                    .googleOauthId(googleId)
                    .avatarUrl(picture)
                    .passwordHash("") // No password for OAuth users
                    .isEmailVerified(true)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(user);
            log.info("Created new user from OAuth2: {}", email);
        }
        
        return new CustomOAuth2User(oAuth2User, user);
    }
}

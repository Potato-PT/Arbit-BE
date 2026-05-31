package com.arbit.app.auth.service;

import com.arbit.app.auth.dto.AuthResponse;
import com.arbit.app.auth.dto.LoginRequest;
import com.arbit.app.auth.dto.SignupRequest;
import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.auth.security.JwtTokenProvider;
import com.arbit.app.common.exception.BusinessException;
import com.arbit.app.common.exception.ErrorCode;
import com.arbit.app.user.entity.User;
import java.time.Year;
import com.arbit.app.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final ResidentialLocationResolver residentialLocationResolver;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider,
                       ResidentialLocationResolver residentialLocationResolver) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.residentialLocationResolver = residentialLocationResolver;
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE);
        }
        ResidentialCoordinates coordinates = residentialLocationResolver.resolve(request.residentialArea());
        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .age(toAge(request.birthYear()))
                .gender(request.gender())
                .residentialArea(request.residentialArea())
                .residentialLatitude(coordinates.latitude())
                .residentialLongitude(coordinates.longitude())
                .build();
        userRepository.save(user);
        return issueTokens(user.getUsername());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (AuthenticationException exception) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return issueTokens(request.username());
    }

    public void logout(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        SecurityContextHolder.clearContext();
    }

    private AuthResponse issueTokens(String username) {
        return AuthResponse.of(
                jwtTokenProvider.createAccessToken(username),
                jwtTokenProvider.createRefreshToken(username)
        );
    }

    private int toAge(Integer birthYear) {
        if (birthYear == null) {
            return 0;
        }
        return Math.max(0, Year.now().getValue() - birthYear);
    }
}

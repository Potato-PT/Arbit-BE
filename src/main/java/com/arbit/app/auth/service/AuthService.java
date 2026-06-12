package com.arbit.app.auth.service;

import com.arbit.app.auth.dto.AuthResponse;
import com.arbit.app.auth.dto.LoginRequest;
import com.arbit.app.auth.dto.SignupRequest;
import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.auth.security.JwtTokenProvider;
import com.arbit.app.common.exception.BusinessException;
import com.arbit.app.common.exception.ErrorCode;
import com.arbit.app.user.entity.User;
import com.arbit.app.user.entity.UserGender;
import com.arbit.app.user.repository.UserRepository;
import java.time.Year;
import java.util.UUID;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String GUEST_USERNAME_PREFIX = "guest_";
    private static final String GUEST_NICKNAME_PREFIX = "Guest";
    private static final String GUEST_RESIDENTIAL_AREA = "NONSELECT";
    private static final double DEFAULT_RESIDENTIAL_LATITUDE = 0.0;
    private static final double DEFAULT_RESIDENTIAL_LONGITUDE = 0.0;
    private static final String USERNAME_NOT_FOUND_MESSAGE = "No signup history exists for this username.";
    private static final String INCORRECT_PASSWORD_MESSAGE = "Password is incorrect.";

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
        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE);
        }
        return issueTokens(user.getUsername());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        if (!userRepository.existsByUsername(request.username())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, USERNAME_NOT_FOUND_MESSAGE);
        }
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (AuthenticationException exception) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, INCORRECT_PASSWORD_MESSAGE);
        }
        return issueTokens(request.username());
    }

    @Transactional
    public AuthResponse guestLogin() {
        String username = generateGuestUsername();
        String password = UUID.randomUUID().toString();
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .nickname(generateGuestNickname())
                .age(0)
                .gender(UserGender.NONSELECT)
                .residentialArea(GUEST_RESIDENTIAL_AREA)
                .residentialLatitude(DEFAULT_RESIDENTIAL_LATITUDE)
                .residentialLongitude(DEFAULT_RESIDENTIAL_LONGITUDE)
                .build();
        userRepository.save(user);
        return issueTokens(user.getUsername());
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

    private String generateGuestUsername() {
        String username;
        do {
            username = GUEST_USERNAME_PREFIX + UUID.randomUUID().toString().replace("-", "");
        } while (userRepository.existsByUsername(username));
        return username;
    }

    private String generateGuestNickname() {
        return GUEST_NICKNAME_PREFIX + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private int toAge(Integer birthYear) {
        if (birthYear == null) {
            return 0;
        }
        return Math.max(0, Year.now().getValue() - birthYear);
    }
}

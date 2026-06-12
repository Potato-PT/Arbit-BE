package com.arbit.app.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.arbit.app.auth.dto.AuthResponse;
import com.arbit.app.auth.dto.LoginRequest;
import com.arbit.app.auth.dto.SignupRequest;
import com.arbit.app.auth.security.JwtTokenProvider;
import com.arbit.app.common.exception.BusinessException;
import com.arbit.app.common.exception.ErrorCode;
import com.arbit.app.user.entity.User;
import com.arbit.app.user.entity.UserGender;
import com.arbit.app.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

    private static final String USERNAME = "loginId1234";
    private static final String PASSWORD = "password1234";

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private JwtTokenProvider jwtTokenProvider;
    private ResidentialLocationResolver residentialLocationResolver;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        authenticationManager = mock(AuthenticationManager.class);
        jwtTokenProvider = mock(JwtTokenProvider.class);
        residentialLocationResolver = mock(ResidentialLocationResolver.class);
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                authenticationManager,
                jwtTokenProvider,
                residentialLocationResolver
        );
    }

    @Test
    void signup_rejectsUsernameThatAlreadyExistsBeforeSave() {
        SignupRequest request = signupRequest();
        when(userRepository.existsByUsername(USERNAME)).thenReturn(true);

        assertBusinessException(
                () -> authService.signup(request),
                ErrorCode.DUPLICATE_RESOURCE,
                "Resource already exists."
        );

        verify(userRepository, never()).saveAndFlush(any(User.class));
        verifyNoInteractions(passwordEncoder, residentialLocationResolver, jwtTokenProvider);
    }

    @Test
    void signup_convertsDatabaseUniqueConflictToDuplicateResource() {
        SignupRequest request = signupRequest();
        when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(residentialLocationResolver.resolve(request.residentialArea()))
                .thenReturn(new ResidentialCoordinates(37.5665, 126.9780));
        when(passwordEncoder.encode(PASSWORD)).thenReturn("encoded-password");
        when(userRepository.saveAndFlush(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate username"));

        assertBusinessException(
                () -> authService.signup(request),
                ErrorCode.DUPLICATE_RESOURCE,
                "Resource already exists."
        );

        verify(userRepository).saveAndFlush(any(User.class));
        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    void login_rejectsUsernameWithoutSignupHistoryWithoutAuthenticating() {
        when(userRepository.existsByUsername(USERNAME)).thenReturn(false);

        assertBusinessException(
                () -> authService.login(new LoginRequest(USERNAME, PASSWORD)),
                ErrorCode.UNAUTHORIZED,
                "No signup history exists for this username."
        );

        verifyNoInteractions(authenticationManager, jwtTokenProvider);
    }

    @Test
    void login_rejectsIncorrectPasswordForExistingUsername() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD);
        when(userRepository.existsByUsername(USERNAME)).thenReturn(true);
        when(authenticationManager.authenticate(authentication))
                .thenThrow(new BadCredentialsException("bad credentials"));

        assertBusinessException(
                () -> authService.login(new LoginRequest(USERNAME, PASSWORD)),
                ErrorCode.UNAUTHORIZED,
                "Password is incorrect."
        );

        verifyNoInteractions(jwtTokenProvider);
    }

    @Test
    void login_issuesTokensForValidCredentials() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD);
        when(userRepository.existsByUsername(USERNAME)).thenReturn(true);
        when(authenticationManager.authenticate(authentication)).thenReturn(authentication);
        when(jwtTokenProvider.createAccessToken(USERNAME)).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(USERNAME)).thenReturn("refresh-token");

        AuthResponse response = authService.login(new LoginRequest(USERNAME, PASSWORD));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
    }

    private SignupRequest signupRequest() {
        return new SignupRequest(
                USERNAME,
                PASSWORD,
                "ArbitUser",
                1998,
                UserGender.MALE,
                "Seoul Mapo-gu"
        );
    }

    private void assertBusinessException(ThrowingOperation operation, ErrorCode errorCode, String message) {
        assertThatThrownBy(operation::run)
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.getErrorCode()).isEqualTo(errorCode);
                    assertThat(businessException.getMessage()).isEqualTo(message);
                });
    }

    @FunctionalInterface
    private interface ThrowingOperation {
        void run();
    }
}

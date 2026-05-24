package com.arbit.app.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arbit.app.auth.dto.AuthResponse;
import com.arbit.app.auth.dto.LoginRequest;
import com.arbit.app.auth.dto.SignupRequest;
import com.arbit.app.auth.security.JwtTokenProvider;
import com.arbit.app.user.entity.User;
import com.arbit.app.user.entity.UserGender;
import com.arbit.app.user.repository.UserRepository;
import java.time.Year;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private ResidentialLocationResolver residentialLocationResolver;

    @Test
    void loginReturnsAccessTokenAndRefreshToken() {
        AuthService authService = new AuthService(
                userRepository,
                passwordEncoder,
                authenticationManager,
                jwtTokenProvider,
                residentialLocationResolver
        );

        LoginRequest request = new LoginRequest("arbit_user_01", "password1234");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(request.username(), null));
        when(jwtTokenProvider.createAccessToken(request.username())).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(request.username())).thenReturn("refresh-token");

        AuthResponse response = authService.login(request);

        verify(authenticationManager)
                .authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
    }

    @Test
    void signupCalculatesAgeAndIssuesTokens() {
        AuthService authService = new AuthService(
                userRepository,
                passwordEncoder,
                authenticationManager,
                jwtTokenProvider,
                residentialLocationResolver
        );

        SignupRequest request = new SignupRequest(
                "arbit_user_01",
                "password1234",
                "ArbitUser",
                1998,
                UserGender.MALE,
                "서울특별시 성북구 북악산로 918"
        );
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(residentialLocationResolver.resolve(request.residentialArea()))
                .thenReturn(new ResidentialCoordinates(37.6104, 126.9978));
        when(jwtTokenProvider.createAccessToken(request.username())).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(request.username())).thenReturn("refresh-token");

        AuthResponse response = authService.signup(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        verify(residentialLocationResolver).resolve(request.residentialArea());
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");

        User savedUser = userCaptor.getValue();
        int expectedAge = Year.now().getValue() - request.birthYear();
        assertThat(savedUser.getUsername()).isEqualTo(request.username());
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getNickname()).isEqualTo(request.nickname());
        assertThat(savedUser.getGender()).isEqualTo(request.gender());
        assertThat(savedUser.getResidentialArea()).isEqualTo(request.residentialArea());
        assertThat(savedUser.getAge()).isEqualTo(expectedAge);
        assertThat(savedUser.getResidentialLatitude()).isEqualTo(37.6104);
        assertThat(savedUser.getResidentialLongitude()).isEqualTo(126.9978);
    }
}

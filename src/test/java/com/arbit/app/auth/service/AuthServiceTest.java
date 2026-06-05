package com.arbit.app.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arbit.app.auth.dto.AuthResponse;
import com.arbit.app.auth.security.JwtTokenProvider;
import com.arbit.app.user.entity.User;
import com.arbit.app.user.entity.UserGender;
import com.arbit.app.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
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
    void guestLoginCreatesGuestUserWithDefaultProfileValues() {
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded-password");
        when(jwtTokenProvider.createAccessToken(any())).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(any())).thenReturn("refresh-token");
        AuthService authService = new AuthService(
                userRepository,
                passwordEncoder,
                authenticationManager,
                jwtTokenProvider,
                residentialLocationResolver
        );

        AuthResponse response = authService.guestLogin();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User guest = captor.getValue();
        assertThat(guest.getUsername()).startsWith("guest_");
        assertThat(guest.getPassword()).isEqualTo("encoded-password");
        assertThat(guest.getNickname()).startsWith("Guest");
        assertThat(guest.getAge()).isZero();
        assertThat(guest.getGender()).isEqualTo(UserGender.NONSELECT);
        assertThat(guest.getResidentialArea()).isEqualTo("NONSELECT");
        assertThat(guest.getResidentialLatitude()).isZero();
        assertThat(guest.getResidentialLongitude()).isZero();
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
    }
}

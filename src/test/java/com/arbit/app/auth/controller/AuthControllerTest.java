package com.arbit.app.auth.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.arbit.app.auth.dto.AuthResponse;
import com.arbit.app.auth.dto.LoginRequest;
import com.arbit.app.auth.dto.SignupRequest;
import com.arbit.app.auth.security.JwtTokenProvider;
import com.arbit.app.auth.service.AuthService;
import com.arbit.app.user.entity.UserGender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void signupReturnsTokensForRoadAddressRequest() throws Exception {
        SignupRequest request = new SignupRequest(
                "arbit_user_01",
                "password1234",
                "ArbitUser",
                1998,
                UserGender.MALE,
                "서울특별시 성북구 북악산로 918"
        );
        when(authService.signup(request)).thenReturn(AuthResponse.of("access-token", "refresh-token"));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "arbit_user_01",
                                  "password": "password1234",
                                  "nickname": "ArbitUser",
                                  "birthYear": 1998,
                                  "gender": "MALE",
                                  "residentialArea": "서울특별시 성북구 북악산로 918"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));

        verify(authService).signup(request);
    }

    @Test
    void loginReturnsTokensForValidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("arbit_user_01", "password1234");
        when(authService.login(request)).thenReturn(AuthResponse.of("access-token", "refresh-token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "arbit_user_01",
                                  "password": "password1234"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));

        verify(authService).login(request);
    }
}

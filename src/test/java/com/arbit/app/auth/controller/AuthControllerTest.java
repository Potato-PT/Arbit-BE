package com.arbit.app.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.arbit.app.auth.dto.LoginRequest;
import com.arbit.app.auth.dto.SignupRequest;
import com.arbit.app.auth.security.JwtTokenProvider;
import com.arbit.app.auth.security.RestAccessDeniedHandler;
import com.arbit.app.auth.security.RestAuthenticationEntryPoint;
import com.arbit.app.auth.service.AuthService;
import com.arbit.app.common.config.SecurityConfig;
import com.arbit.app.common.exception.BusinessException;
import com.arbit.app.common.exception.ErrorCode;
import com.arbit.app.common.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import({
        SecurityConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void signup_returnsConflictWhenDatabaseDetectsDuplicateUsername() throws Exception {
        when(authService.signup(any(SignupRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.DUPLICATE_RESOURCE));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "loginId1234",
                                  "password": "password1234",
                                  "nickname": "ArbitUser",
                                  "birthYear": 1998,
                                  "gender": "MALE",
                                  "residentialArea": "Seoul Mapo-gu"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_RESOURCE"))
                .andExpect(jsonPath("$.error.message").value("Resource already exists."));

        verify(authService).signup(any(SignupRequest.class));
    }

    @Test
    void login_returnsSignupHistoryMessageForUnknownUsername() throws Exception {
        LoginRequest request = new LoginRequest("unknown-user", "password1234");
        when(authService.login(request))
                .thenThrow(new BusinessException(
                        ErrorCode.UNAUTHORIZED,
                        "No signup history exists for this username."
                ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.error.message").value("No signup history exists for this username."));

        verify(authService).login(request);
    }

    @Test
    void login_returnsIncorrectPasswordMessageForExistingUsername() throws Exception {
        LoginRequest request = new LoginRequest("loginId1234", "incorrect-password");
        when(authService.login(request))
                .thenThrow(new BusinessException(ErrorCode.UNAUTHORIZED, "Password is incorrect."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.error.message").value("Password is incorrect."));

        verify(authService).login(request);
    }
}

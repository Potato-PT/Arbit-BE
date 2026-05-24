package com.arbit.app.auth.controller;

import com.arbit.app.auth.dto.AuthResponse;
import com.arbit.app.auth.dto.LoginRequest;
import com.arbit.app.auth.dto.SignupRequest;
import com.arbit.app.auth.service.AuthService;
import com.arbit.app.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "인증", description = "회원가입과 로그인을 처리합니다.")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    @Operation(
            summary = "회원 정보를 입력해 가입하고 토큰을 발급받습니다.",
            description = "사용자 정보를 저장한 뒤 JWT 액세스 토큰과 리프레시 토큰을 함께 발급합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SignupRequest.class),
                            examples = @ExampleObject(
                                    name = "Signup Request",
                                    value = """
                                            {
                                              "username": "loginId1234",
                                              "password": "password1234",
                                              "nickname": "ArbitUser",
                                              "birthYear": 1998,
                                              "gender": "MALE",
                                              "residentialArea": "Seoul Seongbuk-gu Dongseon-dong 85-92"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "회원가입이 완료되고 토큰이 발급됩니다.",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AuthResponse.class),
                                    examples = @ExampleObject(
                                            name = "Signup Response",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "data": {
                                                        "accessToken": "eyJhbGciOiJIUzI1NiJ9.access-token",
                                                        "refreshToken": "eyJhbGciOiJIUzI1NiJ9.refresh-token"
                                                      },
                                                      "error": null
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "입력값이 올바르지 않습니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "409",
                            description = "이미 사용 중인 아이디입니다."
                    )
            }
    )
    public ApiResponse<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ApiResponse.success(authService.signup(request));
    }

    @PostMapping("/login")
    @Operation(
            summary = "아이디와 비밀번호로 로그인하고 토큰을 발급받습니다.",
            description = "등록된 계정 정보로 인증한 뒤 JWT 액세스 토큰과 리프레시 토큰을 반환합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    name = "Login Request",
                                    value = """
                                            {
                                              "username": "arbit_user_01",
                                              "password": "password1234"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "로그인에 성공하고 토큰이 발급됩니다."
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "아이디 또는 비밀번호가 올바르지 않습니다."
                    )
            }
    )
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }
}

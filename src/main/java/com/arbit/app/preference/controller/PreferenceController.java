package com.arbit.app.preference.controller;

import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.common.response.ApiResponse;
import com.arbit.app.preference.dto.CreatePreferenceRequest;
import com.arbit.app.preference.dto.PreferenceCategoriesResponse;
import com.arbit.app.preference.service.PreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Preferences", description = "Preference metadata APIs.")
public class PreferenceController {

    private final PreferenceService preferenceService;

    public PreferenceController(PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @GetMapping("/api/preferences/categories")
    @Operation(
            summary = "Get preference categories",
            description = "Returns the fixed category, mood, and audience options used for preference selection.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Preference categories retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = PreferenceCategoriesApiResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "success": true,
                                                      "data": {
                                                        "keyword1": [
                                                          {
                                                            "category": "전시/미술",
                                                            "subcategories": ["개인전/초대전", "기획/테마 전시", "역사/문화/산업"]
                                                          },
                                                          {
                                                            "category": "클래식 및 독주/독창회",
                                                            "subcategories": ["관현악/교향곡", "기악 독주회", "실내악/앙상블"]
                                                          },
                                                          {
                                                            "category": "교육/체험",
                                                            "subcategories": ["만들기/공방 체험", "도서/독서 연계", "학술/강연"]
                                                          },
                                                          {
                                                            "category": "축제(통합)",
                                                            "subcategories": ["야외 체험 행사", "종합 문화 페스티벌", "체험/참여형 축제", "기념/역사 축제"]
                                                          },
                                                          {
                                                            "category": "연극",
                                                            "subcategories": ["아동/가족극", "기획/프로젝트극", "정통 연극/극단전"]
                                                          },
                                                          {
                                                            "category": "콘서트",
                                                            "subcategories": ["재즈/크로스오버", "대중/인디 음악", "고궁/야외 콘서트", "성악/팝페라"]
                                                          },
                                                          {
                                                            "category": "국악",
                                                            "subcategories": ["전통 국악", "창작/퓨전 국악"]
                                                          },
                                                          {
                                                            "category": "뮤지컬/오페라",
                                                            "subcategories": ["뮤지컬", "오페라"]
                                                          },
                                                          {
                                                            "category": "무용",
                                                            "subcategories": ["발레", "현대/창작무용", "전통무용"]
                                                          },
                                                          {
                                                            "category": "영화",
                                                            "subcategories": ["특별 상영회/페스타", "고전/독립/예술 영화"]
                                                          },
                                                          {
                                                            "category": "기타",
                                                            "subcategories": ["기타"]
                                                          }
                                                        ],
                                                        "keyword2": ["힐링/감성", "신나는/활기찬", "감동/웅장", "전통/문화", "가족친화", "학술/사색적"],
                                                        "keyword3": ["아동/가족", "청소년", "일반 성인", "전 연령", "태그 없음"]
                                                      },
                                                      "error": null
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    public ApiResponse<PreferenceCategoriesResponse> getPreferenceCategories() {
        return ApiResponse.success(preferenceService.getPreferenceCategories());
    }

    @PostMapping("/api/preferences")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Save initial preferences",
            description = "Stores the authenticated user's initial categories and preference keywords after signup.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreatePreferenceRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "keyword1": [
                                                {
                                                  "category": "전시/미술",
                                                  "subcategories": ["개인전/초대전", "기획/테마 전시"]
                                                },
                                                {
                                                  "category": "클래식 및 독주/독창회",
                                                  "subcategories": []
                                                }
                                              ],
                                              "keyword2": ["가족친화", "학술/사색적"],
                                              "keyword3": ["전 연령", "태그 없음"],
                                              "keyword4": ["블라블라"]
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Preferences saved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "success": true,
                                                      "data": null,
                                                      "error": null
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required")
            }
    )
    public ApiResponse<Void> createPreferences(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @Valid @RequestBody CreatePreferenceRequest request) {
        preferenceService.createPreferences(userDetails.id(), request);
        return ApiResponse.ok();
    }

    private record PreferenceCategoriesApiResponse(
            boolean success,
            PreferenceCategoriesResponse data,
            Object error
    ) {
    }
}

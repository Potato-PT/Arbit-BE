package com.arbit.app.user.controller;

import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.common.response.ApiResponse;
import com.arbit.app.user.dto.MyBookmarkResponse;
import com.arbit.app.user.dto.MyProfileResponse;
import com.arbit.app.user.dto.MyReviewResponse;
import com.arbit.app.user.dto.UpdateNicknameRequest;
import com.arbit.app.user.dto.UpdateNicknameResponse;
import com.arbit.app.user.dto.UpdateProfileImageResponse;
import com.arbit.app.user.service.UserMeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users/me")
@Tag(name = "마이페이지", description = "Profile, bookmark, and review APIs for the authenticated user.")
@SecurityRequirement(name = "bearerAuth")
public class UserMeController {

    private final UserMeService userMeService;

    public UserMeController(UserMeService userMeService) {
        this.userMeService = userMeService;
    }

    @GetMapping
    @Operation(
            summary = "Profile inquiry",
            description = "Returns the current user's profile photo, nickname, subscription date, and taste keywords.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Profile retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "success": true,
                                                      "data": {
                                                        "profileImageUrl": "https://cdn.arbit.app/users/8f2c/profile.jpg",
                                                        "nickname": "ArbitUser",
                                                        "subscribedAt": "2026-05-11T15:03:12",
                                                        "tasteKeywords": ["Media Art", "Immersive", "Jazz"]
                                                      },
                                                      "error": null
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required")
            }
    )
    public ApiResponse<MyProfileResponse> getMyProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(userMeService.getMyProfile(userDetails.id()));
    }

    @PutMapping(value = "/profile_image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Modify profile picture",
            description = "Uploads a new profile image and updates the current user's profile image URL.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = ProfileImageUploadRequest.class)
                    )
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Profile image updated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UpdateProfileImageResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "success": true,
                                                      "data": {
                                                        "profileImageUrl": "/uploads/users/8f2c/profile/2a6d-profile.jpg"
                                                      },
                                                      "error": null
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid image file"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required")
            }
    )
    public ApiResponse<UpdateProfileImageResponse> updateProfileImage(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                      @RequestPart("profileImage") MultipartFile profileImage) {
        return ApiResponse.success(userMeService.updateProfileImage(userDetails.id(), profileImage));
    }

    @PutMapping("/nickname")
    @Operation(
            summary = "Modify name",
            description = "Updates the current user's nickname.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateNicknameRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "nickname": "CultureHunter"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Nickname updated successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UpdateNicknameResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "success": true,
                                                      "data": {
                                                        "nickname": "CultureHunter"
                                                      },
                                                      "error": null
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid nickname"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required")
            }
    )
    public ApiResponse<UpdateNicknameResponse> updateNickname(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                              @Valid @org.springframework.web.bind.annotation.RequestBody UpdateNicknameRequest request) {
        return ApiResponse.success(userMeService.updateNickname(userDetails.id(), request.nickname()));
    }

    @GetMapping("/bookmarks")
    @Operation(
            summary = "My bookmark list",
            description = "Returns the current user's bookmarked events in default-tab order.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Bookmark list retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = MyBookmarksApiResponse.class),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "success": true,
                                                      "data": [
                                                        {
                                                          "eventId": 1,
                                                          "title": "Echoes of Silence",
                                                          "posterImageUrl": "https://cdn.arbit.app/events/light-museum/poster.jpg",
                                                          "category": "Media Art",
                                                          "venue": "Metropolitan Museum",
                                                          "startDate": "2026-05-01",
                                                          "endDate": "2026-06-30",
                                                          "bookmarkedAt": "2026-05-20T18:30:00"
                                                        }
                                                      ],
                                                      "error": null
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required")
            }
    )
    public ApiResponse<List<MyBookmarkResponse>> getMyBookmarks(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(userMeService.getMyBookmarks(userDetails.id()));
    }

    @GetMapping("/reviews")
    @Operation(
            summary = "My review list",
            description = "Returns the current user's reviews ordered by most recently updated first.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Review list retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = MyReviewResponse.class)),
                                    examples = @ExampleObject(
                                            value = """
                                                    {
                                                      "success": true,
                                                      "data": [
                                                        {
                                                          "reviewId": 12,
                                                          "title": "Echoes of Silence",
                                                          "posterImageUrl": "https://cdn.arbit.app/events/light-museum/poster.jpg",
                                                          "starScore": 5,
                                                          "content": "The immersive media wall was the highlight.",
                                                          "likes": 0,
                                                          "createdAt": "2026-05-23T09:40:00"
                                                        }
                                                      ],
                                                      "error": null
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required")
            }
    )
    public ApiResponse<List<MyReviewResponse>> getMyReviews(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(userMeService.getMyReviews(userDetails.id()));
    }

    @DeleteMapping
    @Operation(
            summary = "Delete my account",
            description = "Deletes the authenticated user's account and related bookmarks, reviews, taste keywords, and recommendations.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Account deleted successfully",
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
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Authentication is required"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    public ApiResponse<Void> deleteMyAccount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userMeService.deleteMyAccount(userDetails.id());
        return ApiResponse.ok();
    }

    private record ProfileImageUploadRequest(
            @Schema(type = "string", format = "binary", description = "Profile image file")
            MultipartFile profileImage
    ) {
    }

    @Schema(description = "Wrapped bookmark list response")
    private record MyBookmarksApiResponse(
            boolean success,
            @ArraySchema(schema = @Schema(implementation = MyBookmarkResponse.class))
            List<MyBookmarkResponse> data,
            Object error
    ) {
    }
}

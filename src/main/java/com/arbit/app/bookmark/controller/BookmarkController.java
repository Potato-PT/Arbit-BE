package com.arbit.app.bookmark.controller;

import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookmarks")
@Tag(name = "Bookmark", description = "Bookmark APIs used from home and event detail screens.")
@SecurityRequirement(name = "bearerAuth")
public class BookmarkController {

    @PostMapping("/{eventId}")
    @Operation(
            summary = "Add favorite",
            description = "Adds the specified event to the authenticated user's bookmarks. This endpoint is shared by home and event detail screens.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Bookmark added successfully",
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
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Event not found"),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Event is already bookmarked")
            }
    )
    public ApiResponse<Void> addBookmark(
            @Parameter(description = "Event UUID to bookmark", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID eventId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.ok();
    }

    @DeleteMapping("/{eventId}")
    @Operation(
            summary = "Remove favorite",
            description = "Removes the specified event from the authenticated user's bookmarks. This endpoint is shared by home and event detail screens.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Bookmark removed successfully",
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
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Bookmark not found")
            }
    )
    public ApiResponse<Void> removeBookmark(
            @Parameter(description = "Event UUID to unbookmark", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID eventId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.ok();
    }
}

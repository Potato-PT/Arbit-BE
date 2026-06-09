package com.arbit.app.event.controller;

import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.common.response.ApiResponse;
import com.arbit.app.event.service.EventActionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events/{eventId}/actions")
@Tag(name = "Event Actions", description = "Authenticated user behavior collection APIs.")
@SecurityRequirement(name = "bearerAuth")
public class EventActionController {

    private final EventActionService eventActionService;

    public EventActionController(EventActionService eventActionService) {
        this.eventActionService = eventActionService;
    }

    @PostMapping("/homepage-click")
    @Operation(
            summary = "Record external homepage click",
            description = """
                    Records that the authenticated user clicked the external official page or ticketing button
                    from the event detail screen. The client should call this endpoint before opening the external URL.
                    """,
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "Homepage click recorded successfully",
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
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Event not found")
            }
    )
    public ApiResponse<Void> recordHomepageClick(
            @Parameter(description = "Event UUID whose external homepage button was clicked",
                    example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID eventId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        eventActionService.recordHomepageClick(userDetails.id(), eventId);
        return ApiResponse.ok();
    }
}

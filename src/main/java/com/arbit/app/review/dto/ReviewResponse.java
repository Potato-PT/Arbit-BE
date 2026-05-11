package com.arbit.app.review.dto;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        int rating,
        String content,
        String verificationImageUrl,
        LocalDateTime createdAt
) {
}

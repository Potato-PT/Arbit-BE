package com.arbit.app.user.service;

import com.arbit.app.bookmark.entity.Bookmark;
import com.arbit.app.bookmark.repository.BookmarkRepository;
import com.arbit.app.category.repository.UserCategoryRepository;
import com.arbit.app.common.exception.BusinessException;
import com.arbit.app.common.exception.ErrorCode;
import com.arbit.app.event.entity.Event;
import com.arbit.app.keyword.repository.UserPreferenceKeywordRepository;
import com.arbit.app.recommendation.repository.RecommendationRepository;
import com.arbit.app.review.entity.Review;
import com.arbit.app.review.repository.ReviewRepository;
import com.arbit.app.storage.StorageService;
import com.arbit.app.user.dto.MyBookmarkResponse;
import com.arbit.app.user.dto.MyProfileResponse;
import com.arbit.app.user.dto.MyReviewResponse;
import com.arbit.app.user.dto.UpdateNicknameResponse;
import com.arbit.app.user.dto.UpdateProfileImageResponse;
import com.arbit.app.user.entity.User;
import com.arbit.app.user.repository.UserRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserMeService {

    private final UserRepository userRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserCategoryRepository userCategoryRepository;
    private final ReviewRepository reviewRepository;
    private final UserPreferenceKeywordRepository userPreferenceKeywordRepository;
    private final RecommendationRepository recommendationRepository;
    private final ObjectProvider<StorageService> storageServiceProvider;

    public UserMeService(UserRepository userRepository,
                         BookmarkRepository bookmarkRepository,
                         UserCategoryRepository userCategoryRepository,
                         ReviewRepository reviewRepository,
                         UserPreferenceKeywordRepository userPreferenceKeywordRepository,
                         RecommendationRepository recommendationRepository,
                         ObjectProvider<StorageService> storageServiceProvider) {
        this.userRepository = userRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.userCategoryRepository = userCategoryRepository;
        this.reviewRepository = reviewRepository;
        this.userPreferenceKeywordRepository = userPreferenceKeywordRepository;
        this.recommendationRepository = recommendationRepository;
        this.storageServiceProvider = storageServiceProvider;
    }

    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile(UUID userId) {
        User user = getUser(userId);
        List<String> tasteKeywords = userPreferenceKeywordRepository.findAllByUserIdWithKeyword(userId).stream()
                .map(userPreferenceKeyword -> userPreferenceKeyword.getPreferenceKeyword().getValue())
                .toList();
        return new MyProfileResponse(
                user.getProfileImageUrl(),
                user.getNickname(),
                user.getCreatedAt(),
                tasteKeywords
        );
    }

    @Transactional
    public UpdateNicknameResponse updateNickname(UUID userId, String nickname) {
        User user = getUser(userId);
        user.updateNickname(nickname.trim());
        return new UpdateNicknameResponse(user.getNickname());
    }

    @Transactional
    public UpdateProfileImageResponse updateProfileImage(UUID userId, MultipartFile profileImage) {
        User user = getUser(userId);
        validateProfileImage(profileImage);
        StorageService storageService = storageServiceProvider.getIfAvailable();
        if (storageService == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Storage service is not configured.");
        }
        try {
            String objectName = "users/%s/profile/%s".formatted(userId, buildStoredFileName(profileImage.getOriginalFilename()));
            String imageUrl = storageService.upload(
                    objectName,
                    profileImage.getInputStream(),
                    profileImage.getSize(),
                    profileImage.getContentType()
            );
            user.updateProfileImageUrl(imageUrl);
            return new UpdateProfileImageResponse(imageUrl);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Profile image upload failed.");
        } catch (IllegalStateException | IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Profile image upload failed.");
        }
    }

    @Transactional(readOnly = true)
    public List<MyBookmarkResponse> getMyBookmarks(UUID userId) {
        return bookmarkRepository.findAllByUserIdWithEvent(userId).stream()
                .map(this::toBookmarkResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MyReviewResponse> getMyReviews(UUID userId) {
        return reviewRepository.findAllByUserIdWithEvent(userId).stream()
                .map(this::toReviewResponse)
                .toList();
    }

    @Transactional
    public void deleteMyAccount(UUID userId) {
        User user = getUser(userId);
        List<Event> reviewedEvents = reviewRepository.findAllByUserIdWithEvent(userId).stream()
                .map(Review::getEvent)
                .distinct()
                .toList();

        bookmarkRepository.deleteAllByUserId(userId);
        userCategoryRepository.deleteAllByUserId(userId);
        recommendationRepository.deleteAllByUserId(userId);
        userPreferenceKeywordRepository.deleteAllByUserId(userId);
        reviewRepository.deleteAllByUserId(userId);

        reviewedEvents.forEach(this::updateAverageRating);
        userRepository.delete(user);
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    private MyBookmarkResponse toBookmarkResponse(Bookmark bookmark) {
        return new MyBookmarkResponse(
                bookmark.getEvent().getTitle(),
                bookmark.getEvent().getPosterImageUrl(),
                bookmark.getEvent().getCategory().getName(),
                bookmark.getEvent().getVenue(),
                bookmark.getEvent().getStartDate(),
                bookmark.getEvent().getEndDate(),
                bookmark.getCreatedAt()
        );
    }

    private MyReviewResponse toReviewResponse(Review review) {
        return new MyReviewResponse(
                review.getId(),
                review.getEvent().getTitle(),
                review.getEvent().getPosterImageUrl(),
                review.getRating(),
                review.getContent(),
                0L,
                review.getCreatedAt()
        );
    }

    private void validateProfileImage(MultipartFile profileImage) {
        if (profileImage == null || profileImage.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Profile image file is required.");
        }
        String contentType = profileImage.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "Only image files can be uploaded.");
        }
    }

    private String buildStoredFileName(String originalFilename) {
        String safeName = originalFilename == null ? "profile-image" : originalFilename.replaceAll("[^A-Za-z0-9._-]", "_");
        return UUID.randomUUID() + "-" + safeName;
    }

    private void updateAverageRating(Event event) {
        double averageRating = reviewRepository.averageRatingByEventId(event.getId());
        event.updateAverageRating(BigDecimal.valueOf(averageRating).setScale(2, RoundingMode.HALF_UP));
    }
}

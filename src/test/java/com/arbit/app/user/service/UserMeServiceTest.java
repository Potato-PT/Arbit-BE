package com.arbit.app.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arbit.app.bookmark.repository.BookmarkRepository;
import com.arbit.app.category.repository.UserCategoryRepository;
import com.arbit.app.event.entity.Event;
import com.arbit.app.event.entity.EventStatus;
import com.arbit.app.keyword.repository.UserPreferenceKeywordRepository;
import com.arbit.app.recommendation.repository.RecommendationRepository;
import com.arbit.app.review.entity.Review;
import com.arbit.app.review.repository.ReviewRepository;
import com.arbit.app.storage.StorageService;
import com.arbit.app.user.entity.User;
import com.arbit.app.user.repository.UserRepository;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

@ExtendWith(MockitoExtension.class)
class UserMeServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private UserCategoryRepository userCategoryRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserPreferenceKeywordRepository userPreferenceKeywordRepository;

    @Mock
    private RecommendationRepository recommendationRepository;

    @Mock
    private ObjectProvider<StorageService> storageServiceProvider;

    @Test
    void deleteMyAccountRemovesUserRelatedDataAndRecalculatesRatings() throws Exception {
        UserMeService userMeService = new UserMeService(
                userRepository,
                bookmarkRepository,
                userCategoryRepository,
                reviewRepository,
                userPreferenceKeywordRepository,
                recommendationRepository,
                storageServiceProvider
        );

        User user = User.builder()
                .username("arbit_user_01")
                .password("encoded-password")
                .nickname("ArbitUser")
                .age(28)
                .residentialArea("Seoul")
                .build();
        Event firstEvent = createEvent();
        Event secondEvent = createEvent();
        Review firstReview = Review.builder()
                .user(user)
                .event(firstEvent)
                .rating(5)
                .content("great")
                .build();
        Review secondReview = Review.builder()
                .user(user)
                .event(secondEvent)
                .rating(4)
                .content("good")
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(reviewRepository.findAllByUserIdWithEvent(user.getId())).thenReturn(List.of(firstReview, secondReview));
        when(reviewRepository.averageRatingByEventId(firstEvent.getId())).thenReturn(3.5);
        when(reviewRepository.averageRatingByEventId(secondEvent.getId())).thenReturn(0.0);

        userMeService.deleteMyAccount(user.getId());

        verify(bookmarkRepository).deleteAllByUserId(user.getId());
        verify(userCategoryRepository).deleteAllByUserId(user.getId());
        verify(recommendationRepository).deleteAllByUserId(user.getId());
        verify(userPreferenceKeywordRepository).deleteAllByUserId(user.getId());
        verify(reviewRepository).deleteAllByUserId(user.getId());
        verify(userRepository).delete(user);
        assertThat(firstEvent.getAverageRating()).isEqualByComparingTo(new BigDecimal("3.50"));
        assertThat(secondEvent.getAverageRating()).isEqualByComparingTo(new BigDecimal("0.00"));
    }

    private Event createEvent() throws Exception {
        Event event = Event.builder()
                .category(null)
                .title("Event")
                .description("Description")
                .posterImageUrl("https://cdn.arbit.app/events/poster.jpg")
                .venue("Venue")
                .district("District")
                .startDate(LocalDate.of(2026, 5, 1))
                .endDate(LocalDate.of(2026, 6, 1))
                .free(true)
                .status(EventStatus.ONGOING)
                .build();
        setField(event, "id", UUID.randomUUID());
        return event;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

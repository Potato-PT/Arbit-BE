package com.arbit.app.preference.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.arbit.app.category.entity.Category;
import com.arbit.app.category.entity.UserCategory;
import com.arbit.app.category.repository.CategoryRepository;
import com.arbit.app.category.repository.UserCategoryRepository;
import com.arbit.app.keyword.entity.PreferenceKeyword;
import com.arbit.app.keyword.entity.UserPreferenceKeyword;
import com.arbit.app.keyword.repository.PreferenceKeywordRepository;
import com.arbit.app.keyword.repository.UserPreferenceKeywordRepository;
import com.arbit.app.preference.dto.CreatePreferenceRequest;
import com.arbit.app.preference.dto.CreatePreferenceRequest.Keyword1Item;
import com.arbit.app.user.entity.User;
import com.arbit.app.user.entity.UserGender;
import com.arbit.app.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class PreferenceServiceTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private UserRepository userRepository;
    private CategoryRepository categoryRepository;
    private UserCategoryRepository userCategoryRepository;
    private PreferenceKeywordRepository preferenceKeywordRepository;
    private UserPreferenceKeywordRepository userPreferenceKeywordRepository;
    private PreferenceService preferenceService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        categoryRepository = mock(CategoryRepository.class);
        userCategoryRepository = mock(UserCategoryRepository.class);
        preferenceKeywordRepository = mock(PreferenceKeywordRepository.class);
        userPreferenceKeywordRepository = mock(UserPreferenceKeywordRepository.class);
        preferenceService = new PreferenceService(
                userRepository,
                categoryRepository,
                userCategoryRepository,
                preferenceKeywordRepository,
                userPreferenceKeywordRepository
        );
    }

    @Test
    void createPreferencesReplacesExistingSelectionsAndStoresDistinctValues() {
        User user = User.builder()
                .username("arbit_user_01")
                .password("encoded-password")
                .nickname("ArbitUser")
                .age(28)
                .gender(UserGender.MALE)
                .residentialArea("Seoul Seongbuk-gu")
                .residentialLatitude(37.0)
                .residentialLongitude(127.0)
                .build();
        ReflectionTestUtils.setField(user, "id", USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        Category exhibition = Category.builder().name("EXHIBITION").build();
        ReflectionTestUtils.setField(exhibition, "id", 1L);
        PreferenceKeyword family = PreferenceKeyword.builder().value("FAMILY").build();
        ReflectionTestUtils.setField(family, "id", 10L);

        when(categoryRepository.findByNameIn(anyCollection())).thenReturn(List.of(exhibition));
        when(categoryRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(preferenceKeywordRepository.findByValueIn(anyCollection())).thenReturn(List.of(family));
        when(preferenceKeywordRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        CreatePreferenceRequest request = new CreatePreferenceRequest(
                List.of(
                        new Keyword1Item("EXHIBITION", List.of("SOLO", "CURATED")),
                        new Keyword1Item("EXHIBITION", List.of("SOLO")),
                        new Keyword1Item("EDUCATION", List.of("LECTURE"))
                ),
                List.of("FAMILY", "FAMILY"),
                List.of("ALL_AGES"),
                List.of("FREE_TEXT")
        );

        preferenceService.createPreferences(USER_ID, request);

        verify(userCategoryRepository).deleteAllByUserId(USER_ID);
        verify(userPreferenceKeywordRepository).deleteAllByUserId(USER_ID);

        ArgumentCaptor<List<UserCategory>> userCategoryCaptor = ArgumentCaptor.forClass(List.class);
        verify(userCategoryRepository).saveAll(userCategoryCaptor.capture());
        assertEquals(2, userCategoryCaptor.getValue().size());
        assertTrue(userCategoryCaptor.getValue().stream()
                .map(userCategory -> userCategory.getCategory().getName())
                .toList()
                .containsAll(List.of("EXHIBITION", "EDUCATION")));

        ArgumentCaptor<List<UserPreferenceKeyword>> userPreferenceKeywordCaptor = ArgumentCaptor.forClass(List.class);
        verify(userPreferenceKeywordRepository).saveAll(userPreferenceKeywordCaptor.capture());
        assertEquals(6, userPreferenceKeywordCaptor.getValue().size());
        assertTrue(userPreferenceKeywordCaptor.getValue().stream()
                .map(userPreferenceKeyword -> userPreferenceKeyword.getPreferenceKeyword().getValue())
                .toList()
                .containsAll(List.of("SOLO", "CURATED", "LECTURE", "FAMILY", "ALL_AGES", "FREE_TEXT")));
    }
}

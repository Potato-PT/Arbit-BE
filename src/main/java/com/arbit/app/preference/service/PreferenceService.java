package com.arbit.app.preference.service;

import com.arbit.app.category.entity.Category;
import com.arbit.app.category.entity.UserCategory;
import com.arbit.app.category.repository.CategoryRepository;
import com.arbit.app.category.repository.UserCategoryRepository;
import com.arbit.app.common.exception.BusinessException;
import com.arbit.app.common.exception.ErrorCode;
import com.arbit.app.keyword.entity.PreferenceKeyword;
import com.arbit.app.keyword.entity.UserPreferenceKeyword;
import com.arbit.app.keyword.repository.PreferenceKeywordRepository;
import com.arbit.app.keyword.repository.UserPreferenceKeywordRepository;
import com.arbit.app.preference.dto.CreatePreferenceRequest;
import com.arbit.app.preference.dto.PreferenceCategoriesResponse;
import com.arbit.app.preference.dto.PreferenceCategoriesResponse.PreferenceCategoryGroup;
import com.arbit.app.user.entity.User;
import com.arbit.app.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PreferenceService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final UserCategoryRepository userCategoryRepository;
    private final PreferenceKeywordRepository preferenceKeywordRepository;
    private final UserPreferenceKeywordRepository userPreferenceKeywordRepository;

    public PreferenceService(UserRepository userRepository,
                             CategoryRepository categoryRepository,
                             UserCategoryRepository userCategoryRepository,
                             PreferenceKeywordRepository preferenceKeywordRepository,
                             UserPreferenceKeywordRepository userPreferenceKeywordRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.userCategoryRepository = userCategoryRepository;
        this.preferenceKeywordRepository = preferenceKeywordRepository;
        this.userPreferenceKeywordRepository = userPreferenceKeywordRepository;
    }

    @Transactional(readOnly = true)
    public PreferenceCategoriesResponse getPreferenceCategories() {
        return new PreferenceCategoriesResponse(
                List.of(
                        new PreferenceCategoryGroup("전시/미술", List.of("개인전/초대전", "기획/테마 전시", "역사/문화/산업")),
                        new PreferenceCategoryGroup("클래식 및 독주/독창회", List.of("관현악/교향곡", "기악 독주회", "실내악/앙상블")),
                        new PreferenceCategoryGroup("교육/체험", List.of("만들기/공방 체험", "도서/독서 연계", "학술/강연")),
                        new PreferenceCategoryGroup("축제(통합)", List.of("야외 체험 행사", "종합 문화 페스티벌", "체험/참여형 축제", "기념/역사 축제")),
                        new PreferenceCategoryGroup("연극", List.of("아동/가족극", "기획/프로젝트극", "정통 연극/극단전")),
                        new PreferenceCategoryGroup("콘서트", List.of("재즈/크로스오버", "대중/인디 음악", "고궁/야외 콘서트", "성악/팝페라")),
                        new PreferenceCategoryGroup("국악", List.of("전통 국악", "창작/퓨전 국악")),
                        new PreferenceCategoryGroup("뮤지컬/오페라", List.of("뮤지컬", "오페라")),
                        new PreferenceCategoryGroup("무용", List.of("발레", "현대/창작무용", "전통무용")),
                        new PreferenceCategoryGroup("영화", List.of("특별 상영회/페스타", "고전/독립/예술 영화")),
                        new PreferenceCategoryGroup("기타", List.of("기타"))
                ),
                List.of("힐링/감성", "신나는/활기찬", "감동/웅장", "전통/문화", "가족친화", "학술/사색적"),
                List.of("아동/가족", "청소년", "일반 성인", "전 연령", "태그 없음")
        );
    }

    @Transactional
    public void createPreferences(UUID userId, CreatePreferenceRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        Set<String> categoryNames = request.keyword1().stream()
                .map(CreatePreferenceRequest.Keyword1Item::category)
                .map(String::trim)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);

        Set<String> keywordValues = new LinkedHashSet<>();
        request.keyword1().stream()
                .map(CreatePreferenceRequest.Keyword1Item::subcategories)
                .forEach(subcategories -> subcategories.stream()
                        .map(String::trim)
                        .forEach(keywordValues::add));
        request.keyword2().stream().map(String::trim).forEach(keywordValues::add);
        request.keyword3().stream().map(String::trim).forEach(keywordValues::add);
        request.keyword4().stream().map(String::trim).forEach(keywordValues::add);

        userCategoryRepository.deleteAllByUserId(userId);
        userPreferenceKeywordRepository.deleteAllByUserId(userId);

        Map<String, Category> categoriesByName = syncCategories(categoryNames);
        Map<String, PreferenceKeyword> keywordsByValue = syncPreferenceKeywords(keywordValues);

        List<UserCategory> userCategories = categoryNames.stream()
                .map(categoriesByName::get)
                .map(category -> UserCategory.builder()
                        .user(user)
                        .category(category)
                        .build())
                .toList();

        List<UserPreferenceKeyword> userPreferenceKeywords = keywordValues.stream()
                .map(keywordsByValue::get)
                .map(preferenceKeyword -> UserPreferenceKeyword.builder()
                        .user(user)
                        .preferenceKeyword(preferenceKeyword)
                        .build())
                .toList();

        userCategoryRepository.saveAll(userCategories);
        userPreferenceKeywordRepository.saveAll(userPreferenceKeywords);
    }

    private Map<String, Category> syncCategories(Set<String> categoryNames) {
        List<Category> existingCategories = categoryRepository.findByNameIn(categoryNames);
        Map<String, Category> categoriesByName = new LinkedHashMap<>();
        existingCategories.forEach(category -> categoriesByName.put(category.getName(), category));

        List<Category> newCategories = categoryNames.stream()
                .filter(name -> !categoriesByName.containsKey(name))
                .map(name -> Category.builder().name(name).build())
                .toList();

        if (!newCategories.isEmpty()) {
            List<Category> savedCategories = categoryRepository.saveAll(newCategories);
            savedCategories.forEach(category -> categoriesByName.put(category.getName(), category));
        }

        return categoriesByName;
    }

    private Map<String, PreferenceKeyword> syncPreferenceKeywords(Set<String> keywordValues) {
        if (keywordValues.isEmpty()) {
            return Map.of();
        }

        List<PreferenceKeyword> existingKeywords = preferenceKeywordRepository.findByValueIn(keywordValues);
        Map<String, PreferenceKeyword> keywordsByValue = new LinkedHashMap<>();
        existingKeywords.forEach(keyword -> keywordsByValue.put(keyword.getValue(), keyword));

        List<PreferenceKeyword> newKeywords = new ArrayList<>();
        for (String keywordValue : keywordValues) {
            if (!keywordsByValue.containsKey(keywordValue)) {
                newKeywords.add(PreferenceKeyword.builder().value(keywordValue).build());
            }
        }

        if (!newKeywords.isEmpty()) {
            List<PreferenceKeyword> savedKeywords = preferenceKeywordRepository.saveAll(newKeywords);
            savedKeywords.forEach(keyword -> keywordsByValue.put(keyword.getValue(), keyword));
        }

        return keywordsByValue;
    }
}

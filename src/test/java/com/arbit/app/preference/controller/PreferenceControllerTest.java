package com.arbit.app.preference.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.common.exception.GlobalExceptionHandler;
import com.arbit.app.preference.dto.PreferenceCategoriesResponse;
import com.arbit.app.preference.dto.PreferenceCategoriesResponse.PreferenceCategoryGroup;
import com.arbit.app.preference.service.PreferenceService;
import com.arbit.app.user.entity.User;
import com.arbit.app.user.entity.UserGender;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

class PreferenceControllerTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private MockMvc mockMvc;
    private PreferenceService preferenceService;

    @BeforeEach
    void setUp() {
        preferenceService = mock(PreferenceService.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new PreferenceController(preferenceService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalResolver())
                .setValidator(validator)
                .build();
    }

    @Test
    void getPreferenceCategoriesReturnsApiResponseWrappedData() throws Exception {
        PreferenceCategoriesResponse response = new PreferenceCategoriesResponse(
                List.of(new PreferenceCategoryGroup("EXHIBITION", List.of("SOLO", "CURATED"))),
                List.of("HEALING"),
                List.of("ALL_AGES")
        );
        when(preferenceService.getPreferenceCategories()).thenReturn(response);

        mockMvc.perform(get("/api/preferences/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.keyword1[0].category").value("EXHIBITION"))
                .andExpect(jsonPath("$.data.keyword2[0]").value("HEALING"))
                .andExpect(jsonPath("$.data.keyword3[0]").value("ALL_AGES"));
    }

    @Test
    void createPreferencesStoresAuthenticatedUsersSelections() throws Exception {
        doNothing().when(preferenceService).createPreferences(eq(USER_ID), any());

        mockMvc.perform(post("/api/preferences")
                        .principal(authenticatedUser())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "keyword1": [
                                    {
                                      "category": "EXHIBITION",
                                      "subcategories": ["SOLO", "CURATED"]
                                    }
                                  ],
                                  "keyword2": ["FAMILY", "THOUGHTFUL"],
                                  "keyword3": ["ALL_AGES", "NO_TAG"],
                                  "keyword4": ["FREE_TEXT"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.error").doesNotExist());

        verify(preferenceService).createPreferences(eq(USER_ID), any());
    }

    private Authentication authenticatedUser() {
        User user = User.builder()
                .username("arbit_user_01")
                .password("encoded-password")
                .nickname("ArbitUser")
                .age(28)
                .gender(UserGender.MALE)
                .residentialArea("Seoul Seongbuk-gu")
                .residentialLatitude(37.5946)
                .residentialLongitude(127.0231)
                .build();
        ReflectionTestUtils.setField(user, "id", USER_ID);
        CustomUserDetails userDetails = new CustomUserDetails(user);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private static final class AuthenticationPrincipalResolver implements HandlerMethodArgumentResolver {

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                    && CustomUserDetails.class.isAssignableFrom(parameter.getParameterType());
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            Authentication authentication = (Authentication) webRequest.getUserPrincipal();
            return authentication == null ? null : authentication.getPrincipal();
        }
    }
}

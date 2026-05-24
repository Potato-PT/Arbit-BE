package com.arbit.app.user.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.common.exception.GlobalExceptionHandler;
import com.arbit.app.user.dto.MyBookmarkResponse;
import com.arbit.app.user.dto.MyProfileResponse;
import com.arbit.app.user.dto.MyReviewResponse;
import com.arbit.app.user.dto.UpdateNicknameRequest;
import com.arbit.app.user.dto.UpdateNicknameResponse;
import com.arbit.app.user.dto.UpdateProfileImageResponse;
import com.arbit.app.user.entity.User;
import com.arbit.app.user.entity.UserGender;
import com.arbit.app.user.service.UserMeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
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

class UserMeControllerTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private MockMvc mockMvc;
    private UserMeService userMeService;

    @BeforeEach
    void setUp() {
        userMeService = mock(UserMeService.class);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        ObjectMapper objectMapper = new ObjectMapper()
                .findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(new UserMeController(userMeService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setValidator(validator)
                .build();
    }

    @Test
    void getMyProfileReturnsAuthenticatedUserProfile() throws Exception {
        MyProfileResponse response = new MyProfileResponse(
                "https://cdn.arbit.app/users/profile.jpg",
                "ArbitUser",
                LocalDateTime.of(2026, 5, 11, 15, 3, 12),
                List.of("Media Art", "Immersive", "Jazz")
        );
        when(userMeService.getMyProfile(USER_ID)).thenReturn(response);

        mockMvc.perform(get("/api/users/me")
                        .principal(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.profileImageUrl").value("https://cdn.arbit.app/users/profile.jpg"))
                .andExpect(jsonPath("$.data.nickname").value("ArbitUser"))
                .andExpect(jsonPath("$.data.subscribedAt").value("2026-05-11T15:03:12"))
                .andExpect(jsonPath("$.data.tasteKeywords[0]").value("Media Art"))
                .andExpect(jsonPath("$.data.tasteKeywords[1]").value("Immersive"))
                .andExpect(jsonPath("$.data.tasteKeywords[2]").value("Jazz"));

        verify(userMeService).getMyProfile(USER_ID);
    }

    @Test
    void updateProfileImageReturnsUploadedImageUrl() throws Exception {
        MockMultipartFile profileImage = new MockMultipartFile(
                "profileImage",
                "profile.jpg",
                "image/jpeg",
                "image-bytes".getBytes()
        );
        UpdateProfileImageResponse response =
                new UpdateProfileImageResponse("https://cdn.arbit.app/users/1111/profile/new-profile.jpg");
        when(userMeService.updateProfileImage(USER_ID, profileImage)).thenReturn(response);

        mockMvc.perform(multipart("/api/users/me/profile_image")
                        .file(profileImage)
                        .with(request -> {
                            request.setMethod("PUT");
                            request.setUserPrincipal(authenticatedUser());
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.profileImageUrl")
                        .value("https://cdn.arbit.app/users/1111/profile/new-profile.jpg"));

        verify(userMeService).updateProfileImage(USER_ID, profileImage);
    }

    @Test
    void updateNicknameReturnsUpdatedNickname() throws Exception {
        UpdateNicknameRequest request = new UpdateNicknameRequest("CultureHunter");
        when(userMeService.updateNickname(USER_ID, request.nickname()))
                .thenReturn(new UpdateNicknameResponse("CultureHunter"));

        mockMvc.perform(put("/api/users/me/nickname")
                        .principal(authenticatedUser())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "CultureHunter"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("CultureHunter"));

        verify(userMeService).updateNickname(USER_ID, request.nickname());
    }

    @Test
    void updateNicknameRejectsBlankNickname() throws Exception {
        mockMvc.perform(put("/api/users/me/nickname")
                        .principal(authenticatedUser())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": " "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));
    }

    @Test
    void getMyBookmarksReturnsBookmarkList() throws Exception {
        List<MyBookmarkResponse> response = List.of(new MyBookmarkResponse(
                "Echoes of Silence",
                "https://cdn.arbit.app/events/light-museum/poster.jpg",
                "Media Art",
                "Metropolitan Museum",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 6, 30),
                LocalDateTime.of(2026, 5, 20, 18, 30, 0)
        ));
        when(userMeService.getMyBookmarks(USER_ID)).thenReturn(response);

        mockMvc.perform(get("/api/users/me/bookmarks")
                        .principal(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Echoes of Silence"))
                .andExpect(jsonPath("$.data[0].category").value("Media Art"))
                .andExpect(jsonPath("$.data[0].bookmarkedAt").value("2026-05-20T18:30:00"));

        verify(userMeService).getMyBookmarks(USER_ID);
    }

    @Test
    void getMyReviewsReturnsReviewList() throws Exception {
        List<MyReviewResponse> response = List.of(new MyReviewResponse(
                12L,
                "Echoes of Silence",
                "https://cdn.arbit.app/events/light-museum/poster.jpg",
                5,
                "The immersive media wall was the highlight.",
                0L,
                LocalDateTime.of(2026, 5, 23, 9, 40, 0)
        ));
        when(userMeService.getMyReviews(USER_ID)).thenReturn(response);

        mockMvc.perform(get("/api/users/me/reviews")
                        .principal(authenticatedUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].reviewId").value(12))
                .andExpect(jsonPath("$.data[0].title").value("Echoes of Silence"))
                .andExpect(jsonPath("$.data[0].starScore").value(5))
                .andExpect(jsonPath("$.data[0].createdAt").value("2026-05-23T09:40:00"));

        verify(userMeService).getMyReviews(USER_ID);
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

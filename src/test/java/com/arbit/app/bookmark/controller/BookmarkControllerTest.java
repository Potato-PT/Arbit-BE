package com.arbit.app.bookmark.controller;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.bookmark.service.BookmarkService;
import com.arbit.app.user.entity.User;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@ExtendWith(MockitoExtension.class)
class BookmarkControllerTest {

    @Mock
    private BookmarkService bookmarkService;

    private MockMvc mockMvc;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        userDetails = new CustomUserDetails(User.builder()
                .username("loginId1234")
                .password("encoded-password")
                .build());
        org.springframework.test.util.ReflectionTestUtils.setField(userDetails.user(), "id", userId);
        mockMvc = MockMvcBuilders.standaloneSetup(new BookmarkController(bookmarkService))
                .setCustomArgumentResolvers(new TestAuthenticationPrincipalResolver(userDetails))
                .build();
    }

    @Test
    void removeBookmarkReturnsNoContent() throws Exception {
        UUID eventId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        mockMvc.perform(delete("/api/bookmarks/{eventId}", eventId))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(bookmarkService).removeBookmark(userDetails.id(), eventId);
    }

    private record TestAuthenticationPrincipalResolver(CustomUserDetails userDetails)
            implements HandlerMethodArgumentResolver {

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return CustomUserDetails.class.isAssignableFrom(parameter.getParameterType());
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            return userDetails;
        }
    }
}

package com.arbit.app.preference.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.arbit.app.auth.security.CustomUserDetails;
import com.arbit.app.auth.security.JwtTokenProvider;
import com.arbit.app.auth.security.RestAccessDeniedHandler;
import com.arbit.app.auth.security.RestAuthenticationEntryPoint;
import com.arbit.app.common.config.SecurityConfig;
import com.arbit.app.common.exception.BusinessException;
import com.arbit.app.common.exception.ErrorCode;
import com.arbit.app.common.exception.GlobalExceptionHandler;
import com.arbit.app.preference.service.PreferenceService;
import com.arbit.app.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.userdetails.UserDetailsService;

@WebMvcTest(PreferenceController.class)
@Import({
        SecurityConfig.class,
        RestAuthenticationEntryPoint.class,
        RestAccessDeniedHandler.class,
        GlobalExceptionHandler.class
})
class PreferenceControllerTest {

    private static final UUID USER_ID = UUID.fromString("3dbd80e2-1c08-47d3-aa4c-d710e24875d4");
    private static final List<UUID> EXISTING_EVENT_IDS = List.of(
            UUID.fromString("189fc863-ff6b-476b-b01e-772c790f2b3e"),
            UUID.fromString("c6496a05-f929-403c-a8ed-c1e9b3b7860c"),
            UUID.fromString("25b9139b-3ed2-4923-907f-59322a55bc98"),
            UUID.fromString("29acf197-aab6-4b3d-957d-ccd6f420e098"),
            UUID.fromString("d4e90ffe-f663-4ff4-90dd-60db60c8f60a"),
            UUID.fromString("17b4422f-de50-4fab-b78c-35527cb4113e"),
            UUID.fromString("3c48c48e-97e6-41ab-ad4e-2c26a79c48a7"),
            UUID.fromString("b445560d-39b3-40cf-95e7-4dac13ea5ce6"),
            UUID.fromString("1cc2aadf-c4e6-4691-aa9e-d72d027dfb83"),
            UUID.fromString("b3e799cc-01fd-41c9-8376-7c71b6d4882f"),
            UUID.fromString("0453fe13-4525-4653-b099-f4eaa81e296f"),
            UUID.fromString("c5717432-2543-4acb-bf73-ce9730017ba9"),
            UUID.fromString("32355cd5-3c60-42ac-9089-97f03f2e0db5"),
            UUID.fromString("83491854-5e58-4b17-8288-d6a88ff6d52b"),
            UUID.fromString("3cd95282-7559-4876-b149-88826be363ac"),
            UUID.fromString("f7f950dd-e563-4a46-a9ae-67f1e3f622f2"),
            UUID.fromString("43226268-b0ab-4120-904d-611433ed0a22"),
            UUID.fromString("fa00106b-8504-4a44-a97f-3a11187db101"),
            UUID.fromString("d5f473dc-f0bd-466f-bcc6-1c290cf5d163"),
            UUID.fromString("2d95fd6f-319d-48ba-9ead-f5873160034e"),
            UUID.fromString("25b6422c-fbfa-4906-a7e2-ace2c5a62564")
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PreferenceService preferenceService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        User authenticatedUser = org.mockito.Mockito.mock(User.class);
        org.mockito.Mockito.when(authenticatedUser.getId()).thenReturn(USER_ID);
        org.mockito.Mockito.when(authenticatedUser.getUsername()).thenReturn("preference-test-user");
        org.mockito.Mockito.when(authenticatedUser.getPassword()).thenReturn("encoded-password");
        userDetails = new CustomUserDetails(authenticatedUser);
    }

    @Test
    void createPreferences_acceptsSixEvents() throws Exception {
        List<UUID> eventIds = EXISTING_EVENT_IDS.subList(0, 6);

        performAuthenticated(eventIds)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(6))
                .andExpect(jsonPath("$.error").doesNotExist());

        verify(preferenceService).createPreferences(USER_ID, eventIds);
    }

    @Test
    void createPreferences_acceptsTwentyEvents() throws Exception {
        List<UUID> eventIds = EXISTING_EVENT_IDS.subList(0, 20);

        performAuthenticated(eventIds)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(20));

        verify(preferenceService).createPreferences(USER_ID, eventIds);
    }

    @Test
    void createPreferences_acceptsFiveEvents() throws Exception {
        List<UUID> eventIds = EXISTING_EVENT_IDS.subList(0, 5);

        performAuthenticated(eventIds)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(5));

        verify(preferenceService).createPreferences(USER_ID, eventIds);
    }

    @Test
    void createPreferences_rejectsFourEvents() throws Exception {
        assertInvalidRequest(EXISTING_EVENT_IDS.subList(0, 4));
    }

    @Test
    void createPreferences_rejectsTwentyOneEvents() throws Exception {
        assertInvalidRequest(EXISTING_EVENT_IDS);
    }

    @Test
    void createPreferences_rejectsDuplicateEvent() throws Exception {
        List<UUID> eventIds = List.of(
                EXISTING_EVENT_IDS.get(0),
                EXISTING_EVENT_IDS.get(1),
                EXISTING_EVENT_IDS.get(2),
                EXISTING_EVENT_IDS.get(3),
                EXISTING_EVENT_IDS.get(4),
                EXISTING_EVENT_IDS.get(0)
        );

        assertInvalidRequest(eventIds);
    }

    @Test
    void createPreferences_currentlyAcceptsNonexistentEventId() throws Exception {
        UUID nonexistentEventId = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
        List<UUID> eventIds = List.of(
                EXISTING_EVENT_IDS.get(0),
                EXISTING_EVENT_IDS.get(1),
                EXISTING_EVENT_IDS.get(2),
                EXISTING_EVENT_IDS.get(3),
                EXISTING_EVENT_IDS.get(4),
                nonexistentEventId
        );

        performAuthenticated(eventIds)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(6));

        verify(preferenceService).createPreferences(USER_ID, eventIds);
    }

    @Test
    void createPreferences_rejectsNullEvent() throws Exception {
        List<UUID> eventIds = new ArrayList<>(EXISTING_EVENT_IDS.subList(0, 6));
        eventIds.set(5, null);
        String request = """
                {
                  "success": true,
                  "data": [
                    "189fc863-ff6b-476b-b01e-772c790f2b3e",
                    "c6496a05-f929-403c-a8ed-c1e9b3b7860c",
                    "25b9139b-3ed2-4923-907f-59322a55bc98",
                    "29acf197-aab6-4b3d-957d-ccd6f420e098",
                    "d4e90ffe-f663-4ff4-90dd-60db60c8f60a",
                    null
                  ],
                  "error": null
                }
                """;

        assertInvalidRequest(request, eventIds);
    }

    @Test
    void createPreferences_rejectsEmptyEventList() throws Exception {
        assertInvalidRequest(List.of());
    }

    @Test
    void createPreferences_rejectsNullEventList() throws Exception {
        assertInvalidRequest("""
                {
                  "success": true,
                  "data": null,
                  "error": null
                }
                """, null);
    }

    @Test
    void createPreferences_requiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody(EXISTING_EVENT_IDS.subList(0, 6))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHORIZED"));

        verifyNoInteractions(preferenceService);
    }

    @Test
    void createPreferences_rejectsInvalidUuidFormat() throws Exception {
        String request = """
                {
                  "success": true,
                  "data": [
                    "189fc863-ff6b-476b-b01e-772c790f2b3e",
                    "c6496a05-f929-403c-a8ed-c1e9b3b7860c",
                    "25b9139b-3ed2-4923-907f-59322a55bc98",
                    "29acf197-aab6-4b3d-957d-ccd6f420e098",
                    "d4e90ffe-f663-4ff4-90dd-60db60c8f60a",
                    "not-a-uuid"
                  ],
                  "error": null
                }
                """;

        mockMvc.perform(post("/api/preferences")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));

        verifyNoInteractions(preferenceService);
    }

    private org.springframework.test.web.servlet.ResultActions performAuthenticated(List<UUID> eventIds)
            throws Exception {
        return mockMvc.perform(post("/api/preferences")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody(eventIds)));
    }

    private void assertInvalidRequest(List<UUID> eventIds) throws Exception {
        doThrow(new BusinessException(ErrorCode.INVALID_REQUEST, "Select between 5 and 20 distinct events."))
                .when(preferenceService)
                .createPreferences(USER_ID, eventIds);

        assertInvalidRequest(requestBody(eventIds), eventIds);
    }

    private void assertInvalidRequest(String request, List<UUID> expectedEventIds) throws Exception {
        doThrow(new BusinessException(ErrorCode.INVALID_REQUEST, "Select between 5 and 20 distinct events."))
                .when(preferenceService)
                .createPreferences(USER_ID, expectedEventIds);

        mockMvc.perform(post("/api/preferences")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.error.message").value("Select between 5 and 20 distinct events."));

        verify(preferenceService).createPreferences(USER_ID, expectedEventIds);
    }

    private String requestBody(List<UUID> eventIds) throws Exception {
        return objectMapper.writeValueAsString(new RequestBody(true, eventIds, null));
    }

    private record RequestBody(boolean success, List<UUID> data, Object error) {
    }
}

package com.arbit.app.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.arbit.app.user.entity.User;
import com.arbit.app.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthSignupIntegrationTest {

    private static final String USERNAME = "arbit_user_01";
    private static final double EXPECTED_LATITUDE = 37.5946226781717;
    private static final double EXPECTED_LONGITUDE = 127.023128563512;
    private static final double COORDINATE_TOLERANCE = 0.000001;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void signupStoresCoordinatesResolvedByRealKakaoApi() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "arbit_user_01",
                                  "password": "password1234",
                                  "nickname": "ArbitUser",
                                  "birthYear": 1998,
                                  "gender": "MALE",
                                  "residentialArea": "서울특별시 성북구 북악산로 918"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andExpect(jsonPath("$.data.refreshToken").isString());

        Optional<User> savedUser = userRepository.findByUsername(USERNAME);
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getResidentialLatitude())
                .isCloseTo(EXPECTED_LATITUDE, within(COORDINATE_TOLERANCE));
        assertThat(savedUser.get().getResidentialLongitude())
                .isCloseTo(EXPECTED_LONGITUDE, within(COORDINATE_TOLERANCE));
    }

    private static org.assertj.core.data.Offset<Double> within(double tolerance) {
        return org.assertj.core.data.Offset.offset(tolerance);
    }
}

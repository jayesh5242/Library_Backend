package com.example.Library_backend.controller;

import com.example.Library_backend.dto.request.authrequest.LoginRequest;
import com.example.Library_backend.dto.request.authrequest.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.StringContains.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @SpringBootTest = Load the full Spring context
// @AutoConfigureMockMvc = Auto-create MockMvc for HTTP calls
// @ActiveProfiles("test") = Use application-test.properties
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AuthControllerTest {

    // MockMvc simulates HTTP requests without a real server
    @Autowired
    private MockMvc mockMvc;

    // ObjectMapper converts Java objects to JSON strings
    @Autowired
    private ObjectMapper objectMapper;

    // ─── TEST 1: Register New User ─────────────────────
    @Test
    void register_ShouldReturn200_WhenValidData() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Test Student");
        request.setEmail("test@college.edu");
        request.setPassword("password123");
        request.setRole("STUDENT");

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message",
                        containsString("successful")));
    }

    // ─── TEST 2: Register Duplicate Email ─────────────
    @Test
    void register_ShouldReturn400_WhenDuplicateEmail()
            throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Test User");
        request.setEmail("dup@college.edu");
        request.setPassword("password123");
        request.setRole("STUDENT");

        // Register first time - should succeed
        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isOk());

        // Register second time - should fail
        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── TEST 3: Login with Correct Credentials ───────
    @Test
    void login_ShouldReturnToken_WhenValidCredentials()
            throws Exception {
        // First register
        RegisterRequest regReq = new RegisterRequest();
        regReq.setFullName("Login Test User");
        regReq.setEmail("login@college.edu");
        regReq.setPassword("testpass123");
        regReq.setRole("STUDENT");

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq))
        ).andExpect(status().isOk());

        // Then login
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("login@college.edu");
        loginReq.setPassword("testpass123");

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginReq))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken",
                        notNullValue()))
                .andExpect(jsonPath("$.data.role").value("STUDENT"));
    }

    // ─── TEST 4: Login with Wrong Password ────────────
    @Test
    void login_ShouldReturn400_WhenWrongPassword()
            throws Exception {
        LoginRequest loginReq = new LoginRequest();
        loginReq.setEmail("nobody@college.edu");
        loginReq.setPassword("wrongpassword");

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginReq))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ─── TEST 5: Protected Route Without Token ────────
    @Test
    void profile_ShouldReturn403_WhenNoToken()
            throws Exception {
        mockMvc.perform(
                        get("/api/auth/profile")
                )
                .andExpect(status().isForbidden());
    }

    // ─── TEST 6: Register with Empty Email ────────────
    @Test
    void register_ShouldReturn400_WhenEmptyEmail()
            throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Test");
        request.setEmail(""); // empty!
        request.setPassword("password123");

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
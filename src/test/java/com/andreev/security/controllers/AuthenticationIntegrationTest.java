package com.andreev.security.controllers;

import com.andreev.security.user.dto.authentication.AuthenticationResponse;
import com.andreev.security.user.dto.authentication.RevalidateJwtRequest;
import com.andreev.security.utils.AuthenticationTestsUtils;
import com.andreev.security.user.dto.authentication.RegisterRequest;
import com.andreev.security.user.services.authentication.UserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class AuthenticationIntegrationTest {

    private MockMvc mockMvc;
    private UserDetailsService userDetailsService;
    private ObjectMapper objectMapper;
    private AuthenticationTestsUtils authenticationTestsUtils;

    @Autowired
    public AuthenticationIntegrationTest(MockMvc mockMvc, ObjectMapper objectMapper, UserDetailsService userDetailsService, AuthenticationTestsUtils authenticationTestsUtils) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.userDetailsService = userDetailsService;
        this.authenticationTestsUtils = authenticationTestsUtils;
    }

    @Test
    public void testThatRegisterWithValidInformationSentReturnsHttpStatusCodeOf200AndCanDelete() throws Exception {
        RegisterRequest req = authenticationTestsUtils.getRegisterRequest();
        String reqAsJson = authenticationTestsUtils.getRegisterRequestAsJson();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqAsJson))
                .andExpect(status().isOk());
        userDetailsService.deleteUserByEmail(req.getEmail());
    }

    @Test
    public void testThatUserCanAccessRoutesThatRequireAuthenticationUsingToken() throws Exception {
        AuthenticationResponse authResponse = authenticationTestsUtils.getSignInResponse();
        mockMvc.perform(get("/api/demo").header(HttpHeaders.AUTHORIZATION,
                "Bearer " + authResponse.getToken()))
                .andExpect(status().isOk());
    }

    @Test
    public void testThatReavalidateJwtReturnsStatusCodeOf200() throws Exception {
        AuthenticationResponse authResponse = authenticationTestsUtils.getSignInResponse();

        RevalidateJwtRequest revalidateReq = RevalidateJwtRequest.builder()
                .refreshToken(authResponse.getRefreshToken())
                .build();
        String revalidateReqAsJson = objectMapper.writeValueAsString(revalidateReq);

        mockMvc.perform(post("/api/auth/revalidate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(revalidateReqAsJson))
                .andExpect(status().isOk());
    }
}

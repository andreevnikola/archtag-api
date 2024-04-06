package com.andreev.security.controllers;

import com.andreev.security.TestUtils;
import com.andreev.security.dto.authentication.RegisterRequest;
import com.andreev.security.dto.authentication.SigninRequest;
import com.andreev.security.services.authentication.AuthenticationService;
import com.andreev.security.services.authentication.UserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.jfr.ContentType;
import org.apache.catalina.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
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
    private TestUtils testUtils;

    @Autowired
    public AuthenticationIntegrationTest(MockMvc mockMvc, ObjectMapper objectMapper, UserDetailsService userDetailsService, TestUtils testUtils) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.userDetailsService = userDetailsService;
        this.testUtils = testUtils;
    }

    @Test
    public void testThatRegisterWithValidInformationSentReturnsHttpStatusCodeOf200AndCanDelete() throws Exception {
        RegisterRequest req = testUtils.getRegisterRequest();
        String reqAsJson = testUtils.getRegisterRequestAsJson();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqAsJson))
                .andExpect(status().isOk());
        userDetailsService.deleteUserByEmail(req.getEmail());
    }
}

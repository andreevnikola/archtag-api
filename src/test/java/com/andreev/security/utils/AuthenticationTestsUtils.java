package com.andreev.security.utils;

import com.andreev.security.controllers.AuthenticationController;
import com.andreev.security.dto.authentication.AuthenticationResponse;
import com.andreev.security.dto.authentication.RegisterRequest;
import com.andreev.security.dto.authentication.SigninRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@Controller
@RequiredArgsConstructor
public class AuthenticationTestsUtils {

    private final ObjectMapper objectMapper;
    private final MockMvc mockMvc;

    Logger logger = LoggerFactory.getLogger(AuthenticationTestsUtils.class);

    public RegisterRequest getRegisterRequest() {
        return RegisterRequest.builder()
                .email("ioanhailovski@gmail.com")
                .password("ioan_mihailovski")
                .firstname("Ioan")
                .lastname("Mihailovski")
                .build();
    }

    public String getRegisterRequestAsJson() throws JsonProcessingException {
        return objectMapper.writeValueAsString(getRegisterRequest());
    }

    public SigninRequest getSigninRequest() {
        return SigninRequest.builder()
                .email("integrationtest@google.com")
                .password("integration_test")
                .build();
    }

    public String getSigninRequestAsJson() throws JsonProcessingException {
        return objectMapper.writeValueAsString(getSigninRequest());
    }


    public AuthenticationResponse getSignInResponse() throws Exception {
        String reqAsJson = getSigninRequestAsJson();

        MvcResult res = mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(reqAsJson)).andReturn();

        if (res.getResponse().getStatus() != 200) {
            logger.error("Error while trying to sign in: " + res.getResponse().getErrorMessage() + " (" + res.getResponse().getStatus() + ")");
            throw new Exception("Something went wrong while trying to signin");
        }

        String resAsJson = res.getResponse().getContentAsString();
        AuthenticationResponse response =  objectMapper.readValue(resAsJson, AuthenticationResponse.class);

        return response;
    }
}

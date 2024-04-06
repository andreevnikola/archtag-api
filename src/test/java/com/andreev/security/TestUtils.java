package com.andreev.security;

import com.andreev.security.dto.authentication.RegisterRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;

@Controller
@RequiredArgsConstructor
public class TestUtils {

    private final ObjectMapper objectMapper;

    public RegisterRequest getRegisterRequest() {
        return RegisterRequest.builder()
                .email("nikolamihailovski@gmail.com")
                .password("nikola_mihailovski")
                .firstname("Nikola")
                .lastname("Mihailovski")
                .build();
    }

    public String getRegisterRequestAsJson() throws JsonProcessingException {
        return objectMapper.writeValueAsString(getRegisterRequest());
    }
}

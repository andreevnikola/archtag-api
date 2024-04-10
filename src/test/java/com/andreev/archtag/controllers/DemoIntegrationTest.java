package com.andreev.archtag.controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class DemoIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    public DemoIntegrationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @WithUserDetails("integrationtest@google.com")
    public void testThatProtectedDemoRouteReturnsHttp200ToAuthenticatedUsers() throws Exception {
        mockMvc.perform(get("/api/demo"))
                .andExpect(status().isOk());
    }

    @Test
    public void testThatProtectedDemoRouteReturnsHttp403ToNotAuthenticatedUsers() throws Exception {
        mockMvc.perform(get("/api/demo"))
                .andExpect(status().isForbidden());
    }
}

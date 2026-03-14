package com.lsn.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lsn.api.domain.Post;
import com.lsn.api.repository.SocialNetworkRepository;
import com.lsn.server.dto.FollowRequest;
import com.lsn.server.service.SocialNetworkService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FollowController.class)
class FollowControllerTest {

    @MockBean
    private Clock clock;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SocialNetworkService service;

    @MockBean
    private SocialNetworkRepository repository;


    @Test
    void follow_validRequest_returns201() throws Exception {

        mockMvc.perform(post("/users/alice/following")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new FollowRequest("bob"))))
                .andExpect(status().isCreated());
    }

    @Test
    void follow_blankTarget_returns400() throws Exception {

        mockMvc.perform(post("/users/alice/following")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new FollowRequest(""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void wall_noPostsReturnsEmptyList() throws Exception {

        when(repository.findWallByUsername("alice")).thenReturn(List.of());

        mockMvc.perform(get("/users/alice/wall"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void wall_returnsOwnAndFollowedPostsNewestFirst() throws Exception {

        Instant newer = Instant.parse("2024-01-01T11:00:00Z");
        Instant older = Instant.parse("2024-01-01T10:00:00Z");

        when(repository.findWallByUsername("alice")).thenReturn(List.of(
                new Post("alice", "alice post", newer),
                new Post("bob", "bob post", older)
        ));

        mockMvc.perform(get("/users/alice/wall"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("alice"))
                .andExpect(jsonPath("$[0].content").value("alice post"))
                .andExpect(jsonPath("$[0].postedAt").value("2024-01-01T11:00:00Z"))
                .andExpect(jsonPath("$[1].username").value("bob"))
                .andExpect(jsonPath("$[1].content").value("bob post"));
    }
}

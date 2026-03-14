package com.lsn.server.controller;

import com.lsn.api.repository.SocialNetworkRepository;
import com.lsn.server.dto.PostRequest;
import com.lsn.server.dto.PostResponse;
import com.lsn.server.service.SocialNetworkService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PostController {

    private final SocialNetworkService service;
    private final SocialNetworkRepository repository;

    public PostController(SocialNetworkService service, SocialNetworkRepository repository) {
        this.service    = service;
        this.repository = repository;
    }

    @PostMapping("/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public void createPost(@AuthenticationPrincipal UserDetails principal,
                           @Valid @RequestBody PostRequest request) {
        service.post(principal.getUsername(), request.content());
    }

    @GetMapping("/users/{username}/posts")
    public List<PostResponse> getPosts(@PathVariable String username) {
        return repository.findTimelineByUsername(username)
                .stream()
                .map(post -> new PostResponse(post.content(), post.postedAt()))
                .toList();
    }
}

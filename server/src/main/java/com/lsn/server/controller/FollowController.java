package com.lsn.server.controller;

import com.lsn.api.repository.SocialNetworkRepository;
import com.lsn.server.dto.FollowRequest;
import com.lsn.server.dto.WallEntry;
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
public class FollowController {

    private final SocialNetworkService service;
    private final SocialNetworkRepository repository;

    public FollowController(SocialNetworkService service, SocialNetworkRepository repository) {
        this.service    = service;
        this.repository = repository;
    }

    @PostMapping("/following")
    @ResponseStatus(HttpStatus.CREATED)
    public void follow(@AuthenticationPrincipal UserDetails principal, @Valid @RequestBody FollowRequest request) {
        service.follow(principal.getUsername(), request.target());
    }

    @GetMapping("/users/{username}/wall")
    public List<WallEntry> wall(@PathVariable String username) {
        return repository.findWallByUsername(username)
                .stream()
                .map(post -> new WallEntry(post.username(), post.content(), post.postedAt()))
                .toList();
    }
}

package com.lsn.server.dto;

import jakarta.validation.constraints.NotBlank;

public record FollowRequest(@NotBlank String target) {}

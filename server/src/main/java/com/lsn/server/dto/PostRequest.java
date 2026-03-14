package com.lsn.server.dto;

import jakarta.validation.constraints.NotBlank;

public record PostRequest(@NotBlank String content) {}

package com.lsn.server.dto;

import java.time.Instant;

public record PostResponse(String content, Instant postedAt) {}

package com.lsn.api.domain;

import java.time.Instant;

public record Post(String username, String content, Instant postedAt) {}

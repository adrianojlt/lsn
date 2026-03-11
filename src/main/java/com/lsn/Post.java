package com.lsn;

import java.time.Instant;

public record Post(String username, String content, Instant postedAt) {}

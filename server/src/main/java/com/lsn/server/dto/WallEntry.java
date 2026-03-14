package com.lsn.server.dto;

import java.time.Instant;

public record WallEntry(String username, String content, Instant postedAt) {}

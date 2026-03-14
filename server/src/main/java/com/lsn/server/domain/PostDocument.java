package com.lsn.server.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("posts")
@CompoundIndex(def = "{'username': 1, 'postedAt': -1}")
public class PostDocument {

    @Id
    private String id;

    private String username;
    private String content;
    private Instant postedAt;

    public PostDocument() {}

    public PostDocument(String username, String content, Instant postedAt) {
        this.username = username;
        this.content  = content;
        this.postedAt = postedAt;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getPostedAt() {
        return postedAt;
    }

    public void setPostedAt(Instant postedAt) {
        this.postedAt = postedAt;
    }
}

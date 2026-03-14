package com.lsn.server.repository;

import com.lsn.server.domain.PostDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PostMongoRepository extends MongoRepository<PostDocument, String> {
    List<PostDocument> findByUsernameOrderByPostedAtDesc(String username);
    List<PostDocument> findByUsernameInOrderByPostedAtDesc(List<String> usernames);
}

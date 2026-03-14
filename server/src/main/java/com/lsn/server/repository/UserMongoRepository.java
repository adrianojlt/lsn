package com.lsn.server.repository;

import com.lsn.server.domain.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserMongoRepository extends MongoRepository<UserDocument, String> {
    List<UserDocument> findByFollowingContaining(String username);
}

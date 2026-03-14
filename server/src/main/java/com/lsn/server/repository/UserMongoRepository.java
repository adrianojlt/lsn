package com.lsn.server.repository;

import com.lsn.server.domain.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserMongoRepository extends MongoRepository<UserDocument, String> {}

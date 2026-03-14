package com.lsn.server.repository;

import com.lsn.api.domain.Post;
import com.lsn.api.repository.SocialNetworkRepository;
import com.lsn.server.domain.PostDocument;
import com.lsn.server.domain.UserDocument;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class MongoSocialNetworkRepository implements SocialNetworkRepository {

    private final PostMongoRepository postRepo;
    private final UserMongoRepository userRepo;

    public MongoSocialNetworkRepository(PostMongoRepository postRepo, UserMongoRepository userRepo) {
        this.postRepo = postRepo;
        this.userRepo = userRepo;
    }

    @Override
    public void save(Post post) {
        postRepo.save(new PostDocument(post.username(), post.content(), post.postedAt()));
    }

    @Override
    public List<Post> findTimelineByUsername(String username) {
        return postRepo.findByUsernameOrderByPostedAtDesc(username)
                .stream()
                .map(this::toPost)
                .toList();
    }

    @Override
    public void follow(String follower, String followee) {

        UserDocument user = userRepo.findById(follower).orElseGet(() -> new UserDocument(follower));

        if (!user.getFollowing().contains(followee)) {
            user.getFollowing().add(followee);
            userRepo.save(user);
        }
    }

    @Override
    public List<Post> findWallByUsername(String username) {

        List<String> usernames = new ArrayList<>();
        usernames.add(username);

        userRepo.findById(username).ifPresent(user -> usernames.addAll(user.getFollowing()));

        return postRepo.findByUsernameInOrderByPostedAtDesc(usernames)
                .stream()
                .map(this::toPost)
                .toList();
    }

    private Post toPost(PostDocument doc) {
        return new Post(doc.getUsername(), doc.getContent(), doc.getPostedAt());
    }
}

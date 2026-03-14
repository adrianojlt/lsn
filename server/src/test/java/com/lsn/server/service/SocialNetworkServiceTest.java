package com.lsn.server.service;

import com.lsn.api.domain.Post;
import com.lsn.api.repository.SocialNetworkRepository;
import com.lsn.server.domain.UserDocument;
import com.lsn.server.event.EventPublisher;
import com.lsn.server.repository.UserMongoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SocialNetworkServiceTest {

    @Mock
    private SocialNetworkRepository repository;

    @Mock
    private UserMongoRepository userRepo;

    @Mock
    private EventPublisher eventPublisher;

    private SocialNetworkService service;

    private static final Instant FIXED_TIME = Instant.parse("2024-01-01T10:00:00Z");

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(FIXED_TIME, ZoneOffset.UTC);
        service = new SocialNetworkService(repository, userRepo, eventPublisher, clock);
    }

    @Test
    void post_savesPostAndPublishesToFollowers() {
        UserDocument bob = new UserDocument("bob");
        when(userRepo.findByFollowingContaining("alice")).thenReturn(List.of(bob));

        service.post("alice", "hello");

        ArgumentCaptor<Post> postCaptor = ArgumentCaptor.forClass(Post.class);
        verify(repository).save(postCaptor.capture());
        assertThat(postCaptor.getValue().username()).isEqualTo("alice");
        assertThat(postCaptor.getValue().content()).isEqualTo("hello");
        assertThat(postCaptor.getValue().postedAt()).isEqualTo(FIXED_TIME);

        verify(eventPublisher).publishPost(postCaptor.getValue(), List.of("bob"));
    }

    @Test
    void follow_callsRepositoryAndPublishesEvent() {
        service.follow("alice", "bob");

        verify(repository).follow("alice", "bob");
        verify(eventPublisher).publishFollow("alice", "bob");
    }
}

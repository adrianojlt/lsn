package com.lsn.client.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.lang.NonNull;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;

public class FeedListener {

    private static final String FEED_SUBSCRIPTION = "/user/queue/feed";

    private final String token;
    private final String serverUrl;

    private WebSocketStompClient stompClient;
    private volatile StompSession activeSession;

    public FeedListener(String serverUrl, String token) {
        this.token = token;
        this.serverUrl = serverUrl;
    }

    public void start() {

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);

        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(converter);

        String wsUrl = serverUrl.replaceFirst("^http", "ws") + "/ws/websocket";

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "Bearer " + token);

        stompClient.connectAsync(wsUrl, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {

            @Override
            public void afterConnected(@NonNull StompSession session,@NonNull StompHeaders connectedHeaders) {
                activeSession = session;
                session.subscribe(FEED_SUBSCRIPTION, new StompFrameHandler() {

                    @Override
                    public @NonNull Type getPayloadType(@NonNull StompHeaders headers) {
                        return FeedEvent.class;
                    }

                    @Override
                    public void handleFrame(@NonNull StompHeaders headers, Object payload) {
                        System.out.println(payload);
                    }
                });
            }

            @Override
            public void handleTransportError(@NonNull StompSession session,@NonNull Throwable exception) {
                System.err.println("[feed] connection error: " + exception.getMessage());
            }
        });
    }

    public void disconnect() {

        if (activeSession != null && activeSession.isConnected()) {
            activeSession.disconnect();
        }

        if (stompClient != null) {
            stompClient.stop();
        }
    }
}

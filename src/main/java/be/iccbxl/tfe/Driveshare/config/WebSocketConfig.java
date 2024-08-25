package be.iccbxl.tfe.Driveshare.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // ConcurrentMap to keep track of connected users
    private final ConcurrentMap<String, Principal> connectedUsers = new ConcurrentHashMap<>();

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/chat-websocket")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

                // Handle CONNECT command
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    Principal user = accessor.getUser();
                    if (user != null) {
                        connectedUsers.put(user.getName(), user);
                        System.out.println("User connected: " + user.getName());
                        System.out.println("Total connected users: " + connectedUsers.size());
                    }
                }

                // Handle DISCONNECT command
                if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                    Principal user = accessor.getUser();
                    if (user != null) {
                        connectedUsers.remove(user.getName());
                        System.out.println("User disconnected: " + user.getName());
                        System.out.println("Total connected users: " + connectedUsers.size());
                    }
                }

                return message;
            }
        });
    }

    // Optional method to expose the number of connected users
    public int getConnectedUsersCount() {
        return connectedUsers.size();
    }

    public ConcurrentMap<String, Principal> getConnectedUsers() {
        return connectedUsers;
    }
}

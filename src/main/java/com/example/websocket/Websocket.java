package com.example.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration //zeby klasa zamienila sie w beana
@EnableWebSocket
public class Websocket extends BinaryWebSocketHandler implements WebSocketConfigurer {

    private Map<String, User> sessions = Collections.synchronizedMap(new HashMap<String, User>());

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(this, "/chat").setAllowedOrigins("*");
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        System.out.println("Wiadomość przychodząca: " + new String(message.getPayload().array()));

        User userSending = sessions.get(session.getId());
        String messageConverted = new String(message.getPayload().array());

        if (userSending.getNick().isEmpty()) {
            userSending.setNick(messageConverted);
            userSending.getSession().sendMessage(new BinaryMessage(("Twój nick to: " + messageConverted).getBytes()));
        } else {
            for (User user : sessions.values()) {
                user.getSession().sendMessage(new BinaryMessage((userSending.getNick() + ": " + messageConverted).getBytes()));
            }
        }

    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), new User(session));
        session.sendMessage(new BinaryMessage("Podaj swój nick".getBytes()));

        System.out.println("Nowy użytkownik zarejestrowany");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        System.out.println("Użytkownik wyrejestrowany");
    }
}
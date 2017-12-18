package com.example.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.util.*;

@Configuration //zeby klasa zamienila sie w beana
@EnableWebSocket
public class Websocket extends BinaryWebSocketHandler implements WebSocketConfigurer {

    private Map<String, User> sessions = Collections.synchronizedMap(new HashMap<String, User>());

    private List<String> badWords = Collections.synchronizedList(
            new ArrayList<>(Arrays.asList("dupa", "gówno")));

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(this, "/chat").setAllowedOrigins("*");
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        System.out.println("Wiadomość przychodząca: " + new String(message.getPayload().array()));

        User userSending = sessions.get(session.getId());
        String messageConverted = censure(new String(message.getPayload().array()));

        if (messageConverted.contains("/")){
            String[] command = messageConverted.split(" ");
            switch (command[0].substring(1, command[0].length())){
                case "addword": {
                    badWords.add(command[1]);
                    userSending.getSession().sendMessage(new BinaryMessage("Dodałeś nowe słowo".getBytes()));
                    break;
                }
                case "changenick": {
                    userSending.setNick(command[1]);
                    break;
                }
                case "kick": {
                    kickUser(command[1]);
                }
                default: {
                    userSending.getSession().sendMessage(new BinaryMessage("Brak polecenia".getBytes()));
                }
            }
            return;
        }

        if (userSending.getNick().isEmpty()) {
            userSending.setNick(messageConverted);
            userSending.getSession().sendMessage(new BinaryMessage(("Twój nick to: " + messageConverted).getBytes()));
        } else {
            for (User user : sessions.values()) {
                user.getSession().sendMessage(new BinaryMessage((userSending.getNick() + ": " + messageConverted).getBytes()));
            }
        }
    }

    private void kickUser(String nick){
        for (User user : sessions.values()){
            if (user.getNick().equals(nick)){
                try {
                    user.getSession().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String censure(String message){
        String changedMessage = message;
        for (String word : badWords){
            if (message.contains(word)){
                changedMessage = "Jestem głupi";
            }
        }
        return changedMessage;
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
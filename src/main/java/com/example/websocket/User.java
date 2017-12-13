package com.example.websocket;

import org.springframework.web.socket.WebSocketSession;

public class User {

    private WebSocketSession session;
    private String nick;

    public User(WebSocketSession session) {
        this.session = session;
        this.nick = "";
    }

    public WebSocketSession getSession() {

        return session;
    }

    public void setSession(WebSocketSession session) {
        this.session = session;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }
}

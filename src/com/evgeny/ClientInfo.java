package com.evgeny;

import org.java_websocket.WebSocket;

import java.util.Objects;

public class ClientInfo {
    private WebSocket webSocket;
    private String login;
    private String password;

    public ClientInfo(WebSocket webSocket) {
        this.webSocket = webSocket;
    }

    public ClientInfo(WebSocket webSocket, String login) {
        this.webSocket = webSocket;
        this.login = login;
    }

    public ClientInfo(WebSocket webSocket, String login, String password) {
        this.webSocket = webSocket;
        this.login = login;
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientInfo)) return false;
        ClientInfo that = (ClientInfo) o;
        return webSocket.equals(that.webSocket);
    }

    @Override
    public int hashCode() {
        return Objects.hash(webSocket);
    }

    public WebSocket getWebSocket() {
        return webSocket;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

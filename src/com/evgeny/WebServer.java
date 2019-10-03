package com.evgeny;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.logging.Level;

public class WebServer extends WebSocketServer {
    private static int TCP_PORT = 4444;
    private static ArrayList<ClientInfo> clients;
    public WebServer() {
        super(new InetSocketAddress(TCP_PORT));
        clients = new ArrayList<>();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        clients.add(new ClientInfo(conn));
        AppLogger.LOGGER.log(Level.FINE,"new connection from "+ conn.getRemoteSocketAddress().getAddress().getHostName());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        clients.remove(new ClientInfo(conn));
        AppLogger.LOGGER.log(Level.FINE,"someone close connection");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        MessageWorker messageWorker = new MessageWorker(conn,message);
        Thread thread = new Thread(messageWorker);
        thread.start();
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

    @Override
    public void onStart() {

    }

    public static ArrayList<ClientInfo> getClients() {
        return clients;
    }

    public static void updateLogAndPas(WebSocket webSocket, String login, String password){
        for (int i = 0; i < clients.size(); i++) {
            ClientInfo clientInfo = clients.get(i);
            if (clientInfo.getWebSocket().equals(webSocket)){
                clientInfo.setLogin(login);
                clientInfo.setPassword(password);
            }
        }
    }
}

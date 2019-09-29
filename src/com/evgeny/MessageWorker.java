package com.evgeny;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.java_websocket.WebSocket;
import transfers.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.logging.Level;

//TODO добавить метод checkAuthorization и возврат всегда возвращает объект

public class MessageWorker implements Runnable, TypeRequestAnswer {
    private WebSocket webSocket;
    private String RecivedMessage;

    public MessageWorker(WebSocket webSocket, String RecivedMessage) {
        this.webSocket = webSocket;
        this.RecivedMessage = RecivedMessage;
    }

    @Override
    public void run() {
        String type="";
        if (RecivedMessage ==null) return;
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node;
        try{
            node = new ObjectMapper().readValue(RecivedMessage,ObjectNode.class);
            if (node.has("type")){
                type = node.get("type").asText();
            }
        }catch (Exception e){
            AppLogger.LOGGER.log(Level.FINE,"can't create node",e);
            return;
        }
        if (type.equals(".TransferRequestAnswer")){
            TransferRequestAnswer transfer;
            try {
                transfer =(TransferRequestAnswer)objectMapper.readValue(RecivedMessage,TransferRequestAnswer.class);
            } catch (IOException e) {
                AppLogger.LOGGER.log(Level.FINE,"can't deserialize message",e);
                return;
            }
            if (transfer.request.equals(AUTHORIZATION)){
                authorization(transfer);
            }else if (transfer.request.equals(REGISTRATION)){
                registration(transfer);
            }else if (transfer.request.equals(GET_FRIENDS)){
                getFriends(transfer);
            }else if (transfer.request.equals(ADD_FRIEND)){
                addFriend(transfer);
            }else if (transfer.request.equals(GET_REQUEST_IN)){
                getRequestIn(transfer);
            }else if (transfer.request.equals(GET_MESSAGES)){
                getMessages(transfer);
            }
        }else if (type.equals(".Message")){
            Message message;
            try {
                message = (Message)objectMapper.readValue(RecivedMessage,Message.class);
                sendMessage(message);
            } catch (IOException e) {
                AppLogger.LOGGER.log(Level.FINE,"can't deserialize message",e);
                return;
            }
        }
    }

    private void authorization(TransferRequestAnswer transfer){
        String answer = "";
        if (!ChatDBWorker.userIsExist(transfer.login)){
            answer = USER_NOT_EXIST;
        }else{
            if (ChatDBWorker.checkLogAndPass(transfer.login, transfer.password)){
                WebServer.updateLogAndPas(webSocket,transfer.login,transfer.password);
                answer = AUTHORIZATION_DONE;
            }else {
                answer = WRONG_PASSWORD;
            }
        }
        TransferRequestAnswer out = new TransferRequestAnswer(answer);
        ObjectMapper objectMapper = new ObjectMapper();
        StringWriter stringWriter = new StringWriter();
        try {
            objectMapper.writeValue(stringWriter,out);
        } catch (IOException e) {
            AppLogger.LOGGER.log(Level.FINE,"can't serialize message",e);
            return;
        }
        webSocket.send(stringWriter.toString());
    }

    private void registration(TransferRequestAnswer transfer){
        String strAnswer = ERROR;
        if (!checkLogin(transfer.login)){
            strAnswer = BAD_LOGIN;
        }else if (!checkPassword(transfer.password)){
            strAnswer = BAD_PASSWORD;
        }else {
            int status = -1;
            User user = new User(transfer.login,transfer.password);
            status = ChatDBWorker.registerUser(user);
            if (status==1){
                WebServer.updateLogAndPas(webSocket,transfer.login,transfer.password);
                strAnswer = REGISTRATION_DONE;
            }else if (status==0){
                strAnswer = USER_ALREADY_EXIST;
            }else {
                strAnswer = ERROR;
            }
        }
        TransferRequestAnswer out = new TransferRequestAnswer(strAnswer);
        ObjectMapper objectMapper = new ObjectMapper();
        StringWriter stringWriter = new StringWriter();
        try {
            objectMapper.writeValue(stringWriter,out);
        } catch (IOException e) {
            AppLogger.LOGGER.log(Level.FINE,"can't serialize message",e);
            return;
        }
        webSocket.send(stringWriter.toString());
    }

    private void getFriends(TransferRequestAnswer transfer){
        String strAnswer = ERROR;
        ArrayList<User> friendUser;
        if (ChatDBWorker.checkLogAndPass(transfer.login, transfer.password)){
            friendUser = ChatDBWorker.getUserFriend(new User(transfer.login, transfer.password));
            Friends friends = new Friends(friendUser);
            ObjectMapper objectMapper = new ObjectMapper();
            StringWriter stringWriter = new StringWriter();
            try{
                objectMapper.writeValue(stringWriter,friends);
                strAnswer = stringWriter.toString();
            }catch (IOException e){
                AppLogger.LOGGER.log(Level.FINE,"can't serialize friends",e);
                return;
            }
            webSocket.send(strAnswer);
        }else {
            TransferRequestAnswer out = new TransferRequestAnswer(AUTHORIZATION_FAILURE);
            ObjectMapper objectMapper = new ObjectMapper();
            StringWriter stringWriter = new StringWriter();
            try {
                objectMapper.writeValue(stringWriter,out);
                webSocket.send(strAnswer);
            }catch (IOException e){
                AppLogger.LOGGER.log(Level.FINE,"can't serialize TransferRequestAnswer",e);
                return;
            }
        }
    }

    private void addFriend(TransferRequestAnswer transfer){
        String strAnswer = ERROR;
        if (ChatDBWorker.checkLogAndPass(transfer.login, transfer.password)){
            int result = ChatDBWorker.addFriend(new User(transfer.login,transfer.password), new User(transfer.extra));
            if (result>0){
                strAnswer = REQUEST_SENT;
            }else strAnswer = ERROR;
            try {
                String jsonStr = objToJson(new TransferRequestAnswer(strAnswer));
                webSocket.send(jsonStr);
            } catch (IOException e) {
                AppLogger.LOGGER.log(Level.FINE,"can't serialize json",e);
            }
        }else {
            TransferRequestAnswer out = new TransferRequestAnswer(AUTHORIZATION_FAILURE);
            ObjectMapper objectMapper = new ObjectMapper();
            StringWriter stringWriter = new StringWriter();
            try {
                objectMapper.writeValue(stringWriter,out);
                webSocket.send(stringWriter.toString());
            }catch (IOException e){
                AppLogger.LOGGER.log(Level.FINE,"can't serialize TransferRequestAnswer",e);
                return;
            }
        }

    }

    private void getRequestIn(TransferRequestAnswer transfer){
        if (ChatDBWorker.checkLogAndPass(transfer.login, transfer.password)){
            RequestIn requestIn = ChatDBWorker.requestIn(new User(transfer.login,transfer.password));
            try {
                webSocket.send(objToJson(requestIn));
            } catch (IOException e) {
                AppLogger.LOGGER.log(Level.FINE,"can't serialize RequestIn",e);
                return;
            }
        }else {
            TransferRequestAnswer out = new TransferRequestAnswer(AUTHORIZATION_FAILURE);
            try {
                webSocket.send(objToJson(out));
            } catch (IOException e) {
                AppLogger.LOGGER.log(Level.FINE,"can't serialize TransferRequestAnswer",e);
                return;
            }
        }
    }

    private void sendMessage(Message message){
        if (ChatDBWorker.checkLogAndPass(message.login, message.password)){
            ChatDBWorker.sendMessage(message);
            TransferRequestAnswer out = new TransferRequestAnswer(NEW_MESSAGE);
            String answer = ERROR;
            try {
                answer = objToJson(out);
            } catch (IOException e) {
                AppLogger.LOGGER.log(Level.FINE,"can't convert to json",e);
                return;
            }
            User friend = ChatDBWorker.getUserById(message.id_to);
            ArrayList<ClientInfo> clients = WebServer.getClients();
            for (int i = 0; i < clients.size(); i++) {
                if (clients.get(i).getLogin().equals(message.login) || clients.get(i).getLogin().equals(friend.login)){
                    clients.get(i).getWebSocket().send(answer);
                }
            }
        }else {
            TransferRequestAnswer out = new TransferRequestAnswer(AUTHORIZATION_FAILURE);
            try {
                webSocket.send(objToJson(out));
            } catch (IOException e) {
                AppLogger.LOGGER.log(Level.FINE,"can't serialize TransferRequestAnswer",e);
                return;
            }
        }
    }

    private void getMessages(TransferRequestAnswer transfer){
        if (ChatDBWorker.checkLogAndPass(transfer.login, transfer.password)){
            int friend_id = 0;
            int count = 0;
            String []arr = transfer.extra.split(" ");
            try {
                friend_id = Integer.parseInt(arr[0]);
                count = Integer.parseInt(arr[1]);
            }catch (Exception e){
                AppLogger.LOGGER.log(Level.FINE,"can't parse string to int",e);
                return;
            }
            ArrayList<Message> messages = ChatDBWorker.getMessages(new User(transfer.login,transfer.password),friend_id,count);
            Messages arrMessages = new Messages(messages);
            try {
                webSocket.send(objToJson(arrMessages));
            } catch (IOException e) {
                AppLogger.LOGGER.log(Level.FINE,"can't serialize TransferRequestAnswer",e);
                return;
            }
        }else {
            TransferRequestAnswer out = new TransferRequestAnswer(AUTHORIZATION_FAILURE);
            try {
                webSocket.send(objToJson(out));
            } catch (IOException e) {
                AppLogger.LOGGER.log(Level.FINE,"can't serialize TransferRequestAnswer",e);
                return;
            }
        }
    }

    public boolean checkLogin(String login){
        if (login.length()>30) return false;
        return login.matches("\\w+");
    }

    public boolean checkPassword(String password){
        if (password.length()<6 || password.length()>255){
            return false;
        }else return true;
    }

    public String objToJson(Object c) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        StringWriter stringWriter = new StringWriter();
        objectMapper.writeValue(stringWriter,c);
        return stringWriter.toString();
    }
}

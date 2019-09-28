package com.evgeny;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.java_websocket.WebSocket;
import transfers.Friends;
import transfers.TransferRequestAnswer;
import transfers.TypeRequestAnswer;
import transfers.User;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.logging.Level;

//TODO добавить метод objToJson и checkAuthorization

public class MessageWorker implements Runnable, TypeRequestAnswer {
    private WebSocket webSocket;
    private String message;

    public MessageWorker(WebSocket webSocket, String message) {
        this.webSocket = webSocket;
        this.message = message;
    }

    @Override
    public void run() {
        String type="";
        if (message==null) return;
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode node;
        try{
            node = new ObjectMapper().readValue(message,ObjectNode.class);
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
                transfer =(TransferRequestAnswer)objectMapper.readValue(message,TransferRequestAnswer.class);
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

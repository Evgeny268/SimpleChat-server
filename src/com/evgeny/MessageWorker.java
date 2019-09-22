package com.evgeny;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.java_websocket.WebSocket;
import transfers.TransferRequestAnswer;
import transfers.TypeRequestAnswer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;


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
            }
        }
    }

    private void authorization(TransferRequestAnswer transfer){
        String answer = "";
        if (!ChatDBWorker.userIsExist(transfer.login)){
            answer = USER_NOT_EXIST;
        }else{
            if (ChatDBWorker.checkLogAndPass(transfer.login, transfer.password)){
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

    public boolean checkLogin(String login){
        if (login.length()>30) return false;
        return login.matches("\\w+");
    }

    public boolean checkPassword(String password){
        if (password.length()<6 || password.length()>255){
            return false;
        }else return true;
    }
}

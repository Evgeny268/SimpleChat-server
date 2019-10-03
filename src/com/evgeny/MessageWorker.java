package com.evgeny;

import DBUtils.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.java_websocket.WebSocket;
import transfers.*;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;


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
            } else {
                sendError();
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
        }else{
            sendError();
        }
    }

    private void authorization(TransferRequestAnswer transfer){
        String answer = ERROR;
        try {
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
        } catch (Exception e) {
            sendError();
        }
        TransferRequestAnswer out = new TransferRequestAnswer(answer);
        try {
            webSocket.send(objToJson(out));
        } catch (IOException e) {
            AppLogger.LOGGER.log(Level.FINE,"can't serialize message",e);
            sendError();
        }
    }

    private void registration(TransferRequestAnswer transfer){
        String strAnswer = ERROR;
        if (!checkLogin(transfer.login)){
            strAnswer = BAD_LOGIN;
        }else if (!checkPassword(transfer.password)){
            strAnswer = BAD_PASSWORD;
        }else {
            User user = new User(transfer.login,transfer.password);
            try {
                ChatDBWorker.registerUser(user);
                strAnswer = REGISTRATION_DONE;
            } catch (SQLException e) {
                AppLogger.LOGGER.log(Level.FINE,"can't register user",e);
                strAnswer = ERROR;
            } catch (UserAlreadyExistException e) {
                strAnswer = USER_ALREADY_EXIST;
            }
        }
        TransferRequestAnswer out = new TransferRequestAnswer(strAnswer);
        try {
            webSocket.send(objToJson(out));
        } catch (IOException e) {
            AppLogger.LOGGER.log(Level.WARNING,"can't serialize message",e);
            sendError();
        }
    }

    private void getFriends(TransferRequestAnswer transfer){
        try {
            if (ChatDBWorker.checkLogAndPass(transfer.login, transfer.password)){
                ArrayList<User> friendUser = ChatDBWorker.getUserFriend(new User(transfer.login, transfer.password));
                Friends friends = new Friends(friendUser);
                ObjectMapper objectMapper = new ObjectMapper();
                StringWriter stringWriter = new StringWriter();
                try{
                    objectMapper.writeValue(stringWriter,friends);
                    String strAnswer = stringWriter.toString();
                    webSocket.send(strAnswer);
                }catch (IOException e){
                    AppLogger.LOGGER.log(Level.WARNING,"can't serialize friends",e);
                    sendError();
                }
            }else {
                TransferRequestAnswer out = new TransferRequestAnswer(AUTHORIZATION_FAILURE);
                try {
                    webSocket.send(objToJson(out));
                } catch (IOException e) {
                    AppLogger.LOGGER.log(Level.WARNING,"can't serialize data",e);
                    sendError();
                }
            }
        } catch (SQLException e) {
            AppLogger.LOGGER.log(Level.WARNING,"get friend DB error",e);
            sendError();
        }
    }

    private void addFriend(TransferRequestAnswer transfer){
        TransferRequestAnswer out = new TransferRequestAnswer();
        try {
            if (ChatDBWorker.checkLogAndPass(transfer.login, transfer.password)){
                try {
                    ChatDBWorker.addFriend(new User(transfer.login,transfer.password), new User(transfer.extra));
                    out = new TransferRequestAnswer(REQUEST_SENT);
                } catch (AccessRightsException e) {
                    out = new TransferRequestAnswer(AUTHORIZATION_FAILURE);
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (UserNotFoundException e) {
                    out = new TransferRequestAnswer(USER_NOT_EXIST);
                }
            }else {
                out = new TransferRequestAnswer(AUTHORIZATION_FAILURE);
            }
        } catch (SQLException e) {
            out = new TransferRequestAnswer(ERROR);
        }

        try {
            webSocket.send(objToJson(out));
        } catch (IOException e) {
            AppLogger.LOGGER.log(Level.WARNING,"can't serialize TransferRequestAnswer",e);
            sendError();
        }

    }

    private void getRequestIn(TransferRequestAnswer transfer){
        try {
            if (ChatDBWorker.checkLogAndPass(transfer.login, transfer.password)){
                try {
                    RequestIn requestIn = ChatDBWorker.requestIn(new User(transfer.login,transfer.password));
                    webSocket.send(objToJson(requestIn));
                } catch (AccessRightsException e) {
                    TransferRequestAnswer out = new TransferRequestAnswer(AUTHORIZATION_FAILURE);
                    try {
                        webSocket.send(objToJson(out));
                    } catch (IOException ex) {
                        AppLogger.LOGGER.log(Level.WARNING,"can't serialize out",ex);
                        sendError();
                    }
                } catch (SQLException e) {
                    AppLogger.LOGGER.log(Level.WARNING,"db error in requestIn",e);
                    sendError();
                } catch (IOException e) {
                    AppLogger.LOGGER.log(Level.WARNING,"can't serialize RequestIn",e);
                    sendError();
                }
            }else {
                TransferRequestAnswer out = new TransferRequestAnswer(AUTHORIZATION_FAILURE);
                try {
                    webSocket.send(objToJson(out));
                } catch (IOException e) {
                    AppLogger.LOGGER.log(Level.WARNING,"can't serialize TransferRequestAnswer",e);
                    sendError();
                }
            }
        } catch (SQLException e) {
            AppLogger.LOGGER.log(Level.WARNING,"db error",e);
            sendError();
        }
    }

    private void sendMessage(Message message){
        try {
            if (ChatDBWorker.checkLogAndPass(message.login, message.password)){
                try {
                    ChatDBWorker.sendMessage(message);
                    try {
                        webSocket.send(objToJson(new TransferRequestAnswer(NEW_MESSAGE)));
                    } catch (IOException e) {
                        AppLogger.LOGGER.log(Level.WARNING,"can't convert to json",e);
                        sendError();
                    }
                } catch (AccessRightsException e) {
                    try {
                        webSocket.send(objToJson(new TransferRequestAnswer(AUTHORIZATION_FAILURE)));
                    } catch (IOException ex) {
                        AppLogger.LOGGER.log(Level.WARNING,"can't convert to json",ex);
                        sendError();
                    }
                    return;
                } catch (SQLException e) {
                    AppLogger.LOGGER.log(Level.WARNING,"db error",e);
                    sendError();
                    return;
                } catch (UserNotFriendException e) {
                    AppLogger.LOGGER.log(Level.SEVERE,"someone is trying to send a message to a not friend!",e);
                    sendError();
                    return;
                }
                User friend = null;
                try {
                    friend = ChatDBWorker.getUserById(message.id_to);
                } catch (SQLException e) {
                    AppLogger.LOGGER.log(Level.SEVERE,"can't get friend to notify",e);
                    return;
                }
                ArrayList<ClientInfo> clients = WebServer.getClients();
                for (int i = 0; i < clients.size(); i++) {
                    if (clients.get(i).getLogin().equals(message.login) || clients.get(i).getLogin().equals(friend.login)){
                        try {
                            clients.get(i).getWebSocket().send(objToJson(new TransferRequestAnswer(NEW_MESSAGE)));
                        } catch (IOException e) {
                            AppLogger.LOGGER.log(Level.WARNING,"can't convert to json",e);
                        }
                    }
                }
            }else {
                TransferRequestAnswer out = new TransferRequestAnswer(AUTHORIZATION_FAILURE);
                try {
                    webSocket.send(objToJson(out));
                } catch (IOException e) {
                    AppLogger.LOGGER.log(Level.WARNING,"can't serialize TransferRequestAnswer",e);
                    return;
                }
            }
        } catch (SQLException e) {
            AppLogger.LOGGER.log(Level.WARNING,"can't convert to json",e);
            sendError();
        }
    }

    private void getMessages(TransferRequestAnswer transfer){
        try {
            if (ChatDBWorker.checkLogAndPass(transfer.login, transfer.password)){
                int friend_id = 0;
                int count = 0;
                String []arr = transfer.extra.split(" ");
                try {
                    friend_id = Integer.parseInt(arr[0]);
                    count = Integer.parseInt(arr[1]);
                }catch (Exception e){
                    AppLogger.LOGGER.log(Level.WARNING,"can't parse string to int",e);
                    return;
                }
                ArrayList<Message> messages = null;
                try {
                    messages = ChatDBWorker.getMessages(new User(transfer.login,transfer.password),friend_id,count);
                } catch (AccessRightsException e) {
                    try {
                        webSocket.send(objToJson(new TransferRequestAnswer(AUTHORIZATION_FAILURE)));
                    } catch (IOException ex) {
                        AppLogger.LOGGER.log(Level.WARNING,"can't serialize",e);
                        sendError();
                    }
                    return;
                } catch (SQLException e) {
                    AppLogger.LOGGER.log(Level.WARNING,"db error",e);
                    sendError();
                    return;
                } catch (UserNotFriendException e) {
                    AppLogger.LOGGER.log(Level.SEVERE,"someone is trying to send a message to a not friend!",e);
                    sendError();
                    return;
                }
                Messages arrMessages = new Messages(messages);
                try {
                    webSocket.send(objToJson(arrMessages));
                } catch (IOException e) {
                    AppLogger.LOGGER.log(Level.WARNING,"can't serialize TransferRequestAnswer",e);
                    sendError();
                }
            }else {
                TransferRequestAnswer out = new TransferRequestAnswer(AUTHORIZATION_FAILURE);
                try {
                    webSocket.send(objToJson(out));
                } catch (IOException e) {
                    AppLogger.LOGGER.log(Level.FINE,"can't serialize TransferRequestAnswer",e);
                    sendError();
                }
            }
        } catch (SQLException e) {
            AppLogger.LOGGER.log(Level.WARNING,"db error",e);
            sendError();
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

    public boolean checkAuthorization(String login, String password) throws SQLException {
        if (ChatDBWorker.checkLogAndPass(login, password)){
            return true;
        }else {
            TransferRequestAnswer out = new TransferRequestAnswer(AUTHORIZATION_FAILURE);
            try {
                webSocket.send(objToJson(out));
            } catch (IOException e) {
                AppLogger.LOGGER.log(Level.WARNING,"can't serialize TransferRequestAnswer",e);
            }
            return false;
        }
    }

    public void sendError(){
        TransferRequestAnswer out = new TransferRequestAnswer(ERROR);
        ObjectMapper objectMapper = new ObjectMapper();
        StringWriter stringWriter = new StringWriter();
        try {
            objectMapper.writeValue(stringWriter,out);
            webSocket.send(stringWriter.toString());
        } catch (IOException e) {
            webSocket.send(ERROR);
        }
    }
}

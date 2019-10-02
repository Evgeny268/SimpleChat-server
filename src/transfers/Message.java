package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Date;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class Message {

    public int iduser = 0;
    public int idMessage;
    public String login;
    public String password;
    public int id_to;
    public Date date;
    public String text;

    public Message() {
    }

    public Message(String login, String password, int id_to, String text) {
        this.login = login;
        this.password = password;
        this.id_to = id_to;
        this.text = text;
    }

    public Message(int idMessage, int iduser, int id_to, Date date, String text) {
        this.idMessage = idMessage;
        this.iduser = iduser;
        this.id_to = id_to;
        this.date = date;
        this.text = text;
    }
}

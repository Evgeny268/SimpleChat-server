package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class TransferRequestAnswer {
    public String request;
    public String login;
    public String password;
    public String extra;

    public TransferRequestAnswer() {
    }

    public TransferRequestAnswer(String request) {
        this.request = request;
    }

    public TransferRequestAnswer(String request, String login, String password) {
        this.request = request;
        this.login = login;
        this.password = password;
    }

    public TransferRequestAnswer(String request, String login, String password, String extra) {
        this.request = request;
        this.login = login;
        this.password = password;
        this.extra = extra;
    }
}

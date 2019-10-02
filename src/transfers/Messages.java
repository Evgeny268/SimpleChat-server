package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class Messages {

    @JsonDeserialize(as = ArrayList.class, contentAs = Message.class)
    public ArrayList<Message> messages = new ArrayList<>();

    public Messages() {
    }

    public Messages(ArrayList<Message> messages) {
        this.messages = messages;
    }
}

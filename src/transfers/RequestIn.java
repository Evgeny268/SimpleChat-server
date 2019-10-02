package transfers;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class RequestIn {

    @JsonDeserialize(as = ArrayList.class, contentAs = User.class)
    public ArrayList<User> users = new ArrayList<>();

    public RequestIn() {
    }

    public RequestIn(ArrayList<User> users) {
        this.users = users;
    }
}

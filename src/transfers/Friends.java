package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class Friends {
    @JsonDeserialize(as = ArrayList.class, contentAs = User.class)
    public ArrayList<User> friends = new ArrayList<>();

    public Friends() {
    }

    public Friends(ArrayList<User> friends) {
        this.friends = friends;
    }
}

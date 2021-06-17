package utils.tuples;

import net.jini.core.entry.Entry;

import java.util.ArrayList;
import java.util.List;

public class UserTuple implements Entry {
    public String userID;
    public String password;
    public List<String> createdAuctions;

    public UserTuple() {}

    public UserTuple(String userID, String password, List<String> createdAuctions) {
        this.userID = userID;
        this.password = password;
        this.createdAuctions = createdAuctions;
    }
}

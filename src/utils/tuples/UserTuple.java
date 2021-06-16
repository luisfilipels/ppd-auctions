package utils.tuples;

import net.jini.core.entry.Entry;

import java.util.List;

public class UserTuple implements Entry {
    public String userID;
    public String password;
    public List<String> createdAuctions;
}

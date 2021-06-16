package utils.tuples;

import net.jini.core.entry.Entry;

import java.util.HashMap;

public class BatchTuple implements Entry {

    private class Pair {
        int value;
        boolean isPublic;

        Pair(int value, boolean isPublic) {
            this.value = value;
            this.isPublic = isPublic;
        }
    }

    public String id;
    public String description;
    public String sellerId;
    public HashMap<String, Pair> bids = new HashMap<>();

    public BatchTuple(){}

    public BatchTuple(String description, String sellerId) {
        this.description = description;
        this.sellerId = sellerId;
    }
}

package utils.tuples;

import net.jini.core.entry.Entry;

import java.util.HashMap;

public class BatchTuple implements Entry {

    public String id;
    public String description;
    public String sellerId;
    public HashMap<String, String> bids = new HashMap<>();    // Key: Creator, Value: "Value|isPublic"

    public BatchTuple(){}

    public BatchTuple(String id, String description, String sellerId) {
        this.id = id;
        this.description = description;
        this.sellerId = sellerId;
    }

    public void addBid(int value, boolean isPublic) {
        String bidData = value + "|" + isPublic;
        bids.put(sellerId, bidData);
    }
}

package utils.tuples;

import net.jini.core.entry.Entry;

import java.util.HashMap;
import java.util.HashSet;

public class AuctionTrackerTuple implements Entry {

    public HashMap<String, String> auctionMap; // Key: ID, Value: Creator (null for public)

    public AuctionTrackerTuple() {}

}

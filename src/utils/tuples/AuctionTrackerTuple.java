package utils.tuples;

import net.jini.core.entry.Entry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class AuctionTrackerTuple implements Entry {

    public List<String> auctionList; // Key: ID, Value: Creator (null for public)

    public AuctionTrackerTuple() {}

}

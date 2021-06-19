package networking;

import main.MainViewController;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace;
import utils.ClientDataSingleton;
import utils.exceptions.AcquireTupleException;
import utils.exceptions.JavaSpaceNotFoundException;
import utils.tuples.AuctionTrackerTuple;
import utils.tuples.BatchTuple;
import utils.tuples.UserTuple;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NetworkHandlerSingleton {

    private static NetworkHandlerSingleton instance;
    private JavaSpace javaSpace;

    private NetworkHandlerSingleton() { }

    public static NetworkHandlerSingleton getInstance() {
        if (instance == null) {
            instance = new NetworkHandlerSingleton();
        }
        return instance;
    }

    public JavaSpace getJavaSpace() {
        if (javaSpace != null) return javaSpace;
        Lookup finder = new Lookup(JavaSpace.class);
        return (JavaSpace) finder.getService();
    }

    public UserTuple readUser(UserTuple user, long timeout) throws AcquireTupleException {
        try {
            return (UserTuple) javaSpace.read(user, null, timeout);
        } catch (Exception e) {
            throw new AcquireTupleException();
        }
    }

    public UserTuple takeUser(UserTuple user, long timeout) throws  AcquireTupleException {
        try {
            return (UserTuple) javaSpace.take(user, null, timeout);
        } catch (Exception e) {
            throw new AcquireTupleException();
        }
    }

    private void _clearUserTuples() {
        UserTuple tuple = null;
        do {
            try {
                tuple = (UserTuple) javaSpace.take(new UserTuple(), null, 1000);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        } while (tuple != null);
    }

    public void writeAuctionTracker() throws TransactionException, RemoteException {
        javaSpace = getJavaSpace();
        AuctionTrackerTuple auctionTracker = new AuctionTrackerTuple();
        auctionTracker.auctionList = new ArrayList<>();
        javaSpace.write(auctionTracker, null, 60 * 60 * 1000);
    }

    public void writeAuctionTracker(AuctionTrackerTuple tuple) throws TransactionException, RemoteException {
        javaSpace = getJavaSpace();
        javaSpace.write(tuple, null, 60 * 60 * 1000);
    }

    public AuctionTrackerTuple takeAuctionTracker(long timeout) throws Exception{
        javaSpace = getJavaSpace();
        return (AuctionTrackerTuple) javaSpace.take(new AuctionTrackerTuple(), null, timeout);
    }

    public AuctionTrackerTuple readAuctionTracker(long timeout) throws Exception{
        javaSpace = getJavaSpace();
        return (AuctionTrackerTuple) javaSpace.read(new AuctionTrackerTuple(), null, timeout);
    }

    public boolean auctionTrackerExists() throws JavaSpaceNotFoundException{
        javaSpace = getJavaSpace();
        if (javaSpace == null) throw new JavaSpaceNotFoundException();

        AuctionTrackerTuple result = null;
        try {
            result = readAuctionTracker(5000);
        } catch (Exception e) {
            System.out.println("Couldn't check if auction tracker exists!");
            e.printStackTrace();
        }

        return result != null;
    }

    public void writeUserTuple(UserTuple user) {
        try {
            javaSpace.write(user, null, 1000 * 60 * 60);
        } catch (Exception e) {
            System.out.println("Could not save user tuple!");
            e.printStackTrace();
        }
    }

    public void writeAuction(BatchTuple tuple) throws TransactionException, RemoteException, JavaSpaceNotFoundException {
        javaSpace = getJavaSpace();
        if (javaSpace == null) {
            throw new JavaSpaceNotFoundException();
        }
        javaSpace.write(tuple, null, 60 * 60 * 1000);
    }

    public void writeAuction(String id, String description, String sellerID) throws JavaSpaceNotFoundException, TransactionException, RemoteException, AcquireTupleException {
        javaSpace = getJavaSpace();
        if (javaSpace == null) {
            throw new JavaSpaceNotFoundException();
        }

        try {
            AuctionTrackerTuple auctionTracker = takeAuctionTracker(5000);
            if (auctionTracker == null) {
                writeAuctionTracker();
                auctionTracker = takeAuctionTracker(5000);
                if (auctionTracker == null) {
                    System.out.println("Could not acquire auction tracker!");
                    throw new AcquireTupleException();
                }
            } else {
                if (auctionTracker.auctionList == null) {
                    auctionTracker.auctionList = new ArrayList<>();
                }
            }
            auctionTracker.auctionList.add(id);
            writeAuctionTracker(auctionTracker);


            ClientDataSingleton clientData = ClientDataSingleton.getInstance();
            UserTuple myUser = new UserTuple();
            myUser.userID = clientData.userName;

            myUser = takeUser(myUser, 5000);
            if (myUser == null) {
                System.out.println("Couldn't update user!");
                return;
            } else {
                System.out.println("Took user");
            }
            if (myUser.madeAuctions == null) {
                myUser.madeAuctions = new ArrayList<>();
            }
            myUser.madeAuctions.add(id);
            writeUserTuple(myUser);
            System.out.println("Wrote user");
        } catch (Exception e) {
            System.out.println("Couldn't write batch!");
            e.printStackTrace();
            return;
        }

        BatchTuple tuple = new BatchTuple(id, description, sellerID);
        javaSpace.write(tuple, null, 60 * 60 * 1000);
        System.out.println("Attempted to write batch");
    }

    public BatchTuple takeAuctionTuple(String id, long timeout) throws Exception {
        javaSpace = getJavaSpace();
        if (javaSpace == null) throw new JavaSpaceNotFoundException();

        BatchTuple template = new BatchTuple(id, null, null);
        template.bids = null;
        return (BatchTuple) javaSpace.take(template, null, timeout);
    }

    public BatchTuple readAuctionTuple(String auctionID) throws Exception{
        javaSpace = getJavaSpace();
        if (javaSpace == null) throw new JavaSpaceNotFoundException();

        BatchTuple template = new BatchTuple(auctionID, null, null);
        template.bids = null;
        return (BatchTuple) javaSpace.read(template, null, 60 * 60 * 1000);
    }

    public void loginUser(String userName)
            throws JavaSpaceNotFoundException, AcquireTupleException {
        javaSpace = getJavaSpace();
        if (javaSpace == null) {
            throw new JavaSpaceNotFoundException();
        }

        UserTuple userEntering = new UserTuple();
        userEntering.userID = userName;

        UserTuple userEntered = readUser(userEntering, 6000);

        if (userEntered != null) {
            // User exists
            System.out.println("Login ok");
            ClientDataSingleton.getInstance().saveUserDataFromTuple(userEntered);
        } else {
            // User does not exist, so create and proceed
            userEntering.madeAuctions = new ArrayList<>();
            writeUserTuple(userEntering);
            UserTuple testTuple = readUser(userEntering, 6000);
            if (testTuple != null) System.out.println("Creating user");
            ClientDataSingleton.getInstance().saveUserDataFromTuple(userEntering);
        }
        //clearUserTuples();
    }

    private void removeStringFromList(List<String> list, String str) {
        int indexToRemove = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(str)) {
                indexToRemove = i;
                break;
            }
        }
        if (indexToRemove != -1) list.remove(indexToRemove);
    }

    public void deleteAuctionWithID(String auctionID) throws AcquireTupleException {
        javaSpace = getJavaSpace();

        UserTuple myUser = new UserTuple();
        myUser.userID = ClientDataSingleton.getInstance().userName;

        myUser = takeUser(myUser, 6000);
        if (myUser == null) {
            System.out.println("Couldn't find user in tuple space!");
            throw new AcquireTupleException();
        }
        if (myUser.madeAuctions == null) {
            System.out.println("User's tuple didn't have a list!");
            writeUserTuple(myUser);
            return;
        }

        System.out.println("My list before: ");
        System.out.println(myUser.madeAuctions);
        removeStringFromList(myUser.madeAuctions, auctionID);
        System.out.println("My list after: ");
        System.out.println(myUser.madeAuctions);
        writeUserTuple(myUser);

        AuctionTrackerTuple auctionTracker;
        try {
            auctionTracker = takeAuctionTracker(6000);
            if (auctionTracker == null) {
                System.out.println("Couldn't get auction tracker!");
                throw new AcquireTupleException();
            }
            removeStringFromList(auctionTracker.auctionList, auctionID);
            writeAuctionTracker(auctionTracker);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            BatchTuple foundTuple = takeAuctionTuple(auctionID, 6000);

            if (foundTuple == null) {
                throw new AcquireTupleException();
            }
        } catch (Exception e) {
            System.out.println("Couldn't take tuple!");
            e.printStackTrace();
        }
    }


}

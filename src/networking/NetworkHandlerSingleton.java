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

public class NetworkHandlerSingleton {

    private MainViewController mainViewController;
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

    public UserTuple readUser(UserTuple user) throws AcquireTupleException {
        try {
            return (UserTuple) javaSpace.read(user, null, 1000);
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
        javaSpace.write(auctionTracker, null, 5 * 1000);
    }

    public AuctionTrackerTuple takeAuctionTracker() throws Exception{
        javaSpace = getJavaSpace();
        return (AuctionTrackerTuple) javaSpace.take(new AuctionTrackerTuple(), null, 5000);
    }

    private AuctionTrackerTuple readAuctionTracker() {
        javaSpace = getJavaSpace();
        try {
            return (AuctionTrackerTuple) javaSpace.read(new AuctionTrackerTuple(), null, 5000);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean auctionTrackerExists() throws JavaSpaceNotFoundException{
        javaSpace = getJavaSpace();
        if (javaSpace == null) throw new JavaSpaceNotFoundException();
        AuctionTrackerTuple result = readAuctionTracker();

        return result != null;
    }

    public void writeUserTuple(UserTuple user) {
        try {
            javaSpace.write(user, null, 1000 * 60);
        } catch (Exception e) {
            System.out.println("Could not save user tuple!");
            e.printStackTrace();
        }
    }

    public void writeBatch(String id, String description, String sellerID) throws JavaSpaceNotFoundException, TransactionException, RemoteException {
        javaSpace = getJavaSpace();
        if (javaSpace == null) {
            throw new JavaSpaceNotFoundException();
        }

        BatchTuple tuple = new BatchTuple(id, description, sellerID);
        javaSpace.write(tuple, null, 10000);
    }

    public void loginUser(String userName)
            throws JavaSpaceNotFoundException, AcquireTupleException {
        javaSpace = getJavaSpace();
        if (javaSpace == null) {
            throw new JavaSpaceNotFoundException();
        }

        UserTuple userEntering = new UserTuple();
        userEntering.userID = userName;

        UserTuple userEntered = readUser(userEntering);

        if (userEntered != null) {
            // User exists
            System.out.println("Login ok");
            ClientDataSingleton.getInstance().saveUserDataFromTuple(userEntered);
        } else {
            // User does not exist, so create and proceed
            userEntering.madeAuctions = new ArrayList<>();
            writeUserTuple(userEntering);
            UserTuple testTuple = readUser(userEntering);
            if (testTuple != null) System.out.println("Creating user");
            ClientDataSingleton.getInstance().saveUserDataFromTuple(userEntering);
        }
        //clearUserTuples();
    }

    public void setMainController(MainViewController mainViewController) {
        this.mainViewController = mainViewController;
    }

}

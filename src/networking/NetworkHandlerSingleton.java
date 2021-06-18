package networking;

import main.MainViewController;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace;
import utils.ClientDataSingleton;
import utils.exceptions.JavaSpaceNotFoundException;
import utils.exceptions.PasswordIncorrectException;
import utils.tuples.UserTuple;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class NetworkHandlerSingleton {

    private NetworkHandlerSingleton() { }

    private static NetworkHandlerSingleton instance;
    private JavaSpace javaSpace;

    public static NetworkHandlerSingleton getInstance() {
        if (instance == null) {
            instance = new NetworkHandlerSingleton();
        }
        return instance;
    }

    public JavaSpace getJavaSpace() {
        System.out.println(1);
        Lookup finder = new Lookup(JavaSpace.class);
        System.out.println(2);
        return (JavaSpace) finder.getService();
    }

    public UserTuple findUser(UserTuple user) {
        UserTuple userEntered = null;
        try {
            userEntered = (UserTuple) javaSpace.readIfExists(user, null, 1000);
        } catch (UnusableEntryException e) {
            e.printStackTrace();
        } catch (TransactionException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return userEntered;
    }

    private void clearUserTuples() {
        UserTuple tuple = null;
        do {
            try {
                tuple = (UserTuple) javaSpace.take(new UserTuple(null, null), null, 1000);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        } while (tuple != null);
    }

    public void writeUserTuple(UserTuple user) {
        try {
            javaSpace.write(user, null, 1000);
        } catch (Exception e) {
            System.out.println("Could not save user tuple!");
            e.printStackTrace();
        }
    }

    public void loginUser(String userName, String password)
            throws PasswordIncorrectException, JavaSpaceNotFoundException {
        if (javaSpace == null) {
            javaSpace = getJavaSpace();
        }
        if (javaSpace == null) {
            throw new JavaSpaceNotFoundException();
        }

        UserTuple userEntering = new UserTuple();
        userEntering.userID = userName;
        UserTuple userEntered = findUser(userEntering);

        if (findUser(new UserTuple()) == null) {
            System.out.println("Didn't read anything!!!");
        }
        // TODO: Find out why nothing is being returned!!!!!!

        if (userEntered != null) {
            // User exists
            System.out.println("Login ok");
            ClientDataSingleton.getInstance().saveUserDataFromTuple(userEntered);
        } else {
            // User does not exist, so create and proceed
            writeUserTuple(userEntering);
            UserTuple testTuple = findUser(userEntering);
            if (testTuple != null) System.out.println("Creating user");
            ClientDataSingleton.getInstance().saveUserDataFromTuple(userEntering);
        }
        //clearUserTuples();
    }

    public void initialize(MainViewController mainViewController) {

    }

}

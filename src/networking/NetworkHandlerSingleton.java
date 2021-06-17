package networking;

import main.MainViewController;
import net.jini.space.JavaSpace;
import utils.ClientDataSingleton;
import utils.exceptions.JavaSpaceNotFoundException;
import utils.exceptions.PasswordIncorrectException;
import utils.tuples.UserTuple;

import java.nio.file.attribute.UserPrincipalNotFoundException;

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
            userEntered = (UserTuple) javaSpace.take(user, null, 1);
        } catch (Exception e) {
            System.out.println("Problem when finding user!");
            e.printStackTrace();
            return null;
        }
        return userEntered;
    }

    public void writeUserTuple(UserTuple user) {
        try {
            javaSpace.write(user, null, 1);
        } catch (Exception e) {
            System.out.println("Could not save user tuple!");
            e.printStackTrace();
        }
    }

    public void loginUser(String userName, String password)
            throws PasswordIncorrectException, JavaSpaceNotFoundException {
        if (javaSpace == null) {
            javaSpace = getJavaSpace();
            System.out.println(3);
        }
        if (javaSpace == null) {
            throw new JavaSpaceNotFoundException();
        }

        UserTuple userEntering = new UserTuple(userName, password, null);
        UserTuple userEntered = findUser(userEntering);
        if (userEntered != null) {
            // User exists and password is correct
            ClientDataSingleton.getInstance().saveUserDataFromTuple(userEntered);
        } else {
            // User does not exist, or password is incorrect
            UserTuple wrongPasswordUser = new UserTuple(userName, null, null);
            userEntered = findUser(wrongPasswordUser);
            if (userEntered != null) {
                // User exists, so password must be incorrect
                throw new PasswordIncorrectException();
            } else {
                // User does not exist, so create and proceed
                writeUserTuple(userEntering);
                ClientDataSingleton.getInstance().saveUserDataFromTuple(userEntering);
            }
        }
    }

    public void initialize(MainViewController mainViewController) {

    }

}

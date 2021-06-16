package networking;

import main.MainViewController;
import net.jini.space.JavaSpace;

public class NetworkHandlerSingleton {

    private NetworkHandlerSingleton() { }

    private static NetworkHandlerSingleton instance;

    public static NetworkHandlerSingleton getInstance() {
        if (instance == null) {
            instance = new NetworkHandlerSingleton();
        }
        return instance;
    }

    public void initialize(MainViewController mainViewController) {

    }

}

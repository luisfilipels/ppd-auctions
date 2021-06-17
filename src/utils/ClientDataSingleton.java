package utils;

import utils.tuples.UserTuple;

import java.util.ArrayList;
import java.util.List;

public class ClientDataSingleton {

    private static ClientDataSingleton instance;
    private ClientDataSingleton() {
        createdAuctions = new ArrayList<>();
    }

    private List<String> createdAuctions;
    public String userName = "";
    public String password = "";
    private String brokerIP = "";

    public static ClientDataSingleton getInstance() {
        if (instance == null) {
            instance = new ClientDataSingleton();
        }
        return instance;
    }

    public void saveUserDataFromTuple(UserTuple user) {
        this.userName = user.userID;
        this.password = user.password;
    }

    public void setBrokerIP(String ip) {
        this.brokerIP = ip;
    }

    public String getBrokerIP(){
        return brokerIP;
    }

}

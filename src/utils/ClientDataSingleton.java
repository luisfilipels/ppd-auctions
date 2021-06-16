package utils;

import java.util.ArrayList;
import java.util.List;

public class ClientDataSingleton {

    private static ClientDataSingleton instance;
    private ClientDataSingleton() {
        createdAuctions = new ArrayList<>();
    }

    private List<String> createdAuctions;
    private String selfID = "";
    private String brokerIP = "";

    public static ClientDataSingleton getInstance() {
        if (instance == null) {
            instance = new ClientDataSingleton();
        }
        return instance;
    }

    public void setBrokerIP(String ip) {
        this.brokerIP = ip;
    }

    public String getBrokerIP(){
        return brokerIP;
    }

}

package main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.fxml.FXML;
import networking.NetworkHandlerSingleton;
import utils.ClientDataSingleton;

public class MainViewController {

    @FXML
    private ListView<String> logListView;
    private ObservableList<String> logList;

    @FXML
    private ListView<String> topicsListView;
    private ObservableList<String> topicsList;


    public void addLog(String message) {
        logList.add(message);
    }

    @FXML
    void initialize() {
        logList = FXCollections.observableArrayList();
        logListView.setItems(logList);

        topicsList = FXCollections.observableArrayList();
        topicsListView.setItems(topicsList);

        NetworkHandlerSingleton networkHandler = NetworkHandlerSingleton.getInstance();
        networkHandler.initialize(this);

        ClientDataSingleton clientData = ClientDataSingleton.getInstance();


    }



}

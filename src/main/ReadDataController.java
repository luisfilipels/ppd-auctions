package main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import networking.NetworkHandlerSingleton;
import utils.exceptions.JavaSpaceNotFoundException;
import utils.exceptions.PasswordIncorrectException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadDataController {

    @FXML
    private TextField brokerAddressField;

    @FXML
    private Text invalidIPText;

    @FXML
    private Text wrongPasswordText;

    @FXML
    private TextField userField;

    @FXML
    private TextField passwordField;

    @FXML
    private Text failedConnectionText;

    private static final String PATTERN =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    // Got this function from StackOverFlow
    public static boolean isValid(final String ip){
        Pattern pattern = Pattern.compile(PATTERN);
        Matcher matcher = pattern.matcher(ip);
        return matcher.matches();
    }

    private String getIp() {
        String ip = brokerAddressField.getText().trim();
        if (ip.trim().equals("")) {
            return "failover://tcp://localhost:61616";
        } else if(!isValid(ip)) {
            return "invalid";
        } else {
            return "failover://tcp://" + ip + ":61616";
        }
    }

    private void createConnectionAndRefreshTopicsWithIP(String ip) {

    }

    @FXML
    void onRefreshButton() {
        String ip = getIp();
        createConnectionAndRefreshTopicsWithIP(ip);
    }

    @FXML
    public void initialize() {
        invalidIPText.setOpacity(0);
        wrongPasswordText.setOpacity(0);
        failedConnectionText.setOpacity(0);
    }

    @FXML
    public void onConfirmButton(ActionEvent event) {

        String userName = userField.getText().trim();
        String password = passwordField.getText().trim();

        try {
            NetworkHandlerSingleton.getInstance().loginUser(userName, password);
        } catch (PasswordIncorrectException e) {
            System.out.println("Wrong password");
            wrongPasswordText.setOpacity(1);
        } catch (JavaSpaceNotFoundException e) {
            System.out.println("Javaspace not found");
            failedConnectionText.setOpacity(1);
        }
        wrongPasswordText.setOpacity(0);
        failedConnectionText.setOpacity(0);


        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();

        try {
            Stage primaryStage = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
            primaryStage.setTitle("Cliente");
            primaryStage.setScene(new Scene(root, 800, 500));
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

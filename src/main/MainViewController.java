package main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import net.jini.core.transaction.TransactionException;
import networking.NetworkHandlerSingleton;
import utils.ClientDataSingleton;
import utils.exceptions.JavaSpaceNotFoundException;
import utils.tuples.AuctionTrackerTuple;

import java.rmi.RemoteException;
import java.util.Map;

public class MainViewController {

    @FXML
    private ListView<HBox> auctionListView;
    private ObservableList<HBox> auctionList;

    @FXML
    private ListView<String> myAuctionsListView;
    private ObservableList<String> myAuctionsList;

    @FXML
    private TextField auctionIdField;

    @FXML
    private TextArea auctionDescriptionField;

    @FXML
    private Button createAuctionButton;


    @FXML
    void updateViewsButtonClick() {
        updateLists();
    }

    private void openDetailsForAuction(String auctionID) {
        // TODO: Complete this
        System.out.println("Working with auction " + auctionID);
    }

    public void addLog(String auctionId, String bidCount) {
        Text text = new Text();
        text.setText(auctionId + " (" + bidCount + " lances)");
        text.setTextOrigin(VPos.BOTTOM);

        Region spacer = new Region();

        Button button = new Button();
        button.setText("Visualizar");

        button.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                openDetailsForAuction(auctionId);
            }
        });

        HBox box = new HBox();
        box.setHgrow(spacer, Priority.ALWAYS);
        box.getChildren().addAll(text, spacer, button);
        auctionList.add(box);
    }

    @FXML
    void createAuctionButtonClick() {
        NetworkHandlerSingleton networkHandler = NetworkHandlerSingleton.getInstance();
        ClientDataSingleton clientData = ClientDataSingleton.getInstance();
        try {
            networkHandler.writeBatch(auctionIdField.getText(), auctionDescriptionField.getText(), clientData.userName);
        } catch (JavaSpaceNotFoundException e) {
            e.printStackTrace();
        } catch (TransactionException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        updateLists();
    }

    private void updateMainList() throws Exception {
        NetworkHandlerSingleton networkHandler = NetworkHandlerSingleton.getInstance();

        if(!networkHandler.auctionTrackerExists()) {
            networkHandler.writeAuctionTracker();
        }

        AuctionTrackerTuple auctionTracker = networkHandler.readAuctionTracker();
        if (auctionTracker == null) {
            System.out.println("Didn't get the auction tracker!");
            return;
        }
        System.out.println("Got auction tracker");

        auctionList.clear();
        System.out.println("Adding auction list");
        System.out.println("Size: " + auctionTracker.auctionList.size());
        for (String auction : auctionTracker.auctionList) {
            addLog(auction, "0");
        }
    }

    private void updateLists() {
        try {
            updateMainList();
        } catch (Exception e) {
            System.out.println("Couldn't update main list!");
            e.printStackTrace();
        }
    }

    @FXML
    void initialize() {
        auctionList = FXCollections.observableArrayList();
        auctionListView.setItems(auctionList);

        myAuctionsListView.setItems(myAuctionsList);
        myAuctionsList = FXCollections.observableArrayList();

        NetworkHandlerSingleton networkHandler = NetworkHandlerSingleton.getInstance();
        networkHandler.setMainController(this);

        ClientDataSingleton clientData = ClientDataSingleton.getInstance();

        updateLists();

        //addLog("Abc", "123");
        //addLog("Abc", "123");


    }



}

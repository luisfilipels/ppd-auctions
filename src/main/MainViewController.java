package main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import net.jini.core.transaction.TransactionException;
import networking.NetworkHandlerSingleton;
import utils.ClientDataSingleton;
import utils.exceptions.AcquireTupleException;
import utils.exceptions.JavaSpaceNotFoundException;
import utils.tuples.AuctionTrackerTuple;
import utils.tuples.UserTuple;

import java.rmi.RemoteException;

public class MainViewController {

    @FXML
    private ListView<HBox> auctionListView;
    private ObservableList<HBox> auctionList;

    @FXML
    private ListView<HBox> myAuctionsListView;
    private ObservableList<HBox> myAuctionsList;

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

    private void openDetailsWindow() {
        try {
            Stage primaryStage = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource("auctionDetails.fxml"));
            primaryStage.setTitle("Cliente");
            primaryStage.setScene(new Scene(root, 800, 500));
            //primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openDetailsForAuction(String auctionID) {
        ClientDataSingleton.getInstance().setLastClickedID(auctionID);
        openDetailsWindow();
    }

    public void addEntryToMainAuctionList(String auctionId, String bidCount) {
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
        System.out.println("Added entry to main list");
    }

    public void addEntryToMyAuctionList(String auctionID, String bidCount) {
        Text text = new Text();
        text.setText(auctionID + " (" + bidCount + " lances)");
        text.setTextOrigin(VPos.BOTTOM);

        Region spacer = new Region();

        Button button = new Button();
        button.setText("Excluir");

        button.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                try {
                    System.out.println("Deleting auction with id " + auctionID);
                    NetworkHandlerSingleton.getInstance().deleteAuctionWithID(auctionID);
                    updateLists();
                } catch (AcquireTupleException e) {
                    System.out.println("Couldn't acquire tuple to be deleted!");
                    e.printStackTrace();
                }
            }
        });

        HBox box = new HBox();
        box.setHgrow(spacer, Priority.ALWAYS);
        box.getChildren().addAll(text, spacer, button);
        myAuctionsList.add(box);
        System.out.println("Added entry to my list");
    }

    @FXML
    void createAuctionButtonClick() {
        NetworkHandlerSingleton networkHandler = NetworkHandlerSingleton.getInstance();
        ClientDataSingleton clientData = ClientDataSingleton.getInstance();
        try {
            networkHandler.writeAuction(auctionIdField.getText(), auctionDescriptionField.getText(), clientData.userName);
        } catch (JavaSpaceNotFoundException e) {
            e.printStackTrace();
        } catch (TransactionException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AcquireTupleException e) {
            System.out.println("Could not acquire tuple when writing auction.");
            e.printStackTrace();
        }
        updateLists();
    }

    private void updateMainList() throws Exception {
        NetworkHandlerSingleton networkHandler = NetworkHandlerSingleton.getInstance();

        if(!networkHandler.auctionTrackerExists()) {
            networkHandler.writeAuctionTracker();
        }

        AuctionTrackerTuple auctionTracker = networkHandler.readAuctionTracker(6000);
        if (auctionTracker == null) {
            System.out.println("Didn't get the auction tracker!");
            return;
        }
        System.out.println("Got auction tracker");

        auctionList.clear();
        System.out.println("Adding auction list");
        System.out.println("Size of all auctions: " + auctionTracker.auctionList.size());
        for (String auction : auctionTracker.auctionList) {
            addEntryToMainAuctionList(auction, "0");
        }
    }

    private void updateMyList() throws Exception{
        NetworkHandlerSingleton networkHandler = NetworkHandlerSingleton.getInstance();

        if(!networkHandler.auctionTrackerExists()) {
            networkHandler.writeAuctionTracker();
        }

        UserTuple myUser = new UserTuple();
        myUser.userID = ClientDataSingleton.getInstance().userName;
        myUser = networkHandler.readUser(myUser, 6000);
        if (myUser == null) {
            System.out.println("Didn't get my user!");
            return;
        }
        System.out.println("Got my user");

        myAuctionsList.clear();
        System.out.println("Adding auction list");
        System.out.println("Size of my auctions: " + myUser.madeAuctions.size());
        for (String auction : myUser.madeAuctions) {
            addEntryToMyAuctionList(auction, "0");
        }
    }

    private void updateLists() {
        try {
            updateMyList();
            updateMainList();
        } catch (Exception e) {
            System.out.println("Couldn't update lists!");
            e.printStackTrace();
        }
        auctionListView.refresh();
        myAuctionsListView.refresh();
    }

    @FXML
    void initialize() {
        auctionList = FXCollections.observableArrayList();
        auctionListView.setItems(auctionList);

        myAuctionsList = FXCollections.observableArrayList();
        myAuctionsListView.setItems(myAuctionsList);

        NetworkHandlerSingleton networkHandler = NetworkHandlerSingleton.getInstance();
        networkHandler.setMainController(this);

        ClientDataSingleton clientData = ClientDataSingleton.getInstance();

        updateLists();

        //addLog("Abc", "123");
        //addLog("Abc", "123");


    }



}

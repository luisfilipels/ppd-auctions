package main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import net.jini.core.transaction.TransactionException;
import networking.NetworkHandlerSingleton;
import utils.ClientDataSingleton;
import utils.exceptions.AcquireTupleException;
import utils.exceptions.WriteTupleException;
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

    private NetworkHandlerSingleton networkHandler;
    private ClientDataSingleton clientData;

    @FXML
    void initialize() {
        clientData = ClientDataSingleton.getInstance();
        try {
            networkHandler = NetworkHandlerSingleton.getInstance();
            prepareSpaceForAuction();
        } catch (Exception e) {
            System.out.println("Could not prepare JavaSpace for auctions!");
            e.printStackTrace();
            System.exit(0);
        }
        setUpLists();
        updateLists();
    }

    private void setUpLists() {
        auctionList = FXCollections.observableArrayList();
        auctionListView.setItems(auctionList);

        myAuctionsList = FXCollections.observableArrayList();
        myAuctionsListView.setItems(myAuctionsList);
    }

    @FXML
    void createAuctionButtonClick() {
        writeAuction();
        updateLists();
    }

    private void writeAuction() {
        try {
            networkHandler.writeAuction(auctionIdField.getText(), auctionDescriptionField.getText(), clientData.userName);
        } catch (WriteTupleException e) {
            e.printStackTrace();
        } catch (AcquireTupleException e) {
            System.out.println("Could not acquire tuple when writing auction.");
            e.printStackTrace();
        }
    }
    
    @FXML
    void updateViewsButtonClick() {
        updateLists();
    }

    private void updateLists() {
        try {
            updateMyList();
            updateMainList();
        } catch (Exception e) {
            System.out.println("Couldn't update lists!");
            e.printStackTrace();
        }
    }

    private void updateMyList() throws Exception{
        UserTuple myUser = getMyUserWithHandler(networkHandler);

        myAuctionsList.clear();
        for (String auction : myUser.madeAuctions) {
            addEntryToMyAuctionList(auction, "0");
        }
    }

    private UserTuple getMyUserWithHandler(NetworkHandlerSingleton networkHandler) throws AcquireTupleException {
        UserTuple template = new UserTuple();
        template.userID = ClientDataSingleton.getInstance().userName;
        UserTuple myUser = networkHandler.readUser(template, 6000);
        if (myUser == null) {
            System.out.println("Could not acquire myUser!");
            throw new AcquireTupleException();
        }
        return myUser;
    }

    private void updateMainList() throws Exception {
        AuctionTrackerTuple auctionTracker = networkHandler.readAuctionTracker(6000);
        if (auctionTracker == null) {
            System.out.println("Didn't get the auction tracker!");
            throw new AcquireTupleException();
        }

        auctionList.clear();
        for (String auction : auctionTracker.auctionList) {
            addEntryToMainAuctionList(auction, "0");
        }
    }

    private void prepareSpaceForAuction() throws WriteTupleException, AcquireTupleException {
        if (!networkHandler.auctionTrackerExists()) {
            networkHandler.writeAuctionTracker();
        }
    }

    public void addEntryToMyAuctionList(String auctionID, String bidCount) {
        Text text = new Text();
        text.setText(auctionID + " (" + bidCount + " lances)");
        Region spacer = new Region();
        Button button = new Button();
        button.setText("Excluir");
        button.setOnMouseClicked(mouseEvent -> {
            try {
                NetworkHandlerSingleton.getInstance().deleteAuctionWithID(auctionID);
                updateLists();
            } catch (AcquireTupleException e) {
                System.out.println("Couldn't acquire tuple to be deleted!");
                e.printStackTrace();
            } catch (WriteTupleException e) {
                e.printStackTrace(); // TODO: Review this
            }
        });
        HBox box = new HBox();
        box.setHgrow(spacer, Priority.ALWAYS);
        box.getChildren().addAll(text, spacer, button);
        myAuctionsList.add(box);
        System.out.println("Added entry to my list");
    }

    private void addEntryToMainAuctionList(String auctionId, String bidCount) {
        Text text = new Text();
        text.setText(auctionId + " (" + bidCount + " lances)");
        Region spacer = new Region();
        Button button = new Button();
        button.setText("Visualizar/Adicionar lance");
        button.setOnMouseClicked(mouseEvent -> openDetailsForAuction(auctionId));
        HBox box = new HBox();
        box.setHgrow(spacer, Priority.ALWAYS);
        box.getChildren().addAll(text, spacer, button);
        auctionList.add(box);
    }

    private void openDetailsForAuction(String auctionID) {
        ClientDataSingleton.getInstance().setLastClickedID(auctionID);
        openDetailsWindow();
    }

    private void openDetailsWindow() {
        try {
            Stage primaryStage = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource("auctionDetails.fxml"));
            primaryStage.setTitle("Detalhes do lote");
            primaryStage.setScene(new Scene(root, 800, 500));
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}

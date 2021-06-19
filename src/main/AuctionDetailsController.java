package main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import networking.NetworkHandlerSingleton;
import utils.ClientDataSingleton;
import utils.tuples.BatchTuple;

import java.util.Map;

public class AuctionDetailsController {

    @FXML
    private Text auctionIdText;

    @FXML
    private Text auctionDescriptionText;

    @FXML
    private ListView<Text> bidListView;
    private ObservableList<Text> bidList;

    @FXML
    private TextField bidValueField;

    @FXML
    private CheckBox publicBidCheckBox;

    @FXML
    private ScrollPane scrollPane;

    private String auctionID;

    private void setAuctionDescriptionText() throws Exception{
        NetworkHandlerSingleton networkHandler = NetworkHandlerSingleton.getInstance();

        BatchTuple thisBatch = networkHandler.readAuctionTuple(auctionID);
        if (thisBatch == null) {
            System.out.println("Couldn't get batch information!");
            return;
        }
        auctionDescriptionText.setText(thisBatch.description);
    }

    @FXML
    void confirmButtonClick() {
        NetworkHandlerSingleton networkHandler = NetworkHandlerSingleton.getInstance();

        try {
            BatchTuple thisBatch = networkHandler.takeAuctionTuple(auctionID);
            if (thisBatch == null) {
                System.out.println("Couldn't get batch information!");
                return;
            }
            int value = Integer.parseInt(bidValueField.getText());
            boolean isPublic = publicBidCheckBox.isSelected();
            thisBatch.addBid(value, isPublic);
            networkHandler.writeAuction(thisBatch);

            if (networkHandler.readAuctionTuple(auctionID) == null) {
                System.out.println("Didn't write bid!");
            } else {
                System.out.println("Wrote bid");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateBids() {
        NetworkHandlerSingleton networkHandler = NetworkHandlerSingleton.getInstance();
        try {
            BatchTuple thisBatch = networkHandler.readAuctionTuple(auctionID);
            if (thisBatch == null) {
                System.out.println("Couldn't get this batch!");
                return;
            }
            if (thisBatch.bids == null) {
                System.out.println("Bids were null!");
                return;
            }
            for (Map.Entry<String, String> entry : thisBatch.bids.entrySet()) {
                // TODO: Take care of private bids
                String[] bidData = entry.getValue().split("\\|");
                int value = Integer.parseInt(bidData[0]);
                boolean isPublic = bidData[1].equals("true");
                bidList.add(new Text("Criador: " + entry.getKey() + ", valor: " + value));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        bidList = FXCollections.observableArrayList();
        bidListView.setItems(bidList);

        ClientDataSingleton clientData = ClientDataSingleton.getInstance();
        auctionID = clientData.getLastClickedID();
        auctionIdText.setText(auctionID);

        auctionDescriptionText.wrappingWidthProperty().bind(scrollPane.widthProperty());
        try {
            setAuctionDescriptionText();
        } catch (Exception e) {
            System.out.println("Couldn't set description!");
            e.printStackTrace();
        }

        updateBids();
    }


}

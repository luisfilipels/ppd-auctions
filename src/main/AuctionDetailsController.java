package main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
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

    private void closeSelfWindow(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    @FXML
    void cancelButtonClick(ActionEvent event) {
        closeSelfWindow(event);
    }

    @FXML
    void confirmButtonClick(ActionEvent event) {
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
                closeSelfWindow(event);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateBids() {
        NetworkHandlerSingleton networkHandler = NetworkHandlerSingleton.getInstance();
        ClientDataSingleton clientData = ClientDataSingleton.getInstance();
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
                // TODO: Fix private bids
                String[] bidData = entry.getValue().split("\\|");
                int value = Integer.parseInt(bidData[0]);
                boolean isPublic = bidData[1].equals("true");
                String bidCreator = entry.getKey();
                if (bidCreator.equals(clientData.userName)) {
                    // I created the bid, so I should absolutely see the bid
                    if (isPublic) {
                        bidList.add(new Text("Criador: Você (privado), valor: " + value));
                    } else {
                        bidList.add(new Text("Criador: Você, valor: " + value));
                    }
                } else {
                    // I didn't create the bid, so I should only see the bid if I created the
                    // auction, of if it is public
                    if (isPublic) {
                        bidList.add(new Text("Criador: " + bidCreator + ", valor: " + value));
                    } else {
                        if (thisBatch.sellerId.equals(clientData.userName)) {
                            bidList.add(new Text("Criador: " + bidCreator + " (privado), valor: " + value));
                        }
                    }
                }
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

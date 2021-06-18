package main;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import networking.NetworkHandlerSingleton;
import utils.ClientDataSingleton;
import utils.tuples.BatchTuple;

public class AuctionDetailsController {

    @FXML
    private Text auctionIdText;

    @FXML
    private Text auctionDescriptionText;

    @FXML
    private ListView<Text> bidListView;

    @FXML
    private TextField bidValueField;

    @FXML
    private CheckBox publicBidCheckBox;

    @FXML
    private ScrollPane scrollPane;

    private String auctionID;

    private void setAuctionDescriptionText() throws Exception{
        NetworkHandlerSingleton networkHandler = NetworkHandlerSingleton.getInstance();

        BatchTuple thisBatch = networkHandler.takeAuctionTuple(auctionID);
        if (thisBatch == null) {
            System.out.println("Couldn't get batch information!");
            return;
        }
        auctionDescriptionText.setText(thisBatch.description);

        networkHandler.writeAuction(thisBatch);
    }

    @FXML
    public void initialize() {
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
    }


}

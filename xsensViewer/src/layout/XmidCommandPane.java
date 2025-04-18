package layout;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import main.SerialSensorControl;
import xsens.XsMessageID;

/**
 * 
 * Pane for sending commands to the device. 
 * @author Jamie Macaulay
 *
 */
public class XmidCommandPane extends BorderPane {
	
	/**
	 * The port names
	 */
	private ComboBox<XsMessageID> command;
	
	/**
	 * Label which displays the recieved message back
	 */
	private Label messageBackLabel;

	/**
	 * Reference to the sensor controller. 
	 */
	private SerialSensorControl sensorControl; 
	
	
	public XmidCommandPane(SerialSensorControl sensorControl) {
		this.sensorControl=sensorControl;
		this.setCenter(createCommandPane());
	}
	
	
	
	private Pane createCommandPane() {
		// populate the combo list with commands the user can use. 
		ObservableList<XsMessageID> commandOptions = FXCollections.observableArrayList(
		        XsMessageID.XMID_GotoMeasurement,
		        XsMessageID.XMID_GotoConfig,
		        XsMessageID.XMID_SetNoRotation,
		        XsMessageID.XMID_ReqFilterProfile,
		        XsMessageID.XMID_ReqDid,
		        XsMessageID.XMID_ReqDeviceType,
		        XsMessageID.XMID_ReqSDSize,
		        XsMessageID.XMID_ReqOutputConfig,
		        XsMessageID.XMID_ReqOptionFlags

		    );
		command = new ComboBox<XsMessageID>(commandOptions);
		command.getSelectionModel().select(0);

		Button send = new Button("Send"); 
		send.setOnAction((action)->{
			System.out.println("Sending message: " + command.getValue());
			sensorControl.sendMessage(command.getValue()); 
		});
		
		
		send.setMinWidth(90);
		//make combo box same height as button
		command.prefHeightProperty().bind(send.heightProperty());
		
		HBox hBox = new HBox(); 
		hBox.setSpacing(5);
		hBox.getChildren().addAll(command, send);
		HBox.setHgrow(command, Priority.ALWAYS);

		messageBackLabel = new Label(); 
		
		VBox holder = new VBox();
		holder.setSpacing(5);
		holder.getChildren().addAll(hBox, messageBackLabel);
		
		return holder; 
	}

	/**
	 * @return the messageBackLabel
	 */
	public Label getMessageBackLabel() {
		return messageBackLabel;
	}
	
	/**
	 * Set text on message back label. 
	 * @param text - the text on message back label. 
	 */
	public void setMessageBackLabelText(String text) {
		 messageBackLabel.setText(text);
		 messageBackLabel.setTooltip(new Tooltip(text));
	}
	
}
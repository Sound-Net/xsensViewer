package layout;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import main.SensorData;
import main.SerialSensorControl;
import xsens.XMIDOptions;
import xsens.XSensMessage;
import xsens.XsDataIdentifier;
import xsens.XsMessageID;
import xsens.XsOptionID;
import xsens.XBusMessage;

import java.util.ArrayList;

import org.controlsfx.control.ToggleSwitch;


public class XmidOptionsPane extends BorderPane {
	
	private static final int SAMPLE_RATE = 25;

	/**
	 * The port names
	 */
	private ComboBox<XsDataIdentifier> anglesOptionsBox;
	
	/**
	 * Label which displays the recieved message back
	 */
	private Label messageBackLabel;

	/**
	 * Reference to the sensor controller. 
	 */
	private SerialSensorControl sensorControl;

	private double BUTTON_WIDTH = 90;

	private ToggleSwitch inSituCompassSwitch;

	private Button send;

	/**
	 * Output options for xsens sensor. 
	 */
	private XMIDOptions xMIDOptions;

	private ChoiceBox<Integer> frequencyBox; 
	
	
	public XmidOptionsPane(SerialSensorControl sensorControl) {
		this.sensorControl=sensorControl; 
		this.setCenter(createCommandPane());
		sensorControl.addSensorMessageListener((sensorData)->{
			//check to see whether we have raw MT messages. 
				switch (sensorData.flag) {

				case MTMESSAGE:			
					//get options. 
					xMIDOptions = getXMIDOptions(xMIDOptions, sensorData); 
					setParams(xMIDOptions);
					break;
				}
			
		});
	}
	
	
	private void setParams(XMIDOptions xMIDOptions) {
		if (xMIDOptions ==null) {
			 setOptionsDisabled(true); 
			 return;
		}
		if (xMIDOptions.areParamsNull()) {
			 setOptionsDisabled(true); 
			 return;
		}		
		else setOptionsDisabled(false); 

		this.inSituCompassSwitch.setSelected(xMIDOptions.inCompassCal);
		this.anglesOptionsBox.getSelectionModel().select(xMIDOptions.angleOuput);
		
		System.out.println("Frequency!!!:  " + xMIDOptions.sampleRate.intValue()); 
		this.frequencyBox.getSelectionModel().select(Integer.valueOf(xMIDOptions.sampleRate.intValue()));

	}


	/**
	 * Gets the xmid options from the pane
	 * @param xMIDOptions2
	 * @param sensorData
	 * @return
	 */
	private XMIDOptions getXMIDOptions(XMIDOptions xMIDOptions2, SensorData sensorData) {
		XBusMessage xsensMessage  = XSensMessage.XbusMessage_getXBusMessage(sensorData.mtMessage ); 
		
		if (xsensMessage==null) {
			System.out.println("Options returned a null xsens message"); 
			return xMIDOptions2;
		}
		
		
//		System.out.println("XMID: " + xsensMessage.mid);

		switch (xsensMessage.mid) {
		
		case XMID_OutputConfig:
			System.out.println("XMID: XMID_OutputConfig: " +  xsensMessage.len);
			
			ArrayList<XsDataIdentifier> outputConfig = XSensMessage.get_OutputConfig(xsensMessage.charBufferRx); 
			for (int i=0; i<outputConfig.size(); i++) {
				if (outputConfig.get(i).getFrequency()!=null && outputConfig.get(i).getFrequency()>0) {
					xMIDOptions2.angleOuput = outputConfig.get(i); 
					xMIDOptions2.sampleRate = outputConfig.get(i).getFrequency(); 

				}
			}
			
			break;
		case XMID_SetOptionFlagAck:
			System.out.println("XMID: XMID_SetOptionFlagAck: " + xsensMessage.len);
			
			ArrayList<XsOptionID> optionsFlags = XSensMessage.get_OptionConfig(xsensMessage.charBufferRx); 
			
			for (int i=0; i<optionsFlags.size(); i++) {
				if (optionsFlags.get(i) == XsOptionID.EnableInRunCompassCalibration) {
					xMIDOptions2.inCompassCal = true;
				}
			}
			
			break;
		case XMID_SetOptionFlags:
			System.out.println("XMID: XMID_SetOptionFlags: " +  + xsensMessage.len);
			
			break;
		default:
			//do nothing
			break;
		}

//		switch ()
		
		return xMIDOptions2;
	}



	private Pane createCommandPane() {
		VBox holder = new VBox();
		holder.setSpacing(5);
		
		// populate the combo list with commands the user can use. 
		ObservableList<XsDataIdentifier> commandOptions = FXCollections.observableArrayList(
				XsDataIdentifier.XDI_Quaternion,
				XsDataIdentifier.XDI_EulerAngles
		    );
		anglesOptionsBox = new ComboBox<XsDataIdentifier>(commandOptions);
		anglesOptionsBox.getSelectionModel().select(0);
		HBox.setHgrow(anglesOptionsBox, Priority.ALWAYS);
		//anglesOptionsBox.prefWidthProperty().bind(holder.widthProperty().subtract(5));
		
		frequencyBox = new ChoiceBox<Integer>(); 
		frequencyBox.getItems().addAll(25, 50, 75, 100); 
		frequencyBox.setPrefWidth(60);
		
		HBox outputHolder = new HBox(); 
		outputHolder.setAlignment(Pos.CENTER_LEFT);
		outputHolder.setSpacing(5);
		outputHolder.getChildren().addAll(anglesOptionsBox, new Label("FS"), frequencyBox); 
		

		send = new Button("Set"); 
		send.setOnAction((action)->{
			
			try {
				
//			System.out.println("Sending message: " + command.getValue());
			int[] data = new int[4]; 
			
			//set the data out type
			
			int len = XSensMessage.format_OutputConfiguration(data, 
					new XsDataIdentifier[] {xMIDOptions.angleOuput}, new int[] {SAMPLE_RATE}); 
			
			sensorControl.sendMessage(XsMessageID.XMID_SetOutputConfig, data); 
			
			
			Thread.sleep(200);
			
			//set the options
			
			XsOptionID[] xsOptions = new XsOptionID[] {XsOptionID.EnableInRunCompassCalibration}; 
			
			if (xMIDOptions.inCompassCal) {
				len = XSensMessage.format_OptionConfig(data, xsOptions, null);
			}
			else {
				len = XSensMessage.format_OptionConfig(data,null , xsOptions);
			}
			
			sensorControl.sendMessage(XsMessageID.XMID_SetOutputConfig, data); 

			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
			
			//TODO
		});
		
		
		send.setMinWidth(BUTTON_WIDTH);
		//make combo box same height as button
		anglesOptionsBox.prefHeightProperty().bind(send.heightProperty());
		

		Button get = new Button("Get"); 
		get.setOnAction((action)->{
			try {
				
				//reset this because we need to wait for new data to come in. 
				xMIDOptions = new XMIDOptions();
				setOptionsDisabled(true); //reset - will only be anabled once all options have been recieved. 
				
				//set to congig
				sensorControl.sendMessage(XsMessageID.XMID_GotoConfig); 

				Thread.sleep(100);
				
				//request the output configuration
				sensorControl.sendMessage(XsMessageID.XMID_ReqOutputConfig); 
				
				Thread.sleep(100);

				//request the current options
				sensorControl.sendMessage(XsMessageID.XMID_ReqOptionFlags); 

				Thread.sleep(100);
				
			
				if (true) { //TODO - figure out if in measurement mode?
					sensorControl.sendMessage(XsMessageID.XMID_GotoMeasurement); 
				}
				
				//now the listener will listen for responses. 

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		
		});
		get.setPrefWidth(BUTTON_WIDTH);
	
		messageBackLabel = new Label(); 
		
		inSituCompassSwitch= new ToggleSwitch("In-situ compass calibration"); 
		
		HBox hBox = new HBox(); 
		hBox.setSpacing(5);
		hBox.getChildren().addAll(get, send);
		hBox.setAlignment(Pos.CENTER_RIGHT);
		

		holder.getChildren().addAll(outputHolder, inSituCompassSwitch, hBox, messageBackLabel);
		
		return holder; 
	}
	
	/**
	 * Set the option paramters to disabled
	 * @return the messageBackLabel
	 */
	public void setOptionsDisabled(boolean disable) {
		anglesOptionsBox.setDisable(disable);
		inSituCompassSwitch.setDisable(disable);
		send.setDisable(disable);
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
	}

}

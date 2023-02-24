package layout;

import com.fazecast.jSerialComm.SerialPort;

import comms.SerialUtils;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import main.SerialParams;

/**
 * Pane for controlling serial input data
 * @author Jamie Macaulay 
 *
 */
@SuppressWarnings("restriction")
public class SerialCommPane extends BorderPane {

	/**
	 * Thge port names
	 */
	private ComboBox<String> portNames;

	/**
	 * The baud rates
	 */
	private ComboBox<Integer> baudRates;

	/**
	 * Keep a record of the current serial ports. 
	 */
	private SerialPort[] portEnum;

	private HBox baudHolder; 



	public SerialCommPane(){
		this.setCenter(createSerialCommPane()); 
		setSensorNames();
	}


	private Pane createSerialCommPane(){

		VBox holder = new VBox();
		holder.setSpacing(5);	

		Label portNamesLabel = new Label("Select Serial Port"); 
		holder.getChildren().add(portNamesLabel);
		//create a lit of comm ports. 

		portNames= new ComboBox<String>();
		//show different serial ports when the combo box is opened. i.e. adds
		//or removes serial ports when he program is opened.
		portNames.showingProperty().addListener((obsVal, oldVal, newVal)->{
			//note must do this on showing property - set on action causes all sorts of weirdness
			if (newVal) {
				//if the combo box is showing see if the menu needs changed to show correct COM ports. 
				setSensorNames();
			}
		});
//		portNames.setOnAction((action)->{
//			setSensorNames();
//		});
		
		holder.getChildren().add(portNames);


		Label baudRateLabel = new Label("BaudRates"); 
		holder.getChildren().add(baudRateLabel);

		baudRates= new ComboBox<Integer>(); 
		ObservableList<Integer> baudRateList = FXCollections.observableArrayList(SerialUtils.baudRate);
		baudRates.setItems(baudRateList);
		
		baudHolder = new HBox(); 
		baudHolder.setSpacing(5);
		baudHolder.getChildren().add(baudRates);
		holder.getChildren().add(baudHolder);

		return holder;

	}

	/**
	 * Set the controls to show current serial params 
	 * @param serialParams
	 */
	public void setParams(SerialParams serialParams){

		baudRates.setValue(serialParams.baudRate);

		if (portNames.getItems().contains(serialParams.port)){
			baudRates.setValue(serialParams.baudRate);
		}
		else if (portNames.getItems().size()>=1){
			portNames.getSelectionModel().select(0);
		}
	}

	/**
	 * Get the serial parfams from the user settings in the controls
	 * @param serialParams
	 * @return
	 */
	public SerialParams getParams(SerialParams serialParams){
		serialParams.baudRate=baudRates.getValue(); 
		serialParams.port=portEnum[portNames.getSelectionModel().getSelectedIndex()].getSystemPortName();
		
		
		return serialParams;
	}


	private synchronized void setSensorNames(){

		try {
			portEnum = SerialPort.getCommPorts();


			portNames.getItems().clear(); 
			//		    //First, Find an instance of serial port as set in PORT_NAMES.
			//		    while (portEnum.hasMoreElements()) {
			//		        CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			//		        portNames.getItems().add(currPortId.getName()); 
			//		    }

			for (int i=0; i< portEnum.length; i++) {
				portNames.getItems().add(portEnum[i].getDescriptivePortName() + " (" + portEnum[i].getSystemPortName() + ")");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public HBox getBaudHolder() {
		return baudHolder;
	}


	public  ComboBox<Integer> getBaudComboBox() {
		return baudRates;
	}


	public ComboBox<String> getPortComboBox() {
		return portNames;
	}

}

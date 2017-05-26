package layout;

import java.util.Enumeration;
import comms.SerialUtils;
import gnu.io.CommPortIdentifier;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import main.SerialParams;

/**
 * Pane for controlling serial input data
 * @author Jamie Macaulay 
 *
 */
public class SerialCommPane extends BorderPane {
	
	/**
	 * Thge port names
	 */
	private ComboBox<String> portNames;
	
	/**
	 * The baud rates
	 */
	private ComboBox<Integer> baudRates; 

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
		holder.getChildren().add(portNames);
		
		
		Label baudRateLabel = new Label("BaudRates"); 
		holder.getChildren().add(baudRateLabel);

		baudRates= new ComboBox<Integer>(); 
		ObservableList<Integer> baudRateList = FXCollections.observableArrayList(SerialUtils.baudRate);
		baudRates.setItems(baudRateList);
		holder.getChildren().add(baudRates);

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
		serialParams.port=portNames.getValue();
		return serialParams;
	}
	
	
	private void  setSensorNames(){
		  Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();
		  portNames.getItems().clear(); 
		    //First, Find an instance of serial port as set in PORT_NAMES.
		    while (portEnum.hasMoreElements()) {
		        CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
		        portNames.getItems().add(currPortId.getName()); 
		    }
		    
	}

}

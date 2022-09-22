package layout;

import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.control.*;

import main.SensorsControl;
/**
 * Controls the ability to send all sensors commands. 
 * @author Jamie Macaulay
 *
 */
public class MasterCommPane extends BorderPane {
	
	private SensorsControl sensorControl;

	public MasterCommPane(SensorsControl sensorControl) {
		this.sensorControl = sensorControl; 
		this.setCenter(new Label("Sensor Control"));
	}

}

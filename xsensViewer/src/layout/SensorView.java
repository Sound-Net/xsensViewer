package layout;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import main.SensorControl;
import main.SensorData;

public class SensorView extends BorderPane {
	
	/*
	 * Reference to the control
	 */
	private SensorControl sensorControl;

	/**
	 * Handles setting for serial device. 
	 */
	private SerialCommPane serialCommPane;
	
	/**
	 * Shows current data
	 */
	private Label dataLabel;

	/*I*
	 * 
	 */
	private SensorPane3D sensorPane3D; 
	

	public SensorView(SensorControl sensorControl){
		this.sensorControl=sensorControl; 
		
		VBox leftHolder = new VBox(); 
		leftHolder.setSpacing(5);
		
		//leftHolder.getStylesheets().add(darkStyle);
		leftHolder.getChildren().add(serialCommPane=new SerialCommPane());
		serialCommPane.setParams(sensorControl.getParams());
		
		leftHolder.getChildren().addAll(createConnectPane()); 
		
		leftHolder.getChildren().addAll(createDataPane()); 
		
		
		sensorControl.addSensorMessageListener((sensormessage)->{
			
			setDataLabelData(sensormessage);
			sensorPane3D.setOrientationData(sensormessage); 
			
		});
		
		this.setCenter(sensorPane3D=new SensorPane3D());
		this.setLeft(leftHolder);
	}
	
	/*
	 * The pane which starts and stops connection
	 */
	private Pane createConnectPane(){
		
		HBox buttonHolder = new HBox(); 
		buttonHolder.setSpacing(5);
		
		Button startButton = new Button("Connect"); 
		startButton.setOnAction((action)->{
			sensorControl.setParams(serialCommPane.getParams(this.sensorControl.getParams())); 
			sensorControl.startSerial();
		});
		
		
		Button stopButton = new Button("Stop"); 
				stopButton.setOnAction((action)->{
			sensorControl.stopSerial();
		}); 
		
		buttonHolder.getChildren().addAll(new Label("Port Settings"), startButton, stopButton); 
				
		return buttonHolder; 
		
	}
	
	/**
	 * Create pane whihc shows incomming data
	 * @return
	 */
	private Pane createDataPane(){

		VBox dataPane= new VBox(); 
		
		dataPane.getChildren().add(new Label("Data")); 
		dataPane.getChildren().add(dataLabel= new Label("-")); 
		
		return dataPane; 

	}
	
	
	/**
	 * Set the label incomming sensor data
	 * @param sensormessage 
	 */
	public void setDataLabelData(SensorData sensormessage){
		//check if tyhere is orientation data
		if (sensormessage.eularAngles!=null){
			dataLabel.setText("Roll: " + sensormessage.eularAngles[0] + " Pitch: "+sensormessage.eularAngles[1] + " Yaw: "+sensormessage.eularAngles[2]);
		}
		
	}

	/**
	 * Start the serial port. 
	 */
	public void startSerial(){
		sensorControl.startSerial(); 
	}

			
		
}

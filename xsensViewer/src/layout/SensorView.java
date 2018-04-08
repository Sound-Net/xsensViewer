package layout;

import comms.SerialMessageParser.DataTypes;
import comms.SerialUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
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

	private Label yawLabel;

	private Label pitchLabel;

	private Label rollLabel; 
	

	public SensorView(SensorControl sensorControl){
		this.sensorControl=sensorControl; 
		
		VBox leftHolder = new VBox(); 
		leftHolder.setSpacing(5);
		leftHolder.setPrefWidth(250);
		
		//leftHolder.getStylesheets().add(darkStyle);
		leftHolder.getChildren().add(serialCommPane=new SerialCommPane());
		serialCommPane.setParams(sensorControl.getParams());
		
		leftHolder.getChildren().addAll(createConnectPane()); 
		
		leftHolder.getChildren().addAll(createDataTypePane()); 

		
		leftHolder.getChildren().addAll(createDataPane()); 
		
		
		sensorControl.addSensorMessageListener((sensormessage)->{
			
			setEulerData(sensormessage);
			setDataLabelData(sensormessage);
			sensorPane3D.setOrientationData(sensormessage); 
			
		});
		
		StackPane holderPane= new StackPane(); 
		holderPane.getChildren().add(sensorPane3D=new SensorPane3D());
		
		Pane eulerPane= createEulerAnglesPane(); 
		StackPane.setAlignment(eulerPane, Pos.TOP_LEFT);
		holderPane.getChildren().add(eulerPane);
		
		this.setCenter(holderPane);
		this.setLeft(leftHolder);
	}
	
	
	
	private Pane createEulerAnglesPane() {

		GridPane gridPane= new GridPane();
		gridPane.setHgap(5);
		gridPane.setVgap(5);
		
		gridPane.add(new Label("Yaw"), 0, 1);
		gridPane.add(new Label("Pitch"), 0, 2);
		gridPane.add(new Label("Roll"), 0, 3);
		
		gridPane.add(yawLabel=new Label(""), 1, 1);
		gridPane.add(pitchLabel=new Label(""), 1, 2);
		gridPane.add(rollLabel=new Label(""), 1, 3);
		
		gridPane.setPadding(new Insets(5,5,5,5));

		return gridPane;
	}



	private Pane createDataTypePane() {

		VBox dataTypePane= new VBox(); 
		
		dataTypePane.getChildren().add(new Label("Data Type")); 
		
		ComboBox<String> dataTypeBox = new ComboBox<String>(); 
		for (DataTypes value: DataTypes.values()){
			dataTypeBox.getItems().add(value.toString());
		}
		dataTypeBox.setOnAction((action)->{
			this.sensorControl.getSerialParser().setMessageFlag(DataTypes.values()[dataTypeBox.getSelectionModel().getSelectedIndex()]);
		});
		dataTypeBox.getSelectionModel().select(0);
		
		dataTypePane.getChildren().add(dataTypeBox); 

		
		return dataTypePane; 
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
		
		buttonHolder.getChildren().addAll( 
				startButton, stopButton); 
		
		
		VBox vBox = new VBox(); 
		vBox.getChildren().addAll(new Label("Connect: "), buttonHolder);
				
		return vBox; 
		
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
	 * Set the label showing euler data on the 3D display
	 * @param sensormessage 
	 */
	public void setEulerData(SensorData sensormessage){
	
		double[] eularAngles = null; 
		//check if tyhere is orientation data
		if (sensormessage.eularAngles!=null){
			eularAngles=sensormessage.eularAngles;
		}
		else if (sensormessage.quaternion!=null){
			//TODO- should maybe calc this in sensor package. 
			double[] angles = SerialUtils.quat2Eul(sensormessage.quaternion);
			angles[0]=Math.toDegrees(angles[0]); 
			angles[1]=Math.toDegrees(angles[1]); 
			angles[2]=Math.toDegrees(angles[2]); 
			eularAngles=angles;

		}
		
		yawLabel.setText( String.format("%.2f", eularAngles[0]));
		pitchLabel.setText( String.format("%.2f", eularAngles[1]));
		rollLabel.setText( String.format("%.2f", eularAngles[2]));
		
	}
	
	
	/**
	 * Set the label incomming sensor data
	 * @param sensormessage 
	 */
	public void setDataLabelData(SensorData sensormessage){
	
		//check if tyhere is orientation data
		if (sensormessage.eularAngles!=null){
			dataLabel.setText("Roll: " + String.format("%.2f", sensormessage.eularAngles[0]) + " Pitch: "+String.format("%.2f",sensormessage.eularAngles[1]) + 
					" Yaw: "+String.format("%.2f",sensormessage.eularAngles[2]));
		}
		else if (sensormessage.quaternion!=null){
			dataLabel.setText("a " + String.format("%.4f", sensormessage.quaternion[0]) + " b: "+String.format("%.4f", sensormessage.quaternion[1]) + " c: "
					+String.format("%.4f", sensormessage.quaternion[2])+ " d: "+String.format("%.4f", sensormessage.quaternion[2]));
		}
		
	}

	/**
	 * Start the serial port. 
	 */
	public void startSerial(){
		sensorControl.startSerial(); 
	}

			
		
}
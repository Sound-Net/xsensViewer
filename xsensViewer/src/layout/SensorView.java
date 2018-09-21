package layout;


import comms.SerialMessageParser.DataTypes;
import comms.SerialUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import main.SensorControl;
import main.SensorData;

/**
 * The main view.
 * 
 * @author Jamie Macaulay
 *
 */
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
	private Label orientationLabel;

	/*I*
	 * Pane which shows 3D sensor package. 
	 */
	private SensorPane3D sensorPane3D;

	/**
	 * 
	 */
	private Label yawLabel;

	/**
	 * The label showing pitch
	 */
	private Label pitchLabel;

	private Label rollLabel;

	private Label pressureLabel;

	private Label tempLabel;

	private Label RGBLabel;

	private Label batLabel; 

	final String DEGREE  = "\u00b0";

	/**
	 * The start button. 
	 */
	private ButtonBase startButton;

	/**
	 * Pane which sends commands to the sensor package. 
	 */
	private CommandPane commandPane;

	public SensorView(SensorControl sensorControl){
		this.sensorControl=sensorControl; 

		VBox leftHolder = new VBox(); 
		leftHolder.setSpacing(5);
		leftHolder.setPrefWidth(250);

		//leftHolder.getStylesheets().add(darkStyle);
		leftHolder.getChildren().add(serialCommPane=new SerialCommPane());
		serialCommPane.setParams(sensorControl.getParams());

		leftHolder.getChildren().addAll(createConnectPane()); 

		//leftHolder.getChildren().addAll(createDataTypePane()); 

		leftHolder.getChildren().addAll(createDataPane()); 
		
		leftHolder.getChildren().add(this.commandPane = new CommandPane(sensorControl)); 


		sensorControl.addSensorMessageListener((sensormessage)->{
			setEulerData(sensormessage);
			setDataLabelData(sensormessage);
			sensorPane3D.setOrientationData(sensormessage); 
		});

		StackPane holderPane= new StackPane(); 
		holderPane.getChildren().add(sensorPane3D=new SensorPane3D());
		holderPane.setMouseTransparent(true);

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

		startButton = new Button("Connect"); 
		startButton.setOnAction((action)->{
			if (sensorControl.isSerialRunning()) {
				sensorControl.stopSerial();
				setDataLabelsNull();
			}
			else {
				sensorControl.setParams(serialCommPane.getParams(this.sensorControl.getParams())); 
				sensorControl.startSerial();
			}
			updateStartButtonLabel(); 
		});


		Button stopButton = new Button("Deploy"); 
		stopButton.setOnAction((action)->{
			sensorControl.deploy(); 
			updateStartButtonLabel(); 
			setDataLabelsNull(); 
		}); 

		buttonHolder.getChildren().addAll( 
				startButton, stopButton); 


		VBox vBox = new VBox(); 
		vBox.getChildren().addAll(new Label("Connect: "), buttonHolder);

		return vBox; 

	}
	
	/**
	 * Update the label on the settings button. 
	 */
	private void updateStartButtonLabel() {
		if (sensorControl.isSerialRunning()) {
			startButton.setText("Stop");
		}
		else {
			startButton.setText("Connect");
		}
	}

	/**
	 * Create pane whihc shows incomming data
	 * @return
	 */
	private Pane createDataPane(){

		VBox dataPane= new VBox(); 

		dataPane.getChildren().add(setLabelFontBold(new Label("Orientation: "))); 
		dataPane.getChildren().add(orientationLabel= new Label("-")); 

		dataPane.getChildren().add(setLabelFontBold(new Label("Pressure: "))); 
		dataPane.getChildren().add(pressureLabel= new Label("-")); 

		dataPane.getChildren().add(setLabelFontBold(new Label("Temperature: "))); 
		dataPane.getChildren().add(tempLabel= new Label("-")); 

		dataPane.getChildren().add(setLabelFontBold(new Label("RGB: "))); 
		dataPane.getChildren().add(RGBLabel= new Label("-")); 

		dataPane.getChildren().add(setLabelFontBold(new Label("Battery: "))); 
		dataPane.getChildren().add(batLabel= new Label("-")); 

		return dataPane; 

	}
	
	/**
	 * Set label font to bold. 
	 * @param label - the label to set bold
	 * @return a bold label. 
	 */
	private Label setLabelFontBold(Label label) {
		label.setFont(Font.font("Verdanna", FontWeight.BOLD, 30));
		return label; 
	}


	/**
	 * Set the label showing euler data on the 3D display
	 * @param sensormessage - the sensor message containing the data. 
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

		if (eularAngles!=null) {
			yawLabel.setText( String.format("%.2f", eularAngles[0]));
			pitchLabel.setText( String.format("%.2f", eularAngles[1]));
			rollLabel.setText( String.format("%.2f", eularAngles[2]));
		}
	}


	/**
	 * Set the label incoming sensor data
	 * @param sensormessage - the sensor message containing the data. 
	 */
	public void setDataLabelData(SensorData sensormessage){

		//check if tyhere is orientation data
		if (sensormessage.eularAngles!=null){
			orientationLabel.setText("Roll: " + String.format("%.2f", sensormessage.eularAngles[0]) + " Pitch: "+String.format("%.2f",sensormessage.eularAngles[1]) + 
					" Yaw: "+String.format("%.2f",sensormessage.eularAngles[2]));
		}
		else if (sensormessage.quaternion!=null){
			orientationLabel.setText("a: " + String.format("%.4f", sensormessage.quaternion[0]) + " b: "+String.format("%.4f", sensormessage.quaternion[1]) + " c: "
					+String.format("%.4f", sensormessage.quaternion[2])+ " d: "+String.format("%.4f", sensormessage.quaternion[2]));
		}

		if (sensormessage.temperature!=null) {
			tempLabel.setText(String.format("%.2f ", sensormessage.temperature) + DEGREE + "C"); 
		}

		if (sensormessage.pressure!=null) {
			pressureLabel.setText(String.format("%.3f ", sensormessage.pressure) + "mbar"); 
		}

		if (sensormessage.rgb!=null) {
			RGBLabel.setText("red: " + String.format("%.1f", sensormessage.rgb[0]) + " blue: "+String.format("%.1f", sensormessage.rgb[1])  + " green: "
					+String.format("%.1f", sensormessage.rgb[2]));
		}

		if (sensormessage.batteryLevel!=null) {
			double batteryLevel=sensormessage.batteryLevel; 
			if (batteryLevel>98); batteryLevel=100; 
			batLabel.setText(String.format("%.2f ", batteryLevel) + "%: " + String.format("%.2fV", sensormessage.batteryLevelV));
		}
		
		if (sensormessage.flag == DataTypes.MTMESSAGE) {
			String dataStr="MTMessage: "; 
			for (int i=0; i<sensormessage.mtMessage.length; i++) {
				dataStr+=(sensormessage.mtMessage[i] +  " "); 
			}
			commandPane.setMessageBackLabelText(dataStr);
		}
	}
	
	
	/**
	 * Set all the data labels blank. 
	 */
	private void setDataLabelsNull() {
		orientationLabel.setText("-");
		tempLabel.setText("-");
		pressureLabel.setText("-");
		RGBLabel.setText("-");
		batLabel.setText("-");
	}

	/**
	 * Start the serial port. 
	 */
	public void startSerial(){
		sensorControl.startSerial(); 
	}
	
	/**
	 * Start the serial port. 
	 */
	public void stopSerial(){
		sensorControl.stopSerial();
	}



}
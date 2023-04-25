package layout;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone; 

import comms.SerialMessageParser.DataTypes;
import comms.SerialUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import jfxtras.styles.jmetro.MDL2IconFont;
import main.SerialSensorControl;
import xsens.XsMessageID;
import main.DeviceManager;
import main.DeviceManager.DeviceType;
import main.SensorData;
import main.SensorsControl.SensorUpdate;

/**
 * The main view.
 * 
 * @author Jamie Macaulay
 *
 */
public class SerialSensorPane extends BorderPane {

	/*
	 * Reference to the control
	 */
	private SerialSensorControl sensorControl;

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
	private XmidCommandPane commandPane;

	private Label sdLabel;

	private Label timeLabel;

	/**
	 * The default width for buttons to make things consistent. 
	 */
	private double BUTTON_WIDTH = 80;

	/**
	 * Shows the unique ID of the device. 
	 */
	private Label idLabel; 
	
	private DeviceType currentDeviceID = DeviceType.SENSLOGGER_V1;

	private Button eraseButton;

	private Button refreshbutton;

	private Button setTimeButton;

	/**
	 * Shows the current firmware versions
	 */
	private Label firmwareLabel;

	public SerialSensorPane(SerialSensorControl sensorControl){
		
		
		this.sensorControl=sensorControl; 

		VBox leftHolder = new VBox(); 
		leftHolder.setSpacing(5);
		leftHolder.setPrefWidth(250);
		leftHolder.setPadding(new Insets(5,5,5,5));
		
		Label connectLabel = new  Label("Connect Sensor"); 
		SensorView.titlelabel(connectLabel);
		leftHolder.getChildren().addAll(connectLabel); 

		leftHolder.getChildren().add(serialCommPane=new SerialCommPane());
		serialCommPane.setParams(sensorControl.getParams());

		serialCommPane.getBaudHolder().getChildren().addAll(createConnectPane()); 
		
		startButton.prefHeightProperty().bind(serialCommPane.getBaudComboBox().heightProperty());

		//leftHolder.getChildren().addAll(createDataTypePane());;
		
		Label sensorLabel = new  Label("Sensor Data"); 
		sensorLabel.setPadding(new Insets(10,0,0,0));
		SensorView.titlelabel(sensorLabel);
		
		idLabel= new Label(); 
		idLabel.setTextAlignment(TextAlignment.CENTER);
		idLabel.setAlignment(Pos.CENTER_LEFT);
//		idLabel.setTooltip(new Tooltip("The ID of the sensor package (also the ID of the xsens device)"));

		firmwareLabel= new Label(); 
		firmwareLabel.setTextAlignment(TextAlignment.CENTER);
		firmwareLabel.setAlignment(Pos.CENTER_LEFT);
//		firmwareLabel.setTooltip(new Tooltip("The current firmware version"));

		HBox sensorIDLabels = new HBox();
		sensorIDLabels.setSpacing(5);
		sensorIDLabels.setAlignment(Pos.BOTTOM_LEFT);
		sensorIDLabels.getChildren().addAll(sensorLabel, idLabel, firmwareLabel);
		
		leftHolder.getChildren().addAll(sensorIDLabels); 

		leftHolder.getChildren().addAll(createDataPane()); 
		
		Label advLabel = new  Label("Advanced Commands"); 
		advLabel.setPadding(new Insets(10,0,0,0));
		SensorView.titlelabel(advLabel);
		leftHolder.getChildren().add(advLabel); 

		leftHolder.getChildren().add(this.commandPane = new XmidCommandPane(sensorControl)); 

		sensorControl.addSensorMessageListener((sensormessage)->{
			setEulerData(sensormessage);
			setDataLabelData(sensormessage);
			sensorPane3D.setOrientationData(sensormessage); 
		});

		StackPane holderPane= new StackPane(); 
		holderPane.getChildren().add(sensorPane3D=new SensorPane3D());
		//holderPane.setMouseTransparent(true);

		Pane eulerPane= createEulerAnglesPane(); 
		eulerPane.setMouseTransparent(true);
		StackPane.setAlignment(eulerPane, Pos.TOP_LEFT);
		holderPane.getChildren().add(eulerPane);
		
		//need a scroll pane for low DPI screens. 
		ScrollPane scrollPane  = new ScrollPane(leftHolder); 
		scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);

		this.setCenter(holderPane);
		this.setLeft(scrollPane);
	}

	/**
	 * Get the sensor control associated with the pane. 
	 * @return the sensor control. 
	 */
	public SerialSensorControl getSensorControl() {
		return sensorControl;
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
		
				//STOP
				sensorControl.stopSerial();
				
				//need to give the thread just a little time to cancel so everything else works. 
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				setDataLabelsNull();
				sensorControl.notifyUpdate(SensorUpdate.SENSOR_STOP,null); 
				serialCommPane.getBaudComboBox().setDisable(false);
				serialCommPane.getPortComboBox().setDisable(false);

			}
			else {
				//START
				sensorControl.setParams(serialCommPane.getParams(this.sensorControl.getParams())); 
				sensorControl.startSerial();
				sensorControl.notifyUpdate(SensorUpdate.SENSOR_CONNECT,null); 
				serialCommPane.getBaudComboBox().setDisable(true);
				serialCommPane.getPortComboBox().setDisable(true);
			}
			
			updateStartButtonLabel(); 
		});


		Button deployButton = new Button("Deploy"); 
		deployButton.setOnAction((action)->{
			sensorControl.deploy(); 
			updateStartButtonLabel(); 
			setDataLabelsNull(); 
		}); 

		buttonHolder.getChildren().addAll( 
				startButton); 


		VBox vBox = new VBox(); 
		vBox.getChildren().addAll( buttonHolder);

		return vBox; 

	}
	
	/**
	 * Update the label on the settings button. 
	 */
	private void updateStartButtonLabel() {
		//System.out.println("isSerialRunning: " + sensorControl.isSerialRunning());
		if (sensorControl.isSerialRunning()) {
			startButton.setText("Stop");
		}
		else {
			startButton.setText("Connect");
		}
	}

	/**
	 * Create pane which shows incoming data
	 * @return
	 */
	private Pane createDataPane(){

		VBox dataPane= new VBox(); 
		dataPane.setSpacing(5); 
		
		
		BorderPane timeBox = new BorderPane(); 
		//timeBox.setSpacing(10);
		Label sensorTimelabel = new Label("Sensor time ");
		timeBox.setLeft(setLabelFontBold(sensorTimelabel)); 
		
		setTimeButton = new Button("Set"); 
		setTimeButton.setPrefWidth(BUTTON_WIDTH);
		setTimeButton.setOnAction((action)->{
			sensorControl.sendTimeMessage(); 
		});
		timeBox.setRight(setTimeButton); 
		setTimeButton.setDisable(true); 
		//timeBox.setAlignment(Pos.CENTER_LEFT);
		BorderPane.setAlignment(sensorTimelabel, Pos.CENTER_LEFT);
		
		dataPane.getChildren().add(timeBox); 
		dataPane.getChildren().add(timeLabel= new Label("-")); 
		
		

		dataPane.getChildren().add(setLabelFontBold(new Label("Orientation "))); 
		dataPane.getChildren().add(orientationLabel= new Label("-")); 

		dataPane.getChildren().add(setLabelFontBold(new Label("Pressure "))); 
		dataPane.getChildren().add(pressureLabel= new Label("-")); 

		dataPane.getChildren().add(setLabelFontBold(new Label("Temperature "))); 
		dataPane.getChildren().add(tempLabel= new Label("-")); 

		dataPane.getChildren().add(setLabelFontBold(new Label("Light "))); 
		dataPane.getChildren().add(RGBLabel= new Label("-")); 

		dataPane.getChildren().add(setLabelFontBold(new Label("Battery "))); 
		dataPane.getChildren().add(batLabel= new Label("-")); 
		

	
		BorderPane sdBox = new BorderPane(); 
		//sdBox.setSpacing(10);
		Label sdCardLabel = new Label("SD card: "); 
		sdBox.setLeft(setLabelFontBold(sdCardLabel)); 
		
		eraseButton = new Button("Erase"); 
		eraseButton.setPrefWidth(BUTTON_WIDTH);
		eraseButton.setOnAction((action)->{
			sensorControl.sendMessage(XsMessageID.XMID_ReqSDFormat); 
		});
		
		MDL2IconFont iconFont1 = new MDL2IconFont("\uE72C");
		refreshbutton = new Button(""); 
		refreshbutton.setGraphic(iconFont1);
		//refreshbutton.setPrefWidth(BUTTON_WIDTH);
		refreshbutton.setOnAction((action)->{
			sensorControl.sendMessage(XsMessageID.XMID_ReqSDSize); 
		});
		
		refreshbutton.setDisable(true);
		eraseButton.setDisable(true);

		HBox buttonBox = new HBox(); 
		buttonBox.setSpacing(5);
		buttonBox.getChildren().addAll(refreshbutton, eraseButton); 
		buttonBox.setAlignment(Pos.CENTER_LEFT);
		
		sdBox.setRight(buttonBox); 
		BorderPane.setAlignment(sdBox, Pos.CENTER_LEFT);

		dataPane.getChildren().add(sdBox); 
		
		dataPane.getChildren().add(sdLabel= new Label("-")); 


		return dataPane; 

	}
	
	/**
	 * Set label font to bold. 
	 * @param label - the label to set bold
	 * @return a bold label. 
	 */
	private Label setLabelFontBold(Label label) {
		//FIXME
		//label.setFont(Font.font("Sergio", FontWeight.BOLD, 16));
		//label.setStyle("-fx-font-weight: bold");
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
		
		
		if (sensormessage.timeMillis!=null) {
			setTimeButton.setDisable(false); 
			Date date = new Date(sensormessage.timeMillis.get());
			DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
			formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
			timeLabel.setText(formatter.format(date));
		}

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
			tempLabel.setText(String.format("%.2f ", sensormessage.temperature.get()) + DEGREE + "C"); 
		}

		if (sensormessage.pressure!=null) {
			pressureLabel.setText(String.format("%.3f ", sensormessage.pressure.get()) + "mbar"); 
		}

		
		if (sensormessage.lightSpectrum!=null) {
			//there may be two light sensors
			if (sensormessage.lightSpectrum.length==3)
			{			RGBLabel.setText("red: " + String.format("%.1f", sensormessage.lightSpectrum[0]) + " blue: "+String.format("%.1f", sensormessage.lightSpectrum[1])  + " green: "
					+String.format("%.1f", sensormessage.lightSpectrum[2]));
			}
			else {
				RGBLabel.setText("415nm: " + String.format("%.1f", sensormessage.lightSpectrum[0]) + " 445nm: "+String.format("%.1f", sensormessage.lightSpectrum[1])  + " 480nm: " +String.format("%.1f", sensormessage.lightSpectrum[2]) +
						"\n515nm: " + String.format("%.1f", sensormessage.lightSpectrum[3]) + " 555nm: "+String.format("%.1f", sensormessage.lightSpectrum[4])  + " 590nm: " +String.format("%.1f", sensormessage.lightSpectrum[5]) +
						"\n630nm: " + String.format("%.1f", sensormessage.lightSpectrum[6]) + " 680nm: "+String.format("%.1f", sensormessage.lightSpectrum[7])  
						+ "\nClear: " +String.format("%.1f", sensormessage.lightSpectrum[8]) + " NIR: " +String.format("%.1f", sensormessage.lightSpectrum[9]) );
			}
		}

		if (sensormessage.batteryLevel!=null) {
			double batteryLevel=sensormessage.batteryLevel.get(); 
			
			System.out.println("Battary level: " + sensormessage.batteryLevel + "  " + sensormessage.batteryLevelV);
			if (batteryLevel>98.) {
				batteryLevel=100.; 
			}
			batLabel.setText(String.format("%.2f ", batteryLevel) + "%: " + String.format("%.2fV", sensormessage.batteryLevelV.get()));
		}
		
		if (sensormessage.sdUsedSpace!=null) {
			refreshbutton.setDisable(false);
			eraseButton.setDisable(false);
			sdLabel.setText(String.format("%.3f of %.1f GB (%.2f%%) ", sensormessage.sdUsedSpace[0]/1000,  sensormessage.sdUsedSpace[1]/1000,  100*sensormessage.sdUsedSpace[0]/sensormessage.sdUsedSpace[1]));
		
		}
		
		if (sensormessage.deviceID!=null) {
			//bit of a HACK - really this should be in the serial sensor control because it does not directly realte ot viewing data but. 
			this.sensorControl.setSensorUID(new Long(sensormessage.deviceID.get())); 
			idLabel.setText(String.valueOf(sensormessage.deviceID.get())); 
			idLabel.setTooltip(new Tooltip("Device ID: "+ String.valueOf(sensormessage.deviceID.get())));

		}
		
		if (sensormessage.deviceType!=null) {
			this.sensorControl.getDeviceManager().setCurrentDevice(DeviceManager.getDeviceType(sensormessage.deviceType.get())); 
			this.sensorPane3D.setDeviceType(this.sensorControl.getDeviceManager().getCurrentDevice()); 
		}
		
		if (sensormessage.firmwareVersion!=null) {
			this.sensorControl.getDeviceManager().setCurrentFirmware(sensormessage.firmwareVersion.get()); 
			firmwareLabel.setText(sensormessage.firmwareVersion.get()); 
			this.firmwareLabel.setTooltip(new Tooltip("Firmware version: "+ sensormessage.firmwareVersion));
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
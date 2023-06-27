package layout;

import javafx.scene.layout.*;

import java.util.Set;
import java.util.TreeSet;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter; 


import comms.SerialUtils;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.animation.Timeline;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Callback;





import javafx.stage.FileChooser;
import layout.utils.GlowBackground2;
import main.SensorData;
import main.SensorsControl;
import main.SensorsControl.SensorUpdate;
import xsens.XsMessageID;
import javafx.geometry.Pos;

/**
 * Controls the ability to send all sensors commands. 
 * 
 * @author Jamie Macaulay
 *
 */
public class MasterCommPane extends BorderPane {

	private static final double MULTI_BUTTON_WIDTH = 110;

	/**
	 * Reference to the sensors control. 
	 */
	private SensorsControl sensorsControl;

	/**
	 * Button to set the time for all  the time for all. 
	 */
	private Button setTimeButton;

	private GridPane sensorNamePane;

	/**
	 * Button to request the current RTC time. 
	 */
	private Button reqTimeButton;

	/**
	 * Text area to show results from requesting a time. 
	 */
	private TableView<SensorData> reqTimeTable;

	/**
	 * Listener for all incoming messages from all sensors. 
	 */
	private MasterensorMessageListener sensorMessageListener;

	private ButtonBase saveButton;

	//	private ScrollPane reqTimeScrollpane; 



	public MasterCommPane(SensorsControl sensorsControl) {
		this.sensorsControl = sensorsControl; 

		VBox holder = new VBox(); 
		holder.setSpacing(5);
		holder.setPadding(new Insets(5,5,5,5));

		holder.setPrefWidth(MULTI_BUTTON_WIDTH+30);

		Label connectLabel = new  Label("Multi-sensor"); 
		SensorView.titlelabel(connectLabel);
		holder.getChildren().addAll(connectLabel); 

		holder.getChildren().add(sensorNamePane = new GridPane()); 
		sensorNamePane.setHgap(5);
		sensorNamePane.setVgap(5);
		sensorNamePane.setAlignment(Pos.CENTER_LEFT);

		holder.getChildren().add(setTimeButton
				= new Button("Set RTC Time"));
		setTimeButton.setPrefWidth(MULTI_BUTTON_WIDTH);

		//this.setCenter(new Label("Sensor Control"));

		setTimeButton.setOnAction((action->{
			sensorsControl.sendMessage(XsMessageID.XMID_SetRTCTime, null); 
		}));

		/****Time table***/

		reqTimeTable = new TableView<SensorData>();

		holder.getChildren().add(reqTimeButton
				= new Button("Req. RTC Time"));
		reqTimeButton.setPrefWidth(MULTI_BUTTON_WIDTH);

		reqTimeButton.setOnAction((action->{
			reqTimeTable.getItems().clear();
			sensorsControl.sendMessage(XsMessageID.XMID_ReqRTCTime, null); 
		}));

		//reqTimeTextFiled.setPrefWidth(500);
		//		reqTimeScrollpane.setPrefWidth(MULTI_BUTTON_WIDTH);
		//		reqTimeScrollpane.setPrefHeight(150);
		reqTimeTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		reqTimeTable.setMaxWidth(TableView.USE_PREF_SIZE);
		reqTimeTable.setPrefWidth(450);

		//make sure we can select multiple cells. 
		reqTimeTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		final KeyCodeCombination keyCodeCopy = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY);
		reqTimeTable.setOnKeyPressed(event -> {
			if (keyCodeCopy.match(event)) {
				copySelectionToClipboard(reqTimeTable);
			}
		});

		MenuItem item = new MenuItem("Copy");
		item.setOnAction((a) ->{
			copySelectionToClipboard(reqTimeTable);
		});
		ContextMenu menu = new ContextMenu();
		menu.getItems().add(item);
		reqTimeTable.setContextMenu(menu);


		//enable copying. 
		copySelectionToClipboard(reqTimeTable);


		//disable the save button if there are no items in the list. 
		reqTimeTable.getItems().addListener(new ListChangeListener<SensorData>() {
			@Override
			public void onChanged(Change<? extends SensorData> c) {
				enableControls();
			}
		});


		//reqTimeTable.setEditable(true);

		TableColumn<SensorData,  Number> deviceNameCol = new TableColumn<SensorData, Number>("Device ID");
		deviceNameCol.setCellValueFactory(p -> p.getValue().deviceID);



		TableColumn<SensorData, String>  pctimeCol = new TableColumn<SensorData, String>("PC time");
		pctimeCol.setCellValueFactory(new Callback<CellDataFeatures<SensorData, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<SensorData, String> p) {
				// p.getValue() returns the Person instance for a particular TableView row
				return new ReadOnlyObjectWrapper((SerialUtils.millis2StringDate(p.getValue().pcMillis.get(), "HH:mm:ss.SSS"))); 
			}
		});
		TableColumn<SensorData, String>  sensorTimeCol = new TableColumn<SensorData, String>("Sensor time");
		sensorTimeCol.setCellValueFactory(new Callback<CellDataFeatures<SensorData, String>, ObservableValue<String>>() {
			public ObservableValue<String> call(CellDataFeatures<SensorData, String> p) {
				//System.out.println("TIMEMILLIS: " + p.getValue().timeMillis); 
				// p.getValue() returns the Person instance for a particular TableView row
				return new ReadOnlyObjectWrapper((SerialUtils.millis2StringDate(p.getValue().timeMillis.get(), "HH:mm:ss.SSS"))); 
			}
		});

		TableColumn<SensorData, Number> timeDiffCol = new TableColumn<SensorData, Number>("Offset (millis)");
		timeDiffCol.setCellValueFactory(p -> p.getValue().pcMillis.subtract(p.getValue().timeMillis));


		reqTimeTable.getColumns().addAll(deviceNameCol, pctimeCol, sensorTimeCol, timeDiffCol);


		//reqTimeTextFiled.setPrefWidth(MULTI_BUTTON_WIDTH);

		sensorMessageListener=  new MasterensorMessageListener(); 
		updateSensorMessageListeners();

		holder.getChildren().add(reqTimeTable); 


		///add save button for convenience

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save timing");
		fileChooser.getExtensionFilters().addAll(new ExtensionFilter("All Files", "*.*"));

		saveButton = new Button("Save"); 
		saveButton.setOnAction((action)->{
			File saveFile = fileChooser.showSaveDialog(null);
			if (saveFile==null) return; 
			saveTimeData(saveFile, reqTimeTable); 
		});
		HBox saveButtonHolder = new HBox(); 
		saveButtonHolder.setAlignment(Pos.CENTER_RIGHT);
		saveButtonHolder.getChildren().add(saveButton); 
		saveButtonHolder.prefWidthProperty().bind(reqTimeTable.widthProperty());
		saveButtonHolder.maxWidthProperty().bind(reqTimeTable.widthProperty());

		holder.getChildren().add(saveButtonHolder); 

		this.setTop(holder);

		enableControls();
	}



	private void enableControls() {
		if (reqTimeTable.getItems().size() > 0) {
			saveButton.setDisable(false);
		} else {
			saveButton.setDisable(true);
		}
	}


	/**
	 * Set the RTC text. 
	 */
	public void setRTCText(SensorData sensormessage) {
		if (sensormessage.timeMillis==null) {
			System.err.println("MasterCommPane: No time associated with RTC message"); 
			return; 
		}
		for (int i=0; i<sensorsControl.getSensorControls().size(); i++) {
			if (sensormessage.sensorName.get().equals(sensorsControl.getSensorControls().get(i).getSensorName())) {
				//				//set so that multiple RTC messages don't caused multiple things added to tables...
				//				reqTimeTable.getItems().set(i, sensormessage); 

				//remove the correct row number., 
				//				final int ii=i+1; 

				//System.out.println("New table item: " + "TIME MILLIS: " + sensormessage.timeMillis + " PC MILLIS: " + sensormessage.pcMillis + " i " + i + " n items: "+ reqTimeTable.getItems().size()); 

				//if (reqTimeTable.getItems().size()>i) reqTimeTable.getItems().remove(i);
				reqTimeTable.getItems().add(sensormessage);

				//				reqTimeTextFiled.getChildren().removeIf(node -> GridPane.getRowIndex(node) == ii);
				//				reqTimeTextFiled.add(new Label( sensormessage.sensorName), 0, i+1);
				//				reqTimeTextFiled.add(computerTimeLabel, 1, i+1);
				//				reqTimeTextFiled.add(sensorTimeLabel, 2, i+1); 

				return;
			}
		}
		System.err.println("RTC message: could not find sensor name or there was a corrupt RTC message");
	}


	/**
	 * Notifies the MasterCommPane of any updates. 
	 * @param sensorUpdate - the update type. 
	 * @param dataObject - associated update object. 
	 */
	public void notifyUpdate(SensorUpdate sensorUpdate, Object dataObject) {
		Platform.runLater(()->{
			updateNamePane(); 
			updateSensorMessageListeners(); 
		});
	}


	/**
	 * Update the listeners for sensor messages. 
	 */
	private void updateSensorMessageListeners() {
		sensorsControl.removeSensorMessageListener(sensorMessageListener); 
		sensorsControl.addSensorsMessageListener(sensorMessageListener); 
	}


	/**
	 * Called whenever a new message is received from any device. 
	 * @param sensormessage - the new sensor message. 
	 */
	private void newMasterSensorMessage(SensorData sensormessage) {
		//System.out.println("Sensormessage flag: " + sensormessage.flag); 
		switch (sensormessage.flag) {
		case RTCACK:
			setRTCText(sensormessage); 
			break;
		case DEVICEID:
			updateNamePane();
			break;
		default:
			//do nothing
			break;
		}
	}


	/**
	 * Update the pane that shows active sensor packages. 
	 */
	private void updateNamePane() {
		sensorNamePane.getChildren().clear();
		//System.out.print("NO. SENSOR CONTROLS: " +sensorsControl.getSensorControls().size());
		for (int i=0; i<sensorsControl.getSensorControls().size(); i++) {
			//System.out.print("SENSOR: " +sensorControl.getSensorControls().get(i).getSensorName());

			Label label;
			if (sensorsControl.getSensorControls().get(i).getSensorUID()!=null) {
				label = new Label(String.format("%d",sensorsControl.getSensorControls().get(i).getSensorUID())); 
			}
			else {
				label = new Label(sensorsControl.getSensorControls().get(i).getSensorName()); 
			}

			Circle circle = new Circle(10); 

			GlowBackground2 glow;
			if (sensorsControl.getSensorControls().get(i).isConnected()) {
				glow =  new GlowBackground2(circle, Color.DARKGREEN, Color.YELLOWGREEN, 100);
			}
			else{
				glow =  new GlowBackground2(circle, Color.DARKRED, Color.RED, 100);
			}
			glow.getTimeline().setAutoReverse(true);
			glow.getTimeline().rateProperty().set(0.2);
			glow.getTimeline().setCycleCount(Timeline.INDEFINITE);
			glow.getTimeline().play(); 

			sensorNamePane.add(label, 0, i);
			sensorNamePane.add(circle, 1, i);
		}
	}


	/**
	 * Listens for sensor messages for the MasterCommPane. 
	 * @author Jamie Macaulay
	 *
	 */
	class MasterensorMessageListener implements SensorMessageListener {
		@Override
		public void newSensorMessage(SensorData sensorComms) {
			newMasterSensorMessage(sensorComms); 
		}
	}


	public void copySelectionToClipboard(final TableView<?> table) {
		final ClipboardContent clipboardContent = new ClipboardContent();
		clipboardContent.putString(table2String( table, true));
		Clipboard.getSystemClipboard().setContent(clipboardContent);
	}


	private void saveTimeData(File saveFile, final TableView<?> table) {

		if (saveFile == null) {
			System.err.println("The save file was null"); 
		}

		String data = table2String(table, false);

		System.out.println("TABLE DATA"); 
		System.out.println(data);

		File saveTxtFile = changeExtension(saveFile,  ".txt");


		try (PrintWriter out = new PrintWriter(saveTxtFile)) {
			out.println(data);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	private String table2String(final TableView<?> table, boolean selected) {
		final Set<Integer> rows = new TreeSet<>();
		if (selected) {
			for (final TablePosition tablePosition : table.getSelectionModel().getSelectedCells()) {
				rows.add(tablePosition.getRow());
			}
		}
		else {
			for (int i=0; i< table.getItems().size(); i++) {
				rows.add(i);
			}
		}
		final StringBuilder strb = new StringBuilder();
		boolean firstRow = true;
		for (final Integer row : rows) {
			if (!firstRow) {
				strb.append('\n');
			}
			firstRow = false;
			boolean firstCol = true;
			for (final TableColumn<?, ?> column : table.getColumns()) {
				if (!firstCol) {
					strb.append('\t');
				}
				firstCol = false;
				final Object cellData = column.getCellData(row);
				strb.append(cellData == null ? "" : cellData.toString());
			}
		}
		return strb.toString();
	}

	public static File changeExtension(File f, String newExtension) {


		int i = f.getName().lastIndexOf('.');

		String name;
		if (i!=-1) {
			name = f.getName().substring(0,i);
		}
		else {
			name = f.getName();

		}
		return new File(f.getParent(), name + newExtension);
	}
	//	
	//	/**
	//	 * Holds data on the RTC time to presented in a table. 
	//	 * @author Jamie Macaulay 
	//	 *
	//	 */
	//	class RTCTimeData {
	//		
	//		SimpleStringProperty deviceName; 
	//		
	//		SimpleLongProperty pcTime; 
	//		
	//		SimpleLongProperty sensorTime; 
	//
	//		
	//		/**
	//		 * Constructor for an RTC time. 
	//		 * @param deviceName
	//		 * @param pcTime
	//		 * @param sensorTime
	//		 */
	//		RTCTimeData(String deviceName, long pcTime, long sensorTime){
	//			
	//		}
	//		
	//		
	//	     public void setDeviceName(String value) { lastNameProperty().set(value); }
	//	     
	//	     public void setPCTime(String value) { lastNameProperty().set(value); }
	//	     
	//	     public void set(String value) { lastNameProperty().set(value); }
	//		
	//	}

}

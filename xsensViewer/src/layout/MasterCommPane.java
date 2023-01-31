package layout;

import javafx.scene.layout.*;

import java.util.Set;
import java.util.TreeSet;

import comms.SerialUtils;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
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
import javafx.util.Callback;
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
		
		holder.getChildren().add(reqTimeButton
		= new Button("Req. RTC Time"));
		reqTimeButton.setPrefWidth(MULTI_BUTTON_WIDTH);
		
		reqTimeButton.setOnAction((action->{
			reqTimeTable.getItems().clear();
			sensorsControl.sendMessage(XsMessageID.XMID_ReqRTCTime, null); 
		}));
		
		holder.getChildren().add(reqTimeTable = new TableView<SensorData>()); 
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

		//reqTimeTable.setEditable(true);
		   
	    TableColumn<SensorData, String> deviceNameCol = new TableColumn<SensorData, String>("Device");
	    deviceNameCol.setCellValueFactory(p -> p.getValue().sensorName);
	   

	
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
		
	
		this.setTop(holder);
	}
	
	
	/**
	 * Set the RTC text. 
	 */
	public void setRTCText(SensorData sensormessage) {
		for (int i=0; i<sensorsControl.getSensorControls().size(); i++) {
			if (sensormessage.sensorName.get().equals(sensorsControl.getSensorControls().get(i).getSensorName())) {
								
			
				//remove the correct row number., 
//				final int ii=i+1; 
				
				System.out.println("New table item: " + sensormessage.pcMillis + " i " + i + " n items: "+ reqTimeTable.getItems().size()); 
				
				//if (reqTimeTable.getItems().size()>i) reqTimeTable.getItems().remove(i);
				reqTimeTable.getItems().add(sensormessage);

//				reqTimeTextFiled.getChildren().removeIf(node -> GridPane.getRowIndex(node) == ii);
//				reqTimeTextFiled.add(new Label( sensormessage.sensorName), 0, i+1);
//				reqTimeTextFiled.add(computerTimeLabel, 1, i+1);
//				reqTimeTextFiled.add(sensorTimeLabel, 2, i+1); 
				
				return;
			}
		}
		System.err.println("RTC message: could not find sensor name?");
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

		case RTC:
			setRTCText(sensormessage); 
			break;
		default:
			//do nothing
			break;
		}
	}


	/***
	 * Update the pane that shows active sensor packages. 
	 */
	private void updateNamePane() {
		sensorNamePane.getChildren().clear();
		//System.out.print("NO. SENSOR CONTROLS: " +sensorsControl.getSensorControls().size());
		for (int i=0; i<sensorsControl.getSensorControls().size(); i++) {
			//System.out.print("SENSOR: " +sensorControl.getSensorControls().get(i).getSensorName());
			Label label = new Label(sensorsControl.getSensorControls().get(i).getSensorName()); 

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
	    final Set<Integer> rows = new TreeSet<>();
	    for (final TablePosition tablePosition : table.getSelectionModel().getSelectedCells()) {
	        rows.add(tablePosition.getRow());
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
	    final ClipboardContent clipboardContent = new ClipboardContent();
	    clipboardContent.putString(strb.toString());
	    Clipboard.getSystemClipboard().setContent(clipboardContent);
	   
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

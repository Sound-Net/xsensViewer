package layout;

import main.SensorsControl;
import main.SensorsControl.SensorUpdate;
import main.SerialSensorControl;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.JMetroStyleClass;
import jfxtras.styles.jmetro.MDL2IconFont;
import jfxtras.styles.jmetro.Style;
import layout.utils.PamTabFX;
import layout.utils.PamTabPane;

import java.util.ArrayList;

import javafx.concurrent.Task;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;


/**
 * The current sensor view. Shows the sensors that are currently viewed.  
 * 
 * @author Jamie Macaulay
 *
 */
public class SensorView extends BorderPane {
	
	public static enum LayoutType {TABBED_LAYOUT, TILED_LAYOUT}; 
	
	
	/**
	 * The current layout. 
	 */
	public LayoutType currentLayout = LayoutType.TABBED_LAYOUT; 

	/**
	 * Reference to the sensor control.
	 */
	private SensorsControl sensorsControl;
	private PamTabPane tabPane;
	private MasterCommPane masterCommPane;
	
	/**
	 * A list of the current serial sensor panes. Note each pane will have a corresponding control 
	 * in SensorsControl. 
	 */
	private ArrayList<SerialSensorPane> serialSensorPanes = new ArrayList<SerialSensorPane>();
	
	/**
	 * The main split pane. 
	 */
	private SplitPane splitPane;

	private BorderPane devicePaneHolder;

	private BorderPane tiledPane;

	private GridPane deviceTiledPane;

	public SensorView(SensorsControl sensorControl) {
		
		
		this.sensorsControl = sensorControl; 
	
		/***Create the tab pane****/
		
		tabPane = new PamTabPane(Side.TOP);
		tabPane.getAddTabButton().setOnAction((value)->{
			
			//TODO - may need option to add another type pof communication e.g.. USB in future. 
			SerialSensorPane serialSensorPane =  addSerialSensorPane();
			
			addSensorTab("Sensor " + tabPane.getTabs().size(), serialSensorPane); 
		});
		
		MDL2IconFont iconFont1 = new MDL2IconFont("\uE948");
		
		tabPane.getAddTabButton().setGraphic(iconFont1);
		tabPane.getAddTabButton().setPrefWidth(90);
		
		//create a button to show the slides
		
		MDL2IconFont iconFontTab = new MDL2IconFont("\uE8A9");
		iconFontTab.setSize(20);

		Button tabChangeButton = new Button(); 
		tabChangeButton.setMinSize(60,40);
		tabChangeButton.setGraphic(iconFontTab);
		tabChangeButton.setTooltip(new Tooltip("Switch between tab or tiled layout"));
		tabChangeButton.setOnAction((action->{
			
//			//switch the layout flag between tab and tiled
//			if (currentLayout==LayoutType.TABBED_LAYOUT)
//				currentLayout= LayoutType.TILED_LAYOUT;
//			else currentLayout= LayoutType.TABBED_LAYOUT;

			setLayoutType(LayoutType.TILED_LAYOUT); 
		}));
		
		tabPane.setTabEndRegion(tabChangeButton);
		
		/***Create the tiled pane***/
		
		
		MDL2IconFont iconFontTiled = new MDL2IconFont("\uEFA5");
		iconFontTiled.setSize(20);
		iconFontTiled.setRotate(180);
		
		tiledPane = new BorderPane(); 
		Button tileChange = new Button(); 
		tileChange.setMinSize(60,40);
		tileChange.setGraphic(iconFontTiled);
		tileChange.setTooltip(new Tooltip("Switch between tab or tiled layout"));
		tileChange.setOnAction((action->{
			
			setLayoutType(LayoutType.TABBED_LAYOUT); 
		}));
		
		
	    deviceTiledPane = new GridPane();
	    deviceTiledPane.setHgap(5);
	    deviceTiledPane.setVgap(5);
//	    deviceTiledPane.setPrefColumns(2);
	    
	    
	    tiledPane.setCenter(deviceTiledPane);
	    tiledPane.setTop(tileChange);
	    BorderPane.setAlignment(tileChange, Pos.TOP_RIGHT);

		/***************************/

		devicePaneHolder = new BorderPane(); 
	
		splitPane = new SplitPane(); 
        splitPane.getItems().addAll(devicePaneHolder, masterCommPane = new MasterCommPane(sensorControl));
        splitPane.setDividerPosition(0, 0.8);
		
		this.setCenter(splitPane);
//		this.setRight(masterCommPane = new MasterCommPane(sensorControl));
		
        //addSensorTab(0); 
	
		tabPane.layout();	
		setLayoutType(LayoutType.TABBED_LAYOUT); 
		


	}
	
	
	/**
	 * Layout the device pane according to the layout flag. 
	 * @param currentLayout - the layout flag indicating how the pane should look. 
	 */
	private void setLayoutType(LayoutType currentLayout2) {
		//need to clear all the current panes. 
		tabPane.getTabs().clear();
		
		Node pane = null;
		switch (currentLayout2) {
		case TABBED_LAYOUT:
			pane = layoutTabPane() ; 
			break;
		case TILED_LAYOUT:
			pane = layoutTiledPane() ; 
			break;
		default:
			break;
		}
		
		devicePaneHolder.setCenter(pane);
	}
	
	
	/**
	 * Layout the tab pane. 
	 */
	private Node layoutTabPane() {
		 for (int i = 0; i < serialSensorPanes.size(); i++) {
			 addSensorTab("Sensor " + tabPane.getTabs().size(), serialSensorPanes.get(i) ); 
		 }
		 if (serialSensorPanes.size()==0) {
			 addSerialSensorPane();
			 addSensorTab("Sensor " + 0, serialSensorPanes.get(0)); 
			 System.out.println("layoutTabPane: " +serialSensorPanes.size() );
		 }
		

		return tabPane; 
		
	}
	
	/**
	 * Layout the tiled pane. 
	 */
	private Node layoutTiledPane() {
		 for (int i = 0; i < serialSensorPanes.size(); i++) {
			 deviceTiledPane.add(serialSensorPanes.get(i),i%2, (int) Math.floor(i/2));
			 
			 GridPane.setHgrow(serialSensorPanes.get(i), Priority.ALWAYS);

		 }
			
		return tiledPane; 
	}


	/**
	 * Add a sensor tab
	 * @param nTabs
	 */
	public void addSensorTab(String name, 	SerialSensorPane serialSensorPane ) {
		
		PamTabFX pamTabFX = new PamTabFX((name)); 
		
		pamTabFX.setContent(serialSensorPane);
		
		
		pamTabFX.setDetachable(true);
		if (tabPane.getTabs().size()  < 1) {
			pamTabFX.setClosable(false);
		}
		
		tabPane.getTabs().add(pamTabFX); 
		
		pamTabFX.setOnClosed((value)->{
			//sensorControl.removeSensorControl(	serialSensorPane.getSensorControl());
			 Task<Boolean> task = new Task<Boolean>() {
		         @Override 
		         protected Boolean call() throws Exception {
		        	 return removeSerialSensorPane((SerialSensorPane) pamTabFX.getContent());
		         }
		     };
	         Thread th = new Thread(task);
	         th.setDaemon(true);
	         th.start();
	         
	         this.tabPane.layout();
		 
			//removeSerialSensorPane((SerialSensorPane) pamTabFX.getContent());
		});
	}
	

	/**
	 * Remove a sensor pane- this also closes the sensor associated with the pane. 
	 * @param serialSensorPane - the tab ID to remove. 
	 * @return 
	 */
	public boolean removeSerialSensorPane(SerialSensorPane serialSensorPane) {
		sensorsControl.notifyUpdate(SensorUpdate.SENSOR_STOP, serialSensorPane.getSensorControl()); 
		
		
		serialSensorPane.getSensorControl().stop();
		serialSensorPane.getSensorControl().notifyUpdate(SensorUpdate.SENSOR_STOP,null); 

		sensorsControl.getSensorControls().remove(serialSensorPane.getSensorControl());
		
		return serialSensorPanes.remove(serialSensorPane); 
	}
	
	/**
	 * Remove a sensor pane- this also closes the sensor associated with the pane. 
	 * @param i - the tab ID to remove. 
	 * @return 
	 */
	public SerialSensorPane removeSerialSensorPane(int i) {
		
		SerialSensorPane sensorpane = serialSensorPanes.get(i);
		boolean remove = removeSerialSensorPane(sensorpane);
		
		
//		sensorsControl.notifyUpdate(SensorUpdate.SENSOR_STOP, serialSensorPanes.get(i).getSensorControl()); 
//		serialSensorPanes.get(i).getSensorControl().stop();
//		serialSensorPanes.get(i).getSensorControl().notifyUpdate(SensorUpdate.SENSOR_STOP,null); 
//		sensorsControl.getSensorControls().remove(serialSensorPanes.get(i).getSensorControl());
		
		return remove ? sensorpane : null; 
	}
	
	/**
	 * Create a new serial sensor pane. This also create a control associated with the pane. 
	 * @return a new serial sensor pane. 
	 */
	public SerialSensorPane addSerialSensorPane() {
		SerialSensorControl asensorControl; 
		SerialSensorPane serialSensorPane = new SerialSensorPane(asensorControl = new SerialSensorControl(sensorsControl));
		serialSensorPanes.add(serialSensorPane); 
		serialSensorPane.getSensorControl().addSensorUpdateListener((sensorUpdate, dataObject)->{
			sensorsControl.notifyUpdate(sensorUpdate, dataObject); 
			notifyUpdate(sensorUpdate, dataObject); 
		});
		sensorsControl.addSensorControl(	serialSensorPane.getSensorControl());
		
//		System.out.println("ADD SERIAL SENSOR PANE: " + serialSensorPanes.size());

		return serialSensorPane; 
	}
	
	
	public class SensorTab extends PamTabFX {
		
		/**
		 * The serial sensor control. 
		 */
		private SerialSensorControl sensorControl;
		
		public SerialSensorControl getSensorControl() {
			return sensorControl;
		}

		/**
		 * The serial sensor pane
		 */
		private SerialSensorPane serialSensorPane;

		public SensorTab(String name, SerialSensorControl sensorControl) {
			super(name);
			this.sensorControl = sensorControl; 
			this.setContent(serialSensorPane = new SerialSensorPane(sensorControl));
		}
		
	}

	public PamTabPane getTabbedPane() {
		return tabPane;
	}; 
	
	public static void setTheme(Scene scene, Pane root) {
		JMetro jMetro = new JMetro(Style.DARK);
		jMetro.setScene(scene);
		root.getStyleClass().add(JMetroStyleClass.BACKGROUND);
	}
	
	public static Label titlelabel(Label label) {
		label.setStyle("-fx-font-weight: bold; -fx-font-size: 16px");

		return label; 
	}

	
	/**
	 * Called whenever there is an update from one of the sensor panes. 
	 * @param sensorUpdate - the sensor update
	 * @param dataObject the data associated with the update. 
	 */
	private void notifyUpdate(SensorUpdate sensorUpdate, Object dataObject) {
		masterCommPane.notifyUpdate(sensorUpdate, dataObject); 
	}





}

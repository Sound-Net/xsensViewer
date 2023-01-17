package layout;

import main.SensorsControl;
import main.SensorsControl.SensorUpdate;
import main.SerialSensorControl;
import javafx.scene.layout.BorderPane;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.JMetroStyleClass;
import jfxtras.styles.jmetro.MDL2IconFont;
import jfxtras.styles.jmetro.Style;
import layout.utils.PamTabFX;
import layout.utils.PamTabPane;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Pane;


/**
 * The current sensor view. Shows the sensors that are currently viewed.  
 * 
 * @author Jamie Macaulay
 *
 */
public class SensorView extends BorderPane {

	/**
	 * Reference to the sensor control.
	 */
	private SensorsControl sensorControl;
	private PamTabPane tabPane;
	private MasterCommPane masterCommPane;

	public SensorView(SensorsControl sensorControl) {
		
		
		this.sensorControl = sensorControl; 
	
		
		tabPane = new PamTabPane(Side.TOP);
		
		SplitPane splitPane = new SplitPane(); 
        splitPane.getItems().addAll(tabPane, masterCommPane = new MasterCommPane(sensorControl));
        splitPane.setDividerPosition(0, 0.8);
		
		this.setCenter(splitPane);
//		this.setRight(masterCommPane = new MasterCommPane(sensorControl));

		
		tabPane.getAddTabButton().setOnAction((value)->{
			addSensorTab(tabPane.getTabs().size()+1); 
		});
		
		MDL2IconFont iconFont1 = new MDL2IconFont("\uE948");
		
		tabPane.getAddTabButton().setGraphic(iconFont1);
		tabPane.getAddTabButton().setPrefWidth(90);
        //addSensorTab(0); 
	

		tabPane.layout();
		
			
	}
	
	
	/**
	 * Add a sensor tab
	 * @param nTabs
	 */
	public void addSensorTab(int nTabs) {
		
		PamTabFX pamTabFX = new PamTabFX(("Sensor " + nTabs)); 
		
		//TODO - may need option to add another type pof communication e.g.. USB in future. 
		SerialSensorControl asensorControl; 
		pamTabFX.setContent(new SerialSensorPane(asensorControl = new SerialSensorControl(sensorControl)));
		
		asensorControl.addSensorUpdateListener((sensorUpdate, dataObject)->{
			sensorControl.notifyUpdate(sensorUpdate, dataObject); 
			notifyUpdate(sensorUpdate, dataObject); 
		});
		
		pamTabFX.setDetachable(true);
		if (nTabs <= 1) {
			pamTabFX.setClosable(false);
		}
		
		tabPane.getTabs().add(pamTabFX); 
		sensorControl.addSensorControl(asensorControl);
		
		pamTabFX.setOnClosed((value)->{
			sensorControl.removeSensorControl(asensorControl);
		});
	}
	

	/**
	 * Remove a sensor tab- this also closes the sensor associated with the tab. 
	 * @param i - the tab ID to remove. 
	 */
	public void removeSensorTab(int i) {
		sensorControl.notifyUpdate(SensorUpdate.SENSOR_STOP, ((SensorTab) tabPane.getTabs().get(i)).getSensorControl()); 
		((SensorTab) tabPane.getTabs().get(i)).getSensorControl().stop();
		
		tabPane.getTabs().remove(i);
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

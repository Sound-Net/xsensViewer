package layout;

import main.SensorsControl;
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
		this.setCenter(tabPane);
		this.setRight(masterCommPane = new MasterCommPane(sensorControl));

		
		tabPane.getAddTabButton().setOnAction((value)->{
			addSensorTab(tabPane.getTabs().size()+1); 
		});
		
		MDL2IconFont iconFont1 = new MDL2IconFont("\uE948");
		
		tabPane.getAddTabButton().setGraphic(iconFont1);
		tabPane.getAddTabButton().setPrefWidth(90);
        addSensorTab(0); 

		
		tabPane.layout();
		
		
	
	}
	
	
	/**
	 * Add a sensor tab
	 * @param nTabs
	 */
	public void addSensorTab(int nTabs) {
		
		PamTabFX pamTabFX = new PamTabFX(("Sensor " + nTabs)); 
		
		//TODO - may need option to add another type pof communication e.g.. USB in future. 
		pamTabFX.setContent(new SerialSensorPane(new SerialSensorControl()));
		
		pamTabFX.setDetachable(true);
		if (nTabs <= 1) {
			pamTabFX.setClosable(false);
		}
		
		tabPane.getTabs().add(pamTabFX); 
	}
	
	public class SensorTab extends PamTabFX {
		
		/**
		 * The serial sensor control. 
		 */
		private SerialSensorControl sensorControl;
		
		/**
		 * The serial sensor pane
		 */
		private SerialSensorPane serialSensorPane;

		public SensorTab(String name, SerialSensorControl sensorControl) {
			super(name);
			this.sensorControl = sensorControl; 
			this.setContent(serialSensorPane = new SerialSensorPane(new SerialSensorControl()));
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

}

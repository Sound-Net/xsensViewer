package layout;

import main.SensorControl;
import main.SensorsControl;
import main.SensorsControl.SensorUpdate;
import main.SerialSensorControl;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;

import com.pixelduke.transit.Style;
import com.pixelduke.transit.TransitStyleClass;
import com.pixelduke.transit.TransitTheme;

import layout.utils.Icons;
import layout.utils.PamTabFX;
import layout.utils.PamTabPane;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.stage.WindowEvent;


/**
 * The current sensor view. Shows the sensors that are currently viewed.  
 * 
 * @author Jamie Macaulay
 *
 */
public class SensorView extends BorderPane {
	
	public static enum LayoutType {TABBED_LAYOUT, TILED_LAYOUT}; 
	
	/** Style class carrying this application's own dark-mode colours. */
	private static final String DARK_CLASS = "dark";
	
	private static final String DARK_MODE_KEY = "darkMode";
	
	/** Remembers the light/dark choice between runs. */
	private static final Preferences preferences = Preferences.userNodeForPackage(SensorView.class);
	
	/** Size of the square buttons that sit in the top right corner of a layout. */
	private static final double CORNER_BUTTON_WIDTH = 60;
	
	private static final double CORNER_BUTTON_HEIGHT = 40;
	
	
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

	/**
	 * The light/dark switch. Lives in the top right corner of whichever layout is
	 * showing, so it is moved between the two corner boxes below.
	 */
	private Button darkModeButton;

	/** Corner of the tab strip - holds the layout button and the dark mode switch. */
	private HBox tabCorner;

	/** Corner of the tiled layout - the same two controls. */
	private HBox tiledCorner;

	public SensorView(SensorsControl sensorControl) {
		
		
		this.sensorsControl = sensorControl; 
	
		/***Create the tab pane****/
		
		tabPane = new PamTabPane(Side.TOP);
		tabPane.getAddTabButton().setOnAction((value)->{
			
			//TODO - may need option to add another type pof communication e.g.. USB in future. 
			SerialSensorPane serialSensorPane =  addSerialSensorPane();
			
			addSensorTab("Sensor " + tabPane.getTabs().size(), serialSensorPane); 
		});
		
		tabPane.getAddTabButton().setGraphic(Icons.add());
		tabPane.getAddTabButton().getStyleClass().add("icon-button");
		tabPane.getAddTabButton().setPrefWidth(90);
		
		//create a button to show the slides
		
		Button tabChangeButton = new Button(); 
		tabChangeButton.getStyleClass().add("icon-button");
		tabChangeButton.setMinSize(CORNER_BUTTON_WIDTH, CORNER_BUTTON_HEIGHT);
		tabChangeButton.setGraphic(Icons.tiles());
		tabChangeButton.setTooltip(new Tooltip("Switch between tab or tiled layout"));
		tabChangeButton.setOnAction((action->{
			
//			//switch the layout flag between tab and tiled
//			if (currentLayout==LayoutType.TABBED_LAYOUT)
//				currentLayout= LayoutType.TILED_LAYOUT;
//			else currentLayout= LayoutType.TABBED_LAYOUT;

			setLayoutType(LayoutType.TILED_LAYOUT); 
		}));
		
		darkModeButton = createDarkModeButton();

		// The skin measures the end region from its min/pref width, so this cannot
		// be left to the HBox to compute.
		tabCorner = corner(tabChangeButton);
		tabCorner.setMinWidth(CORNER_BUTTON_WIDTH * 2);
		tabPane.setTabEndRegion(tabCorner);
		
		/***Create the tiled pane***/
		
		
		tiledPane = new BorderPane(); 
		Button tileChange = new Button(); 
		tileChange.getStyleClass().add("icon-button");
		tileChange.setMinSize(CORNER_BUTTON_WIDTH, CORNER_BUTTON_HEIGHT);
		tileChange.setGraphic(Icons.tabs());
		tileChange.setTooltip(new Tooltip("Switch between tab or tiled layout"));
		tileChange.setOnAction((action->{
			
			setLayoutType(LayoutType.TABBED_LAYOUT); 
		}));
		
		
	    deviceTiledPane = new GridPane();
	    deviceTiledPane.setHgap(5);
	    deviceTiledPane.setVgap(5);
//	    deviceTiledPane.setPrefColumns(2);
	    
	    
	    tiledPane.setCenter(deviceTiledPane);
	    tiledPane.setTop(tiledCorner = corner(tileChange));
	    BorderPane.setAlignment(tiledCorner, Pos.TOP_RIGHT);

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
	 * A corner box for the top right of a layout, holding the dark mode switch and
	 * that layout's button for swapping to the other layout.
	 *
	 * <p>The application used to carry a title and a one line description across
	 * the top of the window. That cost a strip of height on every window for text
	 * that only repeated the title bar, so the dark mode switch was moved into the
	 * empty right hand end of the tab strip instead, where it costs nothing.
	 *
	 * @param layoutButton - the layout button for this layout.
	 * @return the corner box. The dark mode switch is added by
	 *         {@link #setLayoutType(LayoutType)}, which owns which layout is showing.
	 */
	private HBox corner(Button layoutButton) {
		HBox box = new HBox(layoutButton);
		box.setAlignment(Pos.CENTER_RIGHT);
		return box;
	}


	/**
	 * The light/dark switch - a sun or a moon, matching the other quiet icon
	 * buttons in the corner rather than reading as a primary action.
	 *
	 * @return the switch.
	 */
	private Button createDarkModeButton() {
		Button button = new Button();
		button.getStyleClass().add("icon-button");
		button.setMinSize(CORNER_BUTTON_WIDTH, CORNER_BUTTON_HEIGHT);
		showDarkModeState(button, isDarkMode());
		button.setOnAction(action -> {
			boolean dark = !isDarkMode();
			setDarkMode(dark);
			showDarkModeState(button, dark);
		});
		return button;
	}


	/** Points the switch at the theme it would move to, not the one in use. */
	private static void showDarkModeState(Button button, boolean dark) {
		button.setGraphic(dark ? Icons.sun() : Icons.moon());
		button.setTooltip(new Tooltip(dark ? "Switch to the light theme" : "Switch to the dark theme"));
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
			//only one layout is on screen at a time, so the switch moves with it.
			showDarkModeButtonIn(tabCorner);
			break;
		case TILED_LAYOUT:
			pane = layoutTiledPane() ; 
			showDarkModeButtonIn(tiledCorner);
			break;
		default:
			break;
		}
		
		devicePaneHolder.setCenter(pane);
	}
	
	
	/**
	 * Move the dark mode switch into the corner of the layout that is showing,
	 * ahead of that layout's own button.
	 * 
	 * @param cornerBox - the corner box of the layout being shown. 
	 */
	private void showDarkModeButtonIn(HBox cornerBox) {
		tabCorner.getChildren().remove(darkModeButton);
		tiledCorner.getChildren().remove(darkModeButton);
		cornerBox.getChildren().add(0, darkModeButton);
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
		 
		 for (int i=0; i< deviceTiledPane.getColumnConstraints().size(); i++) {
			 deviceTiledPane.getColumnConstraints().get(i).setPercentWidth(1./serialSensorPanes.size()/2);
		 }

		 for (int i=0; i< deviceTiledPane.getRowConstraints().size(); i++) {
			 deviceTiledPane.getRowConstraints().get(i).setPercentHeight(.1/serialSensorPanes.size());
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
			// send update to the sensors control
			sensorsControl.notifyUpdate(sensorUpdate, dataObject); 
			notifyUpdate(sensorUpdate, dataObject); 
		});
		sensorsControl.addSensorControl(serialSensorPane.getSensorControl());
		
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
	
	/**
	 * A scene that has been given the theme, held so that flipping the dark mode
	 * switch restyles detached sensor windows as well as the main one.
	 */
	private record ThemedScene(TransitTheme theme, Pane root) { }

	private static final List<ThemedScene> themedScenes = new ArrayList<ThemedScene>();

	/**
	 * Applies the shared SoundNet theme to a scene.
	 *
	 * <p>Transit supplies the control styling and {@code style.css} layers this
	 * application's own colours on top. Both are shared with the SoundNet
	 * Firmware Updater, so the two applications look like one product.
	 *
	 * @param scene - the scene to theme.
	 * @param root - the root pane of that scene, which paints the background.
	 */
	public static void setTheme(Scene scene, Pane root) {
		TransitTheme theme = new TransitTheme(scene, currentStyle());
		// Loaded after the theme so these rules win over it.
		scene.getStylesheets().add(SensorView.class.getResource("/style.css").toExternalForm());
		// Transit paints the window background through this style class.
		root.getStyleClass().add(TransitStyleClass.BACKGROUND);

		ThemedScene themed = new ThemedScene(theme, root);
		themedScenes.add(themed);
		applyStyle(themed, isDarkMode());

		// Detached windows come and go; stop holding onto their scene graphs once
		// they are closed. WINDOW_HIDDEN rather than setOnHiding, because
		// PamTabFX already uses that property on its detached stages.
		scene.windowProperty().addListener((obs, oldWindow, window) -> {
			if (window != null) {
				window.addEventHandler(WindowEvent.WINDOW_HIDDEN,
						e -> themedScenes.remove(themed));
			}
		});
	}

	/** Whether the user last left the application in dark mode. Remembered between runs. */
	public static boolean isDarkMode() {
		return preferences.getBoolean(DARK_MODE_KEY, true);
	}

	/** Switches every themed window between the light and dark styles, and remembers it. */
	public static void setDarkMode(boolean dark) {
		preferences.putBoolean(DARK_MODE_KEY, dark);
		for (ThemedScene themed : themedScenes) {
			applyStyle(themed, dark);
		}
	}

	private static void applyStyle(ThemedScene themed, boolean dark) {
		themed.theme().setStyle(dark ? Style.DARK : Style.LIGHT);
		// Transit's dark style does not restate the Modena text colours, so this
		// application's own text needs a matching set of rules in style.css.
		themed.root().getStyleClass().removeIf(DARK_CLASS::equals);
		if (dark) {
			themed.root().getStyleClass().add(DARK_CLASS);
		}
	}

	private static Style currentStyle() {
		return isDarkMode() ? Style.DARK : Style.LIGHT;
	}

	/**
	 * Marks a label as the heading of a section, matching the numbered headings
	 * in the firmware updater.
	 *
	 * @param label - the label to style.
	 * @return the same label.
	 */
	public static Label titlelabel(Label label) {
		label.getStyleClass().add("section-heading");
		return label; 
	}

	
	/**
	 * Called whenever there is an update from one of the sensor panes. 
	 * @param sensorUpdate - the sensor update
	 * @param dataObject the data associated with the update. 
	 */
	private void notifyUpdate(SensorUpdate sensorUpdate, Object dataObject) {
		masterCommPane.notifyUpdate(sensorUpdate, dataObject); 
		sensorsControl.notifyUpdate(sensorUpdate, dataObject);

	}





}

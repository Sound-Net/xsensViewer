package main;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.application.*; 
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.JMetroStyleClass;
import jfxtras.styles.jmetro.Style;
import layout.SerialSensorPane;
import layout.SensorView;

 
/**
 * Starts the JavaFX thread and UI application. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class SensorMain extends Application {
	
//	public URL darkStyle=ClassLoader.getSystemResource("resources/jmetroDarkTheme.css");
	
	
//	public static final String darkStyle = "jmetroDarkTheme.css";

	
    public static void main(String[] args) {
       launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) throws IOException {
     
        StackPane root = new StackPane();
        
        SensorsControl sensorControl = new SensorsControl(); 
        SensorView sensorView = new SensorView(sensorControl); 
        
		primaryStage.getIcons().add( new Image(SensorMain.class.getResourceAsStream("rotate_icon.png"))); 

		root.setPadding(new Insets(5,5,5,5));
		
        root.getChildren().add(sensorView);
        //new JMetro(JMetro.Style.DARK).applyTheme(root);
//        System.out.println(darkStyle.getFile());
        //root.getStylesheets().add(darkStyle);

        Scene scene =  new Scene(root, 750, 550); 
        
        SensorView.setTheme(scene, root); 

        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
            	sensorControl.stop(); 
                Platform.exit();
                System.exit(0);
            }
        });

//        primaryStage.setOnShowing((event)->{
//        	System.out.println("Hello");
//			//create a single default tab. 
//	        sensorView.addSensorTab(1); 
//        });
        
        primaryStage.show();
        
        
        //HACK -don't know why, but for some reason we need this to make sure the tab pane add button is laid out properly...
        //It's something to do with the header area not initialising until after the tab has been added. 
        sensorView.addSensorTab("Sensor " + 0, sensorView.addSerialSensorPane()); 
        
        Platform.runLater(()->{
        sensorView.removeSerialSensorPane(1);
        sensorView.getTabbedPane().getTabs().remove(1); 
        });

       
        sensorView.getTabbedPane().layout(); 

    }
}

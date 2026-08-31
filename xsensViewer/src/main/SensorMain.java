package main;

import java.io.IOException;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.application.*; 
import layout.SensorView;

/**
 * Starts the JavaFX thread and UI application. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class SensorMain extends Application {
	
    public static void main(String[] args) {
       launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) throws IOException {
    	
        String javaVersion = System.getProperty("java.version");

        System.out.println("Running on Java " + javaVersion);

     
        StackPane root = new StackPane();
        
        SensorsControl sensorControl = new SensorsControl(); 
        SensorView sensorView = new SensorView(sensorControl); 
        
		root.setPadding(new Insets(5,5,5,5));
		
        root.getChildren().add(sensorView);

        Scene scene =  new Scene(root, 900, 680); 
        
        SensorView.setTheme(scene, root); 

        // Window, taskbar and dock icon. Generated from the master artwork by
        // tools/make-icons.py; the installers use the .ico/.icns in packaging/.
        primaryStage.getIcons().add(
                new Image(SensorMain.class.getResourceAsStream("/resources/app-icon.png")));

        primaryStage.setTitle("SoundNet Sensor Viewer");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(700);
        primaryStage.setMinHeight(520);

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

package main;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import layout.SensorView;
 
/**
 * Starts the JavaFX thread and UI application. 
 * 
 * @author Jamie Macaulay 
 *
 */
public class SensorMain extends Application {
	
//	public URL darkStyle=ClassLoader.getSystemResource("resources/jmetroDarkTheme.css");
	
	
	public static final String darkStyle = "jmetroDarkTheme.css";

	
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) throws IOException {
     
        StackPane root = new StackPane();
        
        SensorControl sensorControl = new SensorControl(); 
        SensorView sensorView = new SensorView(sensorControl); 
        
		primaryStage.getIcons().add( new Image(SensorMain.class.getResourceAsStream("rotate_icon.png"))); 


        root.getChildren().add(sensorView);
        //new JMetro(JMetro.Style.DARK).applyTheme(root);
//        System.out.println(darkStyle.getFile());
        root.getStylesheets().add(darkStyle);

        primaryStage.setScene(new Scene(root, 750, 550));

        
        primaryStage.show();
        
    }
}

package main;

import java.io.IOException;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import layout.SensorView;
 
public class SensorMain extends Application {
	
	public String darkStyle="resources//jmetroDarkTheme.css";

	
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) throws IOException {
     
        StackPane root = new StackPane();
        
        SensorControl sensorControl = new SensorControl(); 
        SensorView sensorView = new SensorView(sensorControl); 

        root.getChildren().add(sensorView);
        root.getStylesheets().add(darkStyle);

        primaryStage.setScene(new Scene(root, 750, 550));
        primaryStage.show();
        
    }
}

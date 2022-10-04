package layout;

import javafx.scene.layout.*;


import animatefx.animation.GlowBackground;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.animation.Timeline;
import javafx.scene.shape.Circle;
import layout.utils.GlowBackground2;
import main.SensorsControl;
import main.SensorsControl.SensorUpdate;
import xsens.XsMessageID;
import javafx.geometry.Pos;

/**
 * Controls the ability to send all sensors commands. 
 * @author Jamie Macaulay
 *
 */
public class MasterCommPane extends BorderPane {
	
	private SensorsControl sensorControl;

	/**
	 * Button to set the time for all  the time for all. 
	 */
	private Button setTimeButton;

	
	private GridPane sensorNamePane;
	

	public MasterCommPane(SensorsControl sensorControl) {
		this.sensorControl = sensorControl; 
		
		VBox holder = new VBox(); 
		holder.setSpacing(5);
		holder.setPadding(new Insets(5,5,5,5));
		
		Label connectLabel = new  Label("Multi-sensor"); 
		SensorView.titlelabel(connectLabel);
		holder.getChildren().addAll(connectLabel); 
		
		holder.getChildren().add(sensorNamePane = new GridPane()); 
		sensorNamePane.setHgap(5);
		sensorNamePane.setVgap(5);
		sensorNamePane.setAlignment(Pos.CENTER_LEFT);
		
		holder.getChildren().add(setTimeButton
		= new Button("Set Time"));
		//this.setCenter(new Label("Sensor Control"));
		
		setTimeButton.setOnAction((action->{
			sensorControl.sendMessage(XsMessageID.XMID_SetRTCTime, null); 
		}));
		
		this.setTop(holder);
	}
	
	
	public void notifyUpdate(SensorUpdate sensorUpdate, Object dataObject) {
		Platform.runLater(()->{
		updateNamePane(); 
		});
	}

	private void updateNamePane() {
		sensorNamePane.getChildren().clear();
		//System.out.print("NO. SENSOR CONTROLS: " +sensorControl.getSensorControls().size());
		for (int i=0; i<sensorControl.getSensorControls().size(); i++) {
			//System.out.print("SENSOR: " +sensorControl.getSensorControls().get(i).getSensorName());
			Label label = new Label(sensorControl.getSensorControls().get(i).getSensorName()); 

			Circle circle = new Circle(10); 
			
			GlowBackground2 glow;
			if (sensorControl.getSensorControls().get(i).isConnected()) {
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
	

}

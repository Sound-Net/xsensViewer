package layout;

import main.SensorData;

/**
 * Listener for new sensor message packets. 
 * @author Jamie Macaulay 
 *
 */
public interface SensorMessageListener {
	
	/**
	 * Called whenever there is a new message. 
	 */
	public void newSensorMessage(SensorData sensorComms); 

}

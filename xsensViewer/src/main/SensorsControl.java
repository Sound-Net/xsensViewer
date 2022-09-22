package main;

import java.util.ArrayList;

import xsens.XBusMessage;

/**
 * The main control class for the application. Each sensor has it's own controller. 
 * 
 * 
 * @author Jamie Macaulay 
 *
 */
public class SensorsControl  {
	
	private ArrayList<SensorControl> sensorControlList = new ArrayList<SensorControl>(); 

	/**
	 * The sensor control class. 
	 */
	public SensorsControl() {
		
	}
	
	
	
	
	public ArrayList<SensorControl> getSensorControls() {
		return sensorControlList;
	}

	
	/**
	 * Add a sensor control to the master sensor list.
	 * @param sensorControl - the sensor control to add
	 * @return true if the sensor control was added/. 
	 */
	public boolean addSensorControl(SensorControl sensorControl) {
		return sensorControlList.add(sensorControl);
	}
	
	/**
	 * Remove the sensor control 
	 * @param sensorControl - the sensor control to remove
	 * @return true if the sensor control was removed from the list. 
	 */
	public boolean removeSensorControl(SensorControl sensorControl) {
		return sensorControlList.remove(sensorControl);
	}

	/**
	 * Send a message to all active devices
	 */
	public void sendMessage(XBusMessage message) {
		for (int i=0; i< sensorControlList.size(); i++) {
			sensorControlList.get(i).sendMessage(message);
		}
	}

	/**
	 * Stop all sensors and disconnect. 
	 */
	public void stop() {
		for (int i=0; i< sensorControlList.size(); i++) {
			sensorControlList.get(i).stop(); 
		}
		
	}

}

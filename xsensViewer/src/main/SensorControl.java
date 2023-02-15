package main;

import layout.SensorMessageListener;
import xsens.XBusMessage;
import xsens.XsMessageID;

/**
 * Basic interface for sensor control. 
 * @author Jamie Macaulay. 
 *
 */
public interface SensorControl {
	
	/**
	 * Get the name of the sensor. 
	 * @return the name of the sensor. 
	 */
	public String getSensorName(); 
	
	
	
	/**
	 * Get the unique ID of a sensor
	 * @return the unique ID - can be null
	 */
	public Long getSensorUID(); 
	
	
	/**
	 * Stop the sensor. Called whenever the application closes. 
	 */
	public boolean isConnected(); 
	
	/**
	 * Stop the sensor. Called whenever the application closes. 
	 */
	public boolean stop();

	/**
	 * Send a message to the current device
	 * @param message - the message to send. 
	 */
	public void sendMessage(XBusMessage message);
	
	/**
	 * Send a message with some data
	 * @param value
	 * @param data
	 */
	public void sendMessage(XsMessageID value, int[] data);
	
	/**
	 * Add a sensor message listenr
	 * @param sensorMessageListener
	 */
	public void addSensorMessageListener(SensorMessageListener sensorMessageListener);

	/**
	 * Remove a sensor listeners
	 * @param sensorMessageListener
	 * @return
	 */
	public boolean removeSensorMessageListener(SensorMessageListener sensorMessageListener);
	

}

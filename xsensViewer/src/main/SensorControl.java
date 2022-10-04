package main;

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
	
	public void sendMessage(XsMessageID value, int[] data);
	

}

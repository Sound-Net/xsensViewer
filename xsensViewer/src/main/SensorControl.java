package main;

import xsens.XBusMessage;

/**
 * Basic interface for sensor control. 
 * @author Jamie Macaulay. 
 *
 */
public interface SensorControl {

	/**
	 * Stop the sensor. Called whenever the application closes. 
	 */
	public boolean stop();

	/**
	 * Send a message to the current device
	 * @param message - the message to send. 
	 */
	public void sendMessage(XBusMessage message);

}

package main;

import main.SensorsControl.SensorUpdate;

/**
 * Interface for passing messages between different aspects of the program. 
 * @author Jamie Macaulay
 *
 */
public interface SensorUpdateListener {
	
	/**
	 * Notify that an update has occurred
	 * @param sensorUpdate - the snsorUpdate
	 * @param object - extra information passed with the update. 
	 */
	public void notifyUpdate(SensorUpdate sensorUpdate, Object object); 

}

package main;

/**
 * Holds sensor data. Orientation data and depth data. 
 * @author Jamie Macaulay
 *
 */
public class SensorData {
	
	/**
	 * Constructor for angles. 
	 * @param angles - angles. lengfth of 3 is euler angles in degrres. Length of 4 is a quaternion. 
	 */
	public SensorData(double[] angles) {
		if (angles.length==3) eularAngles=angles; 
		if (angles.length==4) quaternion=angles; 

	}

	/**
	 * Eular angles in degrees
	 */
	public double[] eularAngles; 
	
	/**
	 * Quaternion orientation data.
	 */
	public double[] quaternion;


}

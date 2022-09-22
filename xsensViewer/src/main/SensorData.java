package main;

import comms.SerialMessageParser.DataTypes;

/**
 * Holds sensor data. Orientation data and depth data. 
 * 
 * @author Jamie Macaulay
 *
 */
public class SensorData {
	
	/**
	 * The data type. 
	 */
	public DataTypes flag;

	/**
	 * Constructor for angles. 
	 * 
	 * @param angles - angles. length of 3 is Euler angles in degrees. Length of 4 is a quaternion. 
	 */
	public SensorData(double[] data, DataTypes flag) {
		this.flag=flag; 
		if (flag==DataTypes.EULAR_ANGLES) eularAngles=data; 
		if (flag==DataTypes.QUATERNION) quaternion=data; 
		if (flag==DataTypes.LIGHT_SPECTRUM) lightSpectrum=data; 
		if (flag==DataTypes.TEMPERATURE) temperature=data[0]; 
		if (flag==DataTypes.RGBDATA) lightSpectrum=data; 
		if (flag==DataTypes.PRESSURE_TEMPERATURE) {
			this.pressure=data[0];
			this.temperature=data[1]; 
		}
		if (flag==DataTypes.BATTERYDATA) {
			this.batteryLevel=data[0]; 
			this.batteryLevelV=data[1]; 
		}
		if (flag==DataTypes.SD_USED_SPACE) sdUsedSpace = data; 

	}
	
	/**
	 * Constructor which for  raw byte data mt message.   
	 * @param outArray - raw MT message data. 
	 */
	public SensorData(int[] outArray) {
		this.flag=DataTypes.MTMESSAGE; 
		this.mtMessage=outArray; 
	}
	
//	/**
//	 * Constructor for PTData. 
//	 * @param pressure - the pressure data in mbar;
//	 * @param temperature -the temperature in celsuis; 
//	 */
//	public SensorData(double pressure, double temperature) {
//		this.pressure=pressure; 
//		this.temperature=temperature; 
//	}

	
//	/**
//	 * Constructor for battery data. 
//	 * @param pressure - the pressure data in mbar;
//	 * @param temperature -the temperature in celsuis; 
//	 */
//	public SensorData(double batteryLevel) {
//		this.batteryLevel=batteryLevel; 
//		this.batteryLevelV=batteryLevelV; 
//
//	}


	/**
	 * Raw MT message byte data. 
	 */
	public int[] mtMessage;

	/**
	 * Euler angles in degrees.
	 */
	public double[] eularAngles; 
	
	/**
	 * Quaternion orientation data.
	 */
	public double[] quaternion;
	
	/**
	 * Light spectrum data. Measurements will depend on the sensor.
	 */
	public double[] lightSpectrum;
	
	
	/**
	 * Pressure in mbar.
	 */
	public Double pressure; 
	
	/**
	 * The temperature in celsius. 
	 */
	public Double temperature; 
	
	/**
	 * The battery level in %. 
	 */
	public Double batteryLevel; 
	
	
	/**
	 * The battery level in volts.
	 */
	public Double batteryLevelV;

	/**
	 * The currently used space.
	 * sdUsedSpace[0] = space used sdUsedSpace[1] = SD card size.  
	 */
	public double[] sdUsedSpace; 
	


}

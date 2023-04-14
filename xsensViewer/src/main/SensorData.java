package main;

import comms.SerialMessageParser.DataTypes;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

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
	 * Constructor for sensor data from a sensor. 
	 * 
	 * @param angles - angles. length of 3 is Euler angles in degrees. Length of 4 is a quaternion. 
	 */
	public SensorData(long timemillis, double[] data, DataTypes flag) {
		this(data, flag);
		this.timeMillis = new SimpleLongProperty(timemillis);
	}
	
	public SensorData(int[] data, DataTypes flag) {
		this.flag=flag; 
		if (flag==DataTypes.RTC || flag==DataTypes.RTCACK) {
			this.timeMillis=new SimpleLongProperty(((long) data[0])*1000 +  data[1]/1000); 
		}
		this.pcMillis = new SimpleLongProperty(System.currentTimeMillis());
	}


	/**
	 * Constructor for sensor data from a sensor. 
	 * 
	 * @param angles - angles. length of 3 is Euler angles in degrees. Length of 4 is a quaternion. 
	 */
	public SensorData(double[] data, DataTypes flag) {
		this.flag=flag; 
		if (flag==DataTypes.EULAR_ANGLES) eularAngles=data; 
		if (flag==DataTypes.QUATERNION) quaternion=data; 
		if (flag==DataTypes.LIGHT_SPECTRUM) lightSpectrum=data; 
		if (flag==DataTypes.TEMPERATURE) temperature=new SimpleDoubleProperty(data[0]); 
		if (flag==DataTypes.RGBDATA) lightSpectrum=data; 
		if (flag==DataTypes.PRESSURE_TEMPERATURE) {
			this.pressure=new SimpleDoubleProperty(data[0]);
			this.temperature=new SimpleDoubleProperty(data[1]); 
		}
		if (flag==DataTypes.BATTERYDATA) {
			//System.out.println("Battery data: " + data[0] + " " + data[1]);
			this.batteryLevel=new SimpleDoubleProperty(data[0]); 
			this.batteryLevelV=new SimpleDoubleProperty(data[1]); 
		}
		if (flag==DataTypes.SD_USED_SPACE) {
			sdUsedSpace = data; 
		}
		this.pcMillis = new SimpleLongProperty(System.currentTimeMillis());
	}
	
	/**
	 * Constructor which for  raw byte data mt message.   
	 * @param outArray - raw MT message data. 
	 */
	public SensorData(int[] outArray) {
		this.flag=DataTypes.MTMESSAGE; 
		this.mtMessage=outArray; 
		this.pcMillis = new SimpleLongProperty(System.currentTimeMillis());
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
	public SimpleDoubleProperty pressure; 
	
	/**
	 * The temperature in celsius. 
	 */
	public SimpleDoubleProperty temperature; 
	
	/**
	 * The battery level in %. 
	 */
	public SimpleDoubleProperty batteryLevel; 
	
	
	/**
	 * The battery level in volts.
	 */
	public SimpleDoubleProperty batteryLevelV;

	/**
	 * The currently used space.
	 * sdUsedSpace[0] = space used sdUsedSpace[1] = SD card size.  
	 */
	public double[] sdUsedSpace;

	/**
	 * The time in Java millis. 
	 */
	public SimpleLongProperty timeMillis;
	
	/**
	 * The time the message was recieved on the PC
	 */
	public SimpleLongProperty pcMillis;

	/**
	 * The name of the sensor the message came from. 
	 */
	public SimpleStringProperty sensorName;
	
	/**
	 * The device unique identifier
	 */
	public SimpleLongProperty deviceID;

	/**
	 * The device ID. 
	 */
	public SimpleLongProperty deviceType;


	/**
	 * Set the message time value in millis datenum
	 * @param timeMillis - the time in milliseconds. 
	 */
	public void setTimeMillis(long timeMillis) {
		if (this.timeMillis==null) this.timeMillis = new SimpleLongProperty(timeMillis);
		else this.timeMillis.set(timeMillis);
	}

	/**
	 * Set the name of sensor the date came from. 
	 * @param sensorName2 - the name of the sensor. 
	 */
	public void setSensorName(String sensorName2) {
		if (this.sensorName==null) this.sensorName = new SimpleStringProperty(sensorName2);
		else this.sensorName.set(sensorName2);
	}
		
	


}

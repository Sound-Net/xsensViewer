package comms;

import javafx.application.Platform;
import main.SensorControl;
import main.SensorData;

/**
 * Figures out what to do with serial message.
 * @author Jamie Macaulay
 *
 */
public class SerialMessageParser {

	
	public enum DataTypes {
		EULAR_ANGLES, QUATERNION, PRESSURE_TEMPERATURE, BATTERYDATA, RGBDATA, MTDATA
	}

			
	/**
	 * The current incoming data type
	 */
	public DataTypes messageFlag=DataTypes.EULAR_ANGLES; 

	private SensorControl sensorControl;

	public SerialMessageParser(SensorControl sensorControl){
		this.sensorControl=sensorControl;
	}

	/**
	 * Parse the incomming data. 
	 * @param line - the incoming serial line
	 */
	public void parseLine(String dataLine){
		DataTypes messageFlag = getMessageFlag(dataLine);
		
		//remnove the flag from the string so it's just data 
		String[] ary = dataLine.split(" ");
		String stringData=""; 
		
		for (int i=1; i<ary.length; i++) {
			stringData+=ary[i]+" "; 
		}
		
		SensorData sensorData=null; 
		switch (messageFlag){
		case EULAR_ANGLES:
			sensorData= parseString(stringData, 3, messageFlag); 
			break;
		case QUATERNION:
			parseString(stringData, 4, messageFlag);
			break;
		case PRESSURE_TEMPERATURE:
			parseString(stringData, 2, messageFlag);
			break;
		case RGBDATA:
			parseString(stringData, 3,messageFlag);
			break;
		case MTDATA:
			//northing
			break;
		}	
		if (sensorData==null) return;
		newMessage(sensorData);

	}

	/**
	 * Get the integer flag for the message 
	 * @param dataLine - data line
	 * @return the integer flag for the type of message. -1 if message cannot be parsed. 
	 */
	public DataTypes getMessageFlag(String dataLine) {
		String[] ary = dataLine.split(" ");
		DataTypes flag=null; 
		if (ary[0].equals("EL")) {
				flag=DataTypes.EULAR_ANGLES; 
		}
		if (ary[0].equals("QT")) {
			flag=DataTypes.QUATERNION; 

		}
		if (ary[0].equals("PT")) {
			flag=DataTypes.PRESSURE_TEMPERATURE; 

		}
		if (ary[0].equals("RGB")) {
			flag=DataTypes.RGBDATA; 
		}
		if (ary[0].equals("BAT")) {
			flag=DataTypes.BATTERYDATA; 
		}
		return flag; 
	}

	/**
	 * Parse Eular angles in degrees. 
	 * @param dataLine - the string data 
	 * @return 
	 */
	public SensorData parseString(String dataLine, int nDataPoints, DataTypes flag){
		double[] angles=doubleArrayParser(dataLine, nDataPoints);
		if (angles!=null){
			SensorData sensorComms = new SensorData(angles, flag); 
			return sensorComms; 
		}
		return null; 
	}


	private double[] doubleArrayParser(String dataLine, int nNumbers){
		String[] ary = dataLine.split(" ");
		if (ary.length==nNumbers){
			double[] outArray= new double[nNumbers]; 
			//have a set of eulaer angles
			for (int i=0; i<nNumbers; i++){
				outArray[i]=Double.valueOf(ary[i]);
			}
			return outArray; 
		}
		else{
			return null; 
		}
	}

	/**
	 * Called whenever a new message is ready.
	 * @param sensorComms - the new message. 
	 */
	public void newMessage(SensorData sensorComms){
		Platform.runLater(()->{
			sensorControl.newMessage(sensorComms);
		});
	}
	

	/**
	 * Gte the message flag
	 * @return the messageFlag
	 */
	public DataTypes getMessageFlag() {
		return messageFlag;
	}

	/**
	 * Set the message flag
	 * @param messageFlag the messageFlag to set
	 */
	public void setMessageFlag(DataTypes messageFlag) {
		this.messageFlag = messageFlag;
	}



}

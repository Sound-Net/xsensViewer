package comms;

import main.SerialSensorControl;
import main.SensorData;
import javafx.application.Platform;

/**
 * Figures out what to do with serial message.
 * @author Jamie Macaulay
 *
 */
public class SerialMessageParser {

	
	public enum DataTypes {
		EULAR_ANGLES, QUATERNION, PRESSURE_TEMPERATURE, BATTERYDATA, RGBDATA, MTDATA, MTMESSAGE, TEMPERATURE, LIGHT_SPECTRUM, SD_USED_SPACE, NO_DATA
	}

			
	/**
	 * The current incoming data type
	 */
	public DataTypes messageFlag=DataTypes.EULAR_ANGLES; 

	private SerialSensorControl sensorControl;

	public SerialMessageParser(SerialSensorControl sensorControl){
		this.sensorControl=sensorControl;
	}

	/**
	 * Parse the incoming data. 
	 * @param line - the incoming serial line
	 */
	public void parseLine(String dataLine){
		//First - is there a time? 
		
		String[] splitStirng = dataLine.split(":");
		
	
		DataTypes messageFlag = getMessageFlag(splitStirng[splitStirng.length-1].trim());
		if (messageFlag==null) {
//			System.out.println("------------");
//			System.out.println("Could not parse message flag: " + dataLine);
//			System.out.println("------------");
			return; 
		}
		
		//remove the flag from the string so it's just data 
		String[] ary = splitStirng[splitStirng.length-1].trim().split(" ");
		String stringData=""; 
		
		for (int i=1; i<ary.length; i++) {
			stringData+=ary[i]+" "; 
		}
		
		//System.out.println("Incomming data: " + dataLine + " flag: " + messageFlag);
		
		SensorData sensorData=null; 
		switch (messageFlag){
		case EULAR_ANGLES:
			sensorData=parseString(stringData, 3, messageFlag); 
			break;
		case QUATERNION:
			sensorData=parseString(stringData, 4, messageFlag);
			break;
		case PRESSURE_TEMPERATURE:
			sensorData=parseString(stringData, 2, messageFlag);
			break;
		case RGBDATA:
			sensorData=parseString(stringData, 3,messageFlag);
			break;
		case BATTERYDATA:
			sensorData=parseString(stringData, 2, messageFlag);
			break;
		case TEMPERATURE:
			sensorData=parseString(stringData, 1, messageFlag);
			break;
		case LIGHT_SPECTRUM:
			sensorData=parseString(stringData, 10, messageFlag);
			break;
		case SD_USED_SPACE:
			sensorData=parseString(stringData, 2, messageFlag);
			break;
		case NO_DATA:
			sensorData=null;
			break;
		case MTMESSAGE:
			sensorData=parseCommandString(stringData);
			break;
		case MTDATA:
			//northing
			break;
		default:
			break;
		}	
		
		sensorData.timeMillis = parseTime(splitStirng[splitStirng.length-3], splitStirng[splitStirng.length-2]); 
		
		if (sensorData==null) return;
		newMessage(sensorData);

	}

	/**
	 * Parse a time string. 
	 * @param unixTime - the unix time string (e.g. "167595504")
	 * @param millis - a millis string between 0 and 1000 (e.g. "345")
	 * @return the java millis time stamp. 
	 */
	private Long parseTime(String unixTime, String millis) {
		long unixTimeL = Long.valueOf(unixTime.trim()); 
		
		int millisL = Integer.valueOf(millis.trim()); 

		return unixTimeL*1000 +millisL;
	}

	/**
	 * Convert a an MT command string to raw byte data. 
	 * @param stringData - data string
	 * @return sensor data object
	 */
	private SensorData parseCommandString(String stringData) {
		String[] ary = stringData.split(" ");
		int[] outArray= new int[ary.length]; 
		//have a set of euler angles
		for (int i=0; i<outArray.length; i++){
			outArray[i]=Integer.valueOf(ary[i]);
		}
		
		SensorData sensorData = new SensorData(outArray); 
		return sensorData; 
	}

	/**
	 * Get the integer flag for the message 
	 * @param dataLine - data line
	 * @return the integer flag for the type of message. -1 if message cannot be parsed. 
	 */
	public DataTypes getMessageFlag(String dataLine) {
		String[] ary = dataLine.split(" ");
		DataTypes flag=null; 
		if (ary[0].trim().equals("EL")) {
				flag=DataTypes.EULAR_ANGLES; 
		}
		if (ary[0].trim().equals("QT")) {
			flag=DataTypes.QUATERNION; 
		}
		if (ary[0].trim().equals("PT")) {
			flag=DataTypes.PRESSURE_TEMPERATURE; 
		}
		if (ary[0].trim().equals("RGB")) {
			flag=DataTypes.RGBDATA; 
		}
		if (ary[0].trim().equals("BAT")) {
			flag=DataTypes.BATTERYDATA; 
		}
		if (ary[0].trim().equals("MT")) {
			flag=DataTypes.MTMESSAGE; 
		}
		if (ary[0].trim().equals("LSP")) {
			flag=DataTypes.LIGHT_SPECTRUM; 
		}
		if (ary[0].trim().equals("TM")) {
			flag=DataTypes.TEMPERATURE; 
		}
		if (ary[0].trim().equals("SD")) {
			flag=DataTypes.SD_USED_SPACE; 
		}
		if (ary[0].trim().equals("ND")) {
			flag=DataTypes.NO_DATA; 
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
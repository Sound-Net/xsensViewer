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

	/**
	 * Serial data is eular angles
	 */
	public final static int EULAR_ANGLES=0; 

	/**
	 * Serial data is quaternion
	 */
	public final static int QUATERNION=1; 

	/*
	 * Data incoming in MTdATA FORMAT;
	 */
	public final static int MTDATA=2;

	/**
	 * The current incomming data type
	 */
	public int messageFlag=EULAR_ANGLES; 

	private SensorControl sensorControl;

	public SerialMessageParser(SensorControl sensorControl){
		this.sensorControl=sensorControl;
	}

	/**
	 * Parse the incomming data 
	 * @param line - the incoming serial line
	 */
	public void parseLine(String dataLine){
		switch (messageFlag){
		case EULAR_ANGLES:
			parseEularAngles(dataLine);
			break;
		case QUATERNION:
			parseQuaternion(dataLine);
			break;
		case MTDATA:

			break;

		}	
	}

	/**
	 * Parse eular angles in degrees. 
	 * @param dataLine 
	 * @return 
	 */
	public void parseEularAngles(String dataLine){
		double[] angles=doubleArrayParser(dataLine, 3);
		if (angles!=null){
			SensorData sensorComms = new SensorData(angles); 
			 newMessage(sensorComms);
		}
	}


	/**
	 * Parse quaternion/ 
	 */
	public void parseQuaternion(String dataLine){
		double[] angles=doubleArrayParser(dataLine, 3);
		if (angles!=null){
			SensorData sensorComms = new SensorData(angles); 
			newMessage(sensorComms);
		}
	}


	private double[] doubleArrayParser(String dataLine, int nNumbers){
		String[] ary = dataLine.split(" ");
		if (ary.length==nNumbers){
			double[] outArray= new double[nNumbers]; 
			//have a set of eulaer anggles
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



}

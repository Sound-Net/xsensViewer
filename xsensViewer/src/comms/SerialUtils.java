package comms;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;


/**
 * Useful functions used throughtout the program. 
 * @author Jamie Macaulay. 
 *
 */
public class SerialUtils {

	/**
	 * List of standard baud rates for serial communications. 
	 */
	public static Integer[]  baudRate={110, 300, 600, 1200, 2400, 4800, 9600, 14400, 19200, 38400, 57600, 115200, 128000, 256000 };


	/**
	 * Calculate to Euler angles from a quaternion. 
	 * @param quaternion - a quaternion
	 * @return Euler angles in RADIANS. 
	 */
	public static double[] quat2Eul(double[] quaternion){

		Rotation rotation = new Rotation(quaternion[0], quaternion[1], 
				quaternion[2], quaternion[3],false); 

		try{
			double[] angles = rotation.getAngles(RotationOrder.ZYX,  RotationConvention.FRAME_TRANSFORM);
			return angles;
		}

		catch (Exception e){
			System.err.println("Currupt Quaternion: Could not convert to Euler");
			return new double[4]; 
		}

	}
	
	
	/**
	 * Convert a millis date number into a string date with default format "dd-MM-yyyy HH:mm:ss.SSS". 
	 * @param millis - the time in millis date number. 
	 * @return a string representation of time. 
	 */
	public static String millis2StringDate(long millis) {
		return millis2StringDate(millis,  "dd-MM-yyyy HH:mm:ss.SSS") ;
	}
	
	
	/**
	 * Convert a millis date number into a string date. 
	 * @param millis - the time in millis date number. 
	 * @param format - the string format e.g. "dd-MM-yyyy HH:mm:ss.SSS"
	 * @return a string representation of time. 
	 */
	public static String millis2StringDate(long millis, String format) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli (millis),  ZoneOffset.UTC )
		.format(DateTimeFormatter.ofPattern(format));
	}

}
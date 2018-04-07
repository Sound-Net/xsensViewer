package comms;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;

public class SerialUtils {

	/**
	 * 
	 */
	public static Integer[]  baudRate={110, 300, 600, 1200, 2400, 4800, 9600, 14400, 19200, 38400, 57600, 115200, 128000, 256000 };


	/**
	 * Calculate euler angles from a quaternion
	 * @param quaternion - a quaternion
	 * @return euler angles in RADIANS
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

}

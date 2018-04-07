package comms;

import com.fazecast.jSerialComm.SerialPort;

public class SerialPortComm2   {
	
	/**
	 * Open a serial port 
	 * @param comPort
	 */
	public void openPort(SerialPort comPort){
		comPort.setBaudRate(38400);
		comPort.openPort();
	}
	
	
	public static void main(String[] args){
	SerialPort comPort = SerialPort.getCommPorts()[1];
	comPort.setBaudRate(38400);
	comPort.openPort();
	
	System.out.println(" Start recording:");

	try {
	   while (true)
	   {
	      while (comPort.bytesAvailable() <= 0){
//	    		System.out.println(" Sleeping: " + comPort.bytesAvailable() + " " + comPort.getDescriptivePortName());
	         //Thread.sleep(1);
	      }

	      byte[] readBuffer = new byte[comPort.bytesAvailable()];
	      int numRead = comPort.readBytes(readBuffer, readBuffer.length);
	      System.out.println("Read " + numRead + " bytes.");
	
	   }
	} catch (Exception e) { e.printStackTrace(); }
	comPort.closePort();
	}

}

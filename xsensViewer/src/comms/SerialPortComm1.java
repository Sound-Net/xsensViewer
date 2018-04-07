package comms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import gnu.io.CommPortIdentifier; 
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent; 
import gnu.io.SerialPortEventListener; 
import java.util.Enumeration;

/**
 * Reads com port data. 
 * @author Jamie Macaulay 
 *
 */
//derived from SUN's examples in the javax.comm package
import java.io.*;
import java.util.*;

import javax.swing.JOptionPane;

//import javax.comm.*; // for SUN's serial/parallel port libraries
import gnu.io.*; // for rxtxSerial library


/**
* 
* @author David J McLaren - adapted from http://www.captain.at/howto-java-serial-port-javax-comm-rxtx.php
*
*/

abstract public class SerialPortComm1 implements SerialPortEventListener {
	private CommPortIdentifier portId;
//	static CommPortIdentifier saveportId;
	private BufferedReader inputStream;
	private SerialPort     serialPort;
	private OutputStream   outputStream;
	private boolean        outputBufferEmptyFlag = false;
	private int baud;
	private String portName;
	private boolean portOpen;
	private static ArrayList<CommPortIdentifier> comPortList;
	private String portError = null;

	public static String getDefaultSerialPortName() {
//			String defaultOsSerialPort;
//			PamUtils.PlatformInfo.OSType currentOsType = PamUtils.PlatformInfo.calculateOS();
//			switch (currentOsType) { 
//          case WINDOWS: {
          	String defaultOsSerialPort = "COM5"; 
//          	break;
//          }
//          case MACOSX: {
//          	// let's assume will be a Serial via USB as new Macs don't have an inbuilt serial port 
//          	defaultOsSerialPort = "/dev/tty.usbserial";
//          	break;
//          }
//          case LINUX: {
//          	// linux let's also assume default will be that are using a USB port
//      		defaultOsSerialPort = "/dev/ttyUSB0";
//          	break;
//          }
//          default: {
//          	System.out.println("Sorry, PAMGUARD doesn't know about a default serial port for your operating system");
//				defaultOsSerialPort = "unknown";
//				break;
//          }
//      }
//		System.out.println("defaultOsSerialPort="+defaultOsSerialPort);
		return defaultOsSerialPort;
	}
	
	public void initWriteToPort() {
		// initwritetoport() assumes that the port has already been opened and
		try {
			// get the outputstream
			outputStream = serialPort.getOutputStream();
			setPortError(null);
		} catch (IOException e) {
			setPortError("Unable to write to serial port " + portName);
			System.out.println(e);
		}

		//When using USB-to-serial under Linux (and might explain issues under windows too?)
		//can set notifyOnOutputEmpty(true) OK but bits of the application likely to hang as
		//apparently the usbserial linux kernel driver doesn't support OUTPUT_BUFFER_EMPTY 
		//event. 
		//So disabling this as PAMGUARD doesn't seem to need to use that event on Windows,
		//Mac or Linux. CJB 2009=06-09
		//try {
			// activate the OUTPUT_BUFFER_EMPTY notifier
		//	serialPort.notifyOnOutputEmpty(true);
		//} catch (Exception e) {
		//	System.out.println("Error setting event notification");
		//	System.out.println(e.toString());
		//	System.exit(-1);
		//}
	}

	public void writeToPort(String string) {
		try {
			// write string to serial port
			outputStream.write(string.getBytes());
			setPortError(null);
		} catch (IOException e) {
			setPortError("Unable to write to serial port " + portName);
			System.out.println(e);
		}
	}

	public void close(){
		if (serialPort == null) return;
		//adding more pendantic clean up before closing the 
		//serial port in hopes makes things more stable 
		//inspired by this webpge
		//http://embeddedfreak.wordpress.com/2008/08/08/how-to-close-serial-port-in-rxtx/
		//cjb
		
		//System.out.println("About to stop");
		
		//when opened DATA_AVAILABLE notifier was set
		serialPort.notifyOnDataAvailable(false);
		//System.out.println("notifyOnDataAvailable now false");
		
		//also try to remove the event listener
		try {
			serialPort.removeEventListener();
		} 	
		catch (Exception e) { 
			System.out.println(e);
		}
		
		//System.out.println("removedEventListener");
		//System.out.println("calling stop");
		try {
			if (inputStream !=null) {
				inputStream.close();
				inputStream=null;
			}
		} 
		catch (Exception e) { 
			System.out.println(e);
		}
		
		try {
			if (outputStream !=null) {
				outputStream.flush();
				outputStream.close();
				outputStream=null;
			}
		}
		catch (Exception e) { 
			System.out.println(e);
		}

		serialPort.close();
		
		portOpen = false;
		
		//System.out.println("stopped");
	}

	public SerialPortComm1(String portName, int baud, CommPortIdentifier portId, String portIdName) {
		this.portName = portName;
		this.baud = baud;
		this.portId = portId;
		//System.out.println("SerialPortCom"+this.portName);
		/**
		 * Check serial port is not already in use. 
		 */
		String currentOwner = portId.getCurrentOwner();
		if (currentOwner != null) {
			String str = String.format("Serial Port %s is already being used by %s and cannot be opened by %s",
					portId.getName(), currentOwner, portIdName);
			str += "\nSelect a different COM port for one of these modules.";
			System.out.println(str);
			JOptionPane.showMessageDialog(null, str, "Com port error", JOptionPane.ERROR_MESSAGE);
			setPortError("Unable to open serial port " + portName);
			return;
		}

		// Initialise serial port
		try {
//			"PAMGUARD Serial App"
			serialPort = (SerialPort) portId.open(portIdName, 2000);
		} catch (PortInUseException e) {
			System.out.println("Port in USE!");
			setPortError("Serial port " + portName + " is already in use");
			return;
		} 

		try {
			//inputStream = serialPort.getInputStream();
			inputStream = new BufferedReader(new InputStreamReader(serialPort.getInputStream()), 500);
		} 
		catch (IOException e) {
			System.out.println(e);
		}

		try {
			serialPort.addEventListener(this);
		} 
		catch (TooManyListenersException e) {
			setPortError("Error opening serial port " + portName);
			System.out.println(e);			
		}

		// activate the DATA_AVAILABLE notifier
		serialPort.notifyOnDataAvailable(true);

		try {
			// set port parameters
//			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
			serialPort.setSerialPortParams(baud, SerialPort.DATABITS_8, 
					SerialPort.STOPBITS_1, 
					SerialPort.PARITY_NONE);
		} catch (UnsupportedCommOperationException e) {
			System.out.println("UnsupportedCommOperationException:"+e);
		}
		
		initWriteToPort();
		
		portOpen = true;
		setPortError(null);
		
		System.out.println("Initialised Serial Port");
	}

	// override this in your sub-class
	abstract public void readData(StringBuffer result);

	@Override
	public void serialEvent(SerialPortEvent event) {
//	System.out.println(event);
		switch (event.getEventType()) {
//		case SerialPortEvent.BI:
//			System.out.println("Event is: BI " + event.getEventType());
//			break;
//		case SerialPortEvent.OE:
//			System.out.println("Event is: OE " + event.getEventType());
//			break;
//		case SerialPortEvent.FE:
//			System.out.println("Event is: FE " + event.getEventType());
//			break;
//		case SerialPortEvent.PE:
//			System.out.println("Event is: PE " + event.getEventType());
//			break;
//		case SerialPortEvent.CD:
//			System.out.println("Event is: CD " + event.getEventType());
//			break;
//		case SerialPortEvent.CTS:
//			System.out.println("Event is: CTS " + event.getEventType());
//			break;
//		case SerialPortEvent.DSR:
//			System.out.println("Event is: DSR " + event.getEventType());
//			break;
//		case SerialPortEvent.RI:
//			System.out.println("Event is: RI " + event.getEventType());
//			break;
//		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
//			System.out.println("Event is: OUTPUT_BUFFER_EMPTY " + event.getEventType());
//			break;
		case SerialPortEvent.DATA_AVAILABLE:
			// we get here if data has been received
			try {
				StringBuffer result  = new StringBuffer(inputStream.readLine());
				readData(result);
			} catch (IOException e) {			
				/*
				 * Don't print this exception since it's thrown all the time
				 * when there aren't new strings or when strings are coming through slowely. 
				 */
//				System.out.println(e);
			}

			break;
		}
	} 

	public boolean getStatus() {
		if (serialPort == null || portId == null) {
			return false;
		}
		return portOpen;
	}
	
	public static ArrayList<CommPortIdentifier> getPortArrayList(){
		if (comPortList == null) {
			comPortList = new ArrayList<CommPortIdentifier>();
			Enumeration pList = CommPortIdentifier.getPortIdentifiers(  );
			while (pList.hasMoreElements(  )) {
				CommPortIdentifier commPortId = (CommPortIdentifier)pList.nextElement(  );
//				if (!commPortId.isCurrentlyOwned()){
					if (commPortId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
						comPortList.add(commPortId);
					} 
//				}
			}
		}
		return comPortList;
	}

	public static CommPortIdentifier findPortIdentifier(String portName) {
		ArrayList<CommPortIdentifier> portList = getPortArrayList();
		if (portList == null) {
			return null;
		}
		for (CommPortIdentifier id:portList) {
			if (id.getName().equals(portName)) {
				return id;
			}
		}
		return null;
	}
	
	/**
	 * @return the portError
	 */
	public int available() {
		try {
			return serialPort.getInputStream().available();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * @return the portError
	 */
	public String getPortError() {
		return portError;
	}

	/**
	 * @param portError the portError to set
	 */
	public void setPortError(String portError) {
		this.portError = portError;
	}

}
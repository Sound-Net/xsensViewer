package comms;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class SerialComms implements SerialPortEventListener {

	/**
	 * The serial port
	 */
	SerialPort serialPort;

	/**
	 * The current port name; 
	 */
	String portName = "COM4"; 

	private BufferedReader input;
	private OutputStream output;
	private static final int TIME_OUT = 2000;
	
	
	
	private int dataRate = 38400;


	public SerialComms(){

	}


	public void initialize() {
		
		serialPort=null;

		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		//First, Find an instance of serial port as set in PORT_NAMES.
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
				if (currPortId.getName().equals(portName)) {
					portId = currPortId;
					break;
				
			}
		}

		if (portId == null) {
			System.out.println("Could not find COM port." + portName);
			return;
		}

		try {
			
			final int DATA_RATE= dataRate; 
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
					TIME_OUT);
			serialPort.setSerialPortParams(DATA_RATE,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// open the streams
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();

			//serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}


	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	@Override
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				String inputLine=null;
				if (input.ready()) {
					inputLine = input.readLine();
					System.out.println(inputLine);
				}

			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
		// Ignore all the other eventTypes, but you should consider the other ones.
	}

	public static void main(String[] args) throws Exception {
		SerialPortComm3 main = new SerialPortComm3();
		main.initialize();
		Thread t=new Thread() {
			public void run() {
				//the following line will keep this app alive for 1000    seconds,
				//waiting for events to occur and responding to them    (printing incoming messages to console).
				try {Thread.sleep(1000000);} catch (InterruptedException    ie) {}
			}
		};
		t.start();
		System.out.println("Started");
	}


	/**
	 * Get the bufferred reader for the setrial stream. 
	 * @return the bufferrred reader.
	 */
	public BufferedReader getBufferredInput() {
		return input;
	}


	/**
	 * Set the port. 
	 * @param port
	 */
	public void setPort(String port) {
		this.portName=port; 
		
	}
	
	/**
	 * Sert the baud rate
	 * @param baud
	 */
	public void setBaud(int baud) {
		this.dataRate=baud; 
		
	}
	
	
	/**
	 * @return the serialPort
	 */
	public SerialPort getSerialPort() {
		return serialPort;
	}


}

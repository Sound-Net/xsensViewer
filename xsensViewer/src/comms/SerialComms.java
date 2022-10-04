package comms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;


/**
 * Handles comms with one serial port. 
 * 
 * @author Jamie Macaulay
 *
 */
public class SerialComms implements SerialPortDataListener {

	/**
	 * The serial port
	 */
	SerialPort serialPort;

	/**
	 * The current port name; 
	 */
	String portName = "cu.usbmodem14101"; 

	private BufferedReader input;
	private OutputStream output;
	private static final int TIME_OUT = 2000;
	
	private int dataRate = 38400;


	public SerialComms(){

	}


	public void initialize() {
		
		serialPort=null;

		serialPort = SerialPort.getCommPort(portName);		
		
		if (serialPort == null) {
			System.out.println("Could not find COM port." + portName);
			return;
		}

		try {
			
			final int DATA_RATE= dataRate; 
			
			serialPort.openPort();
			serialPort.setBaudRate(DATA_RATE); 

			serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
			// open the streams
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();
			
//			try
//			{
//			   for (int j = 0; j < 100000; ++j)
//			    System.out.print((char)input.read());
//			   input.close();
//			} catch (Exception e) { e.printStackTrace(); }

			//serialPort.addEventListener(this);
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}

	
	@Override
	public void serialEvent(SerialPortEvent event) {
		//System.out.println("Serial port events: " + event.getEventType());
		if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
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
		
	}


	/**
	 * Close the serial port. 
	 */
	public synchronized void close() {
		if (serialPort != null) {
			System.out.println("CLOSE THE SERIAL PORT"); 
			serialPort.closePort();
		}
	}

	public static void main(String[] args) throws Exception {
		
		SerialPort[] commPorts = SerialPort.getCommPorts();
		for (int i=0; i<commPorts.length; i++) {
			System.out.println(commPorts[i].getSystemPortName());
		}
		
		
		
		SerialComms main = new SerialComms();
		main.initialize();
		
		main.serialPort.addDataListener(main); 
		
		Thread t=new Thread() {
			public void run() {
				while (true) {
				//the following line will keep this app alive for 1000    seconds,
				//waiting for events to occur and responding to them    (printing incoming messages to console).
				try {Thread.sleep(100);} catch (InterruptedException    ie) {}
				
				try {
					main.getOutputStream().write(10);
					main.getOutputStream().write(35);
					main.getOutputStream().write(43);
					main.getOutputStream().write(120);


				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				}
			}
		};
		t.start();
		System.out.println("Started");
	}


	/**
	 * Get the buffered reader for the serial stream. 
	 * @return the bufferrred reader.
	 */
	public BufferedReader getBufferredInput() {
		return input;
	}
	
	/**
	 * Get the output stream for writing to the serial port. 
	 * @return the output - the output stream. 
	 */
	public OutputStream getOutputStream() {
		return output;
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


	@Override
	public int getListeningEvents() {
		return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
	}




}

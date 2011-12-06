package eu.somatik.arduino.sensorreader;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Reader implements SerialPortEventListener{

	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;
	
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 9600;
	
	/** The port we're normally going to use. */
	private static final String PORT_NAMES[] = { 
			"/dev/tty.usbserial-A9007UX1", // Mac OS X
			"/dev/ttyUSB0", // Linux
			"COM3", // Windows
			};
	
	private final BlockingQueue<Data> dataQueue;
	
	private SerialPort serialPort;
	
	/** Buffered input stream from the port */
	private InputStream input;
	private BufferedReader reader;
	
	/** The output stream to the port */
	private OutputStream output;

	public Reader() {
		this.dataQueue = new LinkedBlockingQueue<Data>();
	}
	
	void initialize() throws IOException{
		CommPortIdentifier portId = findPort();

		try {
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);
			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// open the streams
			input = serialPort.getInputStream();
			reader = new BufferedReader(new InputStreamReader(input));
			output = serialPort.getOutputStream();

			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * This should be called when you stop using the port.
	 * This will prevent port locking on platforms like Linux.
	 */
	synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}
	
	public List<Data> getData(){
		List<Data> all = new ArrayList<Data>();
		dataQueue.drainTo(all);
		return all;
	}

	private CommPortIdentifier findPort() throws IOException {
		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		List<String> found = new ArrayList<String>();
		// iterate through, looking for the port
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			found.add(currPortId.getName());
			for (String portName : PORT_NAMES) {
				if (currPortId.getName().equals(portName)) {
					portId = currPortId;
					break;
				}
			}
		}

		if (portId == null) {
			throw new IOException("Could not find selected COM port, found " + found);
		}
		return portId;
	}


	/**
	 * Handle an event on the serial port. Read the data and print it.
	 */
	@Override
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				String line = reader.readLine();
//				int available = input.available();
//				byte chunk[] = new byte[available];
//				input.read(chunk, 0, available);
//
//				// Displayed results are codepage dependent
//				String read = new String(chunk);
				handle(line);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// Ignore all the other eventTypes, but you should consider the other ones.
	}

	private void handle(String read) {
		Map<String,String> dataMap = new HashMap<String, String>();
		String[] data = read.split("\t");
		for(int i = 0; i < data.length/2;i++){
			dataMap.put(data[i*2].trim(), data[i*2+1].trim());
		}
		int light = Integer.valueOf(dataMap.get("light"));
		float temp = Float.valueOf(dataMap.get("temp"));
		
		dataQueue.add(new Data(System.currentTimeMillis(), light, temp));
		System.out.println(dataMap);
	}

}

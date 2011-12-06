package eu.somatik.arduino.sensorreader;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class Main{
	
	private final Reader reader;
	private final Timer timer;
	
	public Main() {
		this.reader = new Reader();
		this.timer = new Timer("Poster");
	}
	
	public void start() throws IOException{
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				List<Data> dataList = reader.getData();
				for(Data data:dataList){
					System.out.println("received: " + data);
				}
			}
		}, 0, TimeUnit.SECONDS.toMillis(30));
		this.reader.initialize();
		System.out.println("Started");
	}
	
	public void stop(){
		this.reader.close();
		this.timer.cancel();
	}
	
	/**
	 * Needs jvm param -Djava.library.path=/usr/lib/jni/
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		final Main main = new Main();
		main.start();
		Thread shutdown = new Thread("Shutdown"){
			@Override
			public void run() {
				main.stop();
			};
		};
		Runtime.getRuntime().addShutdownHook(shutdown);
	}
}
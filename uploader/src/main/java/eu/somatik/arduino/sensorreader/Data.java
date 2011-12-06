package eu.somatik.arduino.sensorreader;

public class Data {
	public long timestamp;
	public int light;
	public float temp;
	
	public Data(final long timestamp, final int light, final float temp) {
		this.timestamp = timestamp;
		this.light = light;
		this.temp = temp;
	}
	
	@Override
	public String toString() {
		return String.format("%d / %s %s", timestamp, light, temp);
	}
}

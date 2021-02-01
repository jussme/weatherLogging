package base;

import java.lang.Comparable;
import java.lang.ClassCastException;
import java.text.DecimalFormat;

public class WeatherRecord implements Comparable{
	private final float press;
		public float getPress() {return press;}
	private final float temp;
		public float getTemp() {return temp;}
	private final float hum;
		public float getHum() {return hum;}
	private final long obTime;
		public long getObTime() {return obTime;}
	private final String source;
		public String getSource() {return source;}
	
	public WeatherRecord(float press, float temp, float hum, long obTime) {
		this.press = press;
		this.temp = temp;
		this.hum = hum;
		this.obTime = obTime;
		this.source = "";
	}
	
	public WeatherRecord(float press, float temp, float hum, long obTime, String source) {
		this.press = press;
		this.temp = temp;
		this.hum = hum;
		this.obTime = obTime;
		this.source = source;
	}
	
	public boolean equals(WeatherRecord record) {
		if (this.press == record.press &&
			this.temp == record.temp &&
			this.hum == record.hum &&
			this.obTime == record.obTime)
			return true;
			
		return false;
	}
	
	@Override
	public int compareTo(Object record) {
		WeatherRecord recordThrown = (WeatherRecord) record;
		
		if(this.obTime < recordThrown.obTime)
			return -1;
		else
			if(this.obTime > recordThrown.obTime)
				return 1;
			else 
				return 0;
	}
	
	public String toString() {
		//formatters
		DecimalFormat formattingObjectForPress = new DecimalFormat("###.#");
		DecimalFormat formattingObjectForTemp = new DecimalFormat("##.#");
		DecimalFormat formattingObjectForHum = new DecimalFormat("###");
		
		return formattingObjectForPress.format(press) + "\t" +
			formattingObjectForTemp.format(temp) + "\t" +
			formattingObjectForHum.format(hum) + "\t" +
			obTime + "\r\n";
	}
}
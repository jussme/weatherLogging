package base;

public class WeatherRecord implements Comparable<WeatherRecord>{
	private float[] dataArray;
		public float[] getDataArray() {
			return this.dataArray;
		}
	private final long obTime;
		public long getObTime() {
			return this.obTime;
		}
	
	public WeatherRecord(double[] propertiesPlusObTime) {
		dataArray = new float[propertiesPlusObTime.length - 1];
		for(int it = 0; it < dataArray.length; ++it) {
			dataArray[it] = (float) propertiesPlusObTime[it];
		}
		obTime = (long) propertiesPlusObTime[propertiesPlusObTime.length - 1];
	}
	
	public WeatherRecord(float[] properties, long obTime) {
		this.obTime = obTime;
		this.dataArray = properties;
	}
	
	public boolean equals(WeatherRecord record) {
		for(int it = 0; it < this.dataArray.length; ++it) {
			if(this.dataArray[it] != record.dataArray[it])
				return false;
		}
		
		if(this.obTime != record.obTime)
			return false;
		
		return true;
	}
	
	@Override
	public int compareTo(WeatherRecord record) {
		WeatherRecord recordThrown = (WeatherRecord) record;
		
		if(this.obTime < recordThrown.obTime)
			return -1;
		else
			if(this.obTime > recordThrown.obTime)
				return 1;
			else 
				return 0;
	}
	
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		for(float property : this.dataArray) {
			if(!(property != property))
				stringBuilder.append(
						Math.round(property * 10)/10f + "\t"
				);
			else
				stringBuilder.append("NaN\t");
		}
		return stringBuilder.toString() + obTime; 
	}
}
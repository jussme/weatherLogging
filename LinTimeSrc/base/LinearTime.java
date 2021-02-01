package base;

import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;
import java.util.LinkedList;
import java.io.FileWriter;
import java.util.stream.Stream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.util.function.Supplier;

public class LinearTime {
	private final float timeRes;
	private final String fileName;
	public static void main(String[] args) {
		new LinearTime(args);
	}
	//first argument is the file
	//second argument is the time resolution in seconds
	private LinearTime(String[] args) {
		if(args.length == 2) {
			timeRes = Float.valueOf(args[0]);
			fileName = args[1];
		}else
		if(args.length == 1){
			timeRes = 10 * 60;
			fileName = args[0];
		}else{
			System.out.println("The first argument is the log file, the second is the time resolution in seconds");
			System.exit(1);
			//the compilers shouts when final variables may not be initialized
			timeRes = 0;
			fileName = "";
		}
		FileOperator fileOperator = new FileOperator();
		Matcher matcher = new Matcher();
		
		fileOperator.writeFile(
			matcher.matchRecords(fileOperator.extractRecords(0)),
			"log_czechy.txt"
		);
		
		fileOperator.writeFile(
			matcher.matchRecords(fileOperator.extractRecords(1)),
			"log_lipa.txt"
		);
	}
	
	private class Matcher {
		private WeatherRecord[] matchRecords(WeatherRecord[] records) {
			Arrays.sort(records);
			
			long time0 = records[0].getObTime();
			long timeF = records[records.length - 1].getObTime();
			
			//getting the size (arraySize) of the resultant array of records
			//there must be (arraySize) values between the obTime of the first record, and the last one
			int arraySize = (int) Math.ceil((timeF - time0)/timeRes + 1);
			WeatherRecord[]	result = new WeatherRecord[arraySize];
			
			Set<WeatherRecord>[] bufferArray = new TreeSet[arraySize];
			for(int it = 0; it < arraySize; ++it) {
				bufferArray[it] = new TreeSet<WeatherRecord>();
			}
			
			//asigning the records to brackets in the bufferArray
			for(int it = 0; it < records.length; ++it)	{
				int assignedIndex = Math.round((records[it].getObTime() - time0)/timeRes);
				bufferArray[assignedIndex].add(records[it]);
			}
			
			//avareging; if there is no data for the time bracket(the floats in a WeatherRecord were parsed as NaNs),
			//the avareged WeatherRecord constructor will receive AvarageCounterInstance.value/AvarageCounterInstance.nOfAdditions = 0/0 (NaN)
			//as the default initialization values of 'float value' and 'int additions' fields of AvarageCounter are 0; had not planned this :)
			for(int it = 0; it < bufferArray.length; ++it) {
				class AvarageCounter {
					private float value;
					private int additions;
					private void add(float value) {
						//NaN check from stackOverflow
						if(!(value != value)) {
							this.value+= value;
							++additions;
						}
					}
					private float getValue() {
						return this.value;
					}
					private int getNumberOfAdditions() {
						return this.additions;
					}
				}
				
				AvarageCounter press = new AvarageCounter(), temp = new AvarageCounter(), hum = new AvarageCounter();
				long obTime = (long) (time0 + (int)(timeRes * it));
				
				bufferArray[it].forEach(record -> {
					press.add(record.getPress());
					temp.add(record.getTemp());
					hum.add(record.getHum());
				});
				
				int n = bufferArray[it].size();
				result[it] = new WeatherRecord(press.getValue()/press.getNumberOfAdditions(),
					temp.getValue()/temp.getNumberOfAdditions(),
					hum.getValue()/hum.getNumberOfAdditions(),
					obTime);
			}
			
			return result;
		}
	}
	
	private class FileOperator {
		private WeatherRecord[] extractRecords(int sourceNum) {
			BufferedReader bufferedReader = null;
			try{
				bufferedReader = new BufferedReader(new FileReader(new File(fileName).getAbsolutePath()));
			}catch(FileNotFoundException e){
				e.printStackTrace();
				System.exit(1);
			}
			
			List<WeatherRecord> bufferList = new LinkedList<>();
			int nOfStatsPerSource = 4;
			String regex = "([-\\d\\w.,]+)\t+([-\\d\\w.,]+)\t+([-\\d\\w.,]+)\t+([-\\d\\w.,]+)\t+([-\\d\\w.,]+)\t+([-\\d\\w.,]+)\t+([-\\d\\w.,]+)\t+([-\\d\\w.,]+)\t*";
			String line;
			try{
			while( ((line = bufferedReader.readLine()) != null) && !line.isBlank()) {
				try{
					float press = Float.valueOf(line.replaceAll(regex, "$" + (nOfStatsPerSource * sourceNum + 1)).replace(",","."));
				
					float temp = Float.valueOf(line.replaceAll(regex, "$" + (nOfStatsPerSource * sourceNum + 2)).replace(",","."));
				
					float hum = Float.valueOf(line.replaceAll(regex, "$" + (nOfStatsPerSource * sourceNum + 3)).replace(",","."));
				
					long obTime = Long.valueOf(line.replaceAll(regex, "$" + (nOfStatsPerSource * sourceNum + 4)).replace(",","."));
					
					bufferList.add(new WeatherRecord(press, temp, hum, obTime));
				}
				catch(NumberFormatException e){
					System.err.println("! Formatting error for a record below " + bufferList.get(bufferList.size()-1).getObTime());
					continue;
				}
			}
			/*bufferedReader.lines().forEach(line -> {
				float press = Float.valueOf(line.replaceAll(regex, "$" + (nOfStatsPerSource * sourceNum + 1)).replace(",","."));
				
				float temp = Float.valueOf(line.replaceAll(regex, "$" + (nOfStatsPerSource * sourceNum + 2)).replace(",","."));
				
				float hum = Float.valueOf(line.replaceAll(regex, "$" + (nOfStatsPerSource * sourceNum + 3)).replace(",","."));
				
				long obTime = Long.valueOf(line.replaceAll(regex, "$" + (nOfStatsPerSource * sourceNum + 4)).replace(",","."));
				
				bufferList.add(new WeatherRecord(press, temp, hum, obTime));
			});doesnt work, the resultant file is too short; hits a memory limit?*/
			
				bufferedReader.close();
			}catch(IOException e){
				e.printStackTrace();
				System.exit(1);
			}
			
			return Arrays.copyOf(bufferList.toArray(), bufferList.size(), WeatherRecord[].class);
		}
	
		private void writeFile(WeatherRecord[] records, String outputFileName) {
			try{
				File file = new File(outputFileName);
				file.createNewFile();
				FileWriter fileWriter = new FileWriter(file);
				for (WeatherRecord record : records) {
					fileWriter.write(record.toString());
				}
				fileWriter.close();
			}catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}

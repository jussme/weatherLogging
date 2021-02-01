package base;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class LinearTime {
	private final float timeRes;
	private final String filePath;
	
	public static void main(String[] args) {
		new LinearTime(args);
	}
	
	//first argument is the file
	//second argument is the time resolution in seconds
	private LinearTime(String[] args) {
		if(args.length == 2) {
			timeRes = Float.valueOf(args[0]);
			filePath = args[1];
		}else
		if(args.length == 1){
			timeRes = 10 * 60;
			filePath = args[0];
		}else{
			System.out.println("The first argument is the log file path, the second is the time resolution in seconds(def=10m)\n");
			System.exit(0);
			//the compilers shouts when final variables may not be initialized
			timeRes = 0;
			filePath = "";
		}
		
		FileOperator fileOperator = new FileOperator();
		Matcher matcher = new Matcher();
		
		String fileName = filePath.replaceAll("(.+[\\/](.+)[.]txt|^(.+)[.]txt)", "$2$3");//??
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HHmm_ddMMyyyy");
		WeatherRecord[] weatherRecords = fileOperator.extractRecords();
		fileOperator.writeFile(
			matcher.matchRecords(weatherRecords),
			"linTime_" + fileName +"_"+ dtf.format(LocalDateTime.ofEpochSecond(weatherRecords[weatherRecords.length-1].getObTime(), 0, ZoneOffset.ofHours(1))) + ".txt"
		);
	}
	
	private class Matcher {
		private WeatherRecord[] matchRecords(WeatherRecord[] records) {
			//the log should be sorted, so should the array then, but still
			Arrays.sort(records);
			
			long time0 = records[0].getObTime();
			long timeF = records[records.length - 1].getObTime();
			//there must be (arraySize) values between the obTime of the first record and the last one
			//getting the size (arraySize) of the resultant array of records
			int bufferBracketArraySize = (int) Math.ceil((timeF - time0)/timeRes + 1);
			
			//initializing an array of brackets
			Set<WeatherRecord>[] bufferBracketArray = new TreeSet[bufferBracketArraySize];
			for(int it = 0; it < bufferBracketArraySize; ++it) {
				bufferBracketArray[it] = new TreeSet<WeatherRecord>();
			}
			
			//asigning the records to brackets in the bufferBracketsArray
			for(int it = 0; it < records.length; ++it)	{
				int assignedBracketIndex = Math.round((records[it].getObTime() - time0)/timeRes);
				bufferBracketArray[assignedBracketIndex].add(records[it]);
			}
			
			//avareging; if there is no data for the time bracket(the floats in a WeatherRecord were parsed as NaNs),
			//the avareged WeatherRecord constructor will receive AvarageCounterInstance.value/AvarageCounterInstance.nOfAdditions = 0/0 (NaN)
			//as the default initialization values of 'float value' and 'int additions' fields of AvarageCounter are 0; had not planned this :)
			class AverageCounter {
				private float value;
				private int additions;
				
				private void add(float value) {
					//NaN check from stackOverflow
					if(!(value != value)) {
						this.value += value;
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

			int propertiesPerRecordCount = records[0].getDataArray().length;
			//counting the averages for each time bracket
			WeatherRecord[]	result = new WeatherRecord[bufferBracketArray.length];
			for(int it = 0; it < bufferBracketArray.length; ++it) {
				AverageCounter[] averageCounters = new AverageCounter[propertiesPerRecordCount];
				for(int ittt = 0; ittt < propertiesPerRecordCount; ++ittt) {
					averageCounters[ittt] = new AverageCounter();
				}
				
				//summing all record values of a property
				bufferBracketArray[it].forEach(record -> {
					float[] properties = record.getDataArray();
					for(int itt = 0; itt < propertiesPerRecordCount; ++itt) {
						averageCounters[itt].add(properties[itt]);
					}
				});
				
				//dividing the sums to get the averages
				float[] averagedDataArray = new float[propertiesPerRecordCount];
				for(int itt = 0; itt < propertiesPerRecordCount; ++itt) {
					averagedDataArray[itt] = averageCounters[itt].getValue()/averageCounters[itt].getNumberOfAdditions();
				}
				
				long obTime = (long) (time0 + (int)(timeRes * it));
				result[it] = new WeatherRecord(averagedDataArray, obTime);
			}
			
			
			
			return result;
		}
	}
	
	private class FileOperator {
		private WeatherRecord[] extractRecords() {
			List<WeatherRecord> bufferList = new LinkedList<>();
			String regex = "[\t ]+";
			String currentLine;
			String[] splitLineBuffer = null;
			int propertiesCount = 0;
			double[] parsedProperties;
			
			try (BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(filePath).getAbsolutePath()))) {
				//getting the number of properties(+ obTime) in one record
				
				splitLineBuffer = bufferedReader.readLine().split(regex);
				propertiesCount = splitLineBuffer.length;
				
				//processing the first record, since it's been extracted to check the amount of properties in a record(+ obTime)
				parsedProperties = new double[propertiesCount];
				for(int it = 0; it < propertiesCount; ++it) {
					parsedProperties[it] = Double.valueOf(splitLineBuffer[it]);
				}
				bufferList.add(new WeatherRecord(parsedProperties));
				
				//main loop for processing records
				while( bufferedReader.ready() && !(currentLine = bufferedReader.readLine()).isBlank() ) {
					splitLineBuffer = currentLine.split(regex);
					for(int it = 0; it < propertiesCount; ++it) {
						parsedProperties[it] = Double.valueOf(splitLineBuffer[it]);
					}
					
					bufferList.add(new WeatherRecord(parsedProperties));
				}
			}catch(FileNotFoundException e){
				e.printStackTrace();
				System.exit(1);
			} catch (IOException e1) {
				e1.printStackTrace();
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
					fileWriter.write(record.toString() + "\r\n");
				}
				fileWriter.close();
			}catch(IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}

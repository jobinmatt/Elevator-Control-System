 /**
 * 
 */
package Utils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import Exceptions.GeneralExceptions;

/**
 * @author Brij Patel
 *
 */
public class InputParser {
	private static final Logger inputParserLog = LogManager.getLogger(InputParser.class.getName());
	
	@SuppressWarnings("unchecked")
	private List<SimulationParameters> parseCVSFile(String fileName) {
		List<SimulationParameters> simulationEvents = new ArrayList<>();
		try {
			File inputFile = new File(fileName);
			DateFormat df = new SimpleDateFormat("hh:mm:ss.SSS");
			CsvSchema csvSchema = CsvSchema.builder().setUseHeader(true).build();
			CsvMapper csvMapper = new CsvMapper();
			List<Object> inputEvents = csvMapper.readerFor(Map.class).with(csvSchema)
					.readValues(inputFile).readAll();
			inputEvents.toString();
			for(Object event : inputEvents) {
				if(event instanceof LinkedHashMap) {
					LinkedHashMap<String, String> eventInfo = (LinkedHashMap<String, String>) event;
					String timeString = eventInfo.get("Time");
					Date simulationDate = df.parse(timeString);
					boolean floorButtonDirection;
					if(eventInfo.get("Floor Button").equalsIgnoreCase("Up")) {
						floorButtonDirection = true;
					}
					else if(eventInfo.get("Floor Button").equalsIgnoreCase("Down")) {
						floorButtonDirection = false;
					}
					else {
						throw new GeneralExceptions("Floor Button string is not valid");
					}
					
					simulationEvents.add(new SimulationParameters(simulationDate, Integer.valueOf(eventInfo.get("Floor")), floorButtonDirection,
							Integer.valueOf(eventInfo.get("Car Button"))));
				} else {
					throw new GeneralExceptions("File not in correct format");
				}
			}
		} catch (IOException | GeneralExceptions e) {
			inputParserLog.error(e.getLocalizedMessage());
		} catch (ParseException e) {
			inputParserLog.error("Time String not in correct format");
		}
		
		return simulationEvents;
	}
	
}

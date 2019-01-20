//****************************************************************************
//
// Filename: InputParser.java
//
// Description: Takes in a CVS files and converts it into SimulationParameters
//
//***************************************************************************
package core.Utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import core.Exceptions.InputParserException;

/**
 * @author Brij Patel
 *
 */
public class InputParser {
	private static Logger logger = LogManager.getLogger(InputParser.class);
	private static final String TIME_FORMAT = "hh:mm:ss.SSS";
	private static final String TIME_HEADER = "Time";
	private static final String FLOOR_BUTTON_HEADER = "Floor Button";
	private static final String CAR_BUTTON_HEADER = "Car Button";
	private static final String FLOOR_HEADER = "Floor";

	@SuppressWarnings("unchecked")
	public static List<SimulationEvent> parseCVSFile(String fileName)
			throws InputParserException {
		List<SimulationEvent> simulationEvents = new ArrayList<>();
		long baseIntervalTime = -1;
		try {
			File inputFile = new File(Utils.getBuildDirURI(fileName).getPath());
			DateFormat df = new SimpleDateFormat(TIME_FORMAT);
			CsvSchema csvSchema = CsvSchema.builder().setUseHeader(true).build();
			CsvMapper csvMapper = new CsvMapper();
			List<Object> inputEvents = csvMapper.readerFor(Map.class).with(csvSchema).readValues(inputFile).readAll();
			for (Object event : inputEvents) {
				if (event instanceof LinkedHashMap) {
					LinkedHashMap<String, String> eventInfo = (LinkedHashMap<String, String>) event;
					if (isValidData(eventInfo)) {
						String timeString = eventInfo.get(TIME_HEADER);
						Date simulationDate = df.parse(timeString);
						boolean floorButtonDirection;
						if (eventInfo.get(FLOOR_BUTTON_HEADER).equalsIgnoreCase("Up")) {
							floorButtonDirection = true;
						} else if (eventInfo.get(FLOOR_BUTTON_HEADER).equalsIgnoreCase("Down")) {
							floorButtonDirection = false;
						} else {
							throw new InputParserException("Floor Button string is not valid");
						}
						simulationEvents
						.add(new SimulationEvent(simulationDate, Integer.valueOf(eventInfo.get(FLOOR_HEADER)),
								floorButtonDirection, Integer.valueOf(eventInfo.get(CAR_BUTTON_HEADER))));
						logger.debug("SimulationEvent: " + eventInfo.toString() + " created");
					}
				} else {
					throw new InputParserException("File not in correct format");
				}
			}
			Collections.sort(simulationEvents);
			baseIntervalTime = simulationEvents.get(0).getStartTime().getTime();
			for (SimulationEvent e : simulationEvents) {
				e.setIntervalTime(e.getStartTime().getTime() - baseIntervalTime);
			}
			return simulationEvents;
		} catch (NumberFormatException e) {
			throw new InputParserException(e.getLocalizedMessage());
		} catch (URISyntaxException e1) {
			throw new InputParserException(e1.getLocalizedMessage());
		} catch (IOException e1) {
			throw new InputParserException(e1.getLocalizedMessage());
		} catch (ParseException e1) {
			throw new InputParserException(e1.getLocalizedMessage());
		}
	}

	private static boolean isValidData(LinkedHashMap<String, String> eventInfo) throws InputParserException {
		if (eventInfo.containsKey(TIME_HEADER) && eventInfo.containsKey(FLOOR_BUTTON_HEADER)
				&& eventInfo.containsKey(FLOOR_HEADER) && eventInfo.containsKey(CAR_BUTTON_HEADER)) {
			if (!StringUtils.isEmpty(eventInfo.get(TIME_HEADER))
					&& !StringUtils.isEmpty(eventInfo.get(FLOOR_BUTTON_HEADER))
					&& !StringUtils.isEmpty(eventInfo.get(FLOOR_HEADER))
					&& !StringUtils.isEmpty(eventInfo.get(CAR_BUTTON_HEADER))) {
				if (isNumber(eventInfo.get(FLOOR_HEADER)) && isNumber(eventInfo.get(CAR_BUTTON_HEADER))) {
					return true;
				}
			}
		}
		throw new InputParserException(eventInfo.toString() + ": not in the proper format or empty");
	}

	private static boolean isNumber(String s) {
		boolean isNumber = true;
		for (char c : s.toCharArray()) {
			isNumber = isNumber && Character.isDigit(c);
		}
		return isNumber;
	}

}

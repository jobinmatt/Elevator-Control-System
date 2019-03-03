//****************************************************************************
//
// Filename: InputParser.java
//
// Description: Class gets the CVS file defined in the configuration and
//              parses it to create a list of SimulationEvents
//
// @author Brij Patel
//***************************************************************************

package core;

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

import core.Exceptions.ConfigurationParserException;
import core.Exceptions.InputParserException;
import core.Utils.SimulationRequest;
import core.Utils.Utils;

/**
 * Class gets the CVS file defined in the configuration and parses it to create
 * a list of SimulationEvents
 */
public class InputParser {

	private static Logger logger = LogManager.getLogger(InputParser.class);

	private static final String TIME_FORMAT = "HH:mm:ss.SSS";
	private static final String TIME_HEADER = "Time";
	private static final String FLOOR_BUTTON_HEADER = "Floor_Button";
	private static final String CAR_BUTTON_HEADER = "Car_Button";
	private static final String FLOOR_HEADER = "Floor";
	private static final String ERROR_CODE_HEADER = "ErrorCode";
	private static final String ERROR_ELEVATOR_HEADER = "ErrorElevator";

	@SuppressWarnings({ "unchecked", "deprecation" })
	public static List<SimulationRequest> parseCVSFile() throws InputParserException {

		logger.info("Parsing CVS File... ");
		List<SimulationRequest> simulationEvents = new ArrayList<>();
		long baseIntervalTime = -1;

		try {
			ConfigurationParser configurationParser = ConfigurationParser.getInstance();

			String fileName = configurationParser.getString(ConfigurationParser.CVS_FILENAME);
			logger.debug("CVS File Name: " + fileName);
			File inputFile = new File(Utils.getBuildDirURI(fileName));
			DateFormat df = new SimpleDateFormat(TIME_FORMAT);
			CsvSchema csvSchema = CsvSchema.builder().setUseHeader(true).setColumnSeparator(' ').build();
			CsvMapper csvMapper = new CsvMapper();
			List<Object> inputEvents = csvMapper.readerFor(Map.class).with(csvSchema).readValues(inputFile).readAll();

			for (Object event : inputEvents) {

				if (event instanceof LinkedHashMap) {

					LinkedHashMap<String, String> eventInfo = (LinkedHashMap<String, String>) event;

					if (isValidData(eventInfo)) {
						String timeString = eventInfo.get(TIME_HEADER);
						Date simulationDate = df.parse(timeString);
						Date todayDate = new Date();
						todayDate.setHours(simulationDate.getHours());
						todayDate.setMinutes(simulationDate.getMinutes());
						todayDate.setSeconds(simulationDate.getSeconds());
						Direction floorButtonDirection;

						if (eventInfo.get(FLOOR_BUTTON_HEADER).equalsIgnoreCase("Up")) {
							floorButtonDirection = Direction.UP;
						} else if (eventInfo.get(FLOOR_BUTTON_HEADER).equalsIgnoreCase("Down")) {
							floorButtonDirection = Direction.DOWN;
						} else {
							throw new InputParserException("Floor Button string is not valid");
						}

						simulationEvents
						.add(new SimulationRequest(todayDate, Integer.valueOf(eventInfo.get(FLOOR_HEADER)),
								floorButtonDirection, Integer.valueOf(eventInfo.get(CAR_BUTTON_HEADER)), Integer.valueOf(eventInfo.get(ERROR_CODE_HEADER)), Integer.valueOf(eventInfo.get(ERROR_ELEVATOR_HEADER))));

						logger.debug("SimulationEvent: " + eventInfo.toString() + " created");
					}
				} else {
					throw new InputParserException("File not in correct format");
				}
			}

			Collections.sort(simulationEvents);
			baseIntervalTime = simulationEvents.get(0).getStartTime().getTime();

			for (SimulationRequest e : simulationEvents) {
				e.setIntervalTime(e.getStartTime().getTime() - baseIntervalTime);
			}

			logger.log(LoggingManager.getSuccessLevel(), LoggingManager.SUCCESS_MESSAGE);
			return simulationEvents;
		} catch (NumberFormatException | IOException | ParseException e) {
			throw new InputParserException("Unable to parse file");
		} catch (ConfigurationParserException | URISyntaxException e1) {
			throw new InputParserException("Error with the configuration file");
		}
	}

	private static boolean isValidData(LinkedHashMap<String, String> eventInfo) throws InputParserException {

		if (eventInfo.containsKey(TIME_HEADER) && eventInfo.containsKey(FLOOR_BUTTON_HEADER) &&
				eventInfo.containsKey(FLOOR_HEADER) && eventInfo.containsKey(CAR_BUTTON_HEADER) &&
				eventInfo.containsKey(ERROR_CODE_HEADER) && eventInfo.containsKey(ERROR_ELEVATOR_HEADER)) {

			if (!StringUtils.isEmpty(eventInfo.get(TIME_HEADER)) &&
					!StringUtils.isEmpty(eventInfo.get(FLOOR_BUTTON_HEADER)) &&
					!StringUtils.isEmpty(eventInfo.get(FLOOR_HEADER)) &&
					!StringUtils.isEmpty(eventInfo.get(CAR_BUTTON_HEADER)) &&
					!StringUtils.isEmpty(eventInfo.get(ERROR_ELEVATOR_HEADER)) &&
					!StringUtils.isEmpty(eventInfo.get(ERROR_CODE_HEADER))) {

				if (isNumber(eventInfo.get(FLOOR_HEADER)) && isNumber(eventInfo.get(CAR_BUTTON_HEADER)) &&
						isNumber(eventInfo.get(ERROR_CODE_HEADER)) && isNumber(eventInfo.get(ERROR_ELEVATOR_HEADER))) {
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

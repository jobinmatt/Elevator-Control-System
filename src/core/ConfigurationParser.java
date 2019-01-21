//****************************************************************************
//
// Filename: ConfigurationParser.java
//
// Description: Parser for configuration file
//
//***************************************************************************

package core;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConversionException;
import org.apache.commons.configuration2.tree.xpath.XPathExpressionEngine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.Utils.Utils;
import core.Exceptions.ConfigurationParserException;

public class ConfigurationParser {

    private static Logger logger = LogManager.getLogger(ConfigurationParser.class);

    private static ConfigurationParser configurationParserInstance = null;
    private static XMLConfiguration configuration = null;
    
    public static final String NUMBER_OF_ELEVATORS = "NumberOfElevators";
    public static final String NUMBER_OF_FLOORS= "NumberOfFloors";
    public static final String ELEVATOR_DOOR_TIME_SECONDS = "ElevatorDoorTimeSeconds";
    public static final String ELEVATOR_FLOOR_TRAVEL_TIME_SECONDS = "ElevatorFloorTravelTimeSeconds";

    public static String initialPath = "//Config/";

    public synchronized static ConfigurationParser getInstance() throws ConfigurationParserException {

        if (configurationParserInstance == null) {
            configurationParserInstance = new ConfigurationParser();
            configurationParserInstance.LoadConfig();
        }
        return configurationParserInstance;
    }

    protected void LoadConfig() throws ConfigurationParserException {

        try {
            logger.info("Loading Configuration File...");

            configuration = new XMLConfiguration();
            URI configFileURI= Utils.getBuildDirURI("Configuration.xml");
            if (configFileURI == null) {
                throw new ConfigurationParserException("Impossible to locate Configuration.xml");
            }

            Parameters params = new Parameters();
            File configFile = new File(configFileURI);
            FileBasedConfigurationBuilder<XMLConfiguration> fileConfigurationBuilder = new FileBasedConfigurationBuilder<XMLConfiguration>(XMLConfiguration.class);
            fileConfigurationBuilder.configure(params.fileBased().setFile(configFile));

            configuration = fileConfigurationBuilder.getConfiguration();
            configuration.setThrowExceptionOnMissing(true);
            configuration.setExpressionEngine(new XPathExpressionEngine());
            configuration.read(new File(configFileURI).toURI().toURL().openStream());

            logger.log(LoggingManager.getSuccessLevel(), LoggingManager.SUCCESS_MESSAGE);
        } catch (ConfigurationException | URISyntaxException | IOException e) {
            throw new ConfigurationParserException(e);
        }
     }

    public String getString(String str) throws ConfigurationParserException {

        try {
            return configuration.getString(initialPath + str);
        } catch (NoSuchElementException | ConversionException e) {
            throw new ConfigurationParserException(e);
        }
    }

    public int getInt(String str) throws ConfigurationParserException {

        try {
            return configuration.getInt(initialPath + str);
        } catch (NoSuchElementException | ConversionException e) {
            throw new ConfigurationParserException(e);
        }
    }

    public boolean getBoolean(String str) throws ConfigurationParserException {

        try {
            return configuration.getBoolean(initialPath + str);
        } catch (NoSuchElementException nse) {
            throw new ConfigurationParserException(nse);
        } catch (ConversionException ce) {
            throw new ConfigurationParserException("Element not of type Boolean: 'true' or 'false' ", ce);
        }
    }
}

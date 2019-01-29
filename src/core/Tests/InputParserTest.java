package core.Tests;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import core.InputParser;
import core.Exceptions.InputParserException;
import core.Utils.SimulationRequest;

class InputParserTest {

	@BeforeEach
	void setUp() throws Exception {

	}

	@AfterEach
	void tearDown() throws Exception {

	}

	@Test
	void testParseCVSFile() throws InputParserException {
		List<SimulationRequest> testImport = InputParser.parseCVSFile();
		if (testImport.isEmpty()) {
			fail("List empty");
		} else {

		}
	}

}

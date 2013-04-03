package cz.zcu.kiv.multicloud.core.json;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Extractor {

	JSONParser parser = new JSONParser();

	public void parse(String data) throws ParseException {
		parser.parse(data);
	}

}

package cz.zcu.kiv.multicloud.core.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Json {

	private static Json instance;

	public static Json getInstance() {
		if (instance == null) {
			instance = new Json();
		}
		return instance;
	}

	private final JsonFactory factory;
	private final ObjectMapper mapper;

	private Json() {
		factory = new JsonFactory();
		mapper = new ObjectMapper();
	}

	public JsonFactory getFactory() {
		return factory;
	}

	public ObjectMapper getMapper() {
		return mapper;
	}

}

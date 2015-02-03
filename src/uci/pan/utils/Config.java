package uci.pan.utils;

import java.io.IOException;
import java.util.Properties;

public class Config {
	public static String BASE_PATH;
	public static String CLASS_NAME;
	public static String OUTPUT_FILE_PATH;
	public static int NUM_TREES;
	public static int NUM_FOLDS;
	public static int NUM_REPEATS;

	static {
		Properties prop = new Properties();
		try {
			prop.load(Config.class.getClassLoader().getResourceAsStream(
					"ml.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		BASE_PATH = prop.getProperty("base.path");
		CLASS_NAME = prop.getProperty("class.name");
		OUTPUT_FILE_PATH = BASE_PATH + prop.getProperty("output.file.name");
		NUM_TREES = Integer.parseInt(prop.getProperty("num.trees"));
		NUM_FOLDS = Integer.parseInt(prop.getProperty("num.folds"));
		NUM_REPEATS = Integer.parseInt(prop.getProperty("num.repeats"));

	}
}

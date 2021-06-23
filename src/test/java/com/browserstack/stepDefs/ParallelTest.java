package com.browserstack.stepDefs;

import io.cucumber.core.cli.Main;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.browserstack.utils.Utility;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public class ParallelTest {
    public static ThreadLocal<JSONObject> threadLocalValue = new ThreadLocal<>();

    public static void main(String[] args) throws IOException, ParseException {
        JSONObject testConfigs;
        JSONObject testSelectedConfig;
        JSONParser parser = new JSONParser();
        if (System.getenv("caps") != null) {
            testConfigs = (JSONObject) parser.parse(System.getenv("caps"));
        } else {
            System.out.println("testConfigs");
            testConfigs = (JSONObject) parser.parse(new FileReader("resources/conf/caps.json"));
            System.out.println(testConfigs);
        }
        if (System.getProperty("caps-type") != null) {
            testSelectedConfig = (JSONObject) ((JSONObject) testConfigs.get("tests")).get(System.getProperty("caps-type"));
        } else {
            testSelectedConfig = (JSONObject) ((JSONObject) testConfigs.get("tests")).get("parallel");
        }
        JSONArray environments = (JSONArray) testSelectedConfig.get("env_caps");
        for (Object obj : environments) {
            JSONObject singleConfig = Utility.getCombinedCapability((Map<String, String>) obj, testConfigs, testSelectedConfig);
            Thread thread = new Thread(() -> {
                System.setProperty("parallel", "true");
                threadLocalValue.set(singleConfig);
                try {
                    String[] argv = new String[]{"-g", "", "resources/features/"};
                    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                    Main.run(argv, contextClassLoader);
                } catch (Exception e) {
                    e.getStackTrace();
                } finally {
                    threadLocalValue.remove();
                }
            });
            thread.start();
        }
    }
}

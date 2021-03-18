package stepDefs;


import com.browserstack.local.Local;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import utils.Utility;

import java.io.FileReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SetupSteps {
    public StepData stepData;
    private static final String URL = "https://bstackdemo.com";
    private Local local;

    public SetupSteps(StepData stepData) {
        this.stepData = stepData;
    }
    @Before
    public void setUp(Scenario scenario) throws Exception {

        JSONObject config;
        JSONObject capabilityObject;
        JSONParser parser = new JSONParser();
        DesiredCapabilities caps = new DesiredCapabilities();

        if (StringUtils.isNoneEmpty(System.getProperty("env")) && System.getProperty("env").equalsIgnoreCase("on-prem")) {

            stepData.url = URL;
            stepData.webDriver = new ChromeDriver();
            stepData.webDriver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
            stepData.webDriver.get(stepData.url);
            stepData.webDriver.manage().window().maximize();

        } else if (StringUtils.isNoneEmpty(System.getProperty("env")) && System.getProperty("env").equalsIgnoreCase("docker")) {
            caps.setBrowserName("chrome");
            caps.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            stepData.webDriver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), DesiredCapabilities.chrome());
            stepData.webDriver.manage().window().maximize();
            stepData.url = URL;
        } else if (StringUtils.isNoneEmpty(System.getProperty("env")) && System.getProperty("env").equalsIgnoreCase("remote")) {
            if (System.getenv("caps") != null) {
                config = (JSONObject) parser.parse(System.getenv("caps"));
            } else {
                config = (JSONObject) parser.parse(new FileReader("resources/conf/caps.json"));
            }
            if (System.getProperty("parallel") != null) {
                capabilityObject = new JSONObject();
            } else {
                JSONObject singleCapabilityJson = (JSONObject) ((JSONObject) config.get("tests")).get("local");
                JSONArray environments = (JSONArray) singleCapabilityJson.get("env_caps");
                capabilityObject = Utility.getCombinedCapability((Map<String, String>) environments.get(0), config, singleCapabilityJson);
            }

            Map<String, String> commonCapabilities = (Map<String, String>) capabilityObject.get("capabilities");
            Iterator it = commonCapabilities.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                if (caps.getCapability(pair.getKey().toString()) == null) {
                    caps.setCapability(pair.getKey().toString(), pair.getValue().toString());
                }
            }
            caps.setCapability("name", scenario.getName());

            String username = System.getenv("BROWSERSTACK_USERNAME");
            if (username == null) {
                username = (String) capabilityObject.get("user");
            }
            String accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY");
            if (accessKey == null) {
                accessKey = (String) capabilityObject.get("key");
            }
            stepData.url = (String) capabilityObject.get("application_endpoint");
            if (caps.getCapability("browserstack.local") != null && caps.getCapability("browserstack.local").equals("true")) {
                String localIdentifier = RandomStringUtils.randomAlphabetic(8);
                caps.setCapability("browserstack.localIdentifier", localIdentifier);
                local = new Local();
                Map<String, String> options = Utility.getLocalOptions(config);
                options.put("key", accessKey);
                options.put("localIdentifier", localIdentifier);
                local.start(options);
            }
            System.out.println(caps.toString());
            String URL = String.format("https://%s:%s@hub.browserstack.com/wd/hub", username, accessKey);
            stepData.webDriver = new RemoteWebDriver(new URL(URL), caps);
        }
    }

    @After
    public void teardown(Scenario scenario) throws Exception {
        stepData.webDriver.quit();
    }
}

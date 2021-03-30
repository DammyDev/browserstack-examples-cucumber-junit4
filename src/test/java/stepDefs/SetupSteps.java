package stepDefs;

import com.browserstack.local.Local;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import utils.Utility;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SetupSteps {

    public StepData stepData;
    private static final String bstackAppUrl = "https://bstackdemo.com";
    private Local bstackLocal;
    private static JSONObject config;

    static {
        try {
            config = Utility.getConfig();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SetupSteps(StepData stepData) {
        this.stepData = stepData;
    }

    @Before
    @org.junit.Before
    public void setUp(Scenario scenario) throws Exception {

        JSONObject capabilityObject = new JSONObject();
        DesiredCapabilities caps = new DesiredCapabilities();

        String osCaps = Utility.getOsCaps();
        if (StringUtils.isNoneEmpty(System.getProperty("env")) && System.getProperty("env").equalsIgnoreCase("on-prem")) {

            stepData.url = bstackAppUrl;
            stepData.webDriver = new ChromeDriver();
            stepData.webDriver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
            stepData.webDriver.get(stepData.url);
            stepData.webDriver.manage().window().maximize();

        } else if (StringUtils.isNoneEmpty(System.getProperty("env")) && System.getProperty("env").equalsIgnoreCase("docker")) {
            caps.setBrowserName("chrome");
            caps.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
            stepData.webDriver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), DesiredCapabilities.chrome());
            stepData.webDriver.manage().window().maximize();
            stepData.url = bstackAppUrl;
        } else if (StringUtils.isNoneEmpty(System.getProperty("env")) && System.getProperty("env").equalsIgnoreCase("remote")) {

            JSONArray environments;

            if (System.getProperty("caps-type").equalsIgnoreCase("parallel")) {
                System.out.println("ParallelTest");
                capabilityObject = ParallelTest.threadLocalValue.get();
            } else {
                JSONObject capabilityJson = (JSONObject) ((JSONObject) config.get("tests")).get(osCaps);
                environments = (JSONArray) capabilityJson.get("env_caps");
                capabilityObject = Utility.getCombinedCapability((Map<String, String>) environments.get(0), config, capabilityJson);
            }

            stepData.url = (String) capabilityObject.get("application_endpoint");
            String bstackUrl = Utility.getRemoteUrl(capabilityObject);
            Map<String, String> commonCapabilities = (Map<String, String>) capabilityObject.get("capabilities");
            Iterator it = commonCapabilities.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                if (caps.getCapability(pair.getKey().toString()) == null) {
                    caps.setCapability(pair.getKey().toString(), pair.getValue().toString());
                }
            }

            if (caps.getCapability("browserstack.local") != null && caps.getCapability("browserstack.local").equals("true")) {
                String localIdentifier = RandomStringUtils.randomAlphabetic(8);
                caps.setCapability("browserstack.localIdentifier", localIdentifier);
                bstackLocal = new Local();
                Map<String, String> options = Utility.getLocalOptions(config);
                System.out.println((String) capabilityObject.get("key"));
                options.put("key", (String) capabilityObject.get("key"));
                options.put("localIdentifier", localIdentifier);
                System.out.println("Local Start");
                bstackLocal.start(options);
            }
            caps.setCapability("name", scenario.getName());
            stepData.webDriver = new RemoteWebDriver(new URL(bstackUrl), caps);
        }
    }

    private void setupLocal(DesiredCapabilities caps, JSONObject testConfigs, String accessKey) throws Exception {
        if (caps.getCapability("browserstack.local") != null && caps.getCapability("browserstack.local").equals("true")) {
            String localIdentifier = RandomStringUtils.randomAlphabetic(8);
            caps.setCapability("browserstack.localIdentifier", localIdentifier);
            bstackLocal = new Local();
            Map<String, String> options = Utility.getLocalOptions(testConfigs);
            options.put("key", accessKey);
            options.put("localIdentifier", localIdentifier);
            bstackLocal.start(options);
        }
    }

    @After
    @org.junit.After
    public void teardown(Scenario scenario) throws Exception {
        stepData.webDriver.quit();
    }
}

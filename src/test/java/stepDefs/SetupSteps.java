package stepDefs;

import com.browserstack.local.Local;
import io.cucumber.core.cli.Main;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import utils.Parallelized;
import utils.Utility;

import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith(Parallelized.class)
public class SetupSteps {

    public StepData stepData;
    private static final String URL = "https://bstackdemo.com";
    private Local local;
    private static JSONObject config;
    public static ThreadLocal<JSONObject> threadLocalValue = new ThreadLocal<>();

    @Parameterized.Parameter(value = 0)
    public int taskID;

    @Parameterized.Parameters
    public static Iterable<? extends Object> data() throws Exception {
        List<Integer> taskIDs = new ArrayList<Integer>();

        if(System.getProperty("caps-type").equalsIgnoreCase("parallel")) {
            JSONParser parser = new JSONParser();
            config = (JSONObject) parser.parse(new FileReader("resources/conf/caps.json"));
            int envs = ((JSONArray)config.get("environments")).size();

            for(int i=0; i<envs; i++) {
                taskIDs.add(i);
            }
        }
        return taskIDs;
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

            JSONArray environments;
            config = Utility.getConfig();

            JSONObject capabilityJson = (JSONObject) ((JSONObject) config.get("tests")).get(osCaps);
            environments = (JSONArray) capabilityJson.get("env_caps");
            System.out.println(taskID);

            capabilityObject = Utility.getCombinedCapability((Map<String, String>) environments.get(taskID), config, capabilityJson);

            stepData.url = (String) capabilityObject.get("application_endpoint");
            String URL = Utility.getRemoteUrl(capabilityObject);

            caps = Utility.getFinalCaps(capabilityObject);

            caps.setCapability("name", scenario.getName());
            System.out.println(caps.toString());

            stepData.webDriver = new RemoteWebDriver(new URL(URL), caps);
        }
    }

    @After
    @org.junit.After
    public void teardown(Scenario scenario) throws Exception {
        stepData.webDriver.quit();
    }
}

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import stepDefs.SetupSteps;
import stepDefs.StepData;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "resources/features" ,
        glue= "stepDefs")
public class RunCucumberTests extends SetupSteps {

    public RunCucumberTests(StepData stepData) {
        super(stepData);
    }
}

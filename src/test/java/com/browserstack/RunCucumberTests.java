package com.browserstack;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/com/browserstack" ,
        glue= "src/test/java/com/browserstack/stepDefs")
public class RunCucumberTests {

}

package stepDefs;

import io.cucumber.java.en.And;
import org.openqa.selenium.By;

public class CheckoutPageSteps {

    private StepData stepData;

    public CheckoutPageSteps(StepData stepData) {
        this.stepData = stepData;
    }

    @And("I type {string} in Post Code")
    public void iTypeInPostCode(String postCode) {
        stepData.webDriver.findElement(By.cssSelector(".dynamic-form-field--postCode #provinceInput")).sendKeys(postCode);
    }

    @And("I click on Checkout Button")
    public void iClickOnCheckoutButton() throws InterruptedException {
        stepData.webDriver.findElement(By.id("checkout-shipping-continue")).click();
        Thread.sleep(1500);
        stepData.webDriver.findElement(By.cssSelector(".button")).click();
        Thread.sleep(1000);
    }

}

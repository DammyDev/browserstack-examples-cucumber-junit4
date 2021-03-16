package stepDefs;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

public class HomePageSteps {

    private StepData stepData;

    public HomePageSteps(StepData stepData) {
        this.stepData = stepData;
    }

    @And("I add two products to cart")
    public void iAddProductsToCart() throws InterruptedException {
        stepData.webDriver.findElement(By.cssSelector("#\\31 > .shelf-item__buy-btn")).click();
        Thread.sleep(1000);
        stepData.webDriver.findElement(By.cssSelector("#__next > div > div > div.float-cart.float-cart--open > div.float-cart__close-btn")).click();
        stepData.webDriver.findElement(By.cssSelector("#\\32 > .shelf-item__buy-btn")).click();
        Thread.sleep(1000);
    }

    @And("I click on Buy Button")
    public void iClickOnBuyButton() throws InterruptedException {
        stepData.webDriver.findElement(By.cssSelector(".buy-btn")).click();
        Thread.sleep(1000);
    }

    @And("I press the Apple Vendor Filter")
    public void iPressTheAppleVendorFilter() throws InterruptedException {
        stepData.webDriver.findElement(By.cssSelector(".filters-available-size:nth-child(2) .checkmark")).click();
        Thread.sleep(1000);
    }

    @And("I order by lowest to highest")
    public void iOrderByLowestToHighest() throws InterruptedException {
        WebElement dropdown = stepData.webDriver.findElement(By.cssSelector("select"));
        dropdown.findElement(By.xpath("//option[. = 'Lowest to highest']")).click();
        Thread.sleep(1000);
    }

    @Then("I should see user {string} logged in")
    public void iShouldUserLoggedIn(String user) {
        try {
            String loggedInUser = stepData.webDriver.findElement(By.cssSelector(".username")).getText();
            Assert.assertEquals(user, loggedInUser);
        } catch (NoSuchElementException e) {
            throw new AssertionError(user+" is not logged in");
        }
    }

    @Then("I should see no image loaded")
    public void iShouldSeeNoImageLoaded() {
        try {
            String src = stepData.webDriver.findElement(By.xpath("//img[@alt='iPhone 12']")).getAttribute("src");
            Assert.assertEquals("", src);
        } catch (NoSuchElementException e) {
            throw new AssertionError("Error in logging in");
        }
    }

    @Then("I should see {int} items in the list")
    public void iShouldSeeItemsInTheList(int productCount) {
        try{
            String products = stepData.webDriver.findElement(By.cssSelector(".products-found > span")).getText();
            Assert.assertEquals(products,productCount+" Product(s) found.");
        } catch (NoSuchElementException e) {
            throw new AssertionError("Error in page load");
        }
    }

    @Then("I should see prices in ascending order")
    public void iShouldSeePricesInAscendingOrder() {
        try {
            int secondElementPrice = Integer.parseInt(stepData.webDriver.findElement(By.cssSelector("#\\32 5 .val > b")).getText());
            int firstElementPrice = Integer.parseInt(stepData.webDriver.findElement(By.cssSelector("#\\31 9 .val > b")).getText());
            Assert.assertTrue(secondElementPrice>firstElementPrice);
        } catch (NoSuchElementException e) {
            throw new AssertionError("Error in page load");
        }
    }

}

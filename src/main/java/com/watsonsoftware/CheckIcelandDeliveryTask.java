package com.watsonsoftware;

import com.watsonsoftware.slack.SlackMessage;
import com.watsonsoftware.slack.SlackUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class CheckIcelandDeliveryTask extends TimerTask {

    private static final String DELIVERY_URL = "https://www.iceland.co.uk/book-delivery";
    private static final String POSTCODE = "BT14 8LF";

    @Override
    public void run() {
        Set<String> slotDetails = new HashSet<>();
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, 10000);
        try {
            driver.get(DELIVERY_URL);
            Cookie ck = new Cookie.Builder("dw_shippostalcode", POSTCODE)
                    .domain("www.iceland.co.uk")
                    .path("/")
                    .isHttpOnly(false)
                    .isSecure(true)
                    .build();
            driver.manage().addCookie(ck);
            Thread.sleep(1000);
            WebElement modalButton = driver.findElement(By.id("modal-close-button"));
            Actions actions = new Actions(driver);
            actions.moveToElement(modalButton).click().perform();
            wait.until(presenceOfElementLocated(By.id("postal-code")));
            WebElement postcodeField = driver.findElement(By.id("postal-code"));
            postcodeField.sendKeys(POSTCODE);
            WebElement postcodeButton = driver.findElement(By.name("dwfrm_singleshipping_shippingAddress_addressFields_checkPostcode"));
            postcodeButton.click();
            Thread.sleep(1000);
            modalButton = driver.findElement(By.id("modal-close-button"));
            modalButton.click();
            wait.until(presenceOfElementLocated(By.className("delivery-schedule-days")));
            List<WebElement> slotTabs = driver.findElements(By.className("delivery-schedule-day"));
            slotTabs.forEach(slotTab -> {
                String date = slotTab.getAttribute("data-slots-key");
                slotTab.click();
                wait.until(presenceOfElementLocated(By.className("delivery-schedule-slots")));
                WebElement currentSayDeliverySlot = driver.findElement(By.cssSelector("div.delivery-schedule-slots.active"));
                List<WebElement> slots = currentSayDeliverySlot.findElements(By.className("delivery-schedule-slot"));
                for (WebElement slot : slots) {
                    String[] texts = slot.getText().split("\\n");
                    if (!texts[1].equals("Sorry this slot is fully booke")) {
                        slotDetails.add(date);
                    }
                }
            });
            if (!slotDetails.isEmpty()) {
                sendSlackMessage(slotDetails);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            driver.quit();
        }
    }

    private void sendSlackMessage(Set<String> availableSlots) {
        String slotDetails = String.join("\n", availableSlots);
        SlackMessage message = SlackMessage.builder()
                                           .channel("random")
                                           .username("Delivery Bot")
                                           .text("<@UDKQ8R6H1> There are available delivery slots for Asda:\n" + slotDetails)
                                           .icon_emoji(":twice:")
                                           .build();
        SlackUtils.sendMessage(message);
    }
}

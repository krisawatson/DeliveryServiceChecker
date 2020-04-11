package com.watsonsoftware.store;

import com.watsonsoftware.Logger;
import com.watsonsoftware.config.IcelandConfig;
import com.watsonsoftware.config.SlackConfig;
import com.watsonsoftware.slack.SlackUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class CheckIcelandDeliveryTask extends TimerTask {

    private static final Set<String> NOTIFIED_SLOTS = new HashSet<>();
    private static final WebDriver driver = new ChromeDriver();

    private final SlackConfig slackConfig;
    private final IcelandConfig icelandConfig;

    private CheckIcelandDeliveryTask(SlackConfig slackConfig, IcelandConfig icelandConfig) {
        this.slackConfig = slackConfig;
        this.icelandConfig = icelandConfig;
    }

    public static CheckIcelandDeliveryTask create(SlackConfig slackConfig, IcelandConfig icelandConfig) {
        return new CheckIcelandDeliveryTask(slackConfig, icelandConfig);
    }

    @Override
    public void run() {
        Logger.info("Running request to check for available Iceland slots");
        Set<String> slotDetails = new HashSet<>();
        WebDriverWait wait = new WebDriverWait(driver, 10000);
        try {
            driver.get(icelandConfig.getUrl());
            Cookie ck = new Cookie.Builder("dw_shippostalcode", icelandConfig.getPostcode())
                    .domain("www.iceland.co.uk")
                    .path("/")
                    .isHttpOnly(false)
                    .isSecure(true)
                    .build();
            driver.manage().addCookie(ck);
            Thread.sleep(2000);

            clickPopupIfAppears();
            wait.until(presenceOfElementLocated(By.id("postal-code")));
            WebElement postcodeField = driver.findElement(By.id("postal-code"));
            postcodeField.sendKeys(icelandConfig.getPostcode());
            WebElement postcodeButton = driver.findElement(By.name("dwfrm_singleshipping_shippingAddress_addressFields_checkPostcode"));
            postcodeButton.click();
            Thread.sleep(1000);
            clickPopupIfAppears();
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
                    if (!texts[1].equals("Sorry this slot is fully booked")) {
                        slotDetails.add(date);
                    }
                }
            });
            if (!slotDetails.isEmpty()) {
                // Only notify where the slots haven't already been notified for;
                slotDetails.removeAll(NOTIFIED_SLOTS);
                SlackUtils.sendNotificationSlackMessage(slackConfig, "Iceland", slotDetails);
                NOTIFIED_SLOTS.addAll(slotDetails);
            }
        } catch (Exception e) {
            Logger.error("Failed while scraping iceland grocery slot");
            Logger.error(e.getMessage());
        } finally {
            driver.quit();
        }
    }

    private void clickPopupIfAppears() {
        try {
            WebElement modalButton = driver.findElement(By.id("modal-close-button"));
            modalButton.click();
        } catch (NoSuchElementException ex) {
            Logger.info("Popup didn't appear");
        }
    }
}

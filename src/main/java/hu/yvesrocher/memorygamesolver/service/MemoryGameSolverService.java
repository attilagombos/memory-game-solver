package hu.yvesrocher.memorygamesolver.service;

import static java.util.Comparator.comparing;
import static org.openqa.selenium.By.cssSelector;
import static org.openqa.selenium.By.id;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;

import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class MemoryGameSolverService {

    private static final String LEVEL_1_SELECTOR = "a[onclick*='triggerID:1115']";
    private static final String LEVEL_2_SELECTOR = "span[onclick*='triggerID:1125']";
    private static final String LEVEL_3_SELECTOR = "span[onclick*='triggerID:1126']";

    private static final String GAME_FRAME_ID = "mctr_iframe";
    private static final String CARDS_SELECTOR = "div.mlctr-pexeso-card";
    private static final String CARD_BACK_SELECTOR = "div.mlctr-pexeso-card-in div.mlctr-pexeso-card-back";

    private static final String SELECT_PRIZE_SELECTOR = "span[onclick*='nextstep:2']";
    private static final String ENTER_EMAIL_SELECTOR = "input[type=email]";
    private static final String SEND_PRIZE_SELECTOR = "input[type=submit]";

    private static final String STYLE_ATTRIBUTE = "style";

    @Value("${game.url}")
    private String gameUrl;

    @Value("${retry.delay.ms}")
    private int retryDelayMs;

    @Value("${flip.delay.ms}")
    private int flipDelayMs;

    @Value("${email.address}")
    private String emailAddress;


    @PostConstruct
    public void solve() {
        WebDriver driver = getWebDriver();

        startLevel(driver, LEVEL_1_SELECTOR);

        solveLevel(driver);

        startLevel(driver, LEVEL_2_SELECTOR);

        solveLevel(driver);

        startLevel(driver, LEVEL_3_SELECTOR);

        solveLevel(driver);

        receivePrize(driver);

        driver.quit();
    }

    private WebDriver getWebDriver() {
        WebDriverManager.chromedriver().setup();

        WebDriver driver = new ChromeDriver();

        driver.get(gameUrl);

        return driver;
    }

    private void startLevel(SearchContext searchContext, String levelSelector) {
        WebElement startLevelButton = findElement(getByCss(searchContext, levelSelector));

        clickElement(startLevelButton);
    }

    private void solveLevel(WebDriver driver) {
        switchToFrame(driver);

        List<WebElement> cards = findElements(getByCss(driver, CARDS_SELECTOR));

        cards.sort(comparing(card -> findElement(getByCss(card, CARD_BACK_SELECTOR)).getAttribute(STYLE_ATTRIBUTE)));

        cards.forEach(card -> {
            clickElement(card);
            delay(flipDelayMs);
        });
    }

    private void switchToFrame(WebDriver driver) {
        findElement(() -> driver.findElements(id(GAME_FRAME_ID)));

        driver.switchTo().frame(GAME_FRAME_ID);

        log.info("Switched to frame {}", GAME_FRAME_ID);
    }

    private void receivePrize(WebDriver driver) {
        delay(2000);

        WebElement selectPrizeButton = findElement(getByCss(driver, SELECT_PRIZE_SELECTOR));

        clickElement(selectPrizeButton);

        delay(2000);

        WebElement enterEmailInput = findElement(getByCss(driver, ENTER_EMAIL_SELECTOR));

        enterEmailInput.sendKeys(emailAddress);

        delay(2000);

        WebElement sendPrizeButton = findElement(getByCss(driver, SEND_PRIZE_SELECTOR));

        //clickElement(sendPrizeButton);
    }

    private Supplier<List<WebElement>> getByCss(SearchContext searchContext, String selector) {
        return () -> searchContext.findElements(cssSelector(selector));
    }

    private WebElement findElement(Supplier<List<WebElement>> supplier) {
        return findElements(supplier).get(0);
    }

    private List<WebElement> findElements(Supplier<List<WebElement>> supplier) {
        List<WebElement> result = null;

        while (result == null || result.isEmpty()) {
            try {
                result = supplier.get();
            } catch (NoSuchElementException | NoSuchWindowException exception) {
                log.info("Retry finding elements after {} ms", retryDelayMs);
                delay(retryDelayMs);
            }
        }

        log.info("Found element(s) {}", result);

        return result;
    }

    private void clickElement(WebElement webElement) {
        while (true) {
            try {
                webElement.click();
                log.info("Clicked element {}", webElement);
                break;
            } catch (ElementNotInteractableException exception) {
                log.info("Retry clicking element after {} ms", retryDelayMs);
                delay(retryDelayMs);
            }
        }
    }

    @SneakyThrows
    private void delay(int millis) {
        if (millis > 0) {
            Thread.sleep(millis);
        }
    }
}

package hu.yvesrocher.memorygamesolver.service;

import static java.util.Comparator.comparing;
import static org.openqa.selenium.By.cssSelector;

import java.util.List;

import javax.annotation.PostConstruct;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Service;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.SneakyThrows;

@Service
public class MemoryGameSolverService {

    private static final String GAME_URL = "https://m.yves-rocher.hu/memoria";

    private static final int LOAD_DELAY_MS = 2000;
    private static final int FLIP_DELAY_MS = 200;

    private static final String LEVEL_1_SELECTOR = "a[onclick*='triggerID:1115']";
    private static final String LEVEL_2_SELECTOR = "span[onclick*='triggerID:1125']";
    private static final String LEVEL_3_SELECTOR = "span[onclick*='triggerID:1126']";

    private static final String GAME_FRAME_ID = "mctr_iframe";
    private static final String CARDS_SELECTOR = "div.mlctr-pexeso-card";
    private static final String CARD_BACK_SELECTOR = "div.mlctr-pexeso-card-in div.mlctr-pexeso-card-back";
    private static final String STYLE_ATTRIBUTE = "style";


    @PostConstruct
    public void solve() {
        WebDriver driver = getWebDriver();

        startLevel(driver, LEVEL_1_SELECTOR);

        solveLevel(driver);

        startLevel(driver, LEVEL_2_SELECTOR);

        solveLevel(driver);

        startLevel(driver, LEVEL_3_SELECTOR);

        solveLevel(driver);

        driver.quit();
    }

    private WebDriver getWebDriver() {
        WebDriverManager.chromedriver().setup();

        WebDriver driver = new ChromeDriver();

        driver.get(GAME_URL);

        return driver;
    }

    private void startLevel(WebDriver driver, String levelSelector) {
        delay(LOAD_DELAY_MS);

        WebElement startLevelButton = driver.findElement(cssSelector(levelSelector));

        startLevelButton.click();
    }

    private void solveLevel(WebDriver driver) {
        delay(LOAD_DELAY_MS);

        driver.switchTo().frame(GAME_FRAME_ID);

        List<WebElement> cards = driver.findElements(cssSelector(CARDS_SELECTOR));

        cards.sort(comparing(card -> card.findElement(cssSelector(CARD_BACK_SELECTOR)).getAttribute(STYLE_ATTRIBUTE)));

        cards.forEach(card -> {
            card.click();
            delay(FLIP_DELAY_MS);
        });
    }

    @SneakyThrows
    private void delay(int millis) {
        Thread.sleep(millis);
    }
}

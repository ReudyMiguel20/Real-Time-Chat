import com.microsoft.playwright.*;
import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.dynamic.input.DynamicTesting;
import org.hyperskill.hstest.stage.SpringTest;
import org.hyperskill.hstest.testcase.CheckResult;
import org.junit.After;
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.hyperskill.hstest.testcase.CheckResult.correct;
import static org.hyperskill.hstest.testcase.CheckResult.wrong;

public class ChatTests extends SpringTest {
    final static Random random = new Random();
    final static Pattern overflowPattern = Pattern.compile("^(auto|scroll)$");
    final static int TIMEOUT = 10_000;
    final static int NUM_OF_MESSAGES = 7;
    final static String URL = "http://localhost:28852";
    final static String TITLE = "Chat";

    final static String INPUT_MSG_ID_SELECTOR = "#input-msg";
    final static String SEND_MSG_BTN_ID_SELECTOR = "#send-msg-btn";
    final static String MESSAGES_ID_SELECTOR = "#messages";
    final static String MESSAGE_CLASS_SELECTOR = ".message";
    final static String INCORRECT_OR_MISSING_TITLE_TAG_ERR = "tag \"title\" should have correct text";

    final static String[] RANDOM_MESSAGES = Stream
            .generate(ChatTests::generateRandomMessage)
            .limit(NUM_OF_MESSAGES)
            .toArray(String[]::new);

    final static List<String> sentMessages = new ArrayList<>();

    Playwright playwright;
    Browser browser;
    List<Page> pages = new ArrayList<>();

    @Before
    public void initBrowser() {
        playwright = Playwright.create();

        browser = playwright.firefox().launch(
                new BrowserType
                        .LaunchOptions()
                        .setHeadless(false)
//                        .setSlowMo(15)
                        .setTimeout(1000 * 120));
    }

    @After
    public void closeBrowser() {
        if (playwright != null) {
            playwright.close();
        }
    }

    // Helper functions
    static Page openNewPage(String url, Browser browser, int defaultTimeout) {
        Page page = browser.newContext().newPage();
        page.navigate(url);
        page.setDefaultTimeout(defaultTimeout);
        return page;
    }

    static String generateRandomMessage() {
        return "Test message " + random.nextInt();
    }

    // Tests

    @DynamicTest
    DynamicTesting[] dt = new DynamicTesting[]{
            () -> {
                pages.add(openNewPage(URL, browser, TIMEOUT));
                return correct();
            },
            () -> testShouldContainProperTitle(pages.get(0), TITLE),
            () -> {
                pages.add(openNewPage(URL, browser, TIMEOUT));
                return correct();
            },
            () -> testShouldContainProperTitle(pages.get(1), TITLE),

            // --- TESTS WITH TWO USERS
            // message 0
            () -> testFillInputField(pages.get(0), RANDOM_MESSAGES[0], INPUT_MSG_ID_SELECTOR),
            () -> {
                sentMessages.add(RANDOM_MESSAGES[0]);
                return testPressBtn(pages.get(0), SEND_MSG_BTN_ID_SELECTOR);
            },
            () -> testUserMessagesShouldHaveProperStructureAndContent(pages.get(0), sentMessages),
            () -> testUserMessagesShouldHaveProperStructureAndContent(pages.get(1), sentMessages),
            // message 1
            () -> testFillInputField(pages.get(1), RANDOM_MESSAGES[1], INPUT_MSG_ID_SELECTOR),
            () -> {
                sentMessages.add(RANDOM_MESSAGES[1]);
                return testPressBtn(pages.get(1), SEND_MSG_BTN_ID_SELECTOR);
            },
            () -> testUserMessagesShouldHaveProperStructureAndContent(pages.get(1), sentMessages),
            () -> testUserMessagesShouldHaveProperStructureAndContent(pages.get(0), sentMessages),
            // message 2
            () -> testFillInputField(pages.get(0), RANDOM_MESSAGES[2], INPUT_MSG_ID_SELECTOR),
            () -> {
                sentMessages.add(RANDOM_MESSAGES[2]);
                return testPressBtn(pages.get(0), SEND_MSG_BTN_ID_SELECTOR);
            },
            () -> testUserMessagesShouldHaveProperStructureAndContent(pages.get(0), sentMessages),
            () -> testUserMessagesShouldHaveProperStructureAndContent(pages.get(1), sentMessages),
            // message 3
            () -> testFillInputField(pages.get(1), RANDOM_MESSAGES[3], INPUT_MSG_ID_SELECTOR),
            () -> {
                sentMessages.add(RANDOM_MESSAGES[3]);
                return testPressBtn(pages.get(1), SEND_MSG_BTN_ID_SELECTOR);
            },
            () -> testUserMessagesShouldHaveProperStructureAndContent(pages.get(1), sentMessages),
            () -> testUserMessagesShouldHaveProperStructureAndContent(pages.get(0), sentMessages),
            // message 4
            () -> testFillInputField(pages.get(0), RANDOM_MESSAGES[4], INPUT_MSG_ID_SELECTOR),
            () -> {
                sentMessages.add(RANDOM_MESSAGES[4]);
                return testPressBtn(pages.get(0), SEND_MSG_BTN_ID_SELECTOR);
            },
            () -> testUserMessagesShouldHaveProperStructureAndContent(pages.get(0), sentMessages),
            () -> testUserMessagesShouldHaveProperStructureAndContent(pages.get(1), sentMessages),

            // --- TESTS WITH THREE USERS
            () -> {
                pages.add(openNewPage(URL, browser, TIMEOUT));
                return correct();
            },

            // message 5
            () -> testFillInputField(pages.get(2), RANDOM_MESSAGES[5], INPUT_MSG_ID_SELECTOR),
            () -> {
                sentMessages.add(RANDOM_MESSAGES[5]);
                return testPressBtn(pages.get(2), SEND_MSG_BTN_ID_SELECTOR);
            },
            () -> testUserMessagesShouldHaveProperStructureAndContent(pages.get(0), sentMessages),
            () -> testUserMessagesShouldHaveProperStructureAndContent(pages.get(1), sentMessages),
            () -> testUserMessagesShouldHaveProperStructureAndContent(pages.get(2), sentMessages.subList(sentMessages.size() - 1, sentMessages.size())),

            // message 6
            () -> testFillInputField(pages.get(2), RANDOM_MESSAGES[6], INPUT_MSG_ID_SELECTOR),
            () -> {
                sentMessages.add(RANDOM_MESSAGES[6]);
                return testPressBtn(pages.get(2), SEND_MSG_BTN_ID_SELECTOR);
            },
            () -> testUserMessagesShouldHaveProperStructureAndContent(pages.get(0), sentMessages),
            () -> testUserMessagesShouldHaveProperStructureAndContent(pages.get(1), sentMessages),
            () -> testUserMessagesShouldHaveProperStructureAndContent(pages.get(2), sentMessages.subList(sentMessages.size() - 2, sentMessages.size())),
    };

    CheckResult testShouldContainProperTitle(Page page, String title) {
        return title.equals(page.title()) ? correct() : wrong(INCORRECT_OR_MISSING_TITLE_TAG_ERR);
    }

    CheckResult testFillInputField(Page page, String msg, String inputFieldSelector) {
        try {
            assertThat(page.locator(inputFieldSelector)).isEmpty();
            page.fill(inputFieldSelector, msg);
            return correct();
        } catch (PlaywrightException | AssertionError e) {
            return wrong(e.getMessage());
        }
    }

    CheckResult testPressBtn(Page page, String btnSelector) {
        try {
            page.click(btnSelector);
            return correct();
        } catch (PlaywrightException e) {
            return wrong(e.getMessage());
        }
    }

    CheckResult testUserMessagesShouldHaveProperStructureAndContent(Page page, List<String> sentMessages) {
        Locator allMessagesLocator = page.locator(MESSAGES_ID_SELECTOR).locator(MESSAGE_CLASS_SELECTOR);

        try {
            assertThat(page.locator(MESSAGES_ID_SELECTOR)).hasCSS("overflow-y", overflowPattern);
            assertThat(allMessagesLocator).hasCount(sentMessages.size());

            for (int i = 0; i < sentMessages.size(); i++) {
                Locator messageLocator = allMessagesLocator.nth(i);

                assertThat(messageLocator).isVisible();
                assertThat(messageLocator).hasText(sentMessages.get(i));
            }

            return correct();
        } catch (AssertionError e) {
            return wrong(e.getMessage());
        }
    }
}
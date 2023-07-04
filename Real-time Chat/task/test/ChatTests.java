import com.microsoft.playwright.*;
import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.dynamic.input.DynamicTesting;
import org.hyperskill.hstest.stage.SpringTest;
import org.hyperskill.hstest.testcase.CheckResult;
import org.junit.After;
import org.junit.Before;

import java.util.Arrays;
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
    final static String URL = "http://localhost:28852";
    final static String TITLE = "Chat";

    final static String INPUT_MSG_ID_SELECTOR = "#input-msg";
    final static String SEND_MSG_BTN_ID_SELECTOR = "#send-msg-btn";
    final static String MESSAGES_ID_SELECTOR = "#messages";
    final static String MESSAGE_CLASS_SELECTOR = ".message";
    final static String INCORRECT_OR_MISSING_TITLE_TAG_ERR = "tag \"title\" should have correct text";

    final String[] RANDOM_MESSAGES = Stream
            .generate(ChatTests::generateRandomMessage)
            .limit(5)
            .toArray(String[]::new);

    Playwright playwright;
    Browser browser;
    Page page;

    @Before
    public void initBrowser() {
        playwright = Playwright.create();

        browser = playwright.firefox().launch(
                new BrowserType
                        .LaunchOptions()
                        .setHeadless(false)
                        .setTimeout(1000 * 120)
                        .setSlowMo(15));
    }

    @After
    public void closeBrowser() {
        if (playwright != null) {
            playwright.close();
        }
    }

    // Helper functions

    static String generateRandomMessage() {
        return "Test message " + random.nextInt();
    }

    // Tests

    @DynamicTest
    DynamicTesting[] dt = new DynamicTesting[]{
            () -> testInitAndOpenPage(URL),

            () -> testShouldContainProperTitle(page, TITLE),

            () -> testFillInputField(page, RANDOM_MESSAGES[0], INPUT_MSG_ID_SELECTOR),
            () -> testPressBtn(page, SEND_MSG_BTN_ID_SELECTOR),
            () -> testUserMessagesShouldHaveProperStructureAndContent(page, Arrays.copyOf(RANDOM_MESSAGES, 1)),

            () -> testFillInputField(page, RANDOM_MESSAGES[1], INPUT_MSG_ID_SELECTOR),
            () -> testPressBtn(page, SEND_MSG_BTN_ID_SELECTOR),
            () -> testUserMessagesShouldHaveProperStructureAndContent(page, Arrays.copyOf(RANDOM_MESSAGES, 2)),

            () -> testFillInputField(page, RANDOM_MESSAGES[2], INPUT_MSG_ID_SELECTOR),
            () -> testPressBtn(page, SEND_MSG_BTN_ID_SELECTOR),
            () -> testUserMessagesShouldHaveProperStructureAndContent(page, Arrays.copyOf(RANDOM_MESSAGES, 3)),

            () -> testFillInputField(page, RANDOM_MESSAGES[3], INPUT_MSG_ID_SELECTOR),
            () -> testPressBtn(page, SEND_MSG_BTN_ID_SELECTOR),
            () -> testUserMessagesShouldHaveProperStructureAndContent(page, Arrays.copyOf(RANDOM_MESSAGES, 4)),

            () -> testFillInputField(page, RANDOM_MESSAGES[4], INPUT_MSG_ID_SELECTOR),
            () -> testPressBtn(page, SEND_MSG_BTN_ID_SELECTOR),
            () -> testUserMessagesShouldHaveProperStructureAndContent(page, Arrays.copyOf(RANDOM_MESSAGES, 5)),
    };

    CheckResult testInitAndOpenPage(String url) {
        page = browser.newContext().newPage();
        page.navigate(url);
        page.setDefaultTimeout(TIMEOUT);

        return correct();
    }

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

    CheckResult testUserMessagesShouldHaveProperStructureAndContent(Page page, String[] sentMessages) {
        Locator allMessagesLocator = page.locator(MESSAGES_ID_SELECTOR).locator(MESSAGE_CLASS_SELECTOR);

        try {
            assertThat(page.locator(MESSAGES_ID_SELECTOR)).hasCSS("overflow-y", overflowPattern);
            assertThat(allMessagesLocator).hasCount(sentMessages.length);

            for (int i = 0; i < sentMessages.length; i++) {
                Locator messageLocator = allMessagesLocator.nth(i);

                assertThat(messageLocator).isVisible();
                assertThat(messageLocator).hasText(sentMessages[i]);
            }

            return correct();
        } catch (AssertionError e) {
            return wrong(e.getMessage());
        }
    }
}

package com.minh.helper;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.minh.helper.User.UserData;
import com.minh.selector.Via;
import com.minh.selector.ViaCss;
import com.minh.selector.ViaId;


public class Browser {
	static final Logger log = LoggerFactory.getLogger(Browser.class);

	/**
	 * This constant is used when the Browser is started without profile string.
	 */
	public static final String DEFAULT_PROFILE = "Default";
	/* Available browsers */
	/**
	 * The name of the Firefox WebDriver. Keep in mind that the value of this constant is the name of the process in
	 * Windows.
	 */
	public static final String FIREFOX = "firefox";
	/**
	 * The name of the IE WebDriver. Also the name of the process in Windows.
	 */
	public static final String INTERNET_EXPLORER = "iexplore";
	/**
	 * The name of the Chrome WebDriver and the name of the process in Windows.
	 */
	public static final String CHROME = "chrome";
	/**
	 * The name of the Android WebDriver.
	 */
	public static final String ANDROID = "AndroidDriver";
	/**
	 * The name of the Mock version of the WebDriver.
	 */
	public static final String MOCK = "MockDriver";
	/**
	 * The name of the HTML WebDriver.
	 */
	public static final String HEADLESS = "HtmlUnitDriver";

	/* Javascript snippets to change the visibility of an object JQuery style. */
	/**
	 * This is a String.format javascript that needs the actual selector of the element to be altered in JQuery style.
	 */
	public static final String SHOW_ELEMENT_JAVASCRIPT = "$('%1$s').show().css('opacity','1').css('display','block');";
	/**
	 * This is a String.format javascript that needs the actual selector of the element to be altered in JQuery style.
	 */
	public static final String HIDE_ELEMENT_JAVASCRIPT = "$('%1$s').hide().css('opacity','0').css('display','none');";

	private WebDriver webDriver = null;

	private String browserType = null;

	private long defaultImplicitWait = 30000L;

	private String baseUrl = "";

	/**
	 * Creates a browser object from this class
	 * 
	 * @param browser
	 *            The wanted browser type. One of the constants above can be used.
	 */
	public Browser(String browserType) {
		this.browserType = browserType;
	}

	/**
	 * In case it is needed the url sent in to the get method can be automatically concatenated with a baseUrl.
	 * 
	 * @param baseUrl
	 *            The base url. E.g. http://viaplay.se
	 */
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	/**
	 * This is a lazy constructor for the web driver to make it possible to create the Browser object and set values
	 * prior to the actual start up of the browser.
	 * 
	 * @return The handle to the webDriver.
	 */
	public WebDriver getWebDriver() {
		if (webDriver == null) {
			log.debug("A browser connection to {} is about to be created.", browserType);
			webDriver = new CreateDriver().createWebDriver(browserType);
			resetImplicitWait();
			log.debug("A browser connection to {} is created.", browserType);
		}
		return webDriver;
	}

	/**
	 * This might be needed in some exceptional cases to reset the implicitWait parameter.
	 */
	public void resetImplicitWait() {
		setImplicitWait(defaultImplicitWait);
	}

	/**
	 * This might be needed in some exceptional cases to set the implicitWait parameter to a lower value thus speeding
	 * up unnecessary wait states.
	 */
	public void setImplicitWait(long millis) {
		getWebDriver().manage().timeouts().implicitlyWait(millis, TimeUnit.MILLISECONDS);
		log.debug("Implicit wait is set to {} milliseconds.", millis);
	}

	/**
	 * Exit the web browser.
	 */
	public void quit() {
		log.info("Quitting the web browser.");
		getWebDriver().quit();
	}

	/**
	 * Friendly helper method that makes sure you are logged out before quitting.
	 */
	public void logoutAndQuit() {
		//logout();
		quit();
	}

	/**
	 * This, often used, method fetches a page via the instantiated web browser.
	 * 
	 * @param urlAsString
	 *            The url to the wanted page.
	 * @return The Browser object to enable one liners.
	 */
	public Browser get(String urlAsString) {
		if (urlAsString.indexOf("http") == -1) {
			urlAsString = baseUrl + urlAsString;
		}
		log.info("The page {} is requested.", urlAsString);
		getWebDriver().get(urlAsString);
		return this;
	}

	/**
	 * Method that retrieves the title of the current page and returns that.
	 * 
	 * @return The title of the current page.
	 */
	public String getTitle() {
		return getWebDriver().getTitle();
	}

	/**
	 * Helper method that returns true if the wanted element is visible in the browser or false otherwise.
	 * 
	 * @param via
	 *            The selector of choice to find the wanted element.
	 * @return true if element is visible or false otherwise.
	 */
	private boolean isVisible(Via via) {
		log.debug("Verifying visibility of element {}.", via);
		setImplicitWait(1L);
		WebElement webElement = null;
		try {
			webElement = getWebDriver().findElement(via);
		} catch (NoSuchElementException e) {
			log.debug("No element found that match {}.", via);
			resetImplicitWait();
			return false;
		} finally {
			log.debug("The element {} visibility is {}.", webElement == null ? via : getTagAsString(webElement),
					webElement == null ? false : webElement.isDisplayed());
			resetImplicitWait();
		}
		return webElement.isDisplayed();
	}

	/**
	 * Wrapper method for the executeJavascriptOnElement to simplify the task of making an element appear in the UI.
	 * 
	 * @param via
	 *            a selector of the element to show.
	 */
	public WebElement showElement(Via via) {
		return executeJavascriptOnElement(via, SHOW_ELEMENT_JAVASCRIPT);
	}

	/**
	 * Wrapper method for the executeJavascriptOnElement to simplify the task of making an element disappear in the UI.
	 * 
	 * @param via
	 *            a selector of the element to hide.
	 */
	public WebElement hideElement(Via via) {
		return executeJavascriptOnElement(via, HIDE_ELEMENT_JAVASCRIPT);
	}

	/**
	 * This method executes a Javascript on an element. Select any arbitrary element via.css if the Javascript isn't
	 * related to the element per se.
	 * 
	 * @param via
	 *            a selector to an element
	 * @param javascript
	 *            the javascript to execute. Use the via.css locator if the script isn't related to the element.
	 */
	public WebElement executeJavascriptOnElement(Via via, String javascript) {
		WebElement webElement = getWebDriver().findElement(via);
		String compoundJavascript = "";
		if (via instanceof ViaId) {
			compoundJavascript = String.format(javascript, "#" + webElement.getAttribute("id"));
		}
		if (via instanceof ViaCss) {
			compoundJavascript = String.format(javascript, via.getValue());
		}
		String tag = getTagAsString(webElement);
		log.debug("The javascript " + compoundJavascript + " is about to be executed on tag " + tag);
		executeJavascript(compoundJavascript);
		return webElement;
	}

	/**
	 * This method executes a Javascript, period.
	 * 
	 * @param javascript
	 *            the javascript to execute.
	 */
	public String executeJavascript(String javascript) {
		Object object = ((JavascriptExecutor) webDriver).executeScript(javascript);
		return object == null ? null : object.toString();
	}

	/**
	 * This method is used to enter characters into a input of textarea element.
	 * 
	 * @param keysToSend
	 *            The characters you want to send to the element.
	 * @param via
	 *            The selector of choice to find the element.
	 */
	private void sendKeysToElement(String keysToSend, Via via) {
		WebElement webElement = waitForElement(via);
		log.debug("About to write into element {}.", getTagAsString(webElement));
		webElement.sendKeys(keysToSend);
	}

	/**
	 * Clicks on an element in the browser.
	 * 
	 * @param via
	 *            The selector of choice to find the element.
	 */
	public void clickElement(Via via) {
		WebElement webElement = waitForElement(via);
		log.debug("About to click on element {}.", getTagAsString(webElement));
		webElement.click();
	}

	/**
	 * This method waits for a while (implicitWait milliseconds) if needed to find a particular element. This is needed
	 * more often when transitions are used when displaying elements.
	 * 
	 * @param via
	 *            The selector of choice to find the element.
	 * @return The wanted element.
	 */
	public WebElement waitForElement(Via via) {
		log.debug("Waiting for element {} to appear.", via);
		long endBy = System.currentTimeMillis() + defaultImplicitWait;
		WebElement webElement = null;
		while ((webElement = getWebDriver().findElement(via)).isDisplayed() == false
				&& System.currentTimeMillis() < endBy) {
			log.debug("Waiting for the element {} to appear.", getTagAsString(webElement));
			sleep(100L);
		}
		return webElement;
	}

	/**
	 * This method returns a list of the elements that match a given selector.
	 * 
	 * @param via
	 *            The selector to the elements wanted.
	 * @return A list of WebElements or empty List.
	 */
	public List<WebElement> waitForElements(Via via) {
		return webDriver.findElements(via);
	}

	/**
	 * This helper method sits still for up to implicitWait milliseconds to verify that an element has disappeared.
	 * Convenient to use when the speed of the computer/web browser isn't well known. Use this when waiting for an
	 * process indicator overlay is shown to indicate a lengthy process.
	 * 
	 * @param via
	 *            The selector of choice to find the element.
	 */
	public void waitForElementToDisappear(Via via) {
		log.debug("Waiting for element {} to disappear.", via);
		WebElement elementToWaitFor = null;
		long endAt = System.currentTimeMillis() + defaultImplicitWait;
		setImplicitWait(1L);
		while (System.currentTimeMillis() < endAt) {
			try {
				elementToWaitFor = webDriver.findElement(via);
				if (!elementToWaitFor.isDisplayed()) {
					/* The element is not visible - break the loop */
					resetImplicitWait();
					return;
				}
				log.info("The element {} is still present!", via);
				/* Come this far we found the element - wait! */
				sleep(1000L);
			} catch (NoSuchElementException e) {
				/* No element found - break the loop */
				log.debug("No element {} found!.", via);
				resetImplicitWait();
				return;
			} catch (StaleElementReferenceException e) {
				/* No element found - break the loop */
				log.debug("The element that match {} is stale.", via);
				// resetImplicitWait();
				// return;
			}
			return;
		}
		resetImplicitWait();
		throw new RuntimeException("The element " + via + " did not go away before end of implicit wait ("
				+ defaultImplicitWait + " millis).");
	}

	/**
	 * This helper method is mainly used when logging a user out. It is needed when speedy web browsers like the Chrome
	 * web browsers is shut down before completely logged out. The disadvantage of that is that you are logged in when
	 * starting the browser again.
	 * 
	 * @param cookieName
	 *            The name of the cookie to watch.
	 */
	public void waitForCookieToDisappear(String cookieName) {
		long endAt = System.currentTimeMillis() + defaultImplicitWait;
		Cookie cookie = null;
		while ((cookie = getWebDriver().manage().getCookieNamed(cookieName)) != null
				&& System.currentTimeMillis() < endAt) {
			log.debug("The cookie {} is still present.", cookieName);
			sleep(100L);
		}
		if (cookie != null) {
			log.warn("The expected cookie {} did not disappear before end of implicit wait.", cookieName);
		}
	}

	/**
	 * This convenient helper method is mainly used to improve the readability of the information logged to the logger.
	 * 
	 * @param webElement
	 *            The web element to mimic the tag of.
	 * @return a String that descibes the element in a human readable format.
	 */
	public String getTagAsString(WebElement webElement) {
		if (webElement == null) {
			return "[The web element is null]";
		}

		StringBuffer result = new StringBuffer("<");
		result.append(webElement.getTagName());

		String id = webElement.getAttribute("id");
		if (!"".equals(id)) {
			result.append(" id=\"");
			result.append(id);
			result.append("\"");
		}

		String cls = webElement.getAttribute("class");
		if (!"".equals(cls)) {
			result.append(" class=\"");
			result.append(cls);
			result.append("\"");
		}

		String attr = webElement.getAttribute("style");
		if (!"".equals(attr)) {
			result.append(" style=\"");
			result.append(attr);
			result.append("\"");
		}

		result.append(" />");

		return result.toString();
	}

	/**
	 * Please do not use this method. There is probably always a better way to solve the timing issues I suspect you are
	 * having.
	 * 
	 * @param millis The time to sleep.
	 */
	public void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			log.equals("The sleep on the thread was interrupted!");
			e.printStackTrace();
		}
	}

}

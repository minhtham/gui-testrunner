package com.minh.helper;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateDriver {
	static final Logger log = LoggerFactory.getLogger(CreateDriver.class);

	static final Map<String, String> profileDirs = new HashMap<String, String>();
	static {
		profileDirs.put("win7+", "%1$sC:/Users/%2$s/AppData/Local/Google/Chrome/User Data/%3$s");
		profileDirs
				.put("winXP",
						"%1$sC:\\Documents and Settings\\%2$s\\Local Settings\\Application Data\\Google\\Chrome\\User Data\\%3$s");
		profileDirs.put("mac", "%1$s/Users/%2$s/Library/Application Support/Google/Chrome/%3$s");
		profileDirs.put("linux", "%1$s/home/%2$s/.config/google-chrome/%3$s");
	}

	public WebDriver createWebDriver(String driverName) {
		return createWebDriver(driverName, "Default", null);
	}

	public WebDriver createWebDriver(String driverName, String profile) {
		return createWebDriver(driverName, profile, null);
	}

	/**
	 * This is the createWebDriver main switch method.
	 * 
	 * @param driverName
	 *            The name of the wanted driver.
	 * @return An instantiated driver of your choice.
	 */
	public WebDriver createWebDriver(String driverName, String profile, String[] pathArray) {

		driverName = osSpecificDriverAdjustments(driverName);

		if (driverName.equals(Browser.FIREFOX)) {
			return firefoxWebDriver(profile, pathArray);
		}
		if (driverName.equals(Browser.INTERNET_EXPLORER)) {
			return internetExplorerWebDriver();
		}
		if (driverName.contentEquals(Browser.CHROME)) {
			return chromeWebDriver(profile);
		}
		if (driverName.contentEquals(Browser.HEADLESS)) {
			return headlessWebDriver();
		}
		throw new RuntimeException("No implemented driver was found for the requested driverName " + driverName);
	}

	/**
	 * The ChromeDriver is developed as a collaboration effort between the Selenium team and the Chromium team. Read
	 * more at: http://code.google.com/p/selenium/wiki/ChromeDriver
	 * 
	 * Please be aware that the chromedriver binary must explicitly be set to be executable on Linux platforms when
	 * using maven after git clone. E.g. chmod 555 target/classes/externaltools/chromedriver
	 * 
	 * @return The instantiated Chrome Web Driver.
	 */
	private WebDriver chromeWebDriver(String profile) {
		DesiredCapabilities capabilities = DesiredCapabilities.chrome();

		/* Since this driver uses the wire protocol we need to find the correct driver depending on the environment */
		String driverUrlString = this.getClass().getResource("../../../externaltools/chromedriver").getPath();
		if (isWindows()) {
			/* A unique driver for windows is supplied. */
			driverUrlString += ".exe";
			driverUrlString = driverUrlString.replace("%20", " ");
			log.info("The driver url is: " + driverUrlString);
		}
		if (getOsId().equals("mac")) {
			/* The proprietary driver for OS X is installed locally */
			driverUrlString = "/Users/testadmin/svp/tools/chromedriver/chromedriver";
		}
		System.setProperty("webdriver.chrome.driver", driverUrlString);

		capabilities.setCapability("ACCEPT_SSL_CERTS", true);
		String userDirSwitch = String.format(profileDirs.get(getOsId()), "--user-data-dir=", getUserName(), profile);
		log.info(userDirSwitch);
		capabilities.setCapability("chrome.switches", Arrays.asList(userDirSwitch));

		return new ChromeDriver(capabilities);
	}

	/**
	 * This method instantiates an Internet Explorer web driver and returns it.
	 * 
	 * @return The instantiate web driver
	 */
	private WebDriver internetExplorerWebDriver() {
		DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();

		String driverUrlString = this.getClass()
				.getResource(String.format("../../../externaltools/IEDriverServer%1$s.exe", getOsArch())).getPath();
		System.setProperty("webdriver.ie.driver", driverUrlString);
		capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
		capabilities.setCapability("SUPPORTS_JAVASCRIPT", true);
		capabilities.setJavascriptEnabled(true);
		capabilities.setCapability("ACCEPT_SSL_CERTS", true);
		return new InternetExplorerDriver(capabilities);
	}

	/**
	 * Helper method that return the windows processor architecture word length.
	 * 
	 * @return
	 */
	private String getOsArch() {
		String arch = System.getenv("PROCESSOR_ARCHITECTURE");
		return arch.equalsIgnoreCase("x86") ? "32" : "64";
	}

	/**
	 * This method instantiates a Firefox web driver and returns it.
	 * 
	 * @param profile
	 * @return The instantiate web driver
	 */
	private WebDriver firefoxWebDriver(String profile, String[] pathArray) {
		DesiredCapabilities capabilities = DesiredCapabilities.firefox();

		ProfilesIni profilesIni = new ProfilesIni();

		FirefoxProfile firefoxProfile = profilesIni.getProfile(profile.toLowerCase());
		if (firefoxProfile != null) {
			firefoxProfile.setAcceptUntrustedCertificates(true);
			firefoxProfile.setAssumeUntrustedCertificateIssuer(false);
		} else {
			log.warn("Unable to find the " + profile + " Firefox profile, try to start Firefox manually to create the "
					+ profile + " profile.");
			log.info("You might be running Windows 7 or above that prevents this feature.");
		}

		WebDriver firefox;
		try {
			firefox = new FirefoxDriver(firefoxProfile);
		} catch (Exception e) {
			/* This hack is here to solve the problem that Selenium has to find the firefox executable. */
			if (pathArray != null) {
				File binaryFile = findExecutable(pathArray, "firefox.exe");
				if (binaryFile == null) {
					throw new RuntimeException("The wanted file firefox.exe was not found within the paths in "
							+ Arrays.toString(pathArray));
				}
				FirefoxBinary binary = new FirefoxBinary(binaryFile);
				firefox = new FirefoxDriver(binary, firefoxProfile, capabilities);
			} else {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		return firefox;
	}

	/**
	 * This driver is by far the fastest but it isn't 100% accurate.
	 * 
	 * @return The instantiated browser.
	 */
	private WebDriver headlessWebDriver() {
		log.warn("The headless browser isn't fully working (as of 2012-10-17)");
		HtmlUnitDriver htmlUnitDriver = new HtmlUnitDriver();
		htmlUnitDriver.setJavascriptEnabled(true);
		return htmlUnitDriver;
	}

	/**
	 * To be able to run test over all available browsers in the set this uses Chrome browser as a substitute where IE
	 * is absent.
	 * 
	 * @param driverName
	 * @return The new driverName.
	 */
	private String osSpecificDriverAdjustments(String driverName) {
		if (!isWindows()) {
			if (driverName.equals(Browser.INTERNET_EXPLORER)) {
				log.info(">>> CHROME is used instead of IE on non Windows operating systems!");
				driverName = Browser.CHROME;
			}
		}
		return driverName;
	}

	/**
	 * This helper method traverses through a folder hierarchy to find the wanted file.
	 * 
	 * @param pathArray
	 *            One or more paths to start look from.
	 * @param wanted
	 *            The filename including the extension.
	 * @return The file object pointing to the found file or null.
	 */
	private File findExecutable(String[] pathArray, String wanted) {
		for (String path : pathArray) {
			File file = new File(path);
			return findFile(file, wanted);
		}
		return null;
	}

	/**
	 * Helper method that using recursion traverses down a folder hierarchy to find a specific file.
	 * 
	 * @param file
	 * @param wanted
	 * @return
	 */
	private File findFile(File file, String wanted) {
		if (file.isDirectory()) {
			log.info("Looking into directory " + file.getAbsolutePath() + " for the " + wanted + " file.");
			File[] fileArray = file.listFiles();
			for (File childFile : fileArray) {
				File returnedFile = findFile(childFile, wanted);
				if (returnedFile != null) {
					return returnedFile;
				}
			}
		}
		if (file.isFile()) {
			String found = file.getName();
			if (found.equals(wanted)) {
				return file;
			}
		}
		return null;
	}

	/**
	 * This helper function returns the users name
	 * 
	 * @return
	 */
	private static String getUserName() {
		return System.getProperty("user.name");
	}

	/**
	 * This helper method returns the OS Id.
	 * 
	 * @return A string of the OS Id.
	 */
	private static String getOsId() {
		String osname = System.getProperty("os.name").toLowerCase();
		if (osname.contains("win")) {
			if (osname.contains("xp")) {
				return "winXP";
			} else {
				return "win7+";
			}
		}
		if (osname.contains("mac")) {
			return "mac";
		}
		if (osname.contains("linux")) {
			return "linux";
		}
		log.error("The operating system " + osname + " is not supported by CreateDriver class.");
		throw new RuntimeException("The operating system " + osname + " is not supported by CreateDriver class.");
	}

	private boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().contains("win");
	}

}

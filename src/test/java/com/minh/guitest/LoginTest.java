package com.minh.guitest;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.minh.helper.Browser;
import com.minh.selector.Via;

public class LoginTest {
    /**
     * Standard slf4j logger instantiation
     */
    Logger log = LoggerFactory.getLogger(LoginTest.class);

    /**
     * Our browser object
     */
    Browser browser = null;

    /**
     * This test setup method is run before each test method below. It creates a new browser and sets its default base
     * url. This might not be the desired way for some test cases but is fine for this.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        log.info("Setup of test run");
        browser = new Browser(Browser.FIREFOX);
    }

    /**
     * This test tear down method is run after each test method below. It makes sure the current user is logged out
     * before the browser is closed.
     *
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        browser.logoutAndQuit();
    }


    /**
     * This test maneuvers to all major pages on the Viaplay web site.
     */
    @Test
    public void testGotoPages() {
        browser.get("http://kth.se");
        browser.sleep(1000);
        browser.get("http://di.se");
        browser.sleep(1000);
        browser.get("http://dn.se");
        browser.sleep(1000);
    }

}

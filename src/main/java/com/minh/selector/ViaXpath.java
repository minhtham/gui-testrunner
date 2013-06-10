package com.minh.selector;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Minh
 * Date: 2013-04-24
 * Time: 20:19
 * To change this template use File | Settings | File Templates.
 */
public class ViaXpath extends Via {
    private String value = "";

    public ViaXpath(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public List<WebElement> findElements(SearchContext context) {
        return new ByXPath(value).findElements(context);
    }

    @Override
    public String toString() {
        return String.format("<? that match xpath=\"%s\" />", value);
    }

}
package com.minh.selector;

import java.util.List;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public class ViaCss extends Via {
    String value = "";

    public ViaCss(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public List<WebElement> findElements(SearchContext context) {
        return new ByCssSelector(value).findElements(context);
    }

    @Override
    public String toString() {
        return String.format("that match css=\"%s\"", value);
    }

}

package com.minh.selector;

import java.util.List;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public class ViaName extends Via {
    String value = "";

    public ViaName(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public List<WebElement> findElements(SearchContext context) {
        return new ByName(value).findElements(context);
    }

    @Override
    public String toString() {
        return String.format("<%s />", value);
    }


}

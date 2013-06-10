package com.minh.selector;

import java.util.List;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public class ViaId extends Via {
    private String value = "";

    public ViaId(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public List<WebElement> findElements(SearchContext context) {
        return new ById(value).findElements(context);
    }

    @Override
    public String toString() {
        return String.format("<? id=\"%s\" />", value);
    }


}

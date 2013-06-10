package com.minh.selector;
import org.openqa.selenium.By;

/**
 * Created with IntelliJ IDEA.
 * User: Minh
 * Date: 2013-06-10
 * Time: 22:30
 * To change this template use File | Settings | File Templates.
 */


public abstract class Via extends By {
    protected static String TAG_FORMAT = "<%s %s=\"%s\"/>";

    /**
     * This is the fasted way to address an element.
     * @param value The id of the element.
     * @return
     */
    public static Via id(String value) {
        return new ViaId(value);
    }

    /**
     * This is the preferred way to address an element.
     * @param value The css that identifies the element
     * @return
     */
    public static Via css(String value) {
        return new ViaCss(value);
    }

    /**
     * This addresses and element by its name attribute.
     * @param value The name of the element.
     * @return
     */
    public static Via name(String value) {
        return new ViaName(value);
    }

    /**
     * This addresses and element by its xpath.
     * @param value The name of the element.
     * @return
     */
    public static Via xpath(String value) {
        return new ViaXpath(value);
    }

    public abstract String getValue();

    public abstract String toString();

}

package org.workcraft.plugins.sts;

import java.awt.Color;

import org.workcraft.exceptions.ArgumentException;
import org.workcraft.plugins.builtin.settings.CommonVisualSettings;

public class EventWithLocality {

    static String eventName = "";
    protected static int locality = 1;
    protected Color localityColor = CommonVisualSettings.getBorderColor();
    protected Color fillLocalityColor = CommonVisualSettings.getFillColor();

public EventWithLocality(String eventName, int locality, Color localityColor, Color fillLocalityColor) {
    this.eventName = eventName;
    this.locality = locality;
    this.localityColor = localityColor;
    this.fillLocalityColor = fillLocalityColor; 
    
}
    public String getEventName() {
        return eventName;
    }

    public static void setEventName(String value) {
        if (value == null)
            value = "";
        if (!value.equals(eventName)) {
            eventName = value;
        }
    }

    public int getLocality() {
        return locality;
    }

    public static void setLocality(int value) {
        if (locality != value) {
            if (value < 0) {
                throw new ArgumentException("The number of locality cannot be negative.");
            }
            /*
             * if (value == 0) { throw new
             * ArgumentException("The number of locality cannot be Zero."); }
             */
            locality = value;
        }
    }

    public Color getLocalityColor() {
        return localityColor;
    }

    public void setLocalityColor(Color value) {
        if (!localityColor.equals(value)) {
            localityColor = value;
        }
    }

    public Color getLocalityFillColor() {
        return fillLocalityColor;
    }

    public void setLocalityFillColor(Color value) {
        if (!localityColor.equals(value)) {
            fillLocalityColor = value;
        }
    }
}

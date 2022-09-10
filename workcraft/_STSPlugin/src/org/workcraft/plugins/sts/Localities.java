package org.workcraft.plugins.sts;


import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.ArgumentException;

@DisplayName("Localities")
//@IdentifierPrefix("L")
//@IdentifierPrefix(value = "l", isInternal = true)
@VisualClass(org.workcraft.plugins.sts.VisualLocalities.class)
public class Localities  extends MathNode {
	static String eventName = "";
    protected static int locality = 1;

    public static String getEventName() {
		return eventName;
	}

	public static void setEventName(String value) {
		if (value == null)
			value = "";
		if (!value.equals(eventName)) {
			eventName= value;
		}
	}
	
    public static int getLocality() {
        return locality;
    }

    public static void setLocality(int value) {
        if (locality != value) {
            if (value < 0) {
                throw new ArgumentException("The number of locality cannot be negative.");
            }
            /*if (value == 0) {
                throw new ArgumentException("The number of locality cannot be Zero.");
            }*/
            locality = value;
        }
    }	
}

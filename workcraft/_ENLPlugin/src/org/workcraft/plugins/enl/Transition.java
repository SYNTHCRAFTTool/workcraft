package org.workcraft.plugins.enl;

import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.observation.PropertyChangedEvent;

@IdentifierPrefix("e")
@VisualClass(org.workcraft.plugins.enl.VisualTransition.class)
public class Transition extends MathNode {
    public static final String PROPERTY_LOCALITY = "Locality";
    protected int locality = 1;

    public int getLocality() {
        return locality;
    }

    public void setLocality(int value) {
        if (locality != value) {
            if (value < 0) {
                throw new ArgumentException("The number of locality cannot be negative.");
            }
            this.locality = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_LOCALITY));
        }
    }

    public String getName(ENL net) {
        String t = net.getNodeReference(this);
        return t;
    }
}

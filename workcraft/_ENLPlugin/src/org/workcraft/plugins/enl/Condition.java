package org.workcraft.plugins.enl;

import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.observation.PropertyChangedEvent;

@IdentifierPrefix("b")
@VisualClass(VisualCondition.class)
public class Condition extends MathNode {
	
    public static final String PROPERTY_TOKENS = "Tokens";

    protected int tokens = 0;

    public int getTokens() {
        return tokens;
    }

    public void setTokens(int value) {
        if (tokens != value) {
            if (value < 0) {
                throw new ArgumentException("The number of tokens cannot be negative.");
            }
            if (value > 1) {
                throw new ArgumentException("The number of tokens cannot be more than one.");
            }
            this.tokens = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_TOKENS));
        }
    }
}

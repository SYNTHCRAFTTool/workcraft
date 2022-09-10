package org.workcraft.plugins.sts;

import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.observation.PropertyChangedEvent;

@IdentifierPrefix("U")
//@IdentifierPrefix(value = "u", isInternal = true)
@VisualClass(org.workcraft.plugins.sts.VisualTransitionArc.class)
public class TransitionArc extends MathConnection {

	String name = "";
	String eventName = "";

	 public TransitionArc() {
	    }
	 
	 public TransitionArc(State first, State second) {
	        super(first, second);
	    }

	public String getName() {
		return name;
	}

	public void setName(String value) {
		if (value == null)
			value = "";
		if (!value.equals(name)) {
			name= value;
			sendNotification(new PropertyChangedEvent(this, name));
		}
	}
	

}

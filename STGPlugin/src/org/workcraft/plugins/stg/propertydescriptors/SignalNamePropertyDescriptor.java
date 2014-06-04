package org.workcraft.plugins.stg.propertydescriptors;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;

import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;

public class SignalNamePropertyDescriptor implements PropertyDescriptor {
	private final STG stg;
	private final String signal;

	public SignalNamePropertyDescriptor(STG stg, String signal) {
		this.stg = stg;
		this.signal = signal;
	}

	@Override
	public Map<Object, String> getChoice() {
		return null;
	}

	@Override
	public String getName() {
		return signal + " name";
	}

	@Override
	public Class<?> getType() {
		return String.class;
	}

	@Override
	public Object getValue() throws InvocationTargetException {
		return signal;
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public void setValue(Object value) throws InvocationTargetException {
		if ( !signal.equals(value) ) {
			Collection<SignalTransition> transitions = stg.getSignalTransitions(signal);
			// First, update all the transitions
			for (SignalTransition transition : transitions) {
				transition.setSignalName((String)value);
			}
			// Then, let STG know that transition names have been changed
			for (SignalTransition transition : transitions) {
				stg.setName(transition, (String)value);
			}
		}
	}

	@Override
	public boolean isCombinable() {
		return false;
	}

}

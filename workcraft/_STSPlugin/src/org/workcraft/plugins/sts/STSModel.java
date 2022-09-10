package org.workcraft.plugins.sts;

import java.util.Collection;
import org.workcraft.dom.math.MathModel;

public interface STSModel  extends MathModel{

	Collection<? extends State> getStates();
    void fire();
}

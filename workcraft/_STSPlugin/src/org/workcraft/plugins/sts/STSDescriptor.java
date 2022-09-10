package org.workcraft.plugins.sts;

import org.workcraft.dom.ModelDescriptor;

public class STSDescriptor implements ModelDescriptor {

    @Override
    public String getDisplayName() {
        return "Step Transition System";
    }

    @Override
    public STS createMathModel() {
        return new STS();
    }

    @Override
    public VisualSTSDescriptor getVisualModelDescriptor() {
        return new VisualSTSDescriptor();
    }
}

package org.workcraft.plugins.enl;

import org.workcraft.dom.ModelDescriptor;

public class ENLDescriptor implements ModelDescriptor {

    @Override
    public String getDisplayName() {
        return "Elementary Net System with Localities";
    }

    @Override
    public ENL createMathModel() {
        return new ENL();
    }

    @Override
    public VisualENLDescriptor getVisualModelDescriptor() {
        return new VisualENLDescriptor();
    }
}

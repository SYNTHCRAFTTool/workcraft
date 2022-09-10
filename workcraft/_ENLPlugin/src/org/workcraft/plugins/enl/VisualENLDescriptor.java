package org.workcraft.plugins.enl;

import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.utils.ValidationUtils;

public class VisualENLDescriptor implements VisualModelDescriptor {

    @Override
    public VisualENL create(MathModel mathModel) throws VisualModelInstantiationException {
        ValidationUtils.validateMathModelType(mathModel, ENL.class, VisualENL.class.getSimpleName());
        return new VisualENL((ENL) mathModel);
    }
}

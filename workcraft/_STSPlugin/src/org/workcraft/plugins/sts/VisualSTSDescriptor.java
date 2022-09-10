package org.workcraft.plugins.sts;

import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.utils.ValidationUtils;

public class VisualSTSDescriptor implements VisualModelDescriptor {

    @Override
    public VisualSTS create(MathModel mathModel) throws VisualModelInstantiationException {
        ValidationUtils.validateMathModelType(mathModel, STS.class, VisualSTS.class.getSimpleName());
        return new VisualSTS((STS) mathModel);
    }
}

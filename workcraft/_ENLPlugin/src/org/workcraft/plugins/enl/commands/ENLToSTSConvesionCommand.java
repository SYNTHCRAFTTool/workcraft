package org.workcraft.plugins.enl.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.enl.ENL;
import org.workcraft.plugins.enl.VisualENL;
import org.workcraft.plugins.enl.tools.ENLToSTSConverter;
import org.workcraft.plugins.enl.utils.ElementaryNetUtils;
import org.workcraft.plugins.sts.STSDescriptor;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class ENLToSTSConvesionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Step Transition System";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, ENL.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        final VisualENL venl = me.getAs(VisualENL.class);
        if (ElementaryNetUtils.checkSoundness(venl.getElementaryNetModel(), false)) {
            final ENLToSTSConverter converter = new ENLToSTSConverter(venl);
            return new ModelEntry(new STSDescriptor(), converter.getSTS());
        } else {
            return null;
        }
    }
}

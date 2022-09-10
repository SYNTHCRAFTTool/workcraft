package org.workcraft.plugins.sts.tools;

import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.tools.NodeGeneratorTool;
import org.workcraft.plugins.sts.Localities;

public class EvensLocalitiesGeneratorTool extends NodeGeneratorTool {
    public EvensLocalitiesGeneratorTool() {
    	super(new DefaultNodeGenerator(Localities.class));
}

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Click to set each event with its locality.";
    }
}

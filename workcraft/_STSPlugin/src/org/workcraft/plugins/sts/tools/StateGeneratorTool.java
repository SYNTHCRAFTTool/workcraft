package org.workcraft.plugins.sts.tools;

import java.awt.Cursor;

import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.tools.NodeGeneratorTool;
import org.workcraft.plugins.sts.State;
import org.workcraft.utils.GuiUtils;

public class StateGeneratorTool  extends NodeGeneratorTool {

    public StateGeneratorTool() {
        super(new DefaultNodeGenerator(State.class));
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        // message appears when we put a muse on a state button generator.
        return "Click to create a state.";
    }
    
    @Override
    public Cursor getCursor(boolean menuKeyDown, boolean shiftKeyDown, boolean altKeyDown) {
            return GuiUtils.createCursorFromSVG("images/steptransitionsystem-node-state.svg");
        
    }
}

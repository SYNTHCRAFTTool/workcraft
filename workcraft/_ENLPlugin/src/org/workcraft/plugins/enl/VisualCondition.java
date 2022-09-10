package org.workcraft.plugins.enl;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.utils.Coloriser;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.plugins.builtin.settings.CommonVisualSettings;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

@DisplayName("Condition")
@Hotkey(KeyEvent.VK_B)
@SVGIcon("images/elementarynet-node-condition.svg")
public class VisualCondition extends VisualComponent {

    public VisualCondition(Condition condition) {
        super(condition);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualCondition, Integer>(this, Condition.PROPERTY_TOKENS,
                Integer.class, true, false) {
            @Override
            public void setter(VisualCondition object, Integer value) {
                object.getReferencedCondition().setTokens(value);
            }

            @Override
            public Integer getter(VisualCondition object) {
                return object.getReferencedCondition().getTokens();
            }
        });
    }

    public Condition getReferencedCondition() {
        return (Condition) getReferencedComponent();
    }

    @Override
    public boolean getNameVisibility() {
        return super.getNameVisibility();
    }

    @Override
    public Shape getShape() {
        double size = CommonVisualSettings.getNodeSize() - CommonVisualSettings.getStrokeWidth();
        double pos = -0.5 * size;
        return new Ellipse2D.Double(pos, pos, size, size);
    }

    @Override
    public void draw(DrawRequest r) {
        super.draw(r);
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
        int tokenCount = getReferencedCondition().getTokens();
        double tokenSize = 0.5 * CommonVisualSettings.getNodeSize();
        if (tokenCount == 1) {
            double tokenPos = -0.5 * tokenSize;
            g.fill(new Ellipse2D.Double(tokenPos, tokenPos, tokenSize, tokenSize));
        }
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        double size = CommonVisualSettings.getNodeSize() - CommonVisualSettings.getStrokeWidth();
        return pointInLocalSpace.distanceSq(0, 0) < size * size / 4;
    }
}

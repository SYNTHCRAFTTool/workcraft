package org.workcraft.plugins.enl;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.builtin.settings.CommonVisualSettings;
import org.workcraft.utils.Coloriser;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

@DisplayName("Event")
@Hotkey(KeyEvent.VK_T)
@SVGIcon("images/elementarynet-node-transition.svg")
public class VisualTransition extends VisualComponent {

    public static final String PROPERTY_LOCALITY_COLOR = "Locality color";
    protected Color localityColor = CommonVisualSettings.getBorderColor();
    protected Color transitionLocalityColor = CommonVisualSettings.getFillColor();

    public VisualTransition(Transition transition) {
        super(transition);
        addPropertyDeclarations();
    }

    public VisualTransition(Transition transition, boolean hasColorProperties, boolean hasLabelProperties,
            boolean hasNameProperties) {
        super(transition, hasColorProperties, hasLabelProperties, hasNameProperties);
    }

    // For Locality number.
    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualTransition, Integer>(this, Transition.PROPERTY_LOCALITY,
                Integer.class, true, false) {
            @Override
            public void setter(VisualTransition object, Integer value) {
                object.getReferencedTransition().setLocality(value);
            }

            @Override
            public Integer getter(VisualTransition object) {
                return object.getReferencedTransition().getLocality();
            }
        });

        // For Locality colour.
        addPropertyDeclaration(new PropertyDeclaration<VisualTransition, Color>(this, PROPERTY_LOCALITY_COLOR,
                Color.class, true, true) {
            @Override
            public void setter(VisualTransition object, Color value) {
                object.setLocalityColor(value);
            }

            @Override
            public Color getter(VisualTransition object) {
                return object.getLocalityColor();
            }
        });
    }

    @Override
    public boolean getNameVisibility() {
        return super.getNameVisibility();
    }

    public Transition getReferencedTransition() {
        return (Transition) getReferencedComponent();
    }

    public Color getLocalityColor() {
        return localityColor;
    }

    public void setLocalityColor(Color value) {
        if (!localityColor.equals(value)) {
            localityColor = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_LOCALITY_COLOR));
        }
    }

    @Override
    public void draw(DrawRequest r) {
        super.draw(r);
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        int localityNo = getReferencedTransition().getLocality();
        Color localityColor = getLocalityColor();
        double localitySize = 0.5 * CommonVisualSettings.getNodeSize();
        g.setColor(Coloriser.colorise(localityColor, d.getColorisation()));
        String localityString = Integer.toString(localityNo);
        Font font = g.getFont().deriveFont((float) localitySize);
        Rectangle2D rect = font.getStringBounds(localityString, g.getFontRenderContext());
        g.setFont(font);
        g.drawString(localityString, (float) -rect.getCenterX(), (float) -rect.getCenterY());
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        double size = CommonVisualSettings.getNodeSize() - CommonVisualSettings.getStrokeWidth();
        return pointInLocalSpace.distanceSq(0, 0) < size * size / 4;
    }
}

package org.workcraft.plugins.sts;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.MixUtils;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.Stylable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.builtin.settings.CommonVisualSettings;
import org.workcraft.utils.Coloriser;

@Hotkey(KeyEvent.VK_S)
@DisplayName("State")
@SVGIcon("images/steptransitionsystem-node-state.svg")
public class VisualState extends VisualComponent {

//    public VisualState(State state) {
//        this(state, true, true, true);
//    }
//
//    // not sure about this yet.
//    public VisualState() {
//        this(null, true, true, true);
//    }
//    public VisualState(State state, boolean hasColorProperties, boolean hasLabelProperties, boolean hasNameProperties) {
//        super(state, hasColorProperties, hasLabelProperties, hasNameProperties);
//    }

    public static final String PROPERTY_INITIAL_MARKER_POSITIONING = "Initial marker positioning";

    private static double size = 1.0;
    private static float strokeWidth = 0.1f;
    private Positioning initialMarkerPositioning = Positioning.TOP;

    public VisualState(State state) {
        super(state);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualState, Boolean>(this, State.PROPERTY_INITIAL,
                Boolean.class, false, false) {
            @Override
            public void setter(VisualState object, Boolean value) {
                object.getReferencedState().setInitial(value);
            }

            @Override
            public Boolean getter(VisualState object) {
                return object.getReferencedState().isInitial();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualState, Positioning>(this,
                PROPERTY_INITIAL_MARKER_POSITIONING, Positioning.class, true, true) {
            @Override
            public void setter(VisualState object, Positioning value) {
                object.setInitialMarkerPositioning(value);
            }

            @Override
            public Positioning getter(VisualState object) {
                return object.getInitialMarkerPositioning();
            }
        });
    }

    public Positioning getInitialMarkerPositioning() {
        return initialMarkerPositioning;
    }

    public void setInitialMarkerPositioning(Positioning value) {
        if (initialMarkerPositioning != value) {
            initialMarkerPositioning = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_INITIAL_MARKER_POSITIONING));
        }
    }

    public State getReferencedState() {
        return (State) getReferencedComponent();
    }

    @Override
    public boolean getNameVisibility() {
        return super.getNameVisibility();
    }

    @Override
    public void draw(DrawRequest r) {
        // super.draw(r);
        Graphics2D g = r.getGraphics();
        Decoration d = r.getDecoration();
        // to set the case node size.
        double caseSize = 0.4 * CommonVisualSettings.getNodeSize();
        double casePos = -0.5 * caseSize;
        g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
        g.setStroke(new BasicStroke((float) CommonVisualSettings.getStrokeWidth()));
        g.fill(new Ellipse2D.Double(casePos, casePos, caseSize, caseSize));

        if (getReferencedState().isInitial()) {
            g.setStroke(new BasicStroke(strokeWidth));
            g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
            if (getInitialMarkerPositioning() == Positioning.CENTER) {
                g.fill(getInitialMarkerCenterShape());
            } else {
                AffineTransform savedTransform = g.getTransform();
                AffineTransform rotateTransform = getInitialMarkerPositioning().getTransform();
                g.transform(rotateTransform);
                g.draw(getInitialMarkerShape());
                g.setTransform(savedTransform);
            }
        }
        // to set the name position in the right place
        drawLabelInLocalSpace(r);
        // to make the case node name visible.
        drawNameInLocalSpace(r);
    }

    private Shape getInitialMarkerShape() {
        double arrowSize = size / 4;
        Path2D shape = new Path2D.Double();
        shape.moveTo(0.0, -size - strokeWidth);
        shape.lineTo(0.0, -size / 2 - strokeWidth);
        shape.moveTo(-arrowSize / 2, -size / 2 - strokeWidth / 2 - arrowSize);
        shape.lineTo(0.0, -size / 2 - strokeWidth / 2);
        shape.lineTo(arrowSize / 2, -size / 2 - strokeWidth / 2 - arrowSize);
        return shape;
    }

    private Shape getInitialMarkerCenterShape() {
        double s = size / 4;
        return new Ellipse2D.Double(-s / 2, -s / 2, s, s);
    }

    @Override
    public Rectangle2D getBoundingBoxInLocalSpace() {
        Rectangle2D bb = super.getBoundingBoxInLocalSpace();
        if (getReferencedState().isInitial()) {
            bb = BoundingBoxHelper.union(bb, getInitialMarkerShape().getBounds2D());
        }
        return bb;
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        return pointInLocalSpace.distanceSq(0, 0) < size * size / 4;
    }

    @Override
    public void mixStyle(Stylable... srcs) {
        super.mixStyle(srcs);
        boolean isInitial = false;
        LinkedList<Positioning> initialMarkerPositioning = new LinkedList<>();
        for (Stylable src : srcs) {
            if (src instanceof VisualState) {
                VisualState srcState = (VisualState) src;
                if (srcState.getReferencedState().isInitial()) {
                    isInitial = true;
                }
                initialMarkerPositioning.add(srcState.getInitialMarkerPositioning());
            }
        }
        getReferencedState().setInitial(isInitial);
        setInitialMarkerPositioning(MixUtils.vote(initialMarkerPositioning, Positioning.TOP));
    }
}
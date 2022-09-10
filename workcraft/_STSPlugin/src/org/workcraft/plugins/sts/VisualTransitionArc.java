package org.workcraft.plugins.sts;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.RenderedText;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.builtin.settings.CommonEditorSettings;
import org.workcraft.plugins.builtin.settings.CommonVisualSettings;
import org.workcraft.utils.Coloriser;
import org.workcraft.utils.Geometry;

/*@DisplayName("Transition")
@Hotkey(KeyEvent.VK_T)
@SVGIcon("images/tool-connection.svg")*/
public class VisualTransitionArc extends VisualConnection {
	/** Properties of the step name are: **/
    public static String PROPERTY_STEP = "Step";
	public static final String PROPERTY_STEP_COLOR = "Step color";
	private RenderedText stepRenderedText = new RenderedText("", getNameFont(), Positioning.CENTER,
			new Point2D.Double());
	private Color stepColor = CommonVisualSettings.getNameColor();

	/** Properties of the locality are: **/
	public static final String PROPERTY_LOCALITY_COLOR = "Locality color";
	public static final String PROPERTY_EVENTOFSETP_NAME = "Event";

	public static final String eventName = "";
    protected Color localityColor = CommonVisualSettings.getBorderColor();
    protected Color eventLocalityColor = CommonVisualSettings.getFillColor();
	public static final Map<Set<String>, Integer> eventLocality = new HashMap<>();

	private TransitionArc mathConnectionArc;
	VisualNode fromState;
	VisualNode toState;
	public VisualTransitionArc() {
		this(null, null, null);
	}

	public VisualTransitionArc(TransitionArc mathConnectionArc) {
		super();
		this.mathConnectionArc = mathConnectionArc;
		addPropertyDeclarations();

	}

	public VisualTransitionArc(VisualNode fromState, VisualNode toState, TransitionArc mathConnectionArc) {
		super(mathConnectionArc, fromState, toState);
		this.mathConnectionArc = mathConnectionArc;
		this.fromState = fromState;
		this.toState = toState;
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(
				new PropertyDeclaration<VisualConnection, String>(this, PROPERTY_STEP, String.class, true, true) {
					@Override
					public void setter(VisualConnection object, String value) {
						((VisualTransitionArc) object).setName(value);
					}
					@Override
					public String getter(VisualConnection object) {
						return ((VisualTransitionArc) object).getName();
					}
				});
		addPropertyDeclaration(
				new PropertyDeclaration<VisualConnection, Color>(this, PROPERTY_STEP_COLOR, Color.class, true, true) {
					@Override
					public void setter(VisualConnection object, Color value) {
						((VisualTransitionArc) object).setStepColor(value);
					}
					@Override
					public Color getter(VisualConnection object) {
						return ((VisualTransitionArc) object).getStepColor();
					}
				});
	}

	public String getName() {
		return mathConnectionArc.getName();
	}

	public void setName(String value) {
		mathConnectionArc.setName(value);
	}

	public Font getNameFont() {
		return new Font(Font.SANS_SERIF, Font.ITALIC, 1).deriveFont(0.5f);
	}

	public boolean getNameVisibility() {
		return CommonVisualSettings.getNameVisibility();
	}

	public Color getStepColor() {
		return stepColor;
	}

	public void setStepColor(Color value) {
		if (!stepColor.equals(value)) {
			stepColor = value;
			sendNotification(new PropertyChangedEvent(this, PROPERTY_STEP_COLOR));
		}
	}
	
	protected boolean cacheStepRenderedText(DrawRequest r) {
		String name = this.mathConnectionArc.getName();
		return cacheStepRenderedText(name, getNameFont(), Positioning.CENTER, new Point2D.Double());
	}

	protected boolean cacheStepRenderedText(String text, Font font, Positioning namePositioning, Point2D offset) {
		stepRenderedText = new RenderedText(text, font, Positioning.CENTER, new Point2D.Double());
		return true;
	}

	private AffineTransform getLabelTransform() {
		ConnectionGraphic graphic = getGraphic();
		Point2D middlePoint = graphic.getPointOnCurve(0.5);
		Point2D firstDerivative = graphic.getDerivativeAt(0.5);
		Point2D secondDerivative = graphic.getSecondDerivativeAt(0.5);
		if (firstDerivative.getX() < 0) {
			firstDerivative = Geometry.multiply(firstDerivative, -1);
		}
		Rectangle2D bb = stepRenderedText.getBoundingBox();
		Point2D labelPosition = new Point2D.Double(bb.getCenterX(), bb.getMaxY());
		if (Geometry.crossProduct(firstDerivative, secondDerivative) < 0) {
			labelPosition.setLocation(labelPosition.getX(), bb.getMinY());
		}
		AffineTransform transform = AffineTransform.getTranslateInstance(middlePoint.getX() - labelPosition.getX(),
				middlePoint.getY() - labelPosition.getY());
		AffineTransform rotateTransform = AffineTransform.getRotateInstance(firstDerivative.getX(),
				firstDerivative.getY(), labelPosition.getX(), labelPosition.getY());
		transform.concatenate(rotateTransform);
		return transform;
	}

	protected void drawNameInLocalSpace(DrawRequest r) {
		if (getNameVisibility() && (stepRenderedText != null)) {
			cacheStepRenderedText(r);
			Graphics2D g = r.getGraphics();
			Decoration d = r.getDecoration();
			AffineTransform oldTransform = g.getTransform();
			AffineTransform transform = getLabelTransform();
			g.transform(transform);
			Color background = d.getBackground();
			if (background != null) {
				g.setColor(Coloriser.colorise(CommonEditorSettings.getBackgroundColor(), background));
				Rectangle2D box = BoundingBoxHelper.expand(stepRenderedText.getBoundingBox(), 0.2, 0.0);
				g.fill(box);
			}
			g.setColor(Coloriser.colorise(getStepColor(), d.getColorisation()));
			stepRenderedText.draw(g);
			g.setTransform(oldTransform);
		}
	}

	public void draw(DrawRequest r) {
		this.drawNameInLocalSpace(r);
	}

	public TransitionArc getReferencedTransitionArc() {
		return (TransitionArc) getReferencedConnection();
	}

	private Rectangle2D getLabelBoundingBox() {
		return BoundingBoxHelper.transform(stepRenderedText.getBoundingBox(), getLabelTransform());
	}

	@Override
	public Rectangle2D getBoundingBox() {
		Rectangle2D labelBB = getLabelBoundingBox();
		return BoundingBoxHelper.union(super.getBoundingBox(), labelBB);
	}

	@Override
	public boolean hitTest(Point2D pointInParentSpace) {
		Rectangle2D labelBB = getLabelBoundingBox();
		if (labelBB != null && labelBB.contains(pointInParentSpace))
			return true;
		return super.hitTest(pointInParentSpace);
	}

	public MathConnection getMathMathConnectionArc() {
		return mathConnectionArc;
	}

}

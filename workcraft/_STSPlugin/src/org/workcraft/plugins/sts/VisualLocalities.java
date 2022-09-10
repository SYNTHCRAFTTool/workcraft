package org.workcraft.plugins.sts;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.plugins.builtin.settings.CommonVisualSettings;

@SVGIcon("images/steptransitionsystem-node-state.svg")
public class VisualLocalities extends VisualComponent {
    /** Properties of each locality are: **/
    public static final String PROPERTY_EVENTOFSETP_NAME = "Event";
    public static final String PROPERTY_LOCALITY = "Locality";
    protected Color localityColor = CommonVisualSettings.getBorderColor();
    protected Color fillLocalityColor = CommonVisualSettings.getFillColor();
    public static final Map<Set<String>, Integer> eventLocality = new HashMap<>();

    public VisualLocalities(Localities localities) {
        super(localities);
        addPropertyDeclarations();
        removePropertyDeclarationByName(PROPERTY_NAME_POSITIONING);
        removePropertyDeclarationByName(PROPERTY_NAME_COLOR);
        removePropertyDeclarationByName(PROPERTY_LABEL);
        removePropertyDeclarationByName(PROPERTY_LABEL_POSITIONING);
        removePropertyDeclarationByName(PROPERTY_LABEL_COLOR);
    }

    // For Locality number.
    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualLocalities, String>(this, PROPERTY_EVENTOFSETP_NAME,
                String.class, false, true) {
            @Override
            public void setter(VisualLocalities object, String value) {
                ((VisualLocalities) object).setName(value);
            }

            @Override
            public String getter(VisualLocalities object) {
                return ((VisualLocalities) object).getName();
            }
        });

        addPropertyDeclaration(new PropertyDeclaration<VisualLocalities, Integer>(this, PROPERTY_LOCALITY,
                Integer.class, false, true) {
            @Override
            public void setter(VisualLocalities object, Integer value) {
                ((VisualLocalities) object).setLocality(value);
            }

            @Override
            public Integer getter(VisualLocalities object) {
                return ((VisualLocalities) object).getLocality();
            }
        });
    }

    // Event name.
    public String getName() {
        return Localities.getEventName();
    }

    public void setName(String value) {
        Localities.setEventName(value);
    }

    public Font getNameFont() {
        return new Font(Font.SANS_SERIF, Font.ITALIC, 1).deriveFont(0.5f);
    }

    public int getLocality() {
        return Localities.getLocality();
    }

    public void setLocality(int value) {
        Localities.setLocality(value);
    }

    public Color getLocalityColor() {
        return localityColor;
    }

    public void setLocalityColor(Color value) {
        if (!localityColor.equals(value)) {
            localityColor = value;
        }
    }

    public Color getLocalityFillColor() {
        return fillLocalityColor;
    }

    public void setLocalityFillColor(Color value) {
        if (!localityColor.equals(value)) {
            fillLocalityColor = value;
        }
    }
}

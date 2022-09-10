package org.workcraft.plugins.enl;

import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.dom.Container;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.gui.tools.*;
import org.workcraft.plugins.enl.tools.ConditionGeneratorTool;
import org.workcraft.plugins.enl.tools.ElementaryNetConnectionTool;
import org.workcraft.plugins.enl.tools.ElementaryNetSelectionTool;
import org.workcraft.plugins.enl.tools.ElementaryNetSimulationTool;
import org.workcraft.plugins.enl.tools.TransitionGeneratorTool;
import org.workcraft.plugins.enl.utils.ConversionUtils;
import java.util.ArrayList;
import java.util.List;

@DisplayName("Elementary Net System with Localities")
public class VisualENL extends AbstractVisualModel {

    public VisualENL(ENL model) {
        this(model, null);
    }

    public VisualENL(ENL model, VisualGroup root) {
        super(model, root);
        setGraphEditorTools();
    }

    private void setGraphEditorTools() {
        List<GraphEditorTool> tools = new ArrayList<>();
        tools.add(new ElementaryNetSelectionTool());
        tools.add(new CommentGeneratorTool());
        tools.add(new ElementaryNetConnectionTool());
        tools.add(new ConditionGeneratorTool());
        tools.add(new TransitionGeneratorTool());
        tools.add(new ElementaryNetSimulationTool());
        setGraphEditorTools(tools);
    }

    public ENL getElementaryNetModel() {
        return (ENL) getMathModel();
    }

    public VisualCondition createCondition(String mathName, Container container) {
        if (container == null) {
            container = getRoot();
        }
        Container mathContainer = NamespaceHelper.getMathContainer(this, container);
        Condition condition = getElementaryNetModel().createCondition(mathName, mathContainer);
        VisualCondition visualPlace = new VisualCondition(condition);
        container.add(visualPlace);
        return visualPlace;
    }

    public VisualTransition createTransition(String mathName, Container container) {
        if (container == null) {
            container = getRoot();
        }
        Container mathContainer = NamespaceHelper.getMathContainer(this, container);
        Transition transition = getElementaryNetModel().createTransition(mathName, mathContainer);
        VisualTransition visualTransition = new VisualTransition(transition);
        add(visualTransition);

        return visualTransition;
    }

    @Override
    public void validateConnection(VisualNode first, VisualNode second) throws InvalidConnectionException {
        if (second.equals(first)) {
            throw new InvalidConnectionException("Self-loops are not allowed");
        }

        if ((first instanceof VisualCondition) && (second instanceof VisualCondition)) {
            throw new InvalidConnectionException("Connections between conditions are not allowed");
        }

        if ((first instanceof VisualTransition) && (second instanceof VisualTransition)) {
            throw new InvalidConnectionException("Connections between transitions are not allowed");
        }

        if (ConversionUtils.hasProducingArcConnection(this, first, second)) {
            throw new InvalidConnectionException("This producing arc already exists.");
        }

        if (ConversionUtils.hasConsumingArcConnection(this, first, second)) {
            throw new InvalidConnectionException("This consuming arc already exists.");
        }
        if ((ConversionUtils.hasConsumingORProducingArcConnection(this, first, second))
                || (ConversionUtils.hasConsumingORProducingArcConnection(this, second, first))) {
            throw new InvalidConnectionException("Nodes are already connected.");
        }
    }
}

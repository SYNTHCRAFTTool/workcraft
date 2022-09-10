package org.workcraft.plugins.enl.commands;


import org.workcraft.Framework;
import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.editor.GraphEditorPanel;
import org.workcraft.plugins.enl.VisualENL;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ReductionRuleOneCommand extends AbstractTransformationCommand {

	  @Override
	    public String getDisplayName() {
	        return "Applay reduction rule one (complement regions)";
	    }

	    @Override
	    public Position getPosition() {
	        return Position.BOTTOM_MIDDLE;
	    }

	    @Override
	    public boolean isApplicableTo(WorkspaceEntry we) {
	        return WorkspaceUtils.isApplicable(we, VisualENL.class);
	    }

	    @Override
	    public Void execute(WorkspaceEntry we) {
	        final Framework framework = Framework.getInstance();
	        we.saveMemento();
	       // final VisualElementaryNet venl = WorkspaceUtils.getAs(we, VisualElementaryNet.class);
	        //policy.unbundleTransitions(policy.getVisualBundledTransitions());
	       // final ENLToSTSConverter converter = new ENLToSTSConverter(venl);
	        
	      //  final VisualStepTransitionSystem vsts = converter.getSTS();
	//	       Map<Region, Region>  ComplementRegions = vsts.getStepTransitionSystemModel().getComplementRegions(vsts.getStepTransitionSystemModel());

//	        final ComplementRegions bundler = new ComplementRegions(converter);
	        //bundler.run();
	        if (framework.isInGuiMode()) {
	            final MainWindow mainWindow = framework.getMainWindow();
	            final GraphEditorPanel editor = mainWindow.getCurrentEditor();
	            editor.repaint();
	        }
	        return null;
	    }

	    @Override
	    public void transform(VisualModel model, VisualNode node) {
	    }
}

package org.workcraft.plugins.sts;

import org.workcraft.Framework;
import org.workcraft.Version;
import org.workcraft.plugins.CompatibilityManager;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.sts.commands.EventStateSeparationPropertyCommand;
import org.workcraft.plugins.sts.commands.StateSeparationPropertyCommand;
import org.workcraft.utils.ScriptableCommandUtils;

public class STSPlugin implements Plugin {

    @Override
    public String getDescription() {
        return "Step Transition System Plugin";
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();
        pm.registerModelDescriptor(STSDescriptor.class);
        
        // This command makes the "checkEnlReachability" order to show under the "Verification" command in the menu bar.       
        ScriptableCommandUtils.register(org.workcraft.plugins.sts.commands.ReachabilityVerificationCommand.class, "checkEnlReachability",
                "check if STS satisfies axiom 1");
        // This command makes the "Non-emptyEvents" order to show under the "Verification" command in the menu bar. 
        ScriptableCommandUtils.register(org.workcraft.plugins.sts.commands.NonEmptyEventVerificationCommand.class, "checkNon-emptyEvents",
                "check if STS satisfies axiom 2");
     // This command makes the "State Separation Property" order to show under the "Verification" command in the menu bar. 
        ScriptableCommandUtils.register(StateSeparationPropertyCommand.class, "check State Separation Property",
                "check if STS satisfies axiom 3");
     // This command makes the "Event/State Separation Property" order to show under the "Verification" command in the menu bar. 
        ScriptableCommandUtils.register(EventStateSeparationPropertyCommand.class, "check Event/State Separation Property and maximal execution manner",
                "check if STS satisfies axiom 4");       
    }
    
    private void initCompatibilityManager() {
        final Framework framework = Framework.getInstance();
        final CompatibilityManager cm = framework.getCompatibilityManager();

        Version v310 = new Version(3, 1, 0, Version.Status.RELEASE);

        cm.registerMetaReplacement(v310,
                "<descriptor class=\"org.workcraft.plugins.steptransitionsystem.steptransitionsystemModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.steptransitionsystem.steptransitionsystemDescriptor\"/>");

        Version v323 = new Version(3, 2, 3, Version.Status.RELEASE);

        cm.registerMetaReplacement(v323,
                "<descriptor class=\"org.workcraft.plugins.steptransitionsystem.steptransitionsystemDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.steptransitionsystem.steptransitionsystemDescriptor\"/>");

        cm.registerModelReplacement(v323, "org.workcraft.plugins.steptransitionsystem.StepTransitionSystem", STS.class.getName());

        cm.registerModelReplacement(v323, "org.workcraft.plugins.steptransitionsystem.VisualStepTransitionSystem", VisualSTS.class.getName());
    }
}

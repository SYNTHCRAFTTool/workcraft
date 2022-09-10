package org.workcraft.plugins.enl;

import org.workcraft.Framework;
import org.workcraft.Version;
import org.workcraft.plugins.CompatibilityManager;
import org.workcraft.plugins.Plugin;
import org.workcraft.plugins.PluginManager;
import org.workcraft.plugins.enl.commands.ENLStatisticsCommand;
import org.workcraft.plugins.enl.commands.ENLToSTSConvesionCommand;
import org.workcraft.plugins.enl.commands.CheckSoundnessCommand;
import org.workcraft.plugins.enl.commands.STSToENLConvesionCommand;
import org.workcraft.plugins.enl.commands.STSToENLLCConvesionCommand;
import org.workcraft.plugins.enl.commands.STSToENLLCredRulesConvesionCommand;
import org.workcraft.plugins.enl.commands.STSToENLLCwithCRmConvesionCommand;
import org.workcraft.plugins.enl.commands.STSToENLLCwithCRmbConvesionCommand;
import org.workcraft.plugins.enl.commands.STSToENLLCwithCRmbredRulesConvesionCommand;
import org.workcraft.plugins.enl.commands.STSToENLLCwithCRmredRulesConvesionCommand;
import org.workcraft.plugins.enl.commands.STSToENLWayTowConvesionCommand;
import org.workcraft.plugins.enl.commands.STSToENLWay2redRulesConvesionCommand;
import org.workcraft.plugins.enl.commands.STSToENLredRulesConvesionCommand;

/* still need more methods.*/
import org.workcraft.utils.ScriptableCommandUtils;

@SuppressWarnings("unused")
public class ENLPlugin implements Plugin {

    @Override
    public String getDescription() {
        return "ENL-system with localities plugin";
    }

    @Override
    public void init() {
        final Framework framework = Framework.getInstance();
        final PluginManager pm = framework.getPluginManager();
        pm.registerModelDescriptor(ENLDescriptor.class);
        
        // This command makes the "convertENLToSTS" order to show under the "Conversion" command in the menu bar.
        ScriptableCommandUtils.register(ENLToSTSConvesionCommand.class, "convertENLToSTS",
                "convert the given Elementary net with localities net 'work' into a Step transition system");
        // This command makes the "convertSTSToENL" order to show under the "Conversion" command in the menu bar.
        ScriptableCommandUtils.register(STSToENLConvesionCommand.class, "convertSTSToENL",
                "convert the given Step Transition system 'work' into an ENL-system with localities");
        
     // This command makes the "convertSTSToENL" order to show under the "Conversion" command in the menu bar.
        ScriptableCommandUtils.register(STSToENLWayTowConvesionCommand.class, "convertSTSToENL-Way2",
                "convert the given Step Transition system 'work' into an ENL-system with localities");

        // This command makes the "convertSTSToENLLC" order to show under the "Conversion" command in the menu bar.
        ScriptableCommandUtils.register(STSToENLLCConvesionCommand.class, "convertSTSToENLLC",
                "convert the given Step Transition system 'work' into an ENL/LC-system");

        // This command makes the "convertSTSToENLLC" order to show under the "Conversion" command in the menu bar.
        ScriptableCommandUtils.register(STSToENLLCwithCRmConvesionCommand.class, "convertSTSToENLLC",
                "convert the given Step Transition system 'work' into an ENL/LC-system with the minimum number");

        // This command makes the "convertSTSToENLLC" order to show under the "Conversion" command in the menu bar.
        ScriptableCommandUtils.register(STSToENLLCwithCRmbConvesionCommand.class, "convertSTSToENLLC",
                "convert the given Step Transition system 'work' into an ENL/LC-system with the smallest balance indicators");
        
        // This command makes the "convertSTSToENL" order to show under the "Conversion" command in the menu bar.
        ScriptableCommandUtils.register(STSToENLredRulesConvesionCommand.class, "convertSTSToENL",
                "convert the given Step Transition system 'work' into an ENL-system with localities with applying reduction rules");
        
     // This command makes the "convertSTSToENL" order to show under the "Conversion" command in the menu bar.
        ScriptableCommandUtils.register(STSToENLWay2redRulesConvesionCommand.class, "convertSTSToENL-Way2",
                "convert the given Step Transition system 'work' into an ENL-system with localities with applying reduction rules");

        // This command makes the "convertSTSToENLLC" order to show under the "Conversion" command in the menu bar.
        ScriptableCommandUtils.register(STSToENLLCredRulesConvesionCommand.class, "convertSTSToENLLC",
                "convert the given Step Transition system 'work' into an ENL/LC-system with applying reduction rules");

        // This command makes the "convertSTSToENLLC" order to show under the "Conversion" command in the menu bar.
        ScriptableCommandUtils.register(STSToENLLCwithCRmredRulesConvesionCommand.class, "convertSTSToENLLC",
                "convert the given Step Transition system 'work' into an ENL/LC-system with the minimum number with applying reduction rules");

        // This command makes the "convertSTSToENLLC" order to show under the "Conversion" command in the menu bar.
        ScriptableCommandUtils.register(STSToENLLCwithCRmbredRulesConvesionCommand.class, "convertSTSToENLLC",
                "convert the given Step Transition system 'work' into an ENL/LC-system with the smallest balance indicators with applying reduction rules");
               
        // This command makes the "Non-emptyEvents" order to show under the "Verification" command in the menu bar. 
        ScriptableCommandUtils.register(CheckSoundnessCommand.class, "checkIsolatedEventsandConditions",
                "check the ENL 'work' for pre-post non-empty events and isolated conditions");
                
        ScriptableCommandUtils.register(ENLStatisticsCommand.class, "statENL",
                "advanced complexity estimates for the enl 'work'");
  
    }
    
    private void initCompatibilityManager() {
        final Framework framework = Framework.getInstance();
        final CompatibilityManager cm = framework.getCompatibilityManager();

        Version v310 = new Version(3, 1, 0, Version.Status.RELEASE);

        cm.registerMetaReplacement(v310,
                "<descriptor class=\"org.workcraft.plugins.elementarynet.ElementaryNetModelDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.elementarynet.ElementaryNetDescriptor\"/>");

        Version v323 = new Version(3, 2, 3, Version.Status.RELEASE);

        cm.registerMetaReplacement(v323,
                "<descriptor class=\"org.workcraft.plugins.elementarynet.ElementaryNetDescriptor\"/>",
                "<descriptor class=\"org.workcraft.plugins.elementarynet.ElementaryDescriptor\"/>");

        cm.registerModelReplacement(v323, "org.workcraft.plugins.elementarynet.ElementaryNet", ENL.class.getName());

        cm.registerModelReplacement(v323, "org.workcraft.plugins.elementarynet.VisualElementaryNet", VisualENL.class.getName());
    }
}
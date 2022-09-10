package org.workcraft.plugins.enl.commands;

import java.util.Map;

import org.workcraft.plugins.enl.ENL;
import org.workcraft.plugins.enl.VisualENL;
import org.workcraft.plugins.enl.tools.STSToENLConverter;
import org.workcraft.plugins.sts.Region;
import org.workcraft.plugins.sts.VisualSTS;

public class ComplementRegions {
    private final VisualSTS vsts;
	private final VisualENL venl;
	private final ENL enl;
	 Map<Region, Region> complementRegions;
public ComplementRegions(STSToENLConverter vsts, Map<Region, Region> complementRegions) {
	this.vsts = vsts.getFirstSTS();
	this.venl = vsts.getENL();
	this.enl = venl.getElementaryNetModel();
	this.complementRegions = complementRegions;
}



}

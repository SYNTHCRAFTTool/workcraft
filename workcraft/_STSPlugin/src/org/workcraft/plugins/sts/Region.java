
package org.workcraft.plugins.sts;

import java.util.Set;

import org.workcraft.annotations.IdentifierPrefix;

@IdentifierPrefix(value = "r", isInternal = true)
public class Region {	
	Set<State> r;
	Set<String> in;
	Set<String> out;

	public Region(Set<String> in, Set<State> r,  Set<String> out) {
		this.in = in;
		this.r = r;
		this.out = out;
	}

	public void setRinRegion(Set<State> r) {
		this.r = r;
	}
	
	public Set<State> getRinRegion(){
		return r;
	}
	
	public void setEventsEnteringR(Set<String> in) {
		this.in = in;
	}
	
	public Set<String> getEventsEnteringR(){
		return in;
	}
	
	public void setEventsLeavingR(Set<String> out) {
		this.out = out;
	}
	
	public Set<String> getEventsLeavingR(){
		return out;
	}
}



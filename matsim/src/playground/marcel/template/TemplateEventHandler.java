/* *********************************************************************** *
 * project: org.matsim.*
 * PluginEventHandler.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package playground.marcel.template;

import org.matsim.core.events.AgentDepartureEventImpl;
import org.matsim.core.events.handler.AgentDepartureEventHandler;

public class TemplateEventHandler implements AgentDepartureEventHandler {

	private int counter = 0;
	
	public void handleEvent(final AgentDepartureEventImpl event) {
		this.counter++;
	}

	public void reset(final int iteration) {
		this.counter = 0;
	}
	
	public int getCount() {
		return this.counter;
	}

}

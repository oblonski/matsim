/* *********************************************************************** *
 * project: org.matsim.*
 * SwitchNonChainBasedMode.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
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
package playground.ivt.replanning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.matsim.core.config.Config;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.replanning.modules.AbstractMultithreadedModule;
import org.matsim.population.algorithms.PlanAlgorithm;

/**
 * Randomly mutates mode of non-chain based trips.
 * @author thibautd
 */
public class SwitchNonChainBasedModeModule extends AbstractMultithreadedModule {
	private final Config config;

	public SwitchNonChainBasedModeModule(final Config config) {
		super( config.global() );
		this.config = config;
	}

	@Override
	public PlanAlgorithm getPlanAlgoInstance() {
		return new SwitchNonChainBasedModeAlgorithm(
				getReplanningContext().getTripRouter(),
				getNonChainBasedModes(),
				0.7,
				MatsimRandom.getLocalInstance() );
	}

	private String[] getNonChainBasedModes() {
		final Collection<String> chainbased = Arrays.asList( config.subtourModeChoice().getChainBasedModes() );
		final Collection<String> modes = new ArrayList<String>( );
		for ( String m : config.subtourModeChoice().getModes() ) {
			if ( !chainbased.contains( m ) ) modes.add( m );
		}

		return modes.toArray( new String[ modes.size() ] );
	}
}


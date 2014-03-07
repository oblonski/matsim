/* *********************************************************************** *
 * project: org.matsim.*
 * NewVisNetworkBuilder
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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
package playground.dgrether.xvis.vismodel;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.lanes.ModelLane;
import org.matsim.lanes.data.v20.LaneDefinitions20;
import org.matsim.lanes.data.v20.LanesToLinkAssignment20;
import org.matsim.lanes.utils.LanesUtils;
import org.matsim.lanes.vis.VisLane;
import org.matsim.lanes.vis.VisLaneModelBuilder;
import org.matsim.lanes.vis.VisLinkWLanes;
import org.matsim.signalsystems.data.SignalsData;
import org.matsim.signalsystems.data.signalgroups.v20.SignalGroupData;
import org.matsim.signalsystems.data.signalsystems.v20.SignalData;
import org.matsim.signalsystems.data.signalsystems.v20.SignalSystemData;
import org.matsim.signalsystems.vis.VisSignal;
import org.matsim.signalsystems.vis.VisSignalGroup;
import org.matsim.signalsystems.vis.VisSignalSystem;
import org.matsim.vis.snapshotwriters.SnapshotLinkWidthCalculator;
import org.matsim.vis.snapshotwriters.VisLink;

import playground.dgrether.signalsystems.utils.DgSignalsUtils;


/**
 * @author dgrether
 */
public class VisScenarioBuilder {

	public VisScenario createVisScenario(Scenario scenario) {
		VisScenario visScenario = new VisScenario(scenario.getNetwork());
		CoordinateTransformation transform = visScenario.getVisTransform();
		createVisLinks(scenario, visScenario, transform);
		createVisSignals(scenario, visScenario, transform);
		return visScenario;
	}

	private void createVisSignals(Scenario scenario, VisScenario visScenario, CoordinateTransformation transform) {
		SignalsData signals = (SignalsData) scenario.getScenarioElement(SignalsData.ELEMENT_NAME);
		for (SignalSystemData ssd : signals.getSignalSystemsData().getSignalSystemData().values()){
			Set<Node> nodes = DgSignalsUtils.calculateSignalizedNodes4System(ssd, scenario.getNetwork());
			Node node = nodes.iterator().next();
			Coord visCoord = visScenario.getVisTransform().transform(node.getCoord());
			String systemIdS = ssd.getId().toString();
			VisSignalSystem visSignalSystem = new VisSignalSystem(systemIdS);
			visSignalSystem.setVisCoordinate(new Point2D.Float((float)visCoord.getX(), (float)visCoord.getY()));
			visScenario.getVisSignalSystemsByIdMap().put(systemIdS, visSignalSystem);
			
			Map<Id, SignalGroupData> groups = signals.getSignalGroupsData().getSignalGroupDataBySystemId(ssd.getId());
			for (SignalGroupData group : groups.values()){
				VisSignalGroup visGroup = new VisSignalGroup(systemIdS, group.getId().toString());
				visSignalSystem.addOTFSignalGroup(visGroup);
				for (Id signalId : group.getSignalIds()){
					VisSignal visSignal = new VisSignal(systemIdS, signalId.toString());
					visGroup.addSignal(visSignal);
					SignalData signal = signals.getSignalSystemsData().getSignalSystemData().get(ssd.getId()).getSignalData().get(signalId);
					VisLinkWLanes visLink = visScenario.getLanesLinkData().get(signal.getLinkId().toString());
					if (signal.getLaneIds() == null || signal.getLaneIds().isEmpty()){
						visLink.addSignal(visSignal);
					}
					else {
						for (Id laneId : signal.getLaneIds()){
							VisLane visLane = visLink.getLaneData().get(laneId.toString());
							visLane.addSignal(visSignal);
						}
					}
					
					if (! (signal.getTurningMoveRestrictions() == null || signal.getTurningMoveRestrictions().isEmpty())){
						for (Id outLinkId : signal.getTurningMoveRestrictions()){
							VisLinkWLanes vl = visScenario.getLanesLinkData().get(outLinkId.toString());
							visSignal.addTurningMoveRestriction(vl);
						}
					}
				}
			}
		}
	}

	private void createVisLinks(Scenario scenario, VisScenario visScenario,
			CoordinateTransformation transform) {
		VisLaneModelBuilder visLaneModelBuilder = new VisLaneModelBuilder();
		LaneDefinitions20 lanes = (LaneDefinitions20) scenario.getScenarioElement(LaneDefinitions20.ELEMENT_NAME);
		for (Link l : scenario.getNetwork().getLinks().values()){
			LanesToLinkAssignment20 l2l = null;
			if (lanes != null) {
				l2l = lanes.getLanesToLinkAssignments().get(l.getId());
			}
			VisLink vl = new VisLinkImpl(l);
			List<ModelLane> la = LanesUtils.createLanes(l, l2l);
			VisLinkWLanes link = visLaneModelBuilder.createVisLinkLanes(transform, vl, scenario.getConfig().qsim().getNodeOffset(),  la);
			SnapshotLinkWidthCalculator lwc = new SnapshotLinkWidthCalculator();
			visLaneModelBuilder.recalculatePositions(link, lwc);
			visScenario.getLanesLinkData().put(link.getLinkId(), link);
		}
		visLaneModelBuilder.connect(visScenario.getLanesLinkData());
	}
}
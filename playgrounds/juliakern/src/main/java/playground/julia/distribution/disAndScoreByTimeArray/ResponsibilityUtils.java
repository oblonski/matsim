/* *********************************************************************** *
 * project: org.matsim.*
 * SpatialAveragingForLinkEmissions.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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
package playground.julia.distribution.disAndScoreByTimeArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.matsim.api.core.v01.Id;

import playground.julia.exposure.EmActivity;
import playground.julia.exposure.EmPerCell;
import playground.julia.exposure.ResponsibilityEvent;

public class ResponsibilityUtils {

	int maximalDistance = 3;
	private int noOfXCells;
	private int noOfYCells;
	private Double dist0factor = 0.216;
	private Double dist1factor = 0.132;
	private Double dist2factor = 0.029;
	private Double dist3factor = 0.002;
	
	public ResponsibilityUtils(int maximalDistance, int noOfXCells, int noOfYCells){
		this.maximalDistance = maximalDistance;
		this.noOfXCells = noOfXCells;
		this.noOfYCells = noOfYCells;
	}

	public Map<Id, Double> calculateCausedEmissionCosts(
			Map<Double, Double[][]> duration,
			Map<Double, ArrayList<EmPerCell>> emissionsPerCell) {
		Map<Id, Double> person2costs = new HashMap<Id, Double>();
		
		//go through all time intervals (those without emissions are not relevant)
		for(Double endOfTimeInterval: emissionsPerCell.keySet()){
			ArrayList<EmPerCell> emissionsOfCurrentInterval = emissionsPerCell.get(endOfTimeInterval);
			// em per cell has already price as value and therefore no pollutant type
			// causing person -> add emission price of curent emission per cell X numberOfPeople exposed X timeOfPeople exposed
			for(EmPerCell epc: emissionsOfCurrentInterval){
				Id personId = epc.getPersonId();
				Double exposureDuration =0.0;
				Cell originCell = new Cell(epc.getXbin(), epc.getYbin());
				for(int distance=0; distance <= maximalDistance; distance++){
					for(Cell cell: originCell.getCellsWithExactDistance(noOfXCells, noOfYCells, distance)){
						if(duration.get(endOfTimeInterval)[cell.getX()]!=null){
							if(duration.get(endOfTimeInterval)[cell.getX()][cell.getY()]!=null){
						Double durationForCurrentCell = duration.get(endOfTimeInterval)[cell.getX()][cell.getY()];
						if(durationForCurrentCell!=null){
							switch(distance){
							case 0: exposureDuration += dist0factor * durationForCurrentCell; break;
							case 1: exposureDuration += dist1factor * durationForCurrentCell; break;
							case 2: exposureDuration += dist2factor * durationForCurrentCell; break;
							case 3: exposureDuration += dist3factor * durationForCurrentCell; break;
							}
						}
						}
					}}
				}
				
				if(person2costs.containsKey(personId)){
					Double oldValue = person2costs.get(personId);
					person2costs.put(personId, epc.getConcentration()*exposureDuration+oldValue);
				}else{
					person2costs.put(epc.getPersonId(), epc.getConcentration()*exposureDuration);	
				}
			}
		}
		
		return person2costs;
	}
}
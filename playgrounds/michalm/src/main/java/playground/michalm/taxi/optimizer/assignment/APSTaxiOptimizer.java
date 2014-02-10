/* *********************************************************************** *
 * project: org.matsim.*
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

package playground.michalm.taxi.optimizer.assignment;

import java.util.*;

import org.matsim.contrib.dvrp.MatsimVrpContext;
import org.matsim.contrib.dvrp.data.Vehicle;
import org.matsim.contrib.dvrp.router.*;
import org.matsim.contrib.dvrp.util.LinkTimePair;

import playground.michalm.taxi.model.TaxiRequest;
import playground.michalm.taxi.optimizer.immediaterequest.*;
import playground.michalm.taxi.schedule.TaxiSchedules;


public class APSTaxiOptimizer
    extends OTSTaxiOptimizer
{
    public APSTaxiOptimizer(TaxiScheduler scheduler, MatsimVrpContext context)
    {
        super(scheduler, context);
    }


    protected void scheduleUnplannedRequests()
    {
        for (Vehicle veh : context.getVrpData().getVehicles()) {
            scheduler.removePlannedRequests(TaxiSchedules.getSchedule(veh), unplannedRequests);
        }

        if (unplannedRequests.size() == 0) {
            return;
        }

        List<Vehicle> vehicles = new ArrayList<Vehicle>();
        List<LinkTimePair> departures = new ArrayList<LinkTimePair>();
        for (Vehicle v : context.getVrpData().getVehicles()) {
            LinkTimePair departure = scheduler.getEarliestIdleness(v);
            //LinkTimePair departure = scheduler.getClosestDiversion(v);

            if (departure != null) {
                vehicles.add(v);
                departures.add(departure);
            }
        }

        int vDim = vehicles.size();
        if (vDim == 0) {
            return;
        }

        int rDim = Math.min(vDim, unplannedRequests.size());

        TaxiRequest[] requests = new TaxiRequest[rDim];
        for (int r = 0; r < rDim; r++) {
            requests[r] = unplannedRequests.poll();
        }

        VrpPathCalculator calculator = scheduler.getCalculator();
        ImmediateRequestParams params = scheduler.getParams();
        double[][] costMatrix = new double[vDim][rDim];
        VrpPathWithTravelData[][] paths = new VrpPathWithTravelData[vDim][rDim];

        for (int v = 0; v < vDim; v++) {
            for (int r = 0; r < rDim; r++) {
                LinkTimePair departure = departures.get(v);
                TaxiRequest req = requests[r];

                VrpPathWithTravelData path = calculator.calcPath(departure.link, req.getFromLink(),
                        departure.time);

                costMatrix[v][r] = params.minimizePickupTripTime ? //
                        path.getTravelTime() : //T_P
                        path.getArrivalTime() - req.getSubmissionTime();//T_W

                paths[v][r] = path;
            }

            // for (int r = rDim; r < vDim; r++) {
            //     costMatrix[v][r] = 0;//maybe cost of staying in a unattractive location
            //     paths[v][r] = null;
            // }
        }

        int[] assignment = new HungarianAlgorithm(costMatrix).execute();

        for (int v = 0; v < vehicles.size(); v++) {
            int r = assignment[v];
            if (r == -1) {
                continue;
            }

            VrpPathWithTravelData path = paths[v][r];

            if (path != null) {
                Vehicle veh = vehicles.get(v);
                TaxiRequest req = requests[r];
                scheduler.scheduleRequest(new VehicleRequestPath(veh, req, path));
            }
        }
    }
}
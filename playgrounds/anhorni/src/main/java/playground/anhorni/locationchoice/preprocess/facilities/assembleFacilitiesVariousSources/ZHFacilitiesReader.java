/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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

package playground.anhorni.locationchoice.preprocess.facilities.assembleFacilitiesVariousSources;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.facilities.ActivityFacility;

import playground.anhorni.choiceSetGeneration.helper.ZHFacility;


/*	  0			1		2			3			4			5		6		7			8				9
 * ---------------------------------------------------------------------------------------------------------		
 *0	| ShopID	RetID	Size_descr	dHalt		aAlt02		aAlt10	aAlt20	retailer	Sales_area_m2	classif	
 *1	| Shoptype	PLZ		ORT			STRASSE		HNR			x_CH	y_CH	NAME		HRS_WEEK		TOTAL		
 *2	| mon		V20		V21			V22			V23			V24		tue		V26			V27				V28			
 *3	| V29		V30		wed			V32			V33			V34		V35		V36			thu				V38		
 *4	| V39		V40		V41			V42			fri			V44		V45		V46			V47				V48
 *5	| sat		V50		V51			V52			V53			V54		sun		V56			V57				Tel
 *6	| Email		Web		park_only	Park_joint	Turnover_m2	Hweek	cost_parking_h
 * ---------------------------------------------------------------------------------------------------------
 */


public class ZHFacilitiesReader {
			
	public ZHFacilitiesReader() {
	}
	
	public List<ZHFacility> readFile(final String file)  {
		
		List<ZHFacility> facilities = new Vector<ZHFacility>();
		
		try {
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String curr_line = bufferedReader.readLine(); // Skip header
			
			while ((curr_line = bufferedReader.readLine()) != null) {	
				String[] entries = curr_line.split("\t", -1);
				
				String shopID = entries[0].trim();
				String retailerID =  entries[1].trim();
				int size_descr = Integer.parseInt(entries[2].trim());
				double xCH = Double.parseDouble(entries[4].trim());
				double yCH = Double.parseDouble(entries[5].trim());		
				Coord exactPosition = new CoordImpl(xCH, yCH);
				
				String name = entries[6].trim();
				
				facilities.add(new ZHFacility(
									Id.create(shopID, ActivityFacility.class),
									name,
									new CoordImpl(0.0, 0.0),
									exactPosition, 
									Id.create("0", Link.class),
									Id.create(retailerID, Person.class),
									size_descr,
									0.0,
									0.0));	
			}
			bufferedReader.close();
			fileReader.close();
		
		} catch (IOException e) {
			throw new RuntimeException(e);
		}	
		return facilities;
	}





}

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
package playground.agarwalamit.munich.analysis.userGroup;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.events.handler.EventHandler;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.utils.io.IOUtils;

import playground.agarwalamit.analysis.LoadMyScenarios;
import playground.agarwalamit.siouxFalls.userBenefits.MyUserBenefitsAnalyzer;
import playground.benjamin.scenarios.munich.analysis.filter.PersonFilter;
import playground.benjamin.scenarios.munich.analysis.filter.UserGroup;
import playground.vsp.analysis.modules.monetaryTransferPayments.MonetaryPaymentsAnalyzer;
import playground.vsp.analysis.modules.userBenefits.WelfareMeasure;

/**
 * @author amit
 */
public class UserBenefitsAndTotalWelfarePerUserGroup {

	public static final Logger logger = Logger.getLogger(UserBenefitsAndTotalWelfarePerUserGroup.class);

	public UserBenefitsAndTotalWelfarePerUserGroup(String outputDir, boolean considerAllPersonsInSumOfTolls) {
		personId2UserWelfare_utils = new HashMap<>();
		personId2MonetarizedUserWelfare= new HashMap<>();
		personId2MonetaryPayments = new HashMap<>();
		userGrp2ExcludedToll = new TreeMap<>();
		this.outputDir = outputDir;

		String populationFile = this.outputDir  + "/output_plans.xml.gz";
		String networkFile = this.outputDir  +  "/output_network.xml.gz";
		String configFile =  this.outputDir  + "/output_config.xml";

		this.scenario = LoadMyScenarios.loadScenarioFromPlansNetworkAndConfig(populationFile, networkFile,configFile);
		this.lastIteration = LoadMyScenarios.getLastIteration(configFile);
		this.considerAllPersonsInSumOfTolls = considerAllPersonsInSumOfTolls;
	}

	private String outputDir;
	private Map<Id, Double> personId2UserWelfare_utils;
	private Map<Id, Double> personId2MonetarizedUserWelfare;
	private Map<Id, Double> personId2MonetaryPayments;
	private SortedMap<UserGroup, Double>  userGrp2ExcludedToll;
	private Scenario scenario;
	private int lastIteration;
	private boolean considerAllPersonsInSumOfTolls;

	private final WelfareMeasure wm = WelfareMeasure.SELECTED;

	public static void main(String[] args) {
		String outputDir = "/Users/aagarwal/Desktop/ils4/agarwal/munich/output/1pct/";
		String [] runCases = { "baseCaseCtd","ei","ci","eci"};

		for(String runCase : runCases){
			UserBenefitsAndTotalWelfarePerUserGroup ubtwug = new  UserBenefitsAndTotalWelfarePerUserGroup(outputDir+runCase+"/",false);
			ubtwug.run();
		}
	}

	private void run(){

		storeUserBenefitsMaps();
		getPersonId2MonetaryPayment();

		SortedMap<UserGroup, Double> userGroupToUserWelfare_utils = getParametersPerUserGroup(this.personId2UserWelfare_utils);
		SortedMap<UserGroup, Double> userGroupToUserWelfare_money = getParametersPerUserGroup(this.personId2MonetarizedUserWelfare);
		SortedMap<UserGroup, Double> userGroupToTotalPayment = getTollsPerUserGroup(this.personId2MonetaryPayments);

		String outputFile = this.outputDir+"/analysis/userGrpWelfareAndTollPayments.txt";
		BufferedWriter writer = IOUtils.getBufferedWriter(outputFile);
		
		double sumUtils =0;
		double sumUtils_money =0;
		double sumToll =0;
		double excludeTollSum =0;
		
		try {
			writer.write("UserGroup \t userWelfareUtils \t userWelfareMoney \t tollPayments \t excludedTollIfAny \n");
			for(UserGroup ug : userGroupToTotalPayment.keySet()){
				writer.write(ug+"\t"+userGroupToUserWelfare_utils.get(ug)+"\t"
						+userGroupToUserWelfare_money.get(ug)+"\t"
						+userGroupToTotalPayment.get(ug)+"\t"
						+userGrp2ExcludedToll.get(ug)+"\n");
				sumUtils += userGroupToUserWelfare_utils.get(ug);
				sumUtils_money += userGroupToUserWelfare_money.get(ug);
				sumToll += userGroupToTotalPayment.get(ug);
				excludeTollSum += userGrp2ExcludedToll.get(ug);
			}
			writer.write("total \t"+sumUtils+"\t"+sumUtils_money+"\n"+sumToll+"\t"+excludeTollSum+"\n");
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException("Data is not written into a File. Reason : "+e);
		}
		logger.info("Finished Writing data to file "+outputFile);		
	}

	private SortedMap<UserGroup, Double> getParametersPerUserGroup(Map<Id, Double> inputMap) { 
		SortedMap<UserGroup, Double> outMap = new TreeMap<UserGroup, Double>();
		for(UserGroup ug : UserGroup.values()){
			outMap.put(ug, 0.0);
		}

		for(Id id:inputMap.keySet()){
			UserGroup ug = getUserGrpFromPersonId(id);
			double valueSoFar = outMap.get(ug);
			double value2add = inputMap.get(id) ;
			double newValue = value2add+valueSoFar;
			outMap.put(ug, newValue);
		}
		return outMap;
	}

	private SortedMap<UserGroup, Double> getTollsPerUserGroup(Map<Id, Double> inputMap) { 
		SortedMap<UserGroup, Double> outMap = new TreeMap<UserGroup, Double>();
		for(UserGroup ug : UserGroup.values()){
			outMap.put(ug, 0.0);
			userGrp2ExcludedToll.put(ug, 0.0);
		}

		for(Id id:inputMap.keySet()){
			UserGroup ug = getUserGrpFromPersonId(id);
			double valueSoFar = outMap.get(ug);
			double value2add = inputMap.get(id) ;
			if(!this.considerAllPersonsInSumOfTolls && isPersonIncluded(id)){

			} else {
				double excludeValue = userGrp2ExcludedToll.get(ug);
				userGrp2ExcludedToll.put(ug, excludeValue+value2add);
				value2add = 0;
			}
			double newValue = value2add+valueSoFar;
			outMap.put(ug, newValue);
		}
		return outMap;
	}

	private void storeUserBenefitsMaps(){
		logger.info("User welfare will be calculated using welfare measure as "+ wm.toString());

		MyUserBenefitsAnalyzer userBenefitsAnalyzer = new MyUserBenefitsAnalyzer();
		userBenefitsAnalyzer.init((ScenarioImpl)scenario, this.wm, false);
		userBenefitsAnalyzer.preProcessData();
		userBenefitsAnalyzer.postProcessData();

		userBenefitsAnalyzer.writeResults(this.outputDir+"/analysis/");

		this.personId2UserWelfare_utils = userBenefitsAnalyzer.getPersonId2UserWelfare_utils();
		this.personId2MonetarizedUserWelfare = userBenefitsAnalyzer.getPersonId2MonetarizedUserWelfare();
	}

	private void getPersonId2MonetaryPayment(){
		MonetaryPaymentsAnalyzer paymentsAnalyzer = new MonetaryPaymentsAnalyzer();
		paymentsAnalyzer.init((ScenarioImpl)scenario);
		paymentsAnalyzer.preProcessData();

		EventsManager events = EventsUtils.createEventsManager();
		List<EventHandler> handler = paymentsAnalyzer.getEventHandler();

		for(EventHandler eh : handler){
			events.addHandler(eh);
		}

		MatsimEventsReader reader = new MatsimEventsReader(events);
		reader.readFile(this.outputDir+"/ITERS/it."+this.lastIteration+"/"+this.lastIteration+".events.xml.gz");

		paymentsAnalyzer.postProcessData();
		paymentsAnalyzer.writeResults(this.outputDir+"/analysis/");
		this.personId2MonetaryPayments = paymentsAnalyzer.getPersonId2amount();
	}

	private UserGroup getUserGrpFromPersonId(Id personId){
		PersonFilter pf = new PersonFilter();
		UserGroup outUG = UserGroup.URBAN;
		for(UserGroup ug : UserGroup.values()){
			if(pf.isPersonIdFromUserGroup(personId, ug)) {
				outUG =ug;
				break;
			}
		}
		return outUG;
	}

	private boolean isPersonIncluded(Id personId){
		Id<Person> id = Id.createPersonId(personId.toString());
		double score = scenario.getPopulation().getPersons().get(id).getSelectedPlan().getScore();
		if (score < 0 ) return false;
		else return true;
	}
	
}
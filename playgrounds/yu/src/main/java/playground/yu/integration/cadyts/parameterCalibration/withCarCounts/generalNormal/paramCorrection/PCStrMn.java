///* *********************************************************************** *
// * project: org.matsim.*
// * StrMninclLT.java
// *                                                                         *
// * *********************************************************************** *
// *                                                                         *
// * copyright       : (C) 2008 by the members listed in the COPYING,        *
// *                   LICENSE and WARRANTY file.                            *
// * email           : info at matsim dot org                                *
// *                                                                         *
// * *********************************************************************** *
// *                                                                         *
// *   This program is free software; you can redistribute it and/or modify  *
// *   it under the terms of the GNU General Public License as published by  *
// *   the Free Software Foundation; either version 2 of the License, or     *
// *   (at your option) any later version.                                   *
// *   See also COPYING, LICENSE and WARRANTY file                           *
// *                                                                         *
// * *********************************************************************** */
//
//package playground.yu.integration.cadyts.parameterCalibration.withCarCounts.generalNormal.paramCorrection;
//
//import java.util.Collections;
//
//import org.matsim.api.core.v01.network.Link;
//import org.matsim.api.core.v01.network.Network;
//import org.matsim.api.core.v01.population.Person;
//import org.matsim.api.core.v01.population.Plan;
//import org.matsim.api.core.v01.population.Population;
//import org.matsim.core.config.Config;
//import org.matsim.core.gbl.Gbl;
//import org.matsim.core.gbl.MatsimRandom;
//import org.matsim.core.population.PersonImpl;
//import org.matsim.core.population.PlanImpl;
//import org.matsim.core.replanning.PlanStrategy;
//import org.matsim.core.router.util.TravelTime;
//
//import playground.yu.integration.cadyts.parameterCalibration.withCarCounts.BseStrategyManager;
//import playground.yu.integration.cadyts.parameterCalibration.withCarCounts.PlanToPlanStep;
//import playground.yu.integration.cadyts.parameterCalibration.withCarCounts.generalNormal.scoring.Events2Score4PC;
//import playground.yu.integration.cadyts.parameterCalibration.withCarCounts.generalNormal.scoring.MultinomialLogitCreator;
//import playground.yu.integration.cadyts.parameterCalibration.withCarCounts.mnlValidation.MultinomialLogitChoice;
//import playground.yu.integration.cadyts.parameterCalibration.withCarCounts.parametersCorrection.BseParamCalibrationStrategyManager;
//import utilities.math.BasicStatistics;
//import utilities.math.MultinomialLogit;
//import cadyts.interfaces.matsim.MATSimChoiceParameterCalibrator;
//
//public class PCStrMn extends BseParamCalibrationStrategyManager implements
//		BseStrategyManager {
//	// private final static Logger log = Logger.getLogger(StrMninclLT.class);
//	// private double delta;
//	private final Config config;
//	private Plan oldSelected = null;
//	private MultinomialLogit singleMnl = null;
//
//	private BasicStatistics betaTravelingPtStats = null;
//
//	public PCStrMn(Network net, int firstIteration, Config config
//	// ,int paramDimension
//	) {
//		super(firstIteration);
//		this.net = net;
//		this.config = config;
//		// this.paramDimension = paramDimension;
//	}
//
//	public void init(MATSimChoiceParameterCalibrator<Link> calibrator,
//			TravelTime travelTimeCalculator, MultinomialLogitChoice chooser
//	// ,double delta
//	) {
//		// init(calibrator, travelTimeCalculator);
//		this.calibrator = calibrator;
//		planConverter = new PlanToPlanStep(travelTimeCalculator, net);
//		tt = travelTimeCalculator;
//		// this.worstPlanSelector = new RemoveWorstPlanSelector();
//		// /////////////////////////////////////////////////////////////////////
//		this.chooser = chooser;
//		// this.delta = delta;
//	}
//
//	@Override
//	protected void beforePopulationRunHook(Population population) {
//		// the most things before "removePlans"
//		super.beforePopulationRunHook(population);// iter++
//		// cadyts class - create new BasicStatistics Objects
//		betaTravelingPtStats = new BasicStatistics();
//
//		((Events2Score4PC) chooser).createWriter();
//
//		for (Person person : population.getPersons().values()) {
//			// now there could be #maxPlansPerAgent+?# Plans in choice set
//			// *********************UTILITY CORRECTION********************
//			// ***before removePlans and plan choice, correct utility***
//			generateScoreCorrections(person);
//
//			/* ***********************************************************
//			 * scoringCfg has been done, but they should be newly defined
//			 * because of new calibrated parameters and -- WITHOUT
//			 * utilityCorrections--
//			 * *******************************************************
//			 */
//			chooser.setPersonScore(person);
//		}
//
//		((Events2Score4PC) chooser).closeWriter();
//	}
//
//	@Override
//	protected void afterRemovePlanHook(Plan plan) {
//		super.afterRemovePlanHook(plan);
//		// something could be done here.
//	}
//
//	@Override
//	protected void beforeStrategyRunHook(Person person, PlanStrategy strategy) {
//		// choose reset because of removeWorstPlan
//		resetChooser();
//		// ******************************************************
//		super.beforeStrategyRunHook(person, strategy);
//
//		if (strategy != null) {
//			int maxPlansPerAgent = getMaxPlansPerAgent();
//			if (iter - firstIter > maxPlansPerAgent) {
//				// ENSURE THAT EVERY PLAN IN CHOICE SET HAS BEEN SIMULATED
//				// ATLEAST ONE TIME
//				oldSelected = person.getSelectedPlan();
//
//				if (strategy.getNumberOfStrategyModules() <= 0) {
//					// only with planSelector/-Changer, no new plan will be
//					// created
//					// **************WRITE ATTR.S INTO MNL******************
//					chooser.setPersonAttrs(person,
//							new BasicStatistics[] { betaTravelingPtStats });
//					// now there are only #maxPlansPerAgent# Plans in choice set
//
//					/* ***********************************************************
//					 * set the last chosen plan to cadyts, only works with
//					 * {@code cadyts.interfaces.matsim.ExpBetaPlanChanger},it's
//					 * not to be done with MNL, but "ChangeExpBeta" as well as
//					 * "SelectExpBeta" can still be written in configfile
//					 */
//				} else {// with planInnovation
//					singleMnl = new MultinomialLogitCreator()
//							.createSingle(config);
//					((Events2Score4PC) chooser).setSinglePlanAttrs(oldSelected,
//							singleMnl);
//				}
//			} else {// ***********iter-firstIter<=maxPlanPerAgent************
//				if (iter - firstIter == 1) {
//					/*
//					 * shuffle the Plan Choice Set, to avoid chaotic network
//					 * situation
//					 */
//					Collections.shuffle(person.getPlans(),
//							MatsimRandom.getRandom()/*
//													 * Random-Objekt is not
//													 * generated by every
//													 * calling of shuffle(List)
//													 */);
//				}
//				// ENSURE THAT EVERY PLANS IN CHOICE SET WILL BE EXECUTED ONE
//				// TIME
//				if (iter % maxPlansPerAgent < person.getPlans().size()) {
//					((PersonImpl) person).setSelectedPlan(person.getPlans()
//							.get(iter % maxPlansPerAgent));
//					// ****************************************************
//				}
//			}
//		} else { // strategy==null
//			Gbl.errorMsg("No strategy found!");
//		}
//	}
//
//	@Override
//	protected void afterStrategyRunHook(Person person, PlanStrategy strategy) {
//		super.afterStrategyRunHook(person, strategy);
//		if (strategy != null) {
//			if (iter - firstIter > getMaxPlansPerAgent()) {
//				// ENSURE THAT EVERY PLAN IN CHOICE SET HAS BEEN SIMULATED
//				// ATLEAST ONE TIME
//				if (strategy.getNumberOfStrategyModules() > 0) {
//					/*
//					 * New plan has been created by e.g. ReRoute,
//					 * TimeAllocationMutator etc. Only the old score of last
//					 * selected Plan will set to the new created Plan.
//					 */
//					Plan selectedPlan = person.getSelectedPlan();
//					selectedPlan.setScore(oldSelected.getScore());
//					oldSelected = null;
//
//					/*
//					 * Vector p = new Vector(1/* (single-)choiceSetSize /);
//					 * p.set(0, 1d/* 100% /);
//					 *
//					 * Matrix d = new Matrix(1/* n-choiceSetSize /,
//					 * paramDimension // m-size of parameters that has to be
//					 * calibrated ); for (int i = 0; i < paramDimension; i++) {
//					 * d.setColumn(i, new Vector(0d)); }
//					 */
//					// ******************************************************
//
//					calibrator.selectPlan(0,
//							getSinglePlanChoiceSet(selectedPlan), singleMnl
//
//					/*
//					 * FUNCTIONS FOR CALIBRATOR 1,2 p, d, null FUNCTIONS FOR
//					 * CALIBRATOR 1,2
//					 */
//					);
//					singleMnl = null;
//
//					// **********************************************************
//				} else {// Change-/SelectExpBeta has been done.
//					int selectIdx = person.getPlans().indexOf(
//							person.getSelectedPlan());
//					// ********************************************************
//					MultinomialLogit mnl = ((MultinomialLogitChoice) chooser)
//							.getMultinomialLogit();
//
//					/*
//					 * FUNCTIONS FOR CALIBRATOR 1,2 Vector probs =
//					 * mnl.getProbs();
//					 *
//					 * if (Double.isNaN(probs.sum())) {
//					 * log.fatal("mnl/probs/NaN"); System.out
//					 * .println("selecteIdx from ChangeExpBeta (MATSim)\t" +
//					 * selectIdx + "\nprobs\n" + probs + "\nperson\t" +
//					 * person.getId() + "\nplans:"); List<? extends Plan> plans
//					 * = person.getPlans(); for (int i = 0; i < plans.size();
//					 * i++) { System.out.print(i + ". plan with score\t" +
//					 * plans.get(i).getScore()); if (plans.get(i).isSelected())
//					 * { System.out.println("\tselected"); } else {
//					 * System.out.println(); } } }
//					 *
//					 * List<Integer> attrIndices = new ArrayList<Integer>(); for
//					 * (String paramName : CtlListenerinclLT.paramNames) {
//					 * attrIndices.add(Events2Score4PC_mnl.attrNameList
//					 * .indexOf(paramName)); }
//					 *
//					 * Matrix dProb_dParameters = mnl.get_dProb_dParameters(
//					 * attrIndices, false// with/out ASC /);
//					 *
//					 * List<? extends Matrix> d2ChoiceProb_dParam2 = mnl
//					 * .get_d2P_dbdb(delta, attrIndices, false); FUNCTIONS FOR
//					 * CALIBRATOR 1,2
//					 */
//
//					/* UPDATE PARAMETERS (OBSERVE THE PLAN CHOOSING IN MATSIM) */
//
//					/* int selectedIdx= */calibrator.selectPlan(selectIdx,
//							getPlanChoiceSet((PersonImpl) person), mnl
//					/*
//					 * FUNCTIONS FOR CALIBRATOR 1,2 probs, dProb_dParameters,
//					 * d2ChoiceProb_dParam2 FUNCTIONS FOR CALIBRATOR 1,2
//					 */
//					);
//					// ***************************************************
//				}
//			}
//		} else {// strategy==null
//			Gbl.errorMsg("No strategy found!");
//		}
//	}
//
//	@Override
//	protected void afterRunHook(Population population) {
//		super.afterRunHook(population);
//		// output stats and variabilities
//		statistics = new double[] { betaTravelingPtStats.getAvg(),
//				betaTravelingPtStats.getVar() };
//		System.out.println("BSE_Statistics\tavg.\t" + statistics[0]/*
//																	 * betaTravelingPtAttr
//																	 * .
//																	 */
//				+ "\tvar.\t" + statistics[1]/* betaTravelingPtAttr. */);
//	}
//
//	private void generateScoreCorrections(Person person) {
//		for (Plan plan : person.getPlans()) {
//			generateScoreCorrection(plan);
//		}
//	}
//
//	private void generateScoreCorrection(Plan plan) {
//		planConverter.convert((PlanImpl) plan);
//		cadyts.demand.Plan<Link> planSteps = planConverter.getPlanSteps();
//		double scoreCorrection = calibrator.getUtilityCorrection(planSteps)
//				/ config.planCalcScore().getBrainExpBeta();
//		// #######SAVE "utilityCorrection" 4 MNL.ASC#########
//		plan.getCustomAttributes().put(UTILITY_CORRECTION, scoreCorrection);
//		// ##################################################
//		// Double oldScore = plan.getScore();
//		// if (oldScore == null) {
//		// oldScore = 0d;// dummy setting, the score of plans will be
//		// // calculated between firstIter+1 and firstIter+
//		// }
//		// plan.setScore(oldScore + scoreCorrection);// this line is NOT
//		// necessary
//		// // any more
//	}
//}

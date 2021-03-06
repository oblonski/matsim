package org.matsim.core.scenario;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.households.Households;
import org.matsim.lanes.data.v20.LaneDefinitions20;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.vehicles.Vehicles;


/**
 * Provides ways to get a Scenario from the implementation in this package.
 *
 * @author michaz
 *
 */
public class ScenarioUtils {

	private ScenarioUtils() {
		// make it private, so it cannot be instantiated
	}

	/**
	 *
	 * Creates an unpopulated scenario. The configuration passed into this method is
	 * a) used to determine which containers are required, depending on the options set in the scenario config group, and
	 * b) wrapped in the Scenario
	 *
	 * User code surrenders the config to the scenario. The config should not be externally changed afterwards.
	 *
	 * @param config A {@link Config} object, must not be <code>null</code>
	 *
	 * @see org.matsim.core.config.ConfigUtils#createConfig()
	 */
	public static Scenario createScenario(final Config config) {
		if (config == null) {
			throw new NullPointerException("config must not be null!");
		}
		return new ScenarioImpl(config);
	}

	/**
	 *
	 * Initializes a scenario and populates it with data read from the input files which are named in the config.
	 *
	 */
	public static Scenario loadScenario(final Config config) {
		return ScenarioLoaderImpl.loadScenario(config);
	}

	/**
	 *
	 * Populates a scenario with data read from the input files which are named in the config which is wrapped
	 * in the scenario.
	 *
	 */
	public static void loadScenario(final Scenario scenario) {
		ScenarioLoaderImpl.loadScenario(scenario);
	}
	
	public final static class ScenarioBuilder {
		private ScenarioImpl scenario;
		public ScenarioBuilder( Config config ) {
			this.scenario = new ScenarioImpl( config ) ;
		}
		public ScenarioBuilder addScenarioElement(String name, Object o) {
			scenario.addScenarioElement(name, o); 
			return this ;
		}
		// households:
		/**
		 * This is here if you want to instantiate the empty container
		 */
		public ScenarioBuilder createHouseholdsContainer() { 
			scenario.createHouseholdsContainer() ;
			return this ;
		}
		public ScenarioBuilder setHouseholds( Households households ) {
			scenario.setHouseholds(households);
			return this ;
		}
		// transit schedule:
		/**
		 * This is here if you want to instantiate the empty container
		 */
		public ScenarioBuilder createTransitSchedule() {
			scenario.createTransitScheduleContainer() ;
			return this ;
		}
		public ScenarioBuilder setTransitSchedule( TransitSchedule schedule ) {
			scenario.setTransitSchedule(schedule);
			return this ;
		}
		// vehicles:
		/**
		 * This is here if you want to instantiate the empty container
		 */
		public ScenarioBuilder createVehiclesContainer() {
			scenario.createTransitVehicleContainer() ;
			return this ;
		}
		public ScenarioBuilder setVehicles( Vehicles vehicles ) {
			scenario.setTransitVehicles(vehicles);
			return this;
		}
		// network (always there):
		public ScenarioBuilder setNetwork( Network network ) {
			scenario.setNetwork(network);
			return this ;
		}
		// population (always there):
		public ScenarioBuilder setPopulation( Population population ) {
			scenario.setPopulation(population);
			return this ;
		}
		// facilities (always there, although that does not make a lot of sense):
		public ScenarioBuilder setActivityFacilities( ActivityFacilities facilities ) {
			scenario.setActivityFacilities(facilities);
			return this ;
		}
		// lanes:
		public ScenarioBuilder setLanes( LaneDefinitions20 lanes ) {
			scenario.setLanes(lanes);
			return this ;
		}
		// final creational method:
		public Scenario createScenario() {
			this.scenario.setLocked(); // prevents that one can cast to ScenarioImpl and change the containers again. kai, nov'14
			return this.scenario ;
		}
	}

}

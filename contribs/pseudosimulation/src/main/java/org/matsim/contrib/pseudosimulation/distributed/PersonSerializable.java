
package org.matsim.contrib.pseudosimulation.distributed;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.population.PersonImpl;
import org.matsim.population.Desires;

public class PersonSerializable implements Serializable {
    protected List<PlanSerializable> plans = new ArrayList<>(5);

    public PersonSerializable(Person p) {
        this.id = p.getId().toString();
        PersonImpl person = (PersonImpl) p;
        this.sex = person.getSex();
        this.age = person.getAge();
        this.hasLicense = person.getLicense();
        this.carAvail = person.getCarAvail();
        for (Plan plan : person.getPlans()) {
            PlanSerializable planSerializable = new PlanSerializable(plan);
            plans.add(planSerializable);
            if (plan.equals(person.getSelectedPlan()))
                this.selectedPlan = planSerializable;
        }
    }

    protected String id;
    private String sex;
    private int age = Integer.MIN_VALUE;
    private String hasLicense;
    private String carAvail;
    PlanSerializable selectedPlan = null;
    private TreeSet<String> travelcards = null;
    protected Desires desires = null;

    private Boolean isEmployed;

    public Person getPerson() {
        PersonImpl person = new PersonImpl(Id.createPersonId(id));
        person.setAge(age);
        person.setCarAvail(carAvail);

        person.setEmployed(isEmployed);
        person.setLicence(hasLicense);
        person.setSex(sex);
        for (PlanSerializable planSer : plans) {
            Plan plan = planSer.getPlan(person);
            person.addPlan(plan);
            if(planSer.equals(selectedPlan))
                person.setSelectedPlan(plan);

        }
        return person;
    }
}

package playground.gregor.sims.shelters.linkpenalty;

import java.util.HashMap;
import java.util.SortedMap;
import java.util.Map.Entry;

import org.matsim.api.basic.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.listener.BeforeMobsimListener;
import org.matsim.core.events.AgentMoneyEventImpl;
import org.matsim.core.events.LinkLeaveEventImpl;
import org.matsim.core.events.handler.LinkLeaveEventHandler;
import org.matsim.core.mobsim.queuesim.QueueSimulation;
import org.matsim.core.network.LinkImpl;
import org.matsim.core.network.NetworkLayer;
import org.matsim.evacuation.base.Building;
import org.matsim.signalsystems.control.SignalSystemController;

import playground.gregor.collections.gnuclasspath.TreeMap;

public class ShelterInputCounter implements LinkLeaveEventHandler, BeforeMobsimListener {

	private SortedMap<Id, SignalSystemController> scs;
	private final HashMap<Id,Counter> counts = new HashMap<Id, Counter>();
	private final HashMap<Id, Building> shelterLinkMapping;
	private final HashMap<LinkImpl,LinkInfo> linkInfos = new HashMap<LinkImpl, LinkInfo>();
	
	public ShelterInputCounter(NetworkLayer network, HashMap<Id,Building> shelterLinkMapping) {
		
		this.shelterLinkMapping = shelterLinkMapping;
		
		for (LinkImpl link : network.getLinks().values()) {
			if (link.getId().toString().contains("sl") && link.getId().toString().contains("a")) {
				this.counts.put(link.getId(), new Counter());
			}
		}
	}

	public void handleEvent(LinkLeaveEventImpl event) {
		Counter c = this.counts.get(event.getLink().getId()); 
		if (c != null) {
			c.count++;
			Building b = this.shelterLinkMapping.get(event.getLink().getId());
			c.count++;
			if (c.count > b.getShelterSpace()) {
//				AgentMoneyEvent e = new AgentMoneyEvent(event.getTime(),event.getPersonId(),-(c.count - b.getShelterSpace())/10.);
				AgentMoneyEventImpl e = new AgentMoneyEventImpl(event.getTime(),event.getPersonId(),-12*6);
				QueueSimulation.getEvents().processEvent(e);
				LinkInfo li = this.linkInfos.get(event.getLink());
				if (li == null) {
					li = new LinkInfo();
					this.linkInfos.put(event.getLink(), li);
					li.penalties.put(0.0, new LinkPenalty());
				}
				
				LinkPenalty p = new LinkPenalty();
				li.penalties.put(event.getTime(), p);
				p.penalty = 12*3600;
				
			}
		}
	}
	

	public void reset(int iteration) {
		int c = 0;
		double o = 0;
		for (Entry<Id, Counter> e : this.counts.entrySet()) {
			int count = e.getValue().count;
			int max = this.shelterLinkMapping.get(e.getKey()).getShelterSpace();
			double overload = (double)count / max;
			System.out.println("Link:" + e.getKey() + "  count:" + count + " should be at most:" + max + " overload:" + overload);
			e.getValue().count = 0;
			c++;
			o+=overload;
		}
		System.err.println("avgOverload:" + o/c);
	}



	private static class Counter {
		int count = 0;
	}

	public void notifyBeforeMobsim(BeforeMobsimEvent event) {
		this.linkInfos.clear();
		
	}

	public double getLinkPenalty(Link link, double time) {
		LinkInfo li = this.linkInfos.get(link);
		if (li == null) {
			return 0.;
		}
		Entry<Double,LinkPenalty> e = li.penalties.floorEntry(time);
		return e.getValue().penalty;
	}
	private static class LinkInfo {
		TreeMap<Double,LinkPenalty> penalties = new TreeMap<Double, LinkPenalty>(); 
	}

	private static class LinkPenalty {
		double penalty = 0;
	}
}

package org.matsim.contrib.josm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.josm.OsmConvertDefaults.OsmHighwayDefaults;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.network.LinkImpl;
import org.matsim.core.network.NodeImpl;
import org.matsim.core.utils.geometry.CoordImpl;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.osm.WaySegment;

class NewConverter {
	private final static Logger log = Logger.getLogger(NewConverter.class);

	private final static String TAG_LANES = "lanes";
	private final static String TAG_HIGHWAY = "highway";
	private final static String TAG_MAXSPEED = "maxspeed";
	private final static String TAG_JUNCTION = "junction";
	private final static String TAG_ONEWAY = "oneway";

	private static final List<String> TRANSPORT_MODES = Arrays.asList(
			TransportMode.bike, TransportMode.car, TransportMode.other,
			TransportMode.pt, TransportMode.ride, TransportMode.transit_walk,
			TransportMode.walk);

	static Map<String, OsmHighwayDefaults> highwayDefaults;

	public static void convertOsmLayer(DataSet dataSet, Network network,
			Map<Way, List<Link>> way2Links,
			Map<Link, List<WaySegment>> link2Segments) {
		log.info("=== Starting conversion of Osm data ===");
		if (!dataSet.getWays().isEmpty()) {
			for (Way way : dataSet.getWays()) {
				if (!way.isDeleted()) {
					convertWay(way, network, way2Links, link2Segments);
				}
			}
		}
		log.info("=== End of Conversion. #Links: " + network.getLinks().size()
				+ " | #Nodes: " + network.getNodes().size() + " ===");
	}

	public static void convertWay(Way way, Network network,
			Map<Way, List<Link>> way2Links,
			Map<Link, List<WaySegment>> link2Segments) {
		log.setLevel(Level.INFO);
		log.info("### Way " + way.getUniqueId() + " (" + way.getNodesCount()
				+ " nodes) ###");
		List<Link> links = new ArrayList<Link>();
		highwayDefaults = OsmConvertDefaults.getDefaults();
		if (way.getNodesCount() > 1) {
			if (way.hasKey(TAG_HIGHWAY) || meetsMatsimReq(way.getKeys())) {
				if (highwayDefaults.containsKey(way.getKeys().get(TAG_HIGHWAY))
						|| meetsMatsimReq(way.getKeys())) {
					List<Node> nodeOrder = new ArrayList<Node>();
					StringBuilder nodeOrderLog = new StringBuilder();
					for (int l = 0; l < way.getNodesCount(); l++) {
						Node current = way.getNode(l);
						if (l == 0 || l == way.getNodesCount() - 1) {
							nodeOrder.add(current);
							log.debug("--- Way " + way.getUniqueId()
									+ ": dumped node " + l + " ("
									+ current.getUniqueId()
									+ ") first/last node ");
							nodeOrderLog.append("(" + l + ") ");
						} else if (current.equals(way.getNode(way
								.getNodesCount() - 1))) {
							nodeOrder.add(current); // add node twice so length
													// to this node is not
													// calculated wrong
							log.debug("--- Way " + way.getUniqueId()
									+ ": dumped node " + l + " ("
									+ current.getUniqueId()
									+ ") beginning of loop / closed area ");
							nodeOrderLog.append("(" + l + ") ");
						} else if (current.isConnectionNode()) {
							for (OsmPrimitive prim : current.getReferrers()) {
								if (prim instanceof Way && !prim.equals(way)) {
									if (((Way) prim).hasKey(TAG_HIGHWAY)
											|| meetsMatsimReq(prim.getKeys())) {
										nodeOrder.add(current);
										log.debug("--- Way "
												+ way.getUniqueId()
												+ ": dumped node " + l + " ("
												+ current.getUniqueId()
												+ ") way intersection");
										nodeOrderLog.append("(" + l + ") ");
										break;
									}
								}
							}
						}
					}

					log.debug("--- Way " + way.getUniqueId()
							+ ": order of kept nodes [ "
							+ nodeOrderLog.toString() + "]");

					for (Node node : nodeOrder) {
						Id nodeId = new IdImpl(node.getUniqueId());
						if (!network.getNodes().containsKey(nodeId)) {
							double lat = node.getCoor().lat();
							double lon = node.getCoor().lon();
							org.matsim.api.core.v01.network.Node nn = network
									.getFactory().createNode(
											new IdImpl(node.getUniqueId()),
											new CoordImpl(lon, lat));
							if (node.hasKey(ImportTask.NODE_TAG_ID)) {
								((NodeImpl) nn).setOrigId(node
										.get(ImportTask.NODE_TAG_ID));
							} else {
								((NodeImpl) nn)
										.setOrigId(nn.getId().toString());
							}
							network.addNode(nn);
						} else {
							if (node.hasKey(ImportTask.NODE_TAG_ID)) {
								((NodeImpl) network.getNodes().get(nodeId))
										.setOrigId(node
												.get(ImportTask.NODE_TAG_ID));
							} else {
								((NodeImpl) network.getNodes().get(nodeId))
										.setOrigId(String.valueOf(node
												.getUniqueId()));
							}
							Coord coord = new CoordImpl(node.getCoor().getX(), node.getCoor().getY());
							((NodeImpl) network.getNodes().get(nodeId)).setCoord(coord);
						}

						log.debug("--- Way " + way.getUniqueId()
								+ ": created / updated MATSim node "
								+ node.getUniqueId());

					}

					Double length = 0.;
					Double capacity = 0.;
					Double freespeed = 0.;
					Double nofLanes = 0.;
					boolean oneway = true;
					Set<String> modes = new HashSet<String>();
					modes.add(TransportMode.car);
					boolean onewayReverse = false;
					String id = String.valueOf(way.getUniqueId());

					Map<String, String> keys = way.getKeys();
					if (keys.containsKey(TAG_HIGHWAY)) {
						String highway = keys.get(TAG_HIGHWAY);
						log.debug("--- Way " + way.getUniqueId()
								+ ": is highway tagged - " + highway);

						// load defaults
						OsmHighwayDefaults defaults = highwayDefaults
								.get(highway);
						if (defaults != null) {

							nofLanes = defaults.lanes;
							double laneCapacity = defaults.laneCapacity;
							freespeed = defaults.freespeed;
							oneway = defaults.oneway;

							// check if there are tags that overwrite defaults
							// - check tag "junction"
							if ("roundabout".equals(keys.get(TAG_JUNCTION))) {
								// if "junction" is not set in tags, get()
								// returns null and
								// equals()
								// evaluates to false
								log.debug("--- Way " + way.getUniqueId()
										+ ": junction tag set");
								oneway = true;
							}

							// check tag "oneway"
							String onewayTag = keys.get(TAG_ONEWAY);
							if (onewayTag != null) {
								log.debug("--- Way " + way.getUniqueId()
										+ ": oneway tag set");
								if ("yes".equals(onewayTag)) {
									oneway = true;
								} else if ("true".equals(onewayTag)) {
									oneway = true;
								} else if ("1".equals(onewayTag)) {
									oneway = true;
								} else if ("-1".equals(onewayTag)) {
									onewayReverse = true;
									oneway = false;
								} else if ("no".equals(onewayTag)) {
									oneway = false; // may be used to overwrite
													// defaults
								} else {
									log.warn("--- Way " + way.getUniqueId()
											+ ": could not parse oneway tag");
								}
							}

							// In case trunks, primary and secondary roads are
							// marked as
							// oneway,
							// the default number of lanes should be two instead
							// of one.
							if (highway.equalsIgnoreCase("trunk")
									|| highway.equalsIgnoreCase("primary")
									|| highway.equalsIgnoreCase("secondary")) {
								if (oneway && nofLanes == 1.0) {
									nofLanes = 2.0;
								}
							}

							String maxspeedTag = keys.get(TAG_MAXSPEED);
							if (maxspeedTag != null) {
								log.debug("--- Way " + way.getUniqueId()
										+ ": maxspeed tag set");
								try {
									freespeed = Double.parseDouble(maxspeedTag) / 3.6; // convert
									// km/h to
									// m/s
								} catch (NumberFormatException e) {
									log.warn("--- Way " + way.getUniqueId()
											+ ": could not parse maxspeed tag");
								}
							}

							// check tag "lanes"
							String lanesTag = keys.get(TAG_LANES);
							if (lanesTag != null) {
								log.debug("--- Way " + way.getUniqueId()
										+ ": lanes tag set");
								try {
									double tmp = Double.parseDouble(lanesTag);
									if (tmp > 0) {
										nofLanes = tmp;
									}
								} catch (Exception e) {
									log.warn("--- Way " + way.getUniqueId()
											+ ": could not parse lanes tag");
								}
							}
							// create the link(s)
							capacity = nofLanes * laneCapacity;
						}
					}
					if (keys.containsKey("capacity")) {
						log.debug("--- Way " + way.getUniqueId()
								+ ": MATSim capacity tag set");
						Double capacityTag = parseDoubleIfPossible(keys
								.get("capacity"));
						if (capacityTag != null) {
							capacity = capacityTag;
						} else {
							log.warn("--- Way " + way.getUniqueId()
									+ ": could not parse MATSim capacity tag");
						}
					}
					if (keys.containsKey("freespeed")) {
						log.debug("--- Way " + way.getUniqueId()
								+ ": MATSim freespeed tag set");
						Double freespeedTag = parseDoubleIfPossible(keys
								.get("freespeed"));
						if (freespeedTag != null) {
							freespeed = freespeedTag;
						} else {
							log.warn("--- Way " + way.getUniqueId()
									+ ": could not parse MATSim freespeed tag");
						}
					}
					if (keys.containsKey("permlanes")) {
						log.debug("--- Way " + way.getUniqueId()
								+ ": MATSim permlanes tag set");
						Double permlanesTag = parseDoubleIfPossible(keys
								.get("permlanes"));
						if (permlanesTag != null) {
							nofLanes = permlanesTag;
						} else {
							log.warn("--- Way " + way.getUniqueId()
									+ ": could not parse MATSim permlanes tag");
						}
					}
					if (keys.containsKey("modes")) {
						log.debug("--- Way " + way.getUniqueId()
								+ ": MATSim modes tag set");
						Set<String> tempModes = new HashSet<String>();
						String tempArray[] = keys.get("modes").split(";");
						for (String mode : tempArray) {
							if (TRANSPORT_MODES.contains(mode)) {
								tempModes.add(mode);
							}
						}
						if (tempModes.size() != 0) {
							modes.clear();
							modes.addAll(tempModes);
						} else {
							log.warn("--- Way " + way.getUniqueId()
									+ ": could not parse MATSim modes tag");
						}
					}
					if (keys.containsKey("length")) {
						log.debug("--- Way " + way.getUniqueId()
								+ ": MATSim length tag set");
						Double temp = parseDoubleIfPossible(keys.get("length"));
						if (temp != null) {
							length = temp;
						} else {
							log.warn("--- Way " + way.getUniqueId()
									+ ": could not parse MATSim length tag");
						}
					}
					if (keys.containsKey(ImportTask.WAY_TAG_ID)) {
						log.debug("--- Way " + way.getUniqueId()
								+ ": MATSim id tag set");
						id = keys.get(ImportTask.WAY_TAG_ID);
					}

					long increment = 0;
					for (int k = 1; k < nodeOrder.size(); k++) {
						List<WaySegment> segs = new ArrayList<WaySegment>();
						Node nodeFrom = nodeOrder.get(k - 1);
						Node nodeTo = nodeOrder.get(k);

						if (nodeFrom.equals(nodeTo)) { // skip uninteresting
														// loop
							log.warn("--- Way " + way.getUniqueId()
									+ ": contains loose loop / closed area.");
							break;
						}

						int fromIdx = way.getNodes().indexOf(nodeFrom);
						int toIdx = way.getNodes().indexOf(nodeTo);
						if (fromIdx > toIdx) { // loop, take latter occurence
							toIdx = way.getNodes().lastIndexOf(nodeTo);
						}

						length = 0.;
						for (int m = fromIdx; m < toIdx; m++) {
							segs.add(new WaySegment(way, m));
							length += way
									.getNode(m)
									.getCoor()
									.greatCircleDistance(
											way.getNode(m + 1).getCoor());
						}
						log.debug("--- Way " + way.getUniqueId()
								+ ": length between " + fromIdx + " and "
								+ toIdx + ": " + length);
						List<Link> tempLinks = createLink(network, way,
								nodeFrom, nodeTo, length, increment, oneway,
								onewayReverse, freespeed, capacity, nofLanes,
								modes, id);
						for (Link link : tempLinks) {
							link2Segments.put(link, segs);
						}
						links.addAll(tempLinks);
						increment++;
					}
				}
			}
		}
		log.debug("### Finished Way " + way.getUniqueId() + ". " + links.size()
				+ " links resulted. ###");
		way2Links.put(way, links);
	}

	private static boolean meetsMatsimReq(Map<String, String> keys) {
		if (!keys.containsKey("capacity"))
			return false;
		if (!keys.containsKey("freespeed"))
			return false;
		if (!keys.containsKey("permlanes"))
			return false;
		if (!keys.containsKey("modes"))
			return false;
		return true;
	}

	private static Double parseDoubleIfPossible(String string) {
		try {
			return Double.parseDouble(string);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private static List<Link> createLink(final Network network, final Way way,
			final Node fromNode, final Node toNode, double length,
			long increment, boolean oneway, boolean onewayReverse,
			Double freespeed, Double capacity, Double nofLanes,
			Set<String> modes, String id) {

		// only create link, if both nodes were found, node could be null, since
		// nodes outside a layer were dropped
		List<Link> links = new ArrayList<Link>();
		Id fromId = new IdImpl(fromNode.getUniqueId());
		Id toId = new IdImpl(toNode.getUniqueId());
		if (network.getNodes().get(fromId) != null
				&& network.getNodes().get(toId) != null) {
			String origId = id + "_" + (increment);
			if (!onewayReverse) {
				Link l = network.getFactory().createLink(
						new IdImpl(Long.toString(way.getUniqueId()) + "_"
								+ (increment)), network.getNodes().get(fromId),
						network.getNodes().get(toId));
				l.setLength(length);
				l.setFreespeed(freespeed);
				l.setCapacity(capacity);
				l.setNumberOfLanes(nofLanes);
				l.setAllowedModes(modes);
				if (l instanceof LinkImpl) {
					((LinkImpl) l).setOrigId(origId);
				}
				network.addLink(l);
				links.add(l);
				log.info("--- Way " + way.getUniqueId() + ": link "
						+ ((LinkImpl) l).getOrigId() + " created");
			}
			if (!oneway) {
				Link l = network.getFactory().createLink(
						new IdImpl(Long.toString(way.getUniqueId()) + "_"
								+ (increment) + "_r"),
						network.getNodes().get(toId),
						network.getNodes().get(fromId));
				l.setLength(length);
				l.setFreespeed(freespeed);
				l.setCapacity(capacity);
				l.setNumberOfLanes(nofLanes);
				l.setAllowedModes(modes);
				if (l instanceof LinkImpl) {
					((LinkImpl) l).setOrigId(origId + "_r");
				}
				network.addLink(l);
				links.add(l);
				log.info("--- Way " + way.getUniqueId() + ": link "
						+ ((LinkImpl) l).getOrigId() + " created");
			}
		}
		return links;
	}
}
package com.RouteMappingProject.RouteMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class Model {

    private final Map<String, Node> nodes = new HashMap<>();
    private final Map<String, Way> ways = new HashMap<>();
    private final Map<Integer, Road> roads = new HashMap<>();

    double minLat;
    double maxLat;
    double minLon;
    double maxLon;

    public Model(List<Byte> xml) {
        LoadData(xml);
    }

    public Map<String, Node> getNodes() {
        return nodes;
    }

    public static Road.Type StringToHighwayType(String type) {
        if ("cycleway".equals(type) || "primary_link".equals(type) ||
                "primary".equals(type))
            return Road.Type.Primary;
        else if ("residential".equals(type))
            return Road.Type.Residential;
        else if ("secondary".equals(type) ||
                "secondary_link".equals(type))
            return Road.Type.Secondary;
        else if ("service".equals(type))
            return Road.Type.Service;
        else if ("steps".equals(type) || "pedestrian".equals(type) ||
                "footway".equals(type) || "path".equals(type) ||
                "living_street".equals(type))
            return Road.Type.Footway;
        else if ("tertiary".equals(type) ||
                "tertiary_link".equals(type))
            return Road.Type.Tertiary;
        else if ("trunk".equals(type) || "trunk_link".equals(type))
            return Road.Type.Trunk;
        else if ("unclassified".equals(type))
            return Road.Type.Unclassified;
        else
            return Road.Type.Invalid;
    }

    public Map<Integer, Road> getRoads() {
        return roads;
    }
    public Map<String, Way> getWays() {
        return ways;
    }

    public void LoadData(List<Byte> xml) {
        try {
            // Convert list of bytes to a string
            StringBuilder xmlString = new StringBuilder();
            for (byte b : xml)
                xmlString.append((char) b);

            // Parse XML document using Jsoup
            Document doc = Jsoup.parse(xmlString.toString());

            // Parse bounds
            Element boundsElement = doc.selectFirst("osm > bounds");
            if (boundsElement != null) {
                this.minLat = Double.parseDouble(boundsElement.attr("minlat"));
                this.minLon = Double.parseDouble(boundsElement.attr("minlon"));
                this.maxLat = Double.parseDouble(boundsElement.attr("maxlat"));
                this.maxLon = Double.parseDouble(boundsElement.attr("maxlon"));
            } else {
                throw new IllegalStateException("Error in map bounds");
            }

            // Parse nodes
            Elements nodeElements = doc.select("osm > node");
            for (Element nodeElement : nodeElements) {
                String id = nodeElement.attr("id");
                double lat = Double.parseDouble(nodeElement.attr("lat"));
                double lon = Double.parseDouble(nodeElement.attr("lon"));
                nodes.put(id, new Node(lon, lat, id));
            }

            // Parse ways
            Elements wayElements = doc.select("osm > way");

            for (Element way : wayElements) {
                // Save id as string
                String id = way.attr("id");
                // Get the last current int as number in the map
                double wayNum = ways.size();
                try{
                    Way newWay = new Way(id);
                    // Add the new way in Ways map
                    ways.put(id, newWay);

                    // Iterate through children nodes
                    for (Element child : way.children()) {
                        // Get "nd" or "tag" name
                        String name = child.tagName();
                        if (name.equals("nd")) {
                            // Get ref numbers
                            String ref = child.attr("ref");
                            // Place them at the back of nodes vector
                            newWay.nodes.add(ref);
                        } else if (name.equals("tag")) {
                            String category = child.attr("k");
                            String type = child.attr("v");

                            // Set Direction
                            if(category.equals("oneway") && type.equals("yes")){
                                newWay.setDirection(true);
                            }

                            //Set road type
                            if (category.equals("highway")) {
                                Road.Type road_type = StringToHighwayType(type);
                                if (road_type != Road.Type.Invalid) {
                                    // Create new road and add it in Roads
                                    Road new_road = new Road((int) wayNum, road_type, id);
                                    roads.put((int) wayNum, new_road);
                                }
                            }
                        }
                    }
                } catch (OutOfMemoryError e) {
                    System.err.println("Out of memory error: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // check if a given point is inside map's bounds
    public boolean isInBounds(double lat, double lon){
        if(lat >= this.minLat && lat <= this.maxLat){
            return lon >= this.minLon && lon <= this.maxLon;
        }
        return false;
    }
}

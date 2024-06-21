package com.RouteMappingProject.RouteMapping;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AlgorithmicRunner {
    List<String> finalUrls = new ArrayList<>();
    public static Optional<byte[]> readFile(String filePath) {
        try {
            Path path = Path.of(filePath);
            return Optional.of(Files.readAllBytes(path));
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return Optional.empty();
        }
    }

    public AlgorithmicRunner (){}

    public void runAlgorithm(List<Location> agents, Integer maxCapacity, List<Location> packages) throws IOException {

        // File name here
        String osmDataFile = "volos.osm";
        FileReader fileReader = new FileReader();
        Optional<byte[]> osmDataOptional = fileReader.readFileFromResources(osmDataFile);
        if (osmDataOptional.isEmpty()) {
            System.out.println("Failed to read!");
            return;
        }

        byte[] osmData = osmDataOptional.get();
        System.out.println("Reading OpenStreetMap data from the following file: " + osmDataFile);
        System.out.println("Read osm_data");

        // Convert byte array to list of bytes
        List<Byte> osmDataList = new ArrayList<>();
        for (byte b : osmData) {
            osmDataList.add(b);
        }
        // Instantiate Model
        RouteModel model = new RouteModel(osmDataList);

        // ################## READ AGENTS #######################################
        int numAgents = agents.size();

        List<KdTree.XYZPoint> sources = new ArrayList<>();
        List<KdTree.XYZPoint> destinations = new ArrayList<>();
        List<KdTree.XYZPoint> agentsPositions = new ArrayList<>();

        // initialize agentDistribution class
        AgentDistribution agentDistr = new AgentDistribution(numAgents, maxCapacity);

        // Add initial positions of all the available agents
        for(Location agentLoc : agents){

            float initLat = Float.parseFloat(agentLoc.getLat());
            float initLon = Float.parseFloat(agentLoc.getLng());
            if(model.isInBounds(initLat, initLon)) {
                KdTree.XYZPoint initPosition = new KdTree.XYZPoint(initLon, initLat);
                agentsPositions.add(new KdTree.XYZPoint(initLon, initLat));

                agentDistr.addAgentsPosition(initPosition);
            }
            else{
                System.out.println("Agent's location " + initLat + "," + initLon + " is out of bounds!");
                return;
            }

        }

        // READ SOURCE AND DESTINATION POINTS

        // Check we have an even number of packages (all are pairs src-dest)
        if(packages.size() % 2 != 0){
            System.out.println("All packages must have Soure and Destination! ");
            return;
        }

        for(int i = 0; i < packages.size(); i++){
            // ##### SOURCE ####
            float startLat = Float.parseFloat(packages.get(i).getLat());
            float startLon = Float.parseFloat(packages.get(i).getLng());
            i++;

            if(!model.isInBounds(startLat, startLon)){
                System.out.println("Coordinates " + startLat + "," + startLon + " is out of bounds!");
                return;
            }
            // init the new point
            KdTree.XYZPoint startPoint = new KdTree.XYZPoint(startLon, startLat);

            // ##### DESTINATION ####
            float endLat = Float.parseFloat(packages.get(i).getLat());
            float endLon = Float.parseFloat(packages.get(i).getLng());
            if(!model.isInBounds(endLat, endLon)){
                System.out.println("Coordinates " + endLat + "," + endLon + " is out of bounds!");
                return;
            }
            // init the new point
            KdTree.XYZPoint endPoint = new KdTree.XYZPoint(endLon, endLat);

            // add every new pkg to the tree and assign it to an agent
            agentDistr.assignNewPackage(startPoint, endPoint);
        }

        agentDistr.displayAgents();

        // Apply routePlanner for each one of the agents
        for(List<KdTree.XYZPoint> agentList: agentDistr.getAgents()){

            // get agent's initial position and add it as the first source
            int agentIdx = agentDistr.getAgents().indexOf(agentList);
            KdTree.XYZPoint agentPos = agentsPositions.get(agentIdx);
            sources.add(agentPos);
            // add agents position as the first destination
            destinations.add(agentList.get(0));

            // Split the points in source and destination points. Every pkg has the form of source, destination.
            // for i=0 we have the initial position of the agent, so we skip it
            for(int i=1; i < agentList.size(); i++){
                if(i % 2 == 1)
                    sources.add(new KdTree.XYZPoint(agentList.get(i).x, agentList.get(i).y));
                else
                    destinations.add(new KdTree.XYZPoint(agentList.get(i).x, agentList.get(i).y));
            }


            // Apply routePlanner in each agent's list separately
            RoutePlanning routePlanner = new RoutePlanning(model, sources, destinations, agentPos);
            finalUrls.add(routePlanner.map.finalUrl);
            sources.clear();
            destinations.clear();
        }
    }
}


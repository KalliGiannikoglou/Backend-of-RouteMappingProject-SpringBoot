package com.RouteMappingProject.RouteMapping;

import java.util.*;

public class AgentDistribution {
    int numOfAgents;
    int totalPkgs;
    int loadFactor;
    int MAX_CAPACITY;

    //KdTree with all the existing packages
    KdTree<KdTree.XYZPoint> packages = new KdTree<>();
    // Sorted map where every point is mapped with all the agents that have this location in their list
    SortedMap<KdTree.XYZPoint, List<Integer>> assignedPkgs = new TreeMap<>();
    // List with the lists of all agents packages
    List<List<KdTree.XYZPoint>> agents = new ArrayList<>();

    public AgentDistribution(int nAgents, int max_capacity){
        this.numOfAgents = nAgents;
        this.totalPkgs = 0;
        this.loadFactor = 0;
        MAX_CAPACITY = max_capacity;
    }

    public List<List<KdTree.XYZPoint>> getAgents(){ return agents; }

    public void assignNewPackage(KdTree.XYZPoint xySource, KdTree.XYZPoint xyDestination){

        // get a copy of the current tree of packages
        KdTree tempTree = new KdTree(packages);
        List<KdTree.XYZPoint> nearestPkg = (List<KdTree.XYZPoint>) tempTree.nearestNeighbourSearch(1, xySource);
        boolean added = false;

        while (!added){
            if(nearestPkg == null){
                System.out.println("Sorry, no nearby packages found!");
                break;
            }
            if( nearestPkg.isEmpty()){
                System.out.println("Sorry, no agents available!");
                break;
            }
            else {
                // get the agent number from assignedPkgs list, get the first available item each time
                List<Integer> intList = assignedPkgs.get(nearestPkg.get(0));

                if (intList != null){
                    for (int agentNum : intList) {

                        List<KdTree.XYZPoint> agentsPkgs = agents.get(agentNum);

                        // we define a loadFactor that changes dynamically every time we add pkgs
                        loadFactor = totalPkgs / numOfAgents;
                        // we have to apply /2, since every pkg has 2 points in the list (src and dest)
                        int currPkgs = agentsPkgs.size() / 2;
                        if (currPkgs < MAX_CAPACITY && currPkgs <= loadFactor) {
                            addPkg(xySource, agentsPkgs);
                            addPkg(xyDestination, agentsPkgs);
                            totalPkgs++;
                            added = true;
                            break;
                        }

                        // find all the packets that belong to current agent and remove them from the list
                        for (KdTree.XYZPoint point : agentsPkgs)
                            tempTree.remove(point);
                    }
                }
            }

            nearestPkg = (List<KdTree.XYZPoint>) tempTree.nearestNeighbourSearch(1, xySource);
        }
    }

    public void addPkg(KdTree.XYZPoint newPoint, List<KdTree.XYZPoint> currAgentList){

        // add the new point to the KdTree and in curr agent's list
        packages.add(newPoint);
        currAgentList.add(newPoint);

        // If size == 1, this was the first added element.
        // Thus, the list (new agent) must be inserted in list of lists
        if(currAgentList.size() == 1)
            agents.add(currAgentList);

        // If the point was already assigned to an agent, add the second agent in the pkg list
        if(assignedPkgs.containsKey(newPoint)) {
            // get all the lists (agent indexes) newPoint exists
            List<Integer> allPkgOccurrences = assignedPkgs.get(newPoint);
            // get index of curr list (agent)
            int currInt = agents.indexOf(currAgentList);

            // if the mapping between point and agent does not exist, add it
            if(!allPkgOccurrences.contains(currInt))
                assignedPkgs.get(newPoint).add(agents.indexOf(currAgentList));

        }
        // else, create a new list for the new point and add it
        else{
            List<Integer> pkgAgents = new ArrayList<>();
            pkgAgents.add(agents.indexOf(currAgentList));
            assignedPkgs.put(newPoint, pkgAgents);
        }

    }

    // initially we get all the starting positions of the agents, and we add them as "packages" to the tree
    // So we can search them in nearest Neighbour process and assign packages that are closer to their location
    public void addAgentsPosition(KdTree.XYZPoint initPoint){

        List<KdTree.XYZPoint> newList = new ArrayList<>();
        addPkg(initPoint, newList);
    }

    public void displayAgents(){

        for(List<KdTree.XYZPoint> agent: agents)
            System.out.println("Agent " + agents.indexOf(agent) + ": " + agent);
    }

}

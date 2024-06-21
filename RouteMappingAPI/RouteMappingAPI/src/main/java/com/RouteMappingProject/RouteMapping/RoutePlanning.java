package com.RouteMappingProject.RouteMapping;

import java.util.*;

public class RoutePlanning {
    private final List<RouteModel.RMNode> openList = new ArrayList<>();
    private final List<RouteModel.RMNode> startPoints = new ArrayList<>();
    private final SortedMap<String, List<RouteModel.RMNode>> destPoints = new TreeMap<>();
    protected final RouteModel rmModel;
    protected final SortedMap<String, Boolean> visitedNodes = new TreeMap<>();
    protected boolean checkedAll = false;
    protected MapDisplay map;

    // Store the final optimal tour
    List<RouteModel.RMNode> optTour = new ArrayList<>();
    // final Path with all the points scheduled
    protected List<String> path = new ArrayList<>();

    public RoutePlanning(RouteModel model, List<KdTree.XYZPoint> start, List<KdTree.XYZPoint> end, KdTree.XYZPoint agentLocation) {

        // Initialize the RMModel attribute
        this.rmModel = model;
        this.map = new MapDisplay(rmModel);

        if(start.size() != end.size()){
            System.out.println("Start and End points are not equal!");
            return;
        }

        for(int i = 0; i < start.size(); i++) {
            //  ##### START NODES ####
            this.startPoints.add(rmModel.findClosestNode((float) start.get(i).x, (float) start.get(i).y));
            // the first source point is the agents location
            if(i > 0)
                this.startPoints.get(i).isStartNode = true;
            else
                this.startPoints.get(i).isAgentLocation = true;

            String currStartRef = this.startPoints.get(i).getRef();

            // ##### END NODES ####
            // end nodes are defined by their start pair, we have a lists with all the desination points that have the same source
            RouteModel.RMNode newNode = rmModel.findClosestNode((float) end.get(i).x, (float) end.get(i).y);

            // the first end node is a "fake" the pair for agents location, not an actual destination
            if(i > 0)
                newNode.isEndNode = true;


            if(this.destPoints.containsKey(currStartRef)){
                this.destPoints.get(currStartRef).add(newNode);
            }
            else{
                List<RouteModel.RMNode> newDestList = new ArrayList<>();
                newDestList.add(newNode);
                destPoints.put(currStartRef, newDestList);
            }
        }

        // Create a list with start node first and all the destination nodes after
        List<RouteModel.RMNode> allNodes = new ArrayList<>();

        // add all source points
        for(RouteModel.RMNode node: this.startPoints) {
            allNodes = addNode(allNodes, node);
        }

        List<RouteModel.RMNode> allNodesTraversal = new ArrayList<>(allNodes);
        // find optimal tour
        while( !checkedAll){
            findTraversalOrder(allNodesTraversal);
        }
        System.out.println("Opt tour: " + optTour);

        // If source and destination are the same point
        if(optTour.size() == 1){
            AStarSearch(optTour.get(0).getRef(), optTour.get(0).getRef());
        }

        for(int i = 0; i < optTour.size() - 1 ; i++){
            // Clear the list before starting a new iteration
            this.openList.clear();
            // make null first node's prev to mark separate tours
            optTour.get(i).prev = null;

            AStarSearch(optTour.get(i).getRef(), optTour.get(i+1).getRef());
        }
        System.out.println("Final PAth: " + path);

        this.map.googleMapsDisplay(path, agentLocation);

    }

    public List<RouteModel.RMNode> addNode (List<RouteModel.RMNode> list, RouteModel.RMNode node){

        if(!list.contains(node))
            list.add(node);

        return list;
    }

    // Heuristic approach on TSP. Find a nearly optimal traversal of all the destination points provided.
    //The TSP is equivalent to finding a minimum-weight perfect matching in a complete graph
    // where the weight of an edge is the distance between two destination points.
    public void findTraversalOrder(List<RouteModel.RMNode> nodes){
        Float minDist = Float.MAX_VALUE;
        RouteModel.RMNode minNode = null;
        RouteModel.RMNode curr;

        if( optTour.isEmpty()) {
            curr = nodes.get(0);
            optTour.add(curr);
        }
        else
            // get the last element added
            curr= optTour.get(optTour.size() - 1);

        curr.distances.clear();
        if(curr.isStartNode && !curr.visited){
            for(RouteModel.RMNode node: destPoints.get(curr.getRef())) {
                addNode(nodes, node);
            }
            curr.visited = true;
        }
        nodes.remove(curr);


        if(nodes.isEmpty()){
            checkedAll = true;
            return;
        }

        // Calculate curr node distances with all the remaining in the list
        for (RouteModel.RMNode other : nodes) {
            curr.distances.add((float) curr.manhattanDist(other));
        }

        for(Float temp: curr.distances){
            // Find min of curr nodes distances
            if(temp < minDist){
                int index = curr.distances.indexOf(temp);
                minNode = nodes.get(index);
                minDist = temp;
            }
        }
        // Add the node with min distance in the tour and check their distances with the remaining nodes\
        if(minNode != null) {
            optTour.add(minNode);
            nodes.remove(minNode);
        }
    }

    //The H value of every node is their distance from the end node
    public double calculateHValue(RouteModel.RMNode currNode, RouteModel.RMNode endNode) {
        return currNode.manhattanDist(endNode);
    }

    public void addNeighbors(RouteModel.RMNode currentNode, RouteModel.RMNode endNode,
                             SortedMap<String, RouteModel.RMNode> allNodes) {

        currentNode.findNeighbors(allNodes, visitedNodes);

        for (RouteModel.RMNode neighbor : currentNode.neighbors) {
            if(visitedNodes.get(neighbor.getRef()) == null) {
                neighbor.prev = currentNode;
                neighbor.gVal = (float) (currentNode.gVal + neighbor.manhattanDist(currentNode));
                neighbor.hVal = (float) calculateHValue(neighbor, endNode);
                visitedNodes.put(neighbor.getRef(), true);
                openList.add(neighbor);
            }
        }
    }

    public RouteModel.RMNode findNextNode() {
        // Sort with a decreasing order
        this.openList.sort((v1, v2) -> Float.compare(v2.hVal + v2.gVal, v1.hVal + v1.gVal));
        return this.openList.remove(this.openList.size() - 1);
    }


    public List<String> constructFinalPath(RouteModel.RMNode currNode) {

        // Create pathFound list
        List<String> pathFound = new ArrayList<>();

        String tailStr;
        RouteModel.RMNode tail = null;

        // if the path is not empty, find the last element (tail) of the path
        if (path.size() != 0) {
            tailStr = path.get(path.size() - 1);
            tail = rmModel.getRMNode(tailStr);
        }

        pathFound.add(currNode.getRef());
        // keep going backwards until the tail is found, to add all the new elements
        while (currNode.prev != tail && currNode.prev != null) {
            pathFound.add(currNode.prev.getRef());
            currNode = currNode.prev;
        }
        Collections.reverse(pathFound);

        return pathFound;
    }

    public void AStarSearch(String startNodeRef, String endNodeRef) {

        System.out.println("Start: " + startNodeRef + ", End: " + endNodeRef);
        SortedMap<String, RouteModel.RMNode> allNodesLocal = new TreeMap<>(rmModel.getRouteModelNodes());
        RouteModel.RMNode startNode = allNodesLocal.get(startNodeRef);
        RouteModel.RMNode endNode = allNodesLocal.get(endNodeRef);

        // Clear the visiting list from the prev iteration
        visitedNodes.clear();
        visitedNodes.put(startNodeRef, true);

        this.openList.add(startNode);
        while (!this.openList.isEmpty()) {
            RouteModel.RMNode nextNode = findNextNode();

            if (nextNode.getLon() == endNode.getLon() && nextNode.getLat() == endNode.getLat()) {
                List<String> shortPath = constructFinalPath(nextNode);
                path.addAll(shortPath);
                break;

            } else
                addNeighbors(nextNode, endNode, allNodesLocal);
        }
    }
}

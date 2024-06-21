package com.RouteMappingProject.RouteMapping;

import java.util.*;

public class RouteModel extends Model {

    public static class RMNode extends Node {
        protected int index;
        protected RouteModel parentModel;
        protected RMNode prev;
        protected float hVal = Float.MAX_VALUE;
        protected float gVal = 0.0f;
        protected boolean visited = false;
        public boolean isEndNode = false;
        public boolean isStartNode = false;
        public boolean isAgentLocation = false;
        protected List<RMNode> neighbors = new ArrayList<>();
        // tour planning
        protected List<Float> distances = new ArrayList<>();

        public RMNode( int idx, RouteModel parentModel, Node node) {
            super(node);
            this.parentModel = parentModel;
            this.index = idx;
        }


        public RMNode(double lon, double lat){
            super(lon, lat);
        }

        public void findNeighbors( SortedMap<String, RMNode> allNodes, SortedMap<String, Boolean> visitedNodes) {
            // find all the Ways where the curr road belongs
            List<Road> roads = parentModel.getRoadToWay().get(this.getRef());
            if (roads == null)
                return;

            for (Road road : roads) {
                Way way = parentModel.getWays().get(road.getRef());
                RMNode newNeighbor = findNeighbor(way, allNodes, visitedNodes);
                if (newNeighbor != null) {
                    this.neighbors.add(newNeighbor);
                }
            }
        }

        public List<String> nextListElements(List<String> nodeRefs){
            List<String> nextNodes = new ArrayList<>();
            boolean found = false;

            for(String ref: nodeRefs){
                if(found){
                    nextNodes.add(ref);
                }
                else if(this.getRef().compareTo(ref) == 0){
                    found = true;
                }
            }
            return nextNodes;
        }

        public RMNode findNeighbor(Way way, SortedMap<String, RMNode> allNodes,
                                   SortedMap<String, Boolean> visitedNodes) {
            RMNode closestNode = null;
            List<String> nodeRefs = way.nodes;

            // If the road is one_way check only the nodes after curr
            if(way.isOneWay())
                nodeRefs = nextListElements(nodeRefs);

            for (String ref : nodeRefs) {
                RMNode node = allNodes.get(ref);
                if (this.getRef().compareTo(ref) != 0 && visitedNodes.get(node.getRef()) == null) {
                    if (closestNode == null || this.manhattanDist(node) < this.manhattanDist(closestNode))
                        closestNode = allNodes.get(ref);
                }
            }
            return closestNode;
        }

        double manhattanDist(RMNode node){
            return Math.abs(node.getLon() - getLon()) + Math.abs(node.getLat() - getLat());
        }

        @Override
        public String toString() {
            return getRef() +" (" + getLat() + ", " + getLon() + ")";
        }
    }

    // routeModelNodes is a map with all the existing RMNodes, sorted by their node_id
    private final SortedMap<String, RMNode> routeModelNodes = new TreeMap<>();

    // roadToNodes links a road id with all the ways it belongs
    private final Map<String, List<Road>> roadToWay = new HashMap<>();

    protected KdTree<KdTree.XYZPoint> nodesTree = new KdTree<>();
    private final SortedMap<KdTree.XYZPoint, String> pointToRef = new TreeMap<>();

    public RouteModel(List<Byte> xml) {
        super(xml);
        createRouteModelNodes();
        createHashmap();
    }

    public Map<String, List<Road>> getRoadToWay() { return roadToWay; }
    public SortedMap<String, RMNode> getRouteModelNodes(){ return routeModelNodes; }

    private void createRouteModelNodes() {
        int counter = 0;
        for (Map.Entry<String, Node> entry : getNodes().entrySet()) {
            Node node = entry.getValue();
            RMNode newRmNode = new RMNode(counter, this, node);
            routeModelNodes.put(newRmNode.getRef(), newRmNode);
            counter++;
        }
    }

    protected void createHashmap() {
        // Iterate over all roads
        for (Road road : getRoads().values()) {
            if (road.getType() != Road.Type.Footway) {
                // Iterate over all nodes of the current road
                for (String node_idx : getWays().get(road.getRef()).nodes) {
                    // If the node is not in the hashmap, create a new list for it
                    if (!roadToWay.containsKey(node_idx)) {
                        roadToWay.put(node_idx, new ArrayList<>());
                    }
                    // Add the road to the list of roads for this node
                    roadToWay.get(node_idx).add(road);
                    RouteModel.RMNode roadNode = getRouteModelNodes().get(node_idx);
                    KdTree.XYZPoint point = new KdTree.XYZPoint(roadNode.getLon(), roadNode.getLat());
                    nodesTree.add(point);
                    pointToRef.put(point, roadNode.getRef());
                }
            }
        }
    }

    public RMNode getRMNode (String ref){
        return getRouteModelNodes().get(ref);
    }

    public RMNode findClosestNode(float lon, float lat) {

        List<KdTree.XYZPoint> closestPointList = (List<KdTree.XYZPoint>) nodesTree.nearestNeighbourSearch(1, new KdTree.XYZPoint(lon, lat));
        KdTree.XYZPoint closestPoint = closestPointList.get(0);
        String ref = pointToRef.get(closestPoint);
        System.out.println("Nearest: " + closestPoint + " and ref: " + ref);
        return routeModelNodes.get(ref);
    }
}

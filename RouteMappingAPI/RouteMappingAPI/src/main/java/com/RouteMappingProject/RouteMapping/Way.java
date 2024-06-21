package com.RouteMappingProject.RouteMapping;

import java.util.ArrayList;
import java.util.List;

public class Way {
    protected final List<String> nodes = new ArrayList<>();
    private final String wayId;
    private boolean oneWay = false;

    public Way(String id) {
        this.wayId = id;
    }

    public void setDirection(boolean oneWay){ this.oneWay = oneWay; }
    public boolean isOneWay() { return this.oneWay; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" OneWay: ").append(oneWay).append(" (");
        for (String nodeId : nodes) {
            sb.append(nodeId).append(" - ");
        }
        sb.append(")");
        return sb.toString();
    }
}

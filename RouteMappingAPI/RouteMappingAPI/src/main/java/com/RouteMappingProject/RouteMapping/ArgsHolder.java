package com.RouteMappingProject.RouteMapping;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
/**
 * Class to hold two arguments from POST Request in "/agents"
*/
public class ArgsHolder {
    List<Location> agentsLocations;
    Integer maxCapacity;
}

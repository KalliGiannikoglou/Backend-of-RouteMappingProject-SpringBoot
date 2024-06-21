package com.RouteMappingProject.RouteMapping;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@RestController
@CrossOrigin(origins = "*")
@RequestMapping
public class MapController {

    private final AlgorithmicRunner algorithmRunner;
    private List<Location> allAgents = new ArrayList<>();
    private List<Location> allPackages = new ArrayList<>();
    private Integer totalMaxCapacity = 0;

    public MapController() throws IOException {
        this.algorithmRunner = new AlgorithmicRunner();
    }


    @PostMapping("/agents")
    public ResponseEntity getAgentsLocations(@RequestBody ArgsHolder args){
        System.out.println("Received Agents locations: ");
        for(Location location : args.getAgentsLocations())
            System.out.println("lat: " + location.getLat() + ", lng: " + location.getLng());
        allAgents = args.getAgentsLocations();
        totalMaxCapacity = args.getMaxCapacity();
        System.out.println("Max Capacity: " + totalMaxCapacity);
        return new ResponseEntity("Agent Locations received successfully", HttpStatus.OK);
    }

    @PostMapping("/packages")
    public ResponseEntity getPkgLocations(@RequestBody List<Location> pkgLocations) throws IOException {
        System.out.println("Received Package locations: ");
        for(Location location : pkgLocations)
            System.out.println("lat: " + location.getLat() + ", lng: " + location.getLng());
        allPackages = pkgLocations;
        // Exec algorithmic part after POST methods
        runAlgorithm();
        return new ResponseEntity("Package Locations received successfully", HttpStatus.OK);
    }

    private void runAlgorithm() throws IOException {
        if (!allAgents.isEmpty() && !allPackages.isEmpty()) {
            // Run the algorithm with both lists
            this.algorithmRunner.finalUrls.clear();
            algorithmRunner.runAlgorithm(allAgents, totalMaxCapacity, allPackages);
        }
    }

    @GetMapping("/maps")
    public List<String> sendUrls() throws IOException{
        System.out.println("Final URLS:" + algorithmRunner.finalUrls);
        return algorithmRunner.finalUrls;
    }

}

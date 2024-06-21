package com.RouteMappingProject.RouteMapping;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class FileReader {

    public FileReader(){}
    public Optional<byte[]> readFileFromResources(String fileName) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                System.err.println("File not found in resources: " + fileName);
                return Optional.empty();
            }
            byte[] fileContent = inputStream.readAllBytes();
            return Optional.of(fileContent);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return Optional.empty();
        }
    }
}

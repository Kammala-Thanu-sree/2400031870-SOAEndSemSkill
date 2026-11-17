package com.rideshare.driver;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DriverController {

    @Value("${server.port}")
    private String port;

    @GetMapping("/drivers")
    public Map<String, String> getDrivers() {
        System.out.println("Request handled by Driver Service instance on port " + port);
        return Map.of(
            "service", "driver-service",
            "message", "Driver service is responding",
            "instancePort", port
        );
    }
}

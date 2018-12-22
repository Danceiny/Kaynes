package cc.cannot.dict.web.controller;

import org.springframework.boot.actuate.health.Health;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dict")
public class HealthController {
    @GetMapping(value = "/health")
    public Object hello(String name) {
        return Health.up()
                     .withDetail("message", "It's fine now!")
                     .build();
    }
}
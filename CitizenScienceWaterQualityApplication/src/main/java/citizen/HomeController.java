package citizen;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "<h2>Citizen Science Water Quality Application is running successfully! <br> Execute Microservices</h2>";
    }

    @GetMapping("/health")
    public String health() {
        return "Citizen Science Water Quality Application Status: UP";
    }
}
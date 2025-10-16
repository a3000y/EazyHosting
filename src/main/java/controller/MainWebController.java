package controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;

@Controller
public class MainWebController {

    @GetMapping("/")
    public String showChart(Model model) {
        // Prepare data for the chart
        List<String> labels = Arrays.asList("Red", "Blue", "Yellow", "Green", "Purple", "Orange");
        List<Integer> data = Arrays.asList(12, 19, 3, 5, 2, 3);

        // Add data to the model
        model.addAttribute("chartLabels", labels);
        model.addAttribute("chartData", data);

        return "chart-page"; // Returns the name of the Thymeleaf template
    }
}
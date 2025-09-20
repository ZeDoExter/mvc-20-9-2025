package com.ZeDoExter.mvc.controller;

import com.ZeDoExter.mvc.service.StatsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StatsController {
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/stats")
    public String summary(Model model, HttpSession session) {
        model.addAttribute("accepted", statsService.countAccepted());
        model.addAttribute("rejected", statsService.countRejected());
        model.addAttribute("currentUser", session.getAttribute("currentUserName"));
        model.addAttribute("active", "stats");
        return "stats";
    }
}

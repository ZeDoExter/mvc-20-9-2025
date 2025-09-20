package com.ZeDoExter.mvc.controller;

import com.ZeDoExter.mvc.model.Project;
import com.ZeDoExter.mvc.service.PledgeService;
import com.ZeDoExter.mvc.service.ProjectService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class ProjectController {
    private final ProjectService projectService;
    private final PledgeService pledgeService;

    public ProjectController(ProjectService projectService, PledgeService pledgeService) {
        this.projectService = projectService;
        this.pledgeService = pledgeService;
    }

    @GetMapping({"/", "/projects"})
    public String list(@RequestParam(value = "category", required = false) String category,
                       @RequestParam(value = "sort", required = false, defaultValue = "deadline") String sort,
                       Model model, HttpSession session) {
        List<Project> projects = projectService.listProjects(category, sort);
        model.addAttribute("projects", projects);
        model.addAttribute("categories", projectService.allCategories());
        model.addAttribute("selectedCategory", category == null ? "" : category);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("currentUser", session.getAttribute("currentUserName"));
        model.addAttribute("active", "projects");
        return "projects/list";
    }

    @GetMapping("/projects/{id}")
    public String detail(@PathVariable String id, Model model, HttpSession session) {
        Optional<Project> opt = projectService.getProject(id);
        if (opt.isEmpty()) return "redirect:/projects";
        Project p = opt.get();
        double progress = Math.min(100.0, (p.getCurrentRaised() / p.getGoalAmount()) * 100.0);
        model.addAttribute("project", p);
        model.addAttribute("progress", String.format("%.1f", progress));
        model.addAttribute("hasStretch", p.isHasStretchGoals());
        model.addAttribute("basicModel", projectService.toBasicModel(p));
        model.addAttribute("stretchModel", projectService.toStretchModel(p));
        model.addAttribute("currentUser", session.getAttribute("currentUserName"));
        model.addAttribute("active", "projects");
        return "projects/detail";
    }

    @PostMapping("/projects/{id}/pledge")
    public String pledge(@PathVariable String id,
                         @RequestParam("amount") double amount,
                         @RequestParam(value = "rewardTierId", required = false) String rewardTierId,
                         HttpSession session,
                         RedirectAttributes ra) {
        String userId = (String) session.getAttribute("currentUserId");
        if (userId == null) {
            ra.addFlashAttribute("error", "กรุณาเข้าสู่ระบบก่อนทำรายการ");
            return "redirect:/login?redirect=/projects/" + id;
        }
        PledgeService.Result result = pledgeService.attemptPledge(userId, id, amount, rewardTierId);
        if (result.accepted) {
            ra.addFlashAttribute("success", result.message);
        } else {
            ra.addFlashAttribute("error", result.message);
        }
        return "redirect:/projects/" + id;
    }
}

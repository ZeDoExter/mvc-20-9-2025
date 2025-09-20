package com.ZeDoExter.mvc.controller;

import com.ZeDoExter.mvc.model.User;
import com.ZeDoExter.mvc.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class LoginController {
    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "redirect", required = false) String redirect,
                            Model model) {
        model.addAttribute("redirect", redirect == null ? "/projects" : redirect);
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam(value = "redirect", required = false) String redirect,
                          HttpSession session,
                          RedirectAttributes ra) {
        Optional<User> user = authService.login(username, password);
        if (user.isPresent()) {
            session.setAttribute("currentUserId", user.get().getId());
            session.setAttribute("currentUserName", user.get().getDisplayName());
            return "redirect:" + (redirect == null || redirect.isBlank() ? "/projects" : redirect);
        }
        ra.addFlashAttribute("error", "ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง");
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/projects";
    }
}


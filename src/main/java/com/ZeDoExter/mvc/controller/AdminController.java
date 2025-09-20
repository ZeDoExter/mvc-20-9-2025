package com.ZeDoExter.mvc.controller;

import com.ZeDoExter.mvc.repository.DataStore;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {
    private final DataStore dataStore;

    public AdminController(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    @GetMapping("/admin/seed/reset")
    public String resetSeed(RedirectAttributes ra) {
        try {
            dataStore.getAllProjects(); // touch to ensure init called
            // Clear and reseed
            dataStoreReset();
            ra.addFlashAttribute("success", "รีเซ็ตและสร้างข้อมูลตัวอย่างใหม่แล้ว");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "เกิดข้อผิดพลาดในการรีเซ็ต: " + e.getMessage());
        }
        return "redirect:/projects";
    }

    private void dataStoreReset() throws Exception {
        // Reflectively invoke reset if not public; otherwise call directly
        // But we will call via public method we define below (exposed in DataStore)
        dataStore.resetWithDiverseSamples();
    }
}


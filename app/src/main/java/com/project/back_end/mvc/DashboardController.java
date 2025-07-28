package com.project.back_end.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import com.project.back_end.services.CentralService;

/**
 * MVC controller for routing to admin and doctor dashboards based on token
 * validation.
 */
@Controller
public class DashboardController {

    private final CentralService centralService;

    public DashboardController(CentralService centralService) {
        this.centralService = centralService;
    }

    /**
     * Handles GET requests to /adminDashboard/{token}. Validates the token for the
     * ADMIN role.
     * Forwards to the admin dashboard view on success, or redirects to root on
     * failure.
     */
    @GetMapping("/adminDashboard/{token}")
    public ModelAndView adminDashboard(@PathVariable String token) {
        var response = centralService.validateToken(token, "ADMIN");
        if (response.getStatusCode().is2xxSuccessful()) {
            return new ModelAndView("admin/adminDashboard");
        } else {
            return new ModelAndView("redirect:/");
        }
    }

    /**
     * Handles GET requests to /doctorDashboard/{token}. Validates the token for the
     * DOCTOR role.
     * Forwards to the doctor dashboard view on success, or redirects to root on
     * failure.
     */
    @GetMapping("/doctorDashboard/{token}")
    public ModelAndView doctorDashboard(@PathVariable String token) {
        var response = centralService.validateToken(token, "DOCTOR");
        if (response.getStatusCode().is2xxSuccessful()) {
            return new ModelAndView("doctor/doctorDashboard");
        } else {
            return new ModelAndView("redirect:/");
        }
    }
}

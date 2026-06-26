package com.example.library.controller;

import com.example.library.dto.ApiResponse;
import com.example.library.dto.HomeOverviewResponse;
import com.example.library.service.HomeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/home", "/home"})
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping("/overview")
    public ApiResponse<HomeOverviewResponse> getOverview() {
        return ApiResponse.success(homeService.getOverview());
    }
}

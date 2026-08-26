package org.example.controller;

import org.example.model.CompanySearchResponse;
import org.example.service.CompanySearchService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class CompanySearchController {

    private final CompanySearchService companySearchService;

    public CompanySearchController(CompanySearchService companySearchService) {
        this.companySearchService = companySearchService;
    }

    @GetMapping("/api/companies/search")
    public CompanySearchResponse search(@RequestParam("q") String query) throws Exception {
        return new CompanySearchResponse(companySearchService.search(query));
    }
}

package org.example.service;

import org.example.model.CompanyInfo;
import org.example.model.Officer;
import org.example.model.Psc;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.example.model.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.example.repository.CompanyInfoRepository;
import org.example.repository.QueryRepository;

@Service
public class CompanySearchService {
    private final CompanyInfoRepository companyInfoRepository;
    private final QueryRepository queryRepository;

    public CompanySearchService(CompanyInfoRepository companyInfoRepository, QueryRepository queryRepository) {
        this.companyInfoRepository = companyInfoRepository;
        this.queryRepository = queryRepository;
    }

    public Query checkDbForQueryOrUpdate(String query) {
        System.out.println("checking cache...");
        Query q = queryRepository.findBySearchQuery(query);
        if (q == null) {
            System.out.println("query not found, going to ordinary flow");
            return null;
        }
        long minutesSinceLastUpdate = ChronoUnit.MINUTES.between(q.getDate(), LocalDateTime.now());
        if (minutesSinceLastUpdate >= 1) {
            System.out.println("too old, going to ordinary flow");
            return null;
        } else {
            System.out.println("query found, preparing for retrieval");
            return q;
        }

    }

    public List<CompanyInfo> search(String query) throws Exception {

        Query storedQuery = checkDbForQueryOrUpdate(query);
        if (storedQuery != null) {
            List<CompanyInfo> companyInfos = new ArrayList<>();
            companyInfoRepository.findAllById(storedQuery.getCompanyIds()).forEach(companyInfos::add);
            System.out.println("retrieving the stored values");
            return companyInfos;
        }

        String url = "https://find-and-update.company-information.service.gov.uk/search?q=" + query;

        ArrayList<Document> pages = getTenPages(url);
        ArrayList<String> companyIds = new ArrayList<>();

        System.out.println("starting companyIds extraction");
        pages.forEach((page) -> {
            String[] extractedIds = extractCompanyIds(page);
            Collections.addAll(companyIds, extractedIds);
        });
        System.out.println("extracted company ids, total count is : " + companyIds.size());

        ArrayList<CompanyInfo> companies = new ArrayList<>();
        int minLength = Math.min(companyIds.size(), 100);// you can change this number (100) to something smaller to have results faster (by default i parse maximum of 100 companies)
        for (int i = 0; i < minLength; i++) {
            companies.add(parseCompanyPage(companyIds.get(i), query));
        }
        Query existingQuery = queryRepository.findBySearchQuery(query);
        String existingQueryId = existingQuery != null ? existingQuery.getId() : null;
        queryRepository.save(new Query(existingQueryId, query, companyIds, LocalDateTime.now()));


        return companies;
    }

    public ArrayList<Document> getTenPages(String url) throws Exception {
        ArrayList<Document> pages = new ArrayList<>();
        int counter = 1;
        for (int i = 0; i < 10; i++) {
            System.out.println("getting and storing page No" + counter);
            String pageUrl = url + "&page=" + counter;
            Document page = Jsoup.connect(pageUrl).get();
            pages.add(page);

            counter++;
        }
        System.out.println("storing phase completed");
        return pages;
    }

    public String[] extractCompanyIds(Document page) {
        Elements companyItems = page.select(".results-list li.type-company");

        String[] companyIds = new String[companyItems.size()];
        for (int i = 0; i < companyItems.size(); i++) {
            String href = companyItems.get(i).selectFirst("a").attr("href");
            companyIds[i] = href.substring(href.lastIndexOf('/') + 1);
        }
        return companyIds;
    }

    public CompanyInfo parseCompanyPage(String id, String query) throws Exception {
        System.out.println("parsing the company page " + id);
        String url = "https://find-and-update.company-information.service.gov.uk/company/" + id;
        Document page = Jsoup.connect(url).get();

        Element companyNumberEl = page.selectFirst("#company-number strong");
        Element nameEl = page.selectFirst("h1.heading-xlarge");
        Element statusEl = page.selectFirst("#company-status");
        Element companyTypeEl = page.selectFirst("#company-type-value");
        Element incorporatedOnEl = page.selectFirst("#company-creation-date");
        Element registeredOfficeAddressEl = page.selectFirst("#roa-address");

        String companyNumber = companyNumberEl != null ? companyNumberEl.text() : null;
        String name = nameEl != null ? nameEl.text() : null;
        String status = statusEl != null ? statusEl.text() : null;
        String companyType = companyTypeEl != null ? companyTypeEl.text().trim() : null;
        String incorporatedOn = incorporatedOnEl != null ? incorporatedOnEl.text() : null;
        String registeredOfficeAddress = registeredOfficeAddressEl != null ? registeredOfficeAddressEl.text().trim() : null;

        List<Officer> officers = parseOfficers(id);
        List<Psc> pscs = parsePscs(id);

        CompanyInfo companyInfo = new CompanyInfo(companyNumber, query, name, status, companyType, incorporatedOn,
                registeredOfficeAddress, officers, pscs, LocalDateTime.now());

        companyInfoRepository.save(companyInfo);
        return companyInfo;
    }

    public List<Officer> parseOfficers(String id) throws Exception {
        String url = "https://find-and-update.company-information.service.gov.uk/company/" + id + "/officers";
        Document page = Jsoup.connect(url).get();

        List<Officer> officers = new ArrayList<>();
        for (Element appointment : page.select(".appointments-list > div")) {
            Element nameEl = appointment.selectFirst("[id^=officer-name-]");
            if (nameEl == null) continue;

            Element roleEl = appointment.selectFirst("[id^=officer-role-]");
            Element appointedOnEl = appointment.selectFirst("[id^=officer-appointed-on-]");

            String role = roleEl != null ? roleEl.text().trim() : null;
            String appointedOn = appointedOnEl != null ? appointedOnEl.text().trim() : null;
            officers.add(new Officer(nameEl.text().trim(), role, appointedOn));
        }
        return officers;
    }

    public List<Psc> parsePscs(String id) throws Exception {
        String url = "https://find-and-update.company-information.service.gov.uk/company/" + id + "/persons-with-significant-control";
        Document page = Jsoup.connect(url).get();

        List<Psc> pscs = new ArrayList<>();
        for (Element appointment : page.select(".appointments-list > div")) {
            Element nameEl = appointment.selectFirst("[id^=psc-name-]");
            if (nameEl == null) continue;

            List<String> natureOfControl = new ArrayList<>();
            for (Element noc : appointment.select("[id^=psc-noc-]")) {
                natureOfControl.add(noc.text().trim());
            }
            pscs.add(new Psc(nameEl.text().trim(), natureOfControl));
        }
        return pscs;
    }
}

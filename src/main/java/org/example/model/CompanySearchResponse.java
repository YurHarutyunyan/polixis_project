package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@ToString
public class CompanySearchResponse {
    @JsonProperty("companies")
    private final List<CompanyInfo> companies;
}

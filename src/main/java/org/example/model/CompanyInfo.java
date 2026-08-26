package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@ToString
@Document(collection = "company")
public class CompanyInfo {

    @Id
    private final String companyNumber;

    private final String query;

    private final String name;

    private final String status;

    private final String companyType;

    private final String incorporatedOn;

    private final String registeredOfficeAddress;

    private final List<Officer> officers;

    private final List<Psc> pscs;

    @JsonIgnore
    private final LocalDateTime scrapedAt;
}

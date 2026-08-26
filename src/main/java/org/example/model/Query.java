package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;

@AllArgsConstructor
@Getter
@Document(collection = "query")
public class Query {
    @Id
    String id;

    @Indexed(unique = true)
    String searchQuery;

    ArrayList<String> companyIds;

    LocalDateTime date;
}

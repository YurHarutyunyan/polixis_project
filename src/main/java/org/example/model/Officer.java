package org.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class Officer {
    @JsonProperty("name")
    private final String name;

    @JsonProperty("role")
    private final String role;

    @JsonProperty("appointedOn")
    private final String appointedOn;
}

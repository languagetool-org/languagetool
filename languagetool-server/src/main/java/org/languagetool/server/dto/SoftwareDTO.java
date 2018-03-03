package org.languagetool.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SoftwareDTO {
    @JsonProperty
    public String name;

    @JsonProperty
    public String version;

    @JsonProperty
    public String buildDate;

    @JsonProperty
    public Integer apiVersion;

    @JsonProperty
    public String status;
}

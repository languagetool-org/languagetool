package org.languagetool.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RuleDTO {
    @JsonProperty
    public String id;

    @JsonProperty
    public String subId;

    @JsonProperty
    public String description;

    @JsonProperty
    public List<UrlDTO> urls;

    @JsonProperty
    public String issueType;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public CategoryDTO category;
}


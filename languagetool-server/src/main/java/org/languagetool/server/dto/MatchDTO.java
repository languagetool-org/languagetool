package org.languagetool.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatchDTO {
    @JsonProperty
    public String message;

    @JsonProperty
    public String shortMessage;

    @JsonProperty
    public Integer offset;

    @JsonProperty
    public Integer length;

    @JsonProperty
    public List<ReplacementDTO> replacements;

    @JsonProperty
    public ContextDTO context;

    @JsonProperty
    public String sentence;

    @JsonProperty
    public RuleDTO rule;
}

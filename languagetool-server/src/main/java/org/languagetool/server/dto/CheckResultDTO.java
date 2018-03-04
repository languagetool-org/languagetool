package org.languagetool.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckResultDTO {
    @JsonProperty
    public SoftwareDTO software;

    @JsonProperty
    public LanguageDTO language;

    @JsonProperty
    public List<MatchDTO> matches;
}

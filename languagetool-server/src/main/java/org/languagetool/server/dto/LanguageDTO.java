package org.languagetool.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.ToString;

@ToString
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LanguageDTO {
    @JsonProperty
    public String name;

    @JsonProperty
    public String code;

    @JsonProperty
    public String longCode;
}

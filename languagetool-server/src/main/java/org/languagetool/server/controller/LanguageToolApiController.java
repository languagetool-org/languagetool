package org.languagetool.server.controller;

import org.languagetool.server.dto.CheckResultDTO;
import org.languagetool.server.dto.LanguageDTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;


@Controller
public interface LanguageToolApiController {
    @GetMapping(path = "/languages")
    ResponseEntity<List<LanguageDTO>> languages();

    @PostMapping(path = "/check", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResponseEntity<CheckResultDTO> check(
            @RequestParam("text") String text,
            @RequestParam("language") String language,
            @RequestParam("motherTongue") String motherTongue,
            @RequestParam("preferredVariants") String preferredVariants,
            @RequestParam("enabledRules") String enabledRules,
            @RequestParam("disabledRules") String disabledRules,
            @RequestParam("enabledCategories") String enabledCategories,
            @RequestParam("disabledCategories") String disabledCategories,
            @RequestParam("enabledOnly") boolean enabledOnly
    );
}

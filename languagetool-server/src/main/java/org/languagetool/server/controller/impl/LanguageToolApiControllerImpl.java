package org.languagetool.server.controller.impl;

import lombok.extern.slf4j.Slf4j;
import org.languagetool.server.controller.LanguageToolApiController;
import org.languagetool.server.dto.CheckResultDTO;
import org.languagetool.server.dto.LanguageDTO;
import org.languagetool.server.service.LanguageToolApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Slf4j
@SuppressWarnings("unused")
@Controller
public class LanguageToolApiControllerImpl implements LanguageToolApiController {

    private final LanguageToolApiService languageToolApiService;

    @Autowired
    public LanguageToolApiControllerImpl(LanguageToolApiService languageToolApiService) {
        this.languageToolApiService = languageToolApiService;
    }

    @Override
    public ResponseEntity<List<LanguageDTO>> languages() {
        log.info("GET /languages request");
        ResponseEntity<List<LanguageDTO>> response;
        try {
            List<LanguageDTO> languages = languageToolApiService.languages();
            response = new ResponseEntity<>(languages, HttpStatus.OK);
        } catch (Error e) {
            log.error("Error!", e);
            response = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        log.info("GET /languages response: '{}'", response);
        return response;
    }

    @Override
    public ResponseEntity<CheckResultDTO> check(String text, String language, String motherTongue, String preferredVariants,
                                                String enabledRules, String disabledRules, String enabledCategories,
                                                String disabledCategories, boolean enabledOnly) {
        log.info("POST /check request: " +
                        "text='{}', " +
                        "language='{}', " +
                        "motherTongue='{}', " +
                        "preferredVariants='{}', " +
                        "enabledRules='{}', " +
                        "disabledRules='{}', " +
                        "enabledCategories='{}', " +
                        "disabledCategories='{}', " +
                        "enabledOnly='{}'",
                text,
                language,
                motherTongue,
                preferredVariants,
                enabledRules,
                disabledRules,
                enabledCategories,
                disabledCategories,
                enabledOnly
        );
        ResponseEntity<CheckResultDTO> response;
        try {
            CheckResultDTO checkResultDTO = languageToolApiService.check(text, language, motherTongue, preferredVariants, enabledRules,
                    disabledRules, enabledCategories, disabledCategories, enabledOnly);

            response = new ResponseEntity<>(checkResultDTO, HttpStatus.OK);
        }
        catch (Exception e) {
            log.error("Error!", e);
            response = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        log.info("POST /check request: " +
                        "text='{}', " +
                        "language='{}', " +
                        "motherTongue='{}', " +
                        "preferredVariants='{}', " +
                        "enabledRules='{}', " +
                        "disabledRules='{}', " +
                        "enabledCategories='{}', " +
                        "disabledCategories='{}', " +
                        "enabledOnly='{}', " +
                        "response='{}'",
                text,
                language,
                motherTongue,
                preferredVariants,
                enabledRules,
                disabledRules,
                enabledCategories,
                disabledCategories,
                enabledOnly,
                response
        );
        return response;
    }


}

package org.languagetool.server.service;

import org.languagetool.server.dto.CheckResultDTO;
import org.languagetool.server.dto.LanguageDTO;

import java.util.List;

public interface LanguageToolApiService {

    List<LanguageDTO> languages();

    CheckResultDTO check(String text, String language, String motherTongue, String preferredVariants, String enabledRules,
                         String disabledRules, String enabledCategories, String disabledCategories, boolean enabledOnly);
}

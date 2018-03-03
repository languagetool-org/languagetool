package org.languagetool.server.service.impl;

import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.server.dto.LanguageDTO;
import org.languagetool.server.service.LanguageToolApiService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service("LanguageToolApiService")
public class LanguageToolApiServiceImpl implements LanguageToolApiService {
    @Override
    public List<LanguageDTO> languages() {
        List<LanguageDTO> languageDTOs = new ArrayList<>();

        List<Language> LTLanguages = new ArrayList<>(Languages.get());
        LTLanguages.sort(Comparator.comparing(Language::getName));
        for (Language LTLang : LTLanguages) {
            languageDTOs.add(new LanguageDTO(LTLang.getName(), LTLang.getShortCode(), LTLang.getShortCodeWithCountryAndVariant()));
        }

        return languageDTOs;
    }
}

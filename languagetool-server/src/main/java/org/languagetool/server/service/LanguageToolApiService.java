package org.languagetool.server.service;

import org.languagetool.server.dto.LanguageDTO;

import java.util.List;

public interface LanguageToolApiService {

    List<LanguageDTO> languages();
}

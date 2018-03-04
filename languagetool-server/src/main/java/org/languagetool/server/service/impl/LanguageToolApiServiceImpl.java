package org.languagetool.server.service.impl;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.languagetool.*;
import org.languagetool.language.LanguageIdentifier;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.server.dto.*;
import org.languagetool.server.service.LanguageToolApiService;
import org.languagetool.server.UserLimits;
import org.languagetool.tools.ContextTools;
import org.languagetool.tools.Tools;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

@Service("LanguageToolApiService")
@Slf4j
public class LanguageToolApiServiceImpl implements LanguageToolApiService {

    private LanguageIdentifier identifier = new LanguageIdentifier();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private static final String START_MARKER = "__languagetool_start_marker";
    private static final int CONTEXT_SIZE = 40; // characters


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

    @Override
    public CheckResultDTO check(String text, String language, String motherTongue, String preferredVariants, String enabledRules,
                                String disabledRules, String enabledCategories, String disabledCategories, boolean enabledOnly) {

        if (text == null) {
            throw new IllegalArgumentException("text field should not be null");
        }
        AnnotatedText aText = new AnnotatedTextBuilder().addText(text).build();

//        textChecker.checkText(aText, httpExchange, parameters, errorRequestLimiter, remoteAddress);


        long timeStart = System.currentTimeMillis();
        UserLimits limits = new UserLimits(Integer.MAX_VALUE, -1, null);
        if (aText.getPlainText().length() > limits.getMaxTextLength()) {
            throw new IllegalArgumentException("Your text exceeds the limit of " + limits.getMaxTextLength() +
                    " characters (it's " + aText.getPlainText().length() + " characters). Please submit a shorter text.");
        }

        boolean autoDetectLanguage = "auto".equals(language);

        List<String> preferredVariantsList;
        if (preferredVariants != null) {
            preferredVariantsList = Arrays.asList(preferredVariants.split(",\\s*"));
            if (!autoDetectLanguage) {
                throw new IllegalArgumentException("You specified 'preferredVariants' but you didn't specify 'language=auto'");
            }
        } else {
            preferredVariantsList = Collections.emptyList();
        }

        Language lang;
        if (autoDetectLanguage) {
            lang = detectLanguageOfString(text, null, preferredVariantsList);
        } else {
            lang = Languages.getLanguageForShortCode(language);
        }


        //print("Starting check: " + aText.getPlainText().length() + " chars, #" + count);
        Language motherTongueLanguage = motherTongue != null ? Languages.getLanguageForShortCode(motherTongue) : null;

        List<String> enabledRulesList = new ArrayList<>();
        if (enabledRules != null){
            enabledRulesList.addAll(Arrays.asList(enabledRules.split(", ")));
        }

        List<String> disabledRulesList = new ArrayList<>();
        if (disabledRules != null) {
            disabledRulesList.addAll(Arrays.asList(disabledRules.split(", ")));
        }

        List<CategoryId> enabledCategoriesList = new ArrayList<>();
        List<String> enabledCategoriesListStrings = new ArrayList<>();
        if (enabledCategories != null) {
            enabledCategoriesListStrings.addAll(Arrays.asList(enabledCategories.split(", ")));
        }
        for (String stringId : enabledCategoriesListStrings) {
            enabledCategoriesList.add(new CategoryId(stringId));
        }


        List<CategoryId> disabledCategoriesList = new ArrayList<>();
        List<String> disabledCategoriesListStrings = new ArrayList<>();
        if (disabledCategories != null) {
            disabledCategoriesListStrings.addAll(Arrays.asList(disabledCategories.split(", ")));
        }
        for (String stringId : disabledCategoriesListStrings) {
            disabledCategoriesList.add(new CategoryId(stringId));
        }

        if ((disabledRulesList.size() > 0 || disabledCategoriesList.size() > 0) && enabledOnly) {
            throw new IllegalArgumentException("You cannot specify disabled rules or categories using enabledOnly=true");
        }
        if (enabledRulesList.size() == 0 && enabledCategoriesList.size() == 0 && enabledOnly) {
            throw new IllegalArgumentException("You must specify enabled rules or categories when using enabledOnly=true");
        }

        boolean useQuerySettings = enabledRulesList.size() > 0 || disabledRulesList.size() > 0 ||
                enabledCategoriesList.size() > 0 || disabledCategoriesList.size() > 0;
        boolean allowIncompleteResults = false;
        QueryParams params = new QueryParams(enabledRulesList, disabledRulesList, enabledCategoriesList, disabledCategoriesList, enabledOnly, useQuerySettings, allowIncompleteResults);

        List<RuleMatch> ruleMatchesSoFar = Collections.synchronizedList(new ArrayList<>());

        ;


        Future<List<RuleMatch>> future = executorService.submit(new Callable<List<RuleMatch>>() {
            @Override
            public List<RuleMatch> call() throws Exception {
                return getRuleMatches(aText, lang, motherTongueLanguage, params, f -> ruleMatchesSoFar.add(f));
            }
        });
        String incompleteResultReason = null;
        List<RuleMatch> matches;
        if (limits.getMaxCheckTimeMillis() < 0) {
            try {
                matches = future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                matches = future.get(limits.getMaxCheckTimeMillis(), TimeUnit.MILLISECONDS);
            } catch (ExecutionException e) {
                if (params.allowIncompleteResults && ExceptionUtils.getRootCause(e) instanceof ErrorRateTooHighException) {
                    System.out.println(e.getMessage() + " - returning " + ruleMatchesSoFar.size() + " matches found so far");
                    matches = new ArrayList<>(ruleMatchesSoFar);  // threads might still be running, so make a copy
                    incompleteResultReason = "Results are incomplete: " + ExceptionUtils.getRootCause(e).getMessage();
                } else if (e.getCause() != null && e.getCause() instanceof OutOfMemoryError) {
                    throw (OutOfMemoryError) e.getCause();
                } else {
                    try {
                        throw e;
                    } catch (ExecutionException e1) {
                        throw new RuntimeException(e1);
                    }
                }
            } catch (TimeoutException e) {
                boolean cancelled = future.cancel(true);
                Path loadFile = Paths.get("/proc/loadavg");  // works in Linux only(?)
                String loadInfo = null;
                try {
                    loadInfo = loadFile.toFile().exists() ? Files.readAllLines(loadFile).toString() : "(unknown)";
                } catch (IOException e1) {
                    throw new RuntimeException(e1);
                }

                String message = "Text checking took longer than allowed maximum of " + limits.getMaxCheckTimeMillis() +
                        " milliseconds (cancelled: " + cancelled +
                        ", language: " + lang.getShortCodeWithCountryAndVariant() + ", #-1" +
                        ", " + aText.getPlainText().length() + " characters of text, system load: " + loadInfo + ")";
                if (params.allowIncompleteResults) {
                    System.out.println(message + " - returning " + ruleMatchesSoFar.size() + " matches found so far");
                    matches = new ArrayList<>(ruleMatchesSoFar);  // threads might still be running, so make a copy
                    incompleteResultReason = "Results are incomplete: text checking took longer than allowed maximum of " +
                            String.format(Locale.ENGLISH, "%.2f", limits.getMaxCheckTimeMillis() / 1000.0) + " seconds";
                } else {
                    throw new RuntimeException(message, e);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }


        List<RuleMatch> hiddenMatches = new ArrayList<>();

        String messageSent = "sent";
        String languageMessage = lang.getShortCodeWithCountryAndVariant();
        String referrer = null;

        if (motherTongue != null) {
            languageMessage += " (mother tongue: " + motherTongueLanguage.getShortCodeWithCountryAndVariant() + ")";
        }
        if (autoDetectLanguage) {
            languageMessage += "[auto]";
        }
        log.info("Check done: " + aText.getPlainText().length() + " chars, " + languageMessage + ", #-1, " + referrer + ", "
                + matches.size() + " matches, " + (System.currentTimeMillis() - timeStart) + "ms, agent:-, " + messageSent + ", q:?");

        SoftwareDTO softwareDTO = new SoftwareDTO("LanguageTool", JLanguageTool.VERSION, JLanguageTool.BUILD_DATE, 1, "");
        LanguageDTO languageDTO = new LanguageDTO(lang.getName(), lang.getShortCodeWithCountryAndVariant(), null);
        List<MatchDTO> matchesList = new LinkedList<>();


        for (RuleMatch match : matches){
            ContextTools contextTools = new ContextTools();
            contextTools.setEscapeHtml(false);
            contextTools.setContextSize(CONTEXT_SIZE);
            contextTools.setErrorMarkerStart(START_MARKER);
            contextTools.setErrorMarkerEnd("");

            String context = contextTools.getContext(match.getFromPos(), match.getToPos(), text);
            int contextOffset = context.indexOf(START_MARKER);
            context = context.replaceFirst(START_MARKER, "");

            List<ReplacementDTO> replacementDTOS = new ArrayList<>();
            for (String replacement : match.getSuggestedReplacements()) {
                replacementDTOS.add(new ReplacementDTO(replacement));
            }

            List<UrlDTO> urls = null;
            if (match.getUrl() != null || match.getRule().getUrl() != null) {
                urls = new ArrayList<>();
                if (match.getUrl() != null) {
                    urls.add(new UrlDTO(match.getUrl().toString()));
                } else {
                    urls.add(new UrlDTO(match.getRule().getUrl().toString()));
                }
            }

            CategoryDTO categoryDTO = new CategoryDTO(null, null);
            if (match.getRule().getCategory().getId() != null){
                categoryDTO = new CategoryDTO(match.getRule().getCategory().getId().toString(), match.getRule().getCategory().getName());
            }
            RuleDTO ruleDTO = new RuleDTO(
                    match.getRule().getId(),
                    match.getRule() instanceof AbstractPatternRule ? ((AbstractPatternRule) match.getRule()).getSubId() : null,
                    match.getRule().getDescription(),
                    urls,
                    match.getRule().getLocQualityIssueType().toString(),
                    categoryDTO
                    );

            matchesList.add(new MatchDTO(
                    cleanSuggestion(match.getMessage()),
                    (match.getShortMessage() == null ? null : cleanSuggestion(match.getShortMessage())), match.getFromPos(),
                    match.getToPos()-match.getFromPos(), replacementDTOS,
                    new ContextDTO(context, contextOffset, match.getToPos()-match.getFromPos()),
                    match.getSentence() == null ? null : match.getSentence().getText().trim(),
                    ruleDTO
                    ));
        }
        CheckResultDTO resultDTO = new CheckResultDTO(softwareDTO, languageDTO, matchesList);

        return resultDTO;
    }

    Language detectLanguageOfString(String text, String fallbackLanguage, List<String> preferredVariants) {
        Language lang = identifier.detectLanguage(text);
        if (lang == null) {
            lang = Languages.getLanguageForShortCode(fallbackLanguage != null ? fallbackLanguage : "en");
        }
        if (preferredVariants.size() > 0) {
            for (String preferredVariant : preferredVariants) {
                if (!preferredVariant.contains("-")) {
                    throw new IllegalArgumentException("Invalid format for 'preferredVariants', expected a dash as in 'en-GB': '" + preferredVariant + "'");
                }
                String preferredVariantLang = preferredVariant.split("-")[0];
                if (preferredVariantLang.equals(lang.getShortCode())) {
                    lang = Languages.getLanguageForShortCode(preferredVariant);
                    if (lang == null) {
                        throw new IllegalArgumentException("Invalid 'preferredVariants', no such language/variant found: '" + preferredVariant + "'");
                    }
                }
            }
        } else {
            if (lang.getDefaultLanguageVariant() != null) {
                lang = lang.getDefaultLanguageVariant();
            }
        }
        return lang;
    }


    private List<RuleMatch> getRuleMatches(AnnotatedText aText, Language lang,
                                           Language motherTongue, QueryParams params, RuleMatchListener listener) throws Exception {

        JLanguageTool lt = new JLanguageTool(lang, motherTongue, null);
        lt.setMaxErrorsPerWordRate(0);
        if (params.useQuerySettings) {
            Tools.selectRules(lt, new HashSet<>(params.disabledCategories), new HashSet<>(params.enabledCategories),
                    new HashSet<>(params.disabledRules), new HashSet<>(params.enabledRules), params.useEnabledOnly);
        }
        return lt.check(aText, listener);
    }

    private String cleanSuggestion(String s) {
        return s.replace("<suggestion>", "\"").replace("</suggestion>", "\"");
    }


    private static class QueryParams {
        final List<String> enabledRules;
        final List<String> disabledRules;
        final List<CategoryId> enabledCategories;
        final List<CategoryId> disabledCategories;
        final boolean useEnabledOnly;
        final boolean useQuerySettings;
        final boolean allowIncompleteResults;

        QueryParams(List<String> enabledRules, List<String> disabledRules, List<CategoryId> enabledCategories, List<CategoryId> disabledCategories,
                    boolean useEnabledOnly, boolean useQuerySettings, boolean allowIncompleteResults) {
            this.enabledRules = enabledRules;
            this.disabledRules = disabledRules;
            this.enabledCategories = enabledCategories;
            this.disabledCategories = disabledCategories;
            this.useEnabledOnly = useEnabledOnly;
            this.useQuerySettings = useQuerySettings;
            this.allowIncompleteResults = allowIncompleteResults;
        }
    }
}

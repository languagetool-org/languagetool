
## LanguageTool Evaluation on the Pedler Corpus

2014-07-19

### False Alarms

* Running `RealWordCorpusEvaluator` yields 75 false alarms (i.e. marked with `[  ] <rule_id>`) on the Pedler corpus:
    * Of these, 41 are spell checker matches (MORFOLOGIK_RULE_EN_GB):
        * 17 are proper nouns unknown in the speller dictionary (Teleny, Raskoloniknov, Gregor, ...)
        * 10 are caused be the spell checker being strict about compounds (sidecar vs side-car, wellbeing vs well-being, ...)
        * 7 are words that are probably actually missing from the dictionary (aesthetisize, indiscrete, tonner, ...)
        * 3 are literals ("ddd", Boojum, Snark)
        * 2 are colloquial or obsolete words (agro, Incidently)
        * 2 are US English while the check uses en-GB (movie, math)
    * Of these, 12 are CONFUSION_RULE (7 when also using 3grams)
    * The other 22 are different rules with no clear pattern

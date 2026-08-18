This dictionary for spell-checking Polish texts is licensed under
GPL, LGPL, MPL (Mozilla Public License) and Creative Commons
ShareAlike licenses (see http://creativecommons.org/licenses/sa/1.0).

This version of the dictionary was generated on 2026.08.12 from the
official sjp.pl ispell/myspell sources (sjp-ispell-pl-20260803), with
local fixes. The 2026 joined-spelling rule for "nie" is corrected in two spots:
   - the "b" (nie-) affix flag is stripped from 392 comparative/
     superlative adverb bases whose joined forms are unattested or
     collide with live verb imperatives (e.g. `niebardziej`,
     `niemdlej`, `niełysiej`) — the same class the sjp maintainer has
     already started excluding upstream; note that plain "nie of nie"
     words (e.g. `nieniepokojony`) are rule-correct negations of
     lexemes that begin with the letters "nie" and are kept;
  - 437 edit-typo artifact heads unknown to any corpus or the
    morphological tagger are dropped (`dco`, `kego`, `sracy`).

Hyphenated forms are retained as whole dictionary entries. The Hunspell
release uses `BREAK 0`, preventing arbitrary hyphenation from being accepted
merely because both parts are valid words; its 34,639 listed hyphenated forms
are restored from the previous LanguageTool dictionary. This matches the
whole-token lookup performed by the Morfologik speller.

The word-frequency data used for the LanguageTool dictionary is a
max-merge of:
  - NKJP 1-grams (2012, balanced 300M-token corpus, CC BY),
  - the KWJP100 frequency lists
    (https://github.com/ipipan/kwjp100-varia, modern balanced corpus),
  - the Leipzig Corpora Collection pol_news_2023_1M (CC BY-4.0).
Punctuation-attached corpus tokens are stripped and accumulated into
the bare word (e.g. "żart," / "żart!" -> "żart").

Dictionary maintainer: Marek Futrega (futrega@gmail.com)
Corrections: Marcin Miłkowski

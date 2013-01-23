@ECHO off
:: Check command line parameters
IF (%5)==() GOTO usage
IF (%4)==() GOTO usage
IF (%3)==() GOTO usage
IF (%2)==() GOTO usage
IF (%1)==() GOTO usage
IF (%1)==(?) GOTO usage
IF (%1)==(/?) GOTO usage
IF (%1)==(-?) GOTO usage
IF (%1)==(--?) GOTO usage
IF (%1)==(/help) GOTO usage
IF (%1)==(-help) GOTO usage
IF (%1)==(--help) GOTO usage

IF (%1)==(de) CHCP 1252
java -Xmx512M -cp commons-lang-2.4.jar;bliki-3.0.3.jar;LanguageTool.jar org.languagetool.dev.wikipedia.CheckWikipediaDump - disabled_rules.txt %1 %2 %3 %4 %5
GOTO eof

:usage
ECHO Usage: %0 lang wikipediaXmlDump ruleIds maxNumArticles maxErrors
ECHO Where:
ECHO - lang is a language code such as 'en' or 'de'
ECHO - wikipediaXmlDump is the path to an unpacked Wikipedia XML dump
ECHO - ruleIds is a comma-separated list of rules to be activated, or '-' for the default rules
ECHO - maxNumArticles is the maximum number of articles to check, 0 means no limit
ECHO - maxErrors is the maximum number of errors (more errors will stop), 0 means no limit
ECHO Examples:
ECHO - %0 de dewiki-20111012-pages-articles-partly.xml - 0 100
ECHO - %0 de dewiki-20111012-pages-articles-partly.xml GROSSER_STIEL 50 0 0
ECHO - %0 de dewiki-20111012-pages-articles-partly.xml "GROSSER_STIEL,BISSTRICH" 0 0

:eof
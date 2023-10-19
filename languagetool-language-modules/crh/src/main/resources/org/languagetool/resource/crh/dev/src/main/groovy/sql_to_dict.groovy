#!/bin/env groovy

File out = new File("chr_dict.txt")
out.text = ''

// brute-force reading sql dump

def spaced = new File("spaced.txt")
spaced.text = ""

new File("language_tool_crh_wordbase.sql").eachLine { String line ->
    if( ! line.startsWith("INSERT INTO") )
        return

    line = line.replaceFirst(/^INSERT INTO `language_tool_crh_word_base` VALUES \(\d+, */, '')
    line = line.replaceFirst(/\); *$/, '')

    def parts = line.split(',') \
        .collect {
            it.replaceAll(/^'|'$/, '')
        }

    String posTag = parts[5]
    String form  = parts[2]

    // TODO: I don't know what to do with spaced forms
    if( form.contains(" ") || parts[0].contains(" ") ) {
        spaced << form + '\t' + parts[0] + '\t' + posTag << "\n"
        return
    }
//    form = form.replaceFirst(/^eñ /, '')

    out << form + '\t' + parts[0] + '\t' + posTag << "\n"
}


/*
 0  `word` varchar(228) CHARACTER SET utf8 NOT NULL DEFAULT '',
 1  `language_id` int(11) DEFAULT NULL,
 2  `wordform` varchar(245) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
 3  `transcription` varchar(245) CHARACTER SET utf8 COLLATE utf8_turkish_ci DEFAULT NULL,
 4  `tense_mark` varchar(45) DEFAULT NULL,
 5  `part_of_speech` varchar(45) CHARACTER SET utf8 DEFAULT NULL,
 6  `tense` varchar(45) DEFAULT NULL,
 7  `tense_minor` varchar(45) DEFAULT NULL,
 8  `mood` varchar(45) DEFAULT NULL,
 9  `person` varchar(45) DEFAULT NULL,
10  `plurality` varchar(45) DEFAULT NULL,
  `actor_sex` varchar(45) CHARACTER SET utf8 DEFAULT NULL,
  `casuality` varchar(45) DEFAULT NULL,
  `activeness` varchar(45) DEFAULT NULL,
  `comparison_degree` varchar(45) CHARACTER SET utf8 DEFAULT NULL,
  `transitivity` varchar(45) DEFAULT NULL,
  `perfectness` varchar(45) DEFAULT NULL,
  `animacy` varchar(45) DEFAULT NULL,
  `coloquiality` varchar(45) DEFAULT NULL,
  `adverb_variant` varchar(45) DEFAULT NULL,
  `adjective_variant` varchar(45) DEFAULT NULL,
  `comparative_variant` varchar(45) DEFAULT NULL,
  `gerund_variation` varchar(45) DEFAULT NULL,
  `numerable_variant` varchar(45) DEFAULT NULL,
  `preposition_variant` varchar(45) DEFAULT NULL,
  `pronoun_variant` varchar(45) DEFAULT NULL,
  `suffix_variant` varchar(45) DEFAULT NULL,
  `count_form` varchar(45) DEFAULT NULL,
  `short_form` varchar(45) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL,
  `is_abbreviation` varchar(45) DEFAULT NULL,
  `is_archaism` varchar(45) DEFAULT NULL,
  `is_distorted` varchar(45) DEFAULT NULL,
  `is_error` varchar(45) DEFAULT NULL,
  `is_injected_word` varchar(45) DEFAULT NULL,
  `is_name` varchar(45) DEFAULT NULL,
  `is_negative` varchar(45) DEFAULT NULL,
  `is_toponym` varchar(45) DEFAULT NULL,
  `is_isafet` varchar(45) DEFAULT NULL,
  `is_possessive` varchar(45) DEFAULT NULL,
  `possession_person` varchar(45) DEFAULT NULL,
  `possession_plurality` varchar(45) DEFAULT NULL,
  `noun_sex` varchar(45) DEFAULT NULL,
  `conjunction_type` varchar(45) DEFAULT NULL,
  `preposition_type` varchar(45) DEFAULT NULL,
  `preposition_mood` varchar(45) DEFAULT NULL,
  `particle_type` varchar(45) DEFAULT NULL,
  `custom_attribute1` varchar(45) DEFAULT NULL
*/
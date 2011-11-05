package de.danielnaber.languagetool.dev.conversion.cg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;

import de.danielnaber.languagetool.dev.conversion.cg.CgSet.ST;
import de.danielnaber.languagetool.dev.conversion.cg.CgStrings.KEYWORDS;
import de.danielnaber.languagetool.dev.conversion.cg.CgStrings.STRINGS;
import de.danielnaber.languagetool.dev.conversion.cg.CgContextualTest.POS;
import de.danielnaber.languagetool.dev.conversion.cg.CgStrings.SFLAGS;
import de.danielnaber.languagetool.dev.conversion.cg.CgRule.RFLAGS;

public class CgTextualParser {
    
	// TODO: although this method mostly works, it needs a lot of cleanup and checking before it
	// can be considered a good port of the C++ VISL CG3 TextualParser class
	
    // variables
    private int verbosity_level;
    private int sets_counter;
    private int seen_mapping_prefix;
    private boolean option_vislcg_compat;
    private boolean in_section, in_before_sections, in_after_sections, in_null_section;
//    private String filename;
//    private String locale;
//    private String codepage;
    
    private CgGrammar result;
    // private File ux_stderr;
    
    // the character array of the input stream
    private char[] inArray;
    // the index (acts like the pointer to the current character in the C++ code)
    private int index =0;
    private int length;
    private int nindex; // a secondary index "pointer"
    private int sindex; // a ternary index "pointer"
    private int lpindex; // a quaternary index "pointer"

    // global variables for parsing contextual tests
    private CgContextualTest currentTest;
    private CgContextualTest parentTest;
    private boolean inLinkedTest;
    private ArrayList<Integer> linkedTests = new ArrayList<Integer>();
    private boolean inParentTest;
    
    // constructor
    public CgTextualParser(CgGrammar result, File file) {
        this.result = result;
        // must reinclude this if we decide to let it write to a file
        // this.ux_stderr = file;	
        option_vislcg_compat = false;
        in_after_sections = false;
        in_before_sections = false;
        in_null_section = false;
        in_section = false;
        verbosity_level = 0;
        seen_mapping_prefix = 0;
        sets_counter = 100;
    }
    
    public void setCompatible(boolean compat) {
        option_vislcg_compat = compat;
    }
    
    public void setVerbosity(int level) {
        verbosity_level = level;
    }
    
    public CgGrammar getGrammar() {
        return result;
    }
    
    // TODO: support for various locales; right now it can read UTF-8 fine (e.g. in the icelandic rules file)
    // but it should have explicit allowances for encoding and locale
    public int parse_grammar_from_file(final String filename, final String locale, final String codepage) {
        if (result == null) {
            System.err.println("Grammar hasn't been initialized. Make this happen.");
            System.exit(1);
        }
        
        // open and read in the grammar.txt file
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        // put some buffer space at the beginning, just to be sure
        sb.append("    ");
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
            int c = reader.read();
            while (c != -1) {
                sb.append((char)c);
                c = reader.read();
            }
            inArray = sb.toString().toCharArray();
            reader.close();
        } catch (IOException e) {
            System.err.println("Error opening grammar file");
            System.exit(1);
        } 
        // the index starts at the beginning of the file, after the buffer of spaces
        index = 4;
        length = inArray.length;
                
        result.addAnchor(KEYWORDS.K_START.name(), result.lines);
        
        // allocate magic tags and sets
        {
            CgTag tany = result.allocateTag(stringbits[S_ASTERISK.value], false);
            result.tag_any = tany.hash;   
        }
        {
            CgSet set_c = result.allocateSet();
            set_c.line = 0;
            set_c.setName(stringbits[STRINGS.S_UU_TARGET.value]);
            CgTag t = result.allocateTag(stringbits[STRINGS.S_UU_TARGET.value],false);
            result.addTagToSet(t, set_c);
            result.addSet(set_c);   
        }
        {
            CgSet set_c = result.allocateSet();
            set_c.line = 0;
            set_c.setName(stringbits[STRINGS.S_UU_MARK.value]);
            CgTag t = result.allocateTag(stringbits[STRINGS.S_UU_MARK.value],false);
            result.addTagToSet(t, set_c);
            result.addSet(set_c);   
        }
        CgSet s_right = null;
        {
            CgSet set_c = result.allocateSet();
            set_c.line = 0;
            set_c.setName(stringbits[STRINGS.S_UU_ATTACHTO.value]);
            CgTag t = result.allocateTag(stringbits[STRINGS.S_UU_ATTACHTO.value],false);
            result.addTagToSet(t, set_c);
            result.addSet(set_c);   
            s_right = set_c;
        }
        CgSet s_left = null;
        {
            CgSet set_c = result.allocateSet();
            set_c.line = 0;
            set_c.setName(stringbits[STRINGS.S_UU_LEFT.value]);
            CgTag t = result.allocateTag(stringbits[STRINGS.S_UU_LEFT.value],false);
            result.addTagToSet(t, set_c);
            result.addSet(set_c);
            s_left = set_c;
            
        }
        {
            CgSet set_c = result.allocateSet();
            set_c.line = 0;
            set_c.setName(stringbits[STRINGS.S_UU_RIGHT.value]);
            CgTag t = result.allocateTag(stringbits[STRINGS.S_UU_RIGHT.value],false);
            result.addTagToSet(t, set_c);
            result.addSet(set_c);   
        }
        {
            CgSet set_c = result.allocateSet();
            set_c.line = 0;
            set_c.setName(stringbits[STRINGS.S_UU_PAREN.value]);
            set_c.set_ops.add(STRINGS.S_OR.value);
            set_c.sets.add(s_left.hash);
            set_c.sets.add(s_right.hash);
            result.addSet(set_c);
        }
        
        int error = parseFromChar(filename);	// really the main method of the parsing
        if (error != 0) {
            return error;
        }
        result.addAnchor("end", result.lines);
        
        return 0;
    }
    
    /** 
     * Returns true until we're done parsing the grammar file
     * @return
     */
    private boolean notDone() {
        return index < length;
    }
    
    /**
     * The main parsing method; starts at the beginning of the file (index = 4 because of the buffer)
     * and goes until it's done
     * @param fname
     * @return 0 if successfully parses file
     */
    private int parseFromChar(String fname) {
        if (index >= length) {
            System.err.println("No input stream or input stream is empty");
            System.exit(1);
        }
        // the main loop
        while (notDone()) {
            if (verbosity_level > 0 && result.lines % 500 == 0) {
                System.out.println("Parsing line " + result.lines);
            }
            result.lines += SKIPWS((char)0, (char)0);
            if (index >= length) {
                break;
            }
            // DELIMITERS
            if (ISCHR(index,0,'D','d') && ISCHR(index,9,'S','s') && ISCHR(index,1,'E','e') && ISCHR(index,2,'L','l') &&
                    ISCHR(index,3,'I','i') && ISCHR(index,4,'M','m') && ISCHR(index,5,'I','i') && ISCHR(index,6,'T','t') &&
                    ISCHR(index,7,'E','e') && ISCHR(index,8,'R','r') && !ISSTRING(index,9)) {
                if (result.delimiters != null) {
                    System.err.println("Cannot redefine delimiters on line " + result.lines);
                    System.exit(1);
                }
                CgSet delimiters = new CgSet();
                delimiters.line = result.lines;
                delimiters.setName(stringbits[STRINGS.S_DELIMITSET.value]);
                index += 10;
                result.lines += SKIPWS('=',(char)0);
                if (inArray[index] != '=') {
                    System.err.println("Error encountered before the expected = on line " + result.lines);
                    System.exit(1);
                }
                ++index;
                delimiters = parseTagList(delimiters, false);
                result.addSet(delimiters);
                if (result.delimiters.tags.isEmpty() && result.delimiters.single_tags.isEmpty()) {
                    System.err.println("Error: Delimiters declared, but line empty");
                    System.exit(1);
                }
                result.lines += SKIPWS(';',(char)0);
                if (inArray[index] != ';') {
                    System.err.println("Error: missing ; to end line");
                    System.exit(1);
                }
            } 
            // SOFT-DELIMITERS
            else if (ISCHR(index,0,'S','s') && ISCHR(index,14,'S','s') && ISCHR(index,1,'O','o') && ISCHR(index,2,'F','f')
                    && ISCHR(index,3,'T','t') && ISCHR(index,4,'-','_') && ISCHR(index,5,'D','d') && ISCHR(index,6,'E','e')
                    && ISCHR(index,7,'L','l') && ISCHR(index,8,'I','i') && ISCHR(index,9,'M','m') && ISCHR(index,10,'I','i')
                    && ISCHR(index,11,'T','t') && ISCHR(index,12,'E','e') && ISCHR(index,13,'R','r') && !ISSTRING(index,14)) {
                if (result.soft_delimiters != null) {
                    System.err.println("Cannot redefine soft delimiters on line " + result.lines);
                    System.exit(1);
                }
                CgSet soft_delimiters = new CgSet();
                soft_delimiters.line = result.lines;
                soft_delimiters.setName(stringbits[STRINGS.S_SOFTDELIMITSET.value]);
                index += 15;
                result.lines += SKIPWS('=',(char)0);
                if (inArray[index] != '=') {
                    System.err.println("Error encountered before the expected = on line " + result.lines);
                    System.exit(1);
                }
                ++index;
                soft_delimiters = parseTagList(soft_delimiters,false);
                result.addSet(soft_delimiters);
                if (result.soft_delimiters.tags.isEmpty() && result.soft_delimiters.single_tags.isEmpty()) {
                    System.err.println("Error: Soft-delimiters declared, but line is empty");
                    System.exit(1);
                }
                result.lines += SKIPWS(';',(char)0);
                if (inArray[index] != ';') {
                    System.err.println("Missing closing ; on line " + result.lines);
                    System.exit(1);
                }
            } 
            // MAPPING-PREFIX
            else if (ISCHR(index,0,'M','m') && ISCHR(index,13,'X','x') && ISCHR(index,1, 'A', 'a') && ISCHR(index,2, 'P', 'p')
                    && ISCHR(index,3,'P','p') && ISCHR(index,4, 'I','i') && ISCHR(index,5, 'N', 'n') && ISCHR(index,6, 'G', 'g')
                    && ISCHR(index,7, '-', '_') && ISCHR(index,8, 'P', 'p') && ISCHR(index,9, 'R', 'r') && ISCHR(index,10, 'E', 'e') 
                    && ISCHR(index,11, 'F', 'f') && ISCHR(index,12, 'I', 'i') && !ISSTRING(index,13)) {
                if (seen_mapping_prefix != 0) {     // not sure about this identity check
                        System.err.println("Error: saw mapping prefix on line " + seen_mapping_prefix + ", cannot change.");
                        System.exit(1);
                }
                seen_mapping_prefix = result.lines;
                index += 14;
                result.lines += SKIPWS('=',(char)0);
                if (inArray[index] != '=') {
                    System.err.println("Error encountered before expected = on line " + result.lines);
                    System.exit(1);
                }
                ++index;
                result.lines += SKIPWS((char)0,(char)0);
                nindex = index; // set the secondary pointer
                result.lines += SKIPTOWS_N(';',false);
                StringBuilder mapping_prefix = new StringBuilder();
                for (int i = index; i<nindex; i++) {
                    mapping_prefix.append(inArray[i]);
                }
                result.mapping_prefix = mapping_prefix.toString();
                index = nindex;
                if (result.mapping_prefix == null || result.mapping_prefix == "") {
                    System.err.println("Error: mapping prefix declared on line " + result.lines + " but no definition given");
                    System.exit(1);
                }
                result.lines += SKIPWS(';',(char)0);
                if (inArray[index] != ';') {
                    System.err.println("Missing closing ; at line " + result.lines);
                    System.exit(1);
                }                
            }
            // PREFERRED-TARGETS
            else if (ISCHR(index,0,'P','p') && ISCHR(index,16,'S','s') && ISCHR(index,1,'R','r') && ISCHR(index,2,'E','e') && ISCHR(index,3,'F','f')
                    && ISCHR(index,4,'E','e') && ISCHR(index,5,'R','r') && ISCHR(index,6,'R','r') && ISCHR(index,7,'E','e')
                    && ISCHR(index,8,'D','d') && ISCHR(index,9,'-','_') && ISCHR(index,10,'T','t') && ISCHR(index,11,'A','a') && ISCHR(index,12,'R','r')
                    && ISCHR(index,13,'G','g') && ISCHR(index,14,'E','e') && ISCHR(index,15,'T','t') && !ISSTRING(index,16)) {
                index += 17;
                result.lines += SKIPWS('=',(char)0);
                if (inArray[index] != '=') {
                    System.err.println("Error encountered before expected = on line " + result.lines);
                    System.exit(1);
                }
                ++index;
                result.lines += SKIPWS((char)0,(char)0);
                while (notDone() && inArray[index] != ';') {
                    nindex = index;
                    if (inArray[nindex] == '"') {
                        nindex++;
                        result.lines += SKIPTO_NOSPAN_N('"');
                        if (inArray[nindex] != '"') {
                            System.err.println("Error, missing closing \" on line " + result.lines);
                            System.exit(1);
                        }
                    }
                    result.lines += SKIPTOWS_N(';',true);
                    StringBuilder preferred_targets = new StringBuilder();
                    for (int i=index;i<nindex;i++) {
                        preferred_targets.append(inArray[i]);
                    }
                    CgTag t = result.allocateTag(preferred_targets.toString(), false);
                    result.preferred_targets.add(t.hash);
                    index = nindex;
                    result.lines += SKIPWS((char)0,(char)0);
                }
                if (result.preferred_targets.isEmpty()) {
                    System.err.println("Preferred targets declared, but no definition given on line " + result.lines);
                    System.exit(1);
                }
                result.lines += SKIPWS(';', (char)0);
                if (inArray[index] != ';') {
                    System.err.println("No closing ; at the end of line " + result.lines);
                    System.exit(1);
                }
            }
            // STATIC-SETS
            else if (ISCHR(index,0,'S','s') && ISCHR(index,10,'S','s') && ISCHR(index,1,'T','t') && ISCHR(index,2,'A','a') && ISCHR(index,3,'T','t')
                    && ISCHR(index,4,'I','i') && ISCHR(index,5,'C','c') && ISCHR(index,6,'-','_') && ISCHR(index,7,'S','s') && ISCHR(index,8,'E','e')
                    && ISCHR(index,9,'T','t') && !ISSTRING(index,10)) {
                index += 11;
                result.lines += SKIPWS((char)0,(char)0);
                
                while (notDone() && inArray[index] != ';') {
                    nindex = index;
                    result.lines += SKIPTOWS_N(';', true);
                    StringBuilder static_sets = new StringBuilder();
                    for (int i=index;i<nindex;i++) {
                        static_sets.append(inArray[i]);
                    }
                    result.static_sets.add(static_sets.toString());
                    index = nindex;
                    result.lines += SKIPWS((char)0,(char)0);
                }
                if (result.static_sets.isEmpty()) {
                    System.err.println("Error: static sets declared on line " + result.lines + " but no definitions given");
                    System.exit(1);
                }
                result.lines += SKIPWS(';',(char)0);
                if (inArray[index] != ';') {
                    System.err.println("Error: missing the closing ; at the end of line " + result.lines);
                    System.exit(1);
                }
            }
            // ADDRELATIONS
            else if (ISCHR(index,0,'A','a') && ISCHR(index,11,'S','s') && ISCHR(index,1,'D','d') && ISCHR(index,2,'D','d') && ISCHR(index,3,'R','r') &&
                    ISCHR(index,4,'E','e') && ISCHR(index,5,'L','l') && ISCHR(index,6,'A','a') && ISCHR(index,7,'T','t') && ISCHR(index,8,'I','i') &&
                    ISCHR(index,9,'O','o') && ISCHR(index,10,'N','n') && !ISSTRING(index,11)) {
                parseRule(KEYWORDS.K_ADDRELATIONS);
            }
            // SETRELATIONS
            else if (ISCHR(index,0,'S','s') && ISCHR(index,11,'S','s') && ISCHR(index,1,'E','e') && ISCHR(index,2,'T','t') && ISCHR(index,3,'R','r') &&
                        ISCHR(index,4,'E','e') && ISCHR(index,5,'L','l') && ISCHR(index,6,'A','a') && ISCHR(index,7,'T','t') && ISCHR(index,8,'I','i') &&
                        ISCHR(index,9,'O','o') && ISCHR(index,10,'N','n') && !ISSTRING(index,11)) {
                parseRule(KEYWORDS.K_SETRELATIONS);
            }
            // REMRELATIONS
            else if (ISCHR(index,0,'R','r') && ISCHR(index,11,'S','s') && ISCHR(index,1,'E','e') && ISCHR(index,2,'M','m') && ISCHR(index,3,'R','r') &&
                        ISCHR(index,4,'E','e') && ISCHR(index,5,'L','l') && ISCHR(index,6,'A','a') && ISCHR(index,7,'T','t') && ISCHR(index,8,'I','i') &&
                        ISCHR(index,9,'O','o') && ISCHR(index,10,'N','n') && !ISSTRING(index,11)) {
                parseRule(KEYWORDS.K_REMRELATIONS);
            }
            // ADDRELATION
            else if (ISCHR(index,0,'A','a') && ISCHR(index,10,'N','n') && ISCHR(index,1,'D','d') && ISCHR(index,2,'D','d') && ISCHR(index,3,'R','r') &&
                        ISCHR(index,4,'E','e') && ISCHR(index,5,'L','l') && ISCHR(index,6,'A','a') && ISCHR(index,7,'T','t') && ISCHR(index,8,'I','i') &&
                        ISCHR(index,9,'O','o') && !ISSTRING(index,10)) {
                parseRule(KEYWORDS.K_ADDRELATION);
            }
            // SETRELATION
            else if (ISCHR(index,0,'S','s') && ISCHR(index,10,'N','n') && ISCHR(index,1,'E','e') && ISCHR(index,2,'T','t') && ISCHR(index,3,'R','r') &&
                        ISCHR(index,4,'E','e') && ISCHR(index,5,'L','l') && ISCHR(index,6,'A','a') && ISCHR(index,7,'T','t') && ISCHR(index,8,'I','i') &&
                        ISCHR(index,9,'O','o') && !ISSTRING(index,10)) {
                parseRule(KEYWORDS.K_SETRELATION);
            }
            // REMRELATION
            else if (ISCHR(index,0,'R','r') && ISCHR(index,10,'N','n') && ISCHR(index,1,'E','e') && ISCHR(index,2,'M','m') && ISCHR(index,3,'R','r') &&
                        ISCHR(index,4,'E','e') && ISCHR(index,5,'L','l') && ISCHR(index,6,'A','a') && ISCHR(index,7,'T','t') && ISCHR(index,8,'I','i') &&
                        ISCHR(index,9,'O','o') && !ISSTRING(index,10)) {
                parseRule(KEYWORDS.K_REMRELATION);
            }
            // SETPARENT
            else if (ISCHR(index,0,'S','s') && ISCHR(index,8,'T','t') && ISCHR(index,1,'E','e') && ISCHR(index, 2, 'T','t') && ISCHR(index,3,'P','p') &&
                        ISCHR(index,4,'A','a') && ISCHR(index,5,'R','r') && ISCHR(index,6,'E','e') && ISCHR(index,7,'N','n') && !ISSTRING(index,8)) {
                parseRule(KEYWORDS.K_SETPARENT);
            }
            // SETCHILD
            else if (ISCHR(index,0,'S','s') && ISCHR(index,7,'D','d') && ISCHR(index,1,'E','e') && ISCHR(index,2,'T','t') && ISCHR(index,3,'C','c') &&
                        ISCHR(index,4,'H','h') && ISCHR(index,5,'I','i') && ISCHR(index,6,'L','l') && !ISSTRING(index,7)) {
                parseRule(KEYWORDS.K_SETCHILD);
            }
            // EXTERNAL
            else if (ISCHR(index,0,'E','e') && ISCHR(index,7,'L','l') && ISCHR(index,1,'X','x') && ISCHR(index,2,'T','t') && ISCHR(index,3,'E','e') &&
                    ISCHR(index,4,'R','r') && ISCHR(index,5,'N','n') && ISCHR(index,6,'A','a') && !ISSTRING(index,7)) {
            parseRule(KEYWORDS.K_EXTERNAL);
            }
            // REMCOHORT
            else if (ISCHR(index,0,'R','r') && ISCHR(index,8,'T','t') && ISCHR(index,1,'E','e') && ISCHR(index, 2, 'M','m') && ISCHR(index,3,'C','c') &&
                    ISCHR(index,4,'O','o') && ISCHR(index,5,'H','h') && ISCHR(index,6,'O','o') && ISCHR(index,7,'R','r') && !ISSTRING(index,8)) {
            parseRule(KEYWORDS.K_REMCOHORT);
            }
            // ADDCOHORT
            else if (ISCHR(index,0,'A','a') && ISCHR(index,8,'T','t') && ISCHR(index,1,'D','d') && ISCHR(index, 2, 'D','d') && ISCHR(index,3,'C','c') &&
                    ISCHR(index,4,'O','o') && ISCHR(index,5,'H','h') && ISCHR(index,6,'O','o') && ISCHR(index,7,'R','r') && !ISSTRING(index,8)) {
            parseRule(KEYWORDS.K_ADDCOHORT);
            }
            // SETS
            else if (ISCHR(index,0,'S','s') && ISCHR(index,3,'S','s') && ISCHR(index,1,'E','e') && ISCHR(index,2,'T','t') && !ISSTRING(index,3)) {
                index += 4;
            }
            // LIST
            else if (ISCHR(index,0,'L','l') && ISCHR(index,3,'T','t') && ISCHR(index,1,'I','i') && ISCHR(index,2,'S','s') && !ISSTRING(index,3)) {
                CgSet set = new CgSet();
                set.line = result.lines;
                index += 4;
                result.lines += SKIPWS((char)0,(char)0);
                nindex = index;
                result.lines += SKIPTOWS_N((char)0, true);
                while (inArray[nindex-1] == ',' || inArray[index-1] == ']') {
                    --nindex;
                }
                StringBuilder list_string = new StringBuilder();
                for (int i=index;i<nindex;i++) {
                    list_string.append(inArray[i]);
                }
                set.setName(list_string.toString());
                index = nindex;
                result.lines += SKIPWS('=', (char)0);
                if (inArray[index] != '=') {
                    System.err.println("Error: encountered something before the expected = on line " + result.lines);
                    System.exit(1);
                }
                ++index;
                set = parseTagList(set,false);
//                set.rehash();
                CgSet temp = result.getSet(set.hash);
                if (temp != null) {
                    if (verbosity_level > 0) {
                        System.out.println("Warning: LIST " + set.name + " was defined twice with the same contents on lines " + set.line + " and " + temp.line);
                    }
                }
                result.addSet(set);
                if (set.tags.isEmpty() && set.single_tags.isEmpty() && set.sets.isEmpty()) {
                    System.err.println("Error: list " + set.name + " is declared, but no definitions are given on line " + result.lines);
                    System.exit(1);
                }
                result.lines += SKIPWS(';',(char)0);
                if (inArray[index] != ';') {
                    System.err.println("Error: missing ; at the end of line " + result.lines);
                    System.exit(1);
                }
            }
            // SET 
            else if (ISCHR(index,0,'S','s') && ISCHR(index,2,'T','t') && ISCHR(index,1,'E','e') && !ISSTRING(index,2)) {
                CgSet s = new CgSet();
                s.line = result.lines;
                index += 3;
                result.lines += SKIPWS((char)0,(char)0);
                nindex = index;
                result.lines += SKIPTOWS_N((char)0, true);
                while (inArray[nindex-1] == ',' || inArray[nindex-1] == ']') {
                    --nindex;
                }
                StringBuilder set_name = new StringBuilder();
                for (int i=index;i<nindex;i++) {
                    set_name.append(inArray[i]);
                }
                String sn = set_name.toString();
                s.setName(sn);
                int sh = sn.hashCode();
                index = nindex;
                result.lines += SKIPWS('=',(char)0);
                if (inArray[index] != '=') {
                    System.err.println("Error encountered before expected = on line " + result.lines);
                    System.exit(1);
                }
                ++index;
                s = parseSetInline(s);
//                s.rehash();
                CgSet temp = result.getSet(s.hash);
                if (temp != null) {
                    if (verbosity_level > 0) {
                        System.out.println("Warning: set " + s.name + " was defined twice with the same contents on lines " + s.line + " and " + temp.line);
                    }
                }
                // something to do with aliasing that i don't know if will be necessary
                else if (s.sets.size() == 1 && !(s.type.contains(ST.ST_TAG_UNIFY))) {
                    temp = result.getSet(s.sets.get(s.sets.size() - 1));
                    if (verbosity_level > 0) {
                        System.out.println("Warning: set " + s.name + "at line " + s.line + " has been aliased to " + temp.name + " at line " + temp.line);
                    }
                    result.set_alias.put(sh, temp.hash);
                    result.destroySet(s);
                    s = temp;
                }
                result.addSet(s);
                if (s.sets.isEmpty() && s.tags.isEmpty() && s.single_tags.isEmpty()) {
                    System.err.println("Error: set " + s.name + " declared on line " + s.line + " but no definition");
                    System.exit(1);
                }
                result.lines += SKIPWS(';',(char)0);
                if (inArray[index] != ';') {
                    System.err.println("Error: missing closing ; at line " + result.lines);
                    System.exit(1);
                }
            }
            // MAPPINGS
            else if (ISCHR(index,0,'M','m') && ISCHR(index,7,'S','s') && ISCHR(index,1,'A','a') && ISCHR(index,2,'P','p') && ISCHR(index,3,'P','p') &&
                    ISCHR(index,4,'I','i') && ISCHR(index,5,'N','n') && ISCHR(index,6,'G','g') && !ISSTRING(index,7)) {
                index += 8;
                in_before_sections = true;
                in_section = false;
                in_after_sections = false;
                in_null_section = false;
                sindex = index;
                SKIPLN_S();
                SKIPWS_S((char)0, (char)0);
                result.lines += SKIPWS((char)0,(char)0);
                if (index != sindex) {
                    nindex = index;
                    result.lines += SKIPTOWS_N((char)0,true);
                    StringBuilder anchor = new StringBuilder();
                    for (int i=index;i < nindex;i++) {
                        anchor.append(inArray[i]);
                    }
                    result.addAnchor(anchor.toString(), result.lines);
                    index = nindex;
                }
            }
            // CORRECTIONS
            else if (ISCHR(index,0,'C','c') && ISCHR(index,10,'S','s') && ISCHR(index,1,'O','o') && ISCHR(index,2,'R','r') && ISCHR(index,3,'R','r') &&
                    ISCHR(index,4,'E','e') && ISCHR(index,5,'C','c') && ISCHR(index,6,'T','t') && ISCHR(index,7,'I','i') && ISCHR(index,8,'O','o') &&
                    ISCHR(index,9,'N','n') && !ISSTRING(index,10)) {
                index += 11;
                in_before_sections = true;
                in_section = false;
                in_after_sections = false;
                in_null_section = false;
                sindex = index;
                SKIPLN_S();
                SKIPWS_S((char)0,(char)0);
                result.lines += SKIPWS((char)0,(char)0);
                if (index != sindex) {
                    nindex = index;
                    result.lines += SKIPTOWS_N((char)0,true);
                    StringBuilder anchor = new StringBuilder();
                    for (int i=index;i<nindex;i++) {
                        anchor.append(inArray[i]);
                    }
                    result.addAnchor(anchor.toString(), result.lines);
                    index = nindex;
                }
            }
            // BEFORE-SECTIONS
            else if (ISCHR(index,0,'B','b') && ISCHR(index,14,'S','s') && ISCHR(index,1,'E','e') && ISCHR(index,2,'F','f') && ISCHR(index,3,'O','o') &&
                    ISCHR(index,4,'R','r') && ISCHR(index,5,'E','e') && ISCHR(index,6,'-','_') && ISCHR(index,7,'S','s') && ISCHR(index,8,'E','e') &&
                    ISCHR(index,9,'C','c') && ISCHR(index,10,'T','t') && ISCHR(index,11,'I','i') && ISCHR(index,12,'O','o') && ISCHR(index,13,'N','n') &&
                    !ISSTRING(index,14)) {
                index += 15;
                in_before_sections = true;
                in_section = false;
                in_after_sections = false;
                in_null_section = false;
                sindex = index;
                SKIPLN_S();
                SKIPWS_S((char)0,(char)0);
                result.lines += SKIPWS((char)0,(char)0);
                if (index != sindex) {
                    nindex = index;
                    result.lines += SKIPTOWS_N((char)0, true);
                    StringBuilder anchor = new StringBuilder();
                    for (int i=index;i<nindex;i++) {
                        anchor.append(inArray[i]);
                    }
                    result.addAnchor(anchor.toString(), result.lines);
                    index = nindex;
                }                
            }
            // SECTION
            else if (ISCHR(index,0,'S','s') && ISCHR(index,6,'N','n') && ISCHR(index,1,'E','e') && ISCHR(index,2,'C','c') && ISCHR(index,3,'T','t') &&
                    ISCHR(index,4,'I','i') && ISCHR(index,5,'O','o') && !ISSTRING(index,6)) {
                index += 7;
                result.sections.add(result.lines);
                in_before_sections = false;
                in_section = true;
                in_after_sections = false;
                in_null_section = false;
                sindex = index;
                SKIPLN_S();
                SKIPWS_S((char)0,(char)0);
                result.lines += SKIPWS((char)0,(char)0);
                if (index != sindex) {
                    nindex = index;
                    result.lines += SKIPTOWS_N((char)0, true);
                    StringBuilder anchor = new StringBuilder();
                    for (int i=index;i<nindex;i++) {
                        anchor.append(inArray[i]);
                    }
                    result.addAnchor(anchor.toString(), result.lines);
                    index = nindex;
                }
            }
            // CONSTRAINTS
            else if (ISCHR(index,0,'C','c') && ISCHR(index,10,'S','s') && ISCHR(index,1,'O','o') && ISCHR(index,2,'N','n') && ISCHR(index,3,'S','s') && 
                    ISCHR(index,4,'T','t') && ISCHR(index,5,'R','r') && ISCHR(index,6,'A','a') && ISCHR(index,7,'I','i') && ISCHR(index,8,'N','n') &&
                    ISCHR(index,9,'T','t') && !ISSTRING(index,10)) {
                index += 11;
                result.sections.add(result.lines);
                in_before_sections = false;
                in_section = true;
                in_after_sections = false;
                in_null_section = false;
                sindex = index;
                SKIPLN_S();
                SKIPWS_S((char)0, (char)0);
                result.lines += SKIPWS((char)0,(char)0);
                if (index != sindex) {
                    nindex = index;
                    result.lines += SKIPTOWS_N((char)0, true);
                    StringBuilder anchor = new StringBuilder();
                    for (int i=index;i<nindex;i++) {
                        anchor.append(inArray[i]);
                    }
                    result.addAnchor(anchor.toString(), result.lines);
                    index = nindex;
                }
            }
            // AFTER-SECTIONS
            else if (ISCHR(index,0,'A','a') && ISCHR(index,13,'S','s') && ISCHR(index,1,'F','f') && ISCHR(index,2,'T','t') && ISCHR(index,3,'E','e') && 
                    ISCHR(index,4,'R','r') && ISCHR(index,5,'-','_') && ISCHR(index,6,'S','s') && ISCHR(index,7,'E','e') && ISCHR(index,8,'C','c') &&
                    ISCHR(index,9,'T','t') && ISCHR(index,10,'I','i') && ISCHR(index,11,'O','o') && ISCHR(index,12,'N','n') && !ISSTRING(index,13)) {
                index += 14;
                in_before_sections = false;
                in_section = false;
                in_after_sections = true;
                in_null_section = false;
                sindex = index;
                SKIPLN_S();
                SKIPWS_S((char)0,(char)0);
                result.lines += SKIPWS((char)0,(char)0);
                if (index != sindex) {
                    nindex = index;
                    result.lines += SKIPTOWS_N((char)0, true);
                    StringBuilder anchor = new StringBuilder();
                    for (int i=index;i<nindex;i++) {
                        anchor.append(inArray[i]);
                    }
                    result.addAnchor(anchor.toString(), result.lines);
                    index = nindex;
                }
            }
            // NULL-SECTION
            else if (ISCHR(index,0,'N','n') && ISCHR(index,11,'N','n') && ISCHR(index,1,'U','u') && ISCHR(index,2,'L','l') && ISCHR(index,3,'L','l') && 
                    ISCHR(index,4,'-','_') && ISCHR(index,5,'S','s') && ISCHR(index,6,'E','e') && ISCHR(index,7,'C','c') && ISCHR(index,8,'T','t') &&
                    ISCHR(index,9,'I','i') && ISCHR(index,10,'O','o') && !ISSTRING(index,11)) {
                index += 12;
                in_before_sections = false;
                in_section = false;
                in_after_sections = false;
                in_null_section = true;
                sindex = index;
                SKIPLN_S();
                SKIPWS_S((char)0,(char)0);
                result.lines += SKIPWS((char)0,(char)0);
                if (index != sindex) {
                    nindex = index;
                    result.lines += SKIPTOWS_N((char)0,true);
                    StringBuilder anchor = new StringBuilder();
                    for (int i=index;i<nindex;i++) {
                        anchor.append(inArray[i]);
                    }
                    result.addAnchor(anchor.toString(), result.lines);
                    index = nindex;
                }
            }
            // ANCHOR
            else if (ISCHR(index,0,'A','a') && ISCHR(index,5,'R','r') && ISCHR(index,1,'N','n') && ISCHR(index,2,'C','c') && ISCHR(index,3,'H','h') &&
                    ISCHR(index,4,'O','o') && !ISSTRING(index,5)) {
                index += 6;
                result.lines += SKIPWS((char)0,(char)0);
                nindex = index;
                result.lines += SKIPTOWS_N((char)0,true);
                StringBuilder anchor = new StringBuilder();
                for (int i=index;i<nindex;i++) {
                    anchor.append(inArray[i]);
                }
                result.addAnchor(anchor.toString(), result.lines);
                index = nindex;
                result.lines += SKIPWS(';', (char)0);
                // don't know if ANCHOR should require a ; or not. Looks like it currently does
                if (inArray[index] != ';') {
                    System.err.println("Error: missing closing ; on line " + result.lines);
                    System.exit(1);
                }                
            }
            // INCLUDE (not supported)
            else if (ISCHR(index,0,'I','i') && ISCHR(index,6,'E','e') && ISCHR(index,1,'N','n') && ISCHR(index,2,'C','c') && ISCHR(index,3,'L','l') &&
                    ISCHR(index,4,'U','u') && ISCHR(index,5,'D','d') && !ISSTRING(index,6)) {
                System.err.println("INCLUDE keyword not supported yet in this CG parser. Please paste the contents of the other files in directly");
                System.exit(1);
            }
            // IFF
            else if (ISCHR(index,0,'I','i') && ISCHR(index,2,'F','f') && ISCHR(index,1,'F','f') && !ISSTRING(index,2)) {
                parseRule(KEYWORDS.K_IFF);
            }
            // MAP
            else if (ISCHR(index,0,'M','m') && ISCHR(index,2,'P','p') && ISCHR(index,1,'A','a') && !ISSTRING(index,2)) {
                parseRule(KEYWORDS.K_MAP);
            }
            // ADD
            else if (ISCHR(index,0,'A','a') && ISCHR(index,2,'D','d') && ISCHR(index,1,'D','d') && !ISSTRING(index,2)) {
                parseRule(KEYWORDS.K_ADD);
            }
            // APPEND
            else if (ISCHR(index,0,'A','a') && ISCHR(index,5,'D','d') && ISCHR(index,1,'P','p') && ISCHR(index,2,'P','p') &&
                    ISCHR(index,3,'E','e') && ISCHR(index,4,'N','n') && !ISSTRING(index,5)) {
                parseRule(KEYWORDS.K_APPEND);
            }
            // SELECT
            else if (ISCHR(index,0,'S','s') && ISCHR(index,5,'T','t') && ISCHR(index,1,'E','e') && ISCHR(index,2,'L','l') &&
                    ISCHR(index,3,'E','e') && ISCHR(index,4,'C','c') && !ISSTRING(index, 5)) {
                parseRule(KEYWORDS.K_SELECT);
            }
            // REMOVE
            else if (ISCHR(index,0,'R','r') && ISCHR(index,5,'E','e') && ISCHR(index,1,'E','e') && ISCHR(index,2,'M','m') && 
                    ISCHR(index,3,'O','o') && ISCHR(index,4,'V','v') && !ISSTRING(index,5)) {
                parseRule(KEYWORDS.K_REMOVE);
            }       
            // REPLACE
            else if (ISCHR(index,0,'R','r') && ISCHR(index,6,'E','e') && ISCHR(index,1,'E','e') && ISCHR(index,2,'P','p') &&
                    ISCHR(index,3,'L','l') && ISCHR(index,4,'A','a') && ISCHR(index,5,'C','c') && !ISSTRING(index,6)) {
                parseRule(KEYWORDS.K_REPLACE);
            }
            // DELIMIT
            else if (ISCHR(index,0,'D','d') && ISCHR(index,6,'T','t') && ISCHR(index,1,'E','e') && ISCHR(index,2,'L','l') &&
                    ISCHR(index,3,'I','i') && ISCHR(index,4,'M','m') && ISCHR(index,5,'I','i') && !ISSTRING(index,6)) {
                parseRule(KEYWORDS.K_DELIMIT);
            }
            // SUBSTITUTE
            else if (ISCHR(index,0,'S','s') && ISCHR(index,9,'E','e') && ISCHR(index,1,'U','u') && ISCHR(index,2,'B','b') && ISCHR(index,3,'S','s') &&
                    ISCHR(index,4,'T','t') && ISCHR(index,5,'I','i') && ISCHR(index,6,'T','t') && ISCHR(index,7,'U','u') && ISCHR(index,8,'T','t') &&
                    !ISSTRING(index,9)) {
                parseRule(KEYWORDS.K_SUBSTITUTE);
            }
            // COPY
            else if (ISCHR(index,0,'C','c') && ISCHR(index,3,'Y','y') && ISCHR(index,1,'O','o') && ISCHR(index,2,'P','p') && !ISSTRING(index,3)) {
                parseRule(KEYWORDS.K_COPY);
            }
            // JUMP
            else if (ISCHR(index,0,'J','j') && ISCHR(index,3,'P','p') && ISCHR(index,1,'U','u') && ISCHR(index,2,'M','m') && !ISSTRING(index,3)) {
                parseRule(KEYWORDS.K_JUMP);
            }
            // MOVE
            else if (ISCHR(index,0,'M','m') && ISCHR(index,3,'E','e') && ISCHR(index,1,'O','o') && ISCHR(index,2,'V','v') && !ISSTRING(index,3)) {
                parseRule(KEYWORDS.K_MOVE);
            }
            // SWITCH
            else if (ISCHR(index,0,'S','s') && ISCHR(index,5,'H','h') && ISCHR(index,1,'W','w') && ISCHR(index,2,'I','i') && ISCHR(index,3,'T','t') &&
                    ISCHR(index,4,'C','c') && !ISSTRING(index,5)) {
                parseRule(KEYWORDS.K_SWITCH);
            }
            // EXECUTE
            else if (ISCHR(index,0,'E','e') && ISCHR(index,6,'E','e') && ISCHR(index,1,'X','x') && ISCHR(index,2,'E','e') && ISCHR(index,3,'C','c') &&
                    ISCHR(index,4,'U','u') && ISCHR(index,5,'T','t') && !ISSTRING(index,6)) {
                parseRule(KEYWORDS.K_EXECUTE);
            }
            // UNMAP
            else if (ISCHR(index,0,'U','u') && ISCHR(index,4,'P','p') && ISCHR(index,1,'N','n') && ISCHR(index,2,'M','m') && ISCHR(index,3,'A','a') &&
                    !ISSTRING(index,4)) {
                parseRule(KEYWORDS.K_UNMAP);
            }
            // TEMPLATE (not supported)
            else if (ISCHR(index,0,'T','t') && ISCHR(index,7,'E','e') && ISCHR(index,1,'E','e') && ISCHR(index,2,'M','m') && ISCHR(index,3,'P','p') &&
                    ISCHR(index,4,'L','l') && ISCHR(index,5,'A','a') && ISCHR(index,6,'T','t') && !ISSTRING(index,7)) {
                System.err.println("Templates not supported yet in this CG parser. Sorry.");
                System.exit(1);
            }
            // PARENTHESES
            else if (ISCHR(index,0,'P','p') && ISCHR(index,10,'S','s') && ISCHR(index,1,'A','a') && ISCHR(index,2,'R','r') &&
                    ISCHR(index,3,'E','e') && ISCHR(index,4,'N','n') && ISCHR(index,5,'T','t') && ISCHR(index,6,'H','h') &&
                    ISCHR(index,7,'E','e') && ISCHR(index,8,'S','s') && ISCHR(index,9,'E','e') && !ISSTRING(index,10)) {
                index += 11;
                result.lines += SKIPWS('=',(char)0);
                if (inArray[index] != '=') {
                    System.err.println("Error: encountered a problem before the expected = on line " + result.lines);
                    System.exit(1);
                }
                ++index;
                result.lines += SKIPWS((char)0,(char)0);
                while (notDone() && inArray[index] != ';') {
                    CgTag left = null;
                    CgTag right = null;
                    nindex = index;
                    result.lines += SKIPTOWS_N('(',true);
                    if (inArray[nindex] != '(') {
                        System.err.println("Error encountered " + inArray[nindex] + " before the expected ) on line " + result.lines);
                        System.exit(1);
                    }
                    nindex++;
                    result.lines += SKIPWS_N((char)0,(char)0);
                    index = nindex;
                    if (inArray[nindex] == '"') {
                        nindex++;
                        result.lines += SKIPTO_NOSPAN_N('"');
                        if (inArray[nindex] != '"') {
                            System.err.println("Error: missing closing \" on line " + result.lines);
                            System.exit(1);
                        }
                    }
                    result.lines += SKIPTOWS_N(')', true);
                    StringBuilder parens = new StringBuilder();
                    for (int i=index;i<nindex;i++) {
                        parens.append(inArray[i]);
                    }
                    left = result.allocateTag(parens.toString(), false);
                    result.lines += SKIPWS_N((char)0,(char)0);
                    index = nindex;
                    
                    if (inArray[index] == ')') {
                        System.err.println("Error: encountered ) before the expected Right tag on line " + result.lines);
                        System.exit(1);
                    }
                    
                    if (inArray[nindex] == '"') {
                        nindex++;
                        result.lines += SKIPTO_NOSPAN_N('"');
                        if (inArray[nindex] != '"') {
                            System.err.println("Missing closing \" on line " + result.lines);
                            System.exit(1);
                        }
                    }
                    result.lines += SKIPTOWS_N(')', true);
                    StringBuilder parens2 = new StringBuilder();
                    for (int i=index;i<nindex;i++) {
                        parens2.append(inArray[i]);
                    }
                    right = result.allocateTag(parens2.toString(), false);
                    result.lines += SKIPWS_N((char)0,(char)0);
                    index = nindex;
                    
                    if (inArray[index] != ')') {
                        System.err.println("Error: encounted " + inArray[index] + " before expected ) on line " + result.lines);
                        System.exit(1);
                    }
                    ++index;
                    result.lines += SKIPWS((char)0,(char)0);
                    
                    if (left != null && right != null) {
                        result.parentheses.put(left.hash, right.hash);
                        result.parentheses_reverse.put(right.hash, left.hash);
                    }
                }
                if (result.parentheses.isEmpty()) {
                    System.out.println("Error: parentheses declared, but no definitions given on line " + result.lines);
                    System.exit(1);
                }
                result.lines += SKIPWS(';',(char)0);
                if (inArray[index] != ';') {
                    System.err.println("Error: missing closing ; on line " + result.lines);
                    System.exit(1);
                }
            }
            // END
            else if (ISCHR(index,0,'E','e') && ISCHR(index,2,'D','d') && ISCHR(index,1,'N','n')) {
                if (ISNL(index-1) || ISSPACE(index-1)) {
                    if (inArray[index+3] == (char)0 || ISNL(index+3) || ISSPACE(index+3)) {
                        break;
                    }
                }
                ++index;
            }
            // no keyword found at this position, skip a character
            else {
                if (inArray[index] == ';' || inArray[index] == '"') {
                    if (inArray[index] == '"') {
                        ++index;
                        result.lines += SKIPTO_NOSPAN_P('"');
                        if (inArray[index] != '"') {
                            System.err.println("Error: Missing closing \" on line " + result.lines);
                            System.exit(1);
                        }
                    }
                    result.lines += SKIPTOWS_P((char)0,false);
                }
                if (notDone() && inArray[index] != ';' && inArray[index] != '"' && !ISNL(index) && !ISSPACE(index)) {
                    System.err.println("Error: garbage data on line " + result.lines + "; I'm not really sure what's going on here");
                    System.exit(1);
                }
                if (ISNL(index)) {
                    result.lines += 1;
                }
                ++index;
            }
        }
        return 0;
    }
    
    /**
     * Adds a CgRule to the CgGrammar
     * @param rule
     */
    private void addRuleToGrammar(CgRule rule) {
        if (this.in_section) {
            rule.section = result.sections.size() - 1;
            result.addRule(rule);
        }
        else if (this.in_before_sections) {
            rule.section = -1;
            result.addRule(rule);
        }
        else if (this.in_after_sections) {
            rule.section = -2;
            result.addRule(rule);
        }
        else if (this.in_null_section) {
            rule.section = -3;
            result.addRule(rule);
        }
        else {
            result.destroyRule(rule);
            System.err.println("Error: rule definition attempted outside of a section on line " + result.lines);
            System.exit(1);
        }
    }
    
    /**
     * Parses a list of tags from the current index position, returning a CgSet
     * @param s
     * @param isinline
     * @return
     */
    private CgSet parseTagList(CgSet s, boolean isinline) {
        if (isinline) {
            if (inArray[index] != '(') {
                System.err.println("Error: Missing opening ( on line " + result.lines);
                System.exit(1);
            }
            ++index;
        }
        while (notDone() && inArray[index] != ';' && inArray[index] != ')') {
            result.lines += SKIPWS(';',')');
            if (notDone() && inArray[index] != ';' && inArray[index] != ')') {
                if (inArray[index] == '(') {
                    ++index;
                    ArrayList<CgTag> tags = new ArrayList<CgTag>();
                    while (notDone() && inArray[index] != ';' && inArray[index] != ')') {
                        nindex = index;
                        if (inArray[nindex] == '"') {
                            nindex++;
                            result.lines += SKIPTO_NOSPAN_N('"');
                            if (inArray[nindex] != '"') {
                                System.err.println("Error: missing closing \" on line " + result.lines);
                                System.exit(1);
                            }
                        }
                        result.lines += SKIPTOWS_N(')', true);
                        StringBuilder sb = new StringBuilder();
                        for (int i=index;i<nindex;i++) {
                            sb.append(inArray[i]);
                        }
                        CgTag t = result.allocateTag(sb.toString(), false);
                        tags.add(t);
                        index = nindex;
                        result.lines += SKIPWS(';', ')');
                    }
                    if (inArray[index] != ')') {
                        System.err.println("Error: missing closing ) on line " + result.lines);
                        System.exit(1);
                    }
                    ++index;
                    
                    if (tags.size() == 1) {
                        s.addTag(tags.get(tags.size()-1));	// these methods are my own - slightly different from those in the original C++ code
                    } else {
                        CgCompositeTag ct = result.allocateCompositeTag();
                        for (int i=0;i<tags.size();i++) {
                            ct.addTag(tags.get(i));
                        }
                        s.addCompositeTag(ct);
                    }
                }
                else {
                nindex = index;
                if (inArray[nindex] == '"') {
                    nindex++;
                    result.lines += SKIPTO_NOSPAN_N('"');
                    if (inArray[nindex] != '"') {
                        System.err.println("Error: missing closing \" on line " + result.lines);
                        System.exit(1);
                    }
                }
                if (isinline) {
                    result.lines += SKIPTOWS_N(')', true);
                } else {
                    result.lines += SKIPTOWS_N((char)0,true);
                }
                StringBuilder sb = new StringBuilder();
                for (int i=index;i<nindex;i++) {
                    sb.append(inArray[i]);
                }
                CgTag t = result.allocateTag(sb.toString(), false);
                s.addTag(t);
                index = nindex;
            }
        }
    }
    if (isinline) {
        if (inArray[index] != ')') {
            System.err.println("Error: missing closing ) on line " + result.lines);
            System.exit(1);
        }
        ++index;
    }
        return s;
    }
    
    private CgSet parseSetInline(CgSet s) {
        ArrayList<Integer> set_ops = new ArrayList<Integer>();
        ArrayList<Integer> sets = new ArrayList<Integer>();
        
        boolean wantop = false;
        while (notDone() && inArray[index] != ';' && inArray[index] != ')') {
            result.lines += SKIPWS(';', ')');
            if (notDone() && inArray[index] != ';' && inArray[index] != ')') {
                if (!wantop) {
                    if (inArray[index] == '(') {
                        ++index;
                        CgSet set_c = result.allocateSet();
                        set_c.line = result.lines;
                        set_c.setName(sets_counter++);
                        ArrayList<CgTag> tags = new ArrayList<CgTag>();
                        
                        while (notDone() && inArray[index] != ';' && inArray[index] != ')') {
                            result.lines += SKIPWS(';',')');
                            nindex = index;
                            if (inArray[nindex] == '"') {
                                nindex++;
                                result.lines += SKIPTO_NOSPAN_N('"');
                                if (inArray[nindex] != '"') {
                                    System.err.println("Error: missing closing \" at line " + result.lines);
                                    System.exit(1);
                                }
                            }
                            result.lines += SKIPTOWS_N(')',true);
                            StringBuilder sb = new StringBuilder();
                            for (int i=index;i<nindex;i++) {
                                sb.append(inArray[i]);
                            }
                            CgTag t = result.allocateTag(sb.toString(), false);
                            tags.add(t);
                            index = nindex;
                            result.lines += SKIPWS(';', ')');
                        }
                        if (inArray[index] != ')') {
                            System.err.println("Error: missing closing ) on line " + result.lines);
                            System.exit(1);
                        }
                        ++index;
                        
                        if (tags.size() == 1) {
                            set_c.addTag(tags.get(tags.size()-1));
                        } else {
                            CgCompositeTag ct = result.allocateCompositeTag();
                            for (int i=0;i<tags.size();i++) {
                                ct.addTag(tags.get(i));
                            }
                            set_c.addCompositeTag(ct);
                        }
                        result.addSet(set_c);
                        sets.add(set_c.hash);
                    }
                    else {
                        nindex = index;
                        result.lines += SKIPTOWS_N(')', true);
                        while (inArray[nindex-1] == ',' || inArray[nindex-1] == ']') {
                            --nindex;
                        }
                        StringBuilder sb = new StringBuilder();
                        for (int i=index;i<nindex;i++) {
                            sb.append(inArray[i]);
                        }
                        CgSet tmp = result.parseSet(sb.toString());
                        int sh = tmp.hash;
                        sets.add(sh);
                        index = nindex;
                    }
                    if (!set_ops.isEmpty() && (set_ops.get(set_ops.size()-1) == STRINGS.S_SET_ISECT_U.value || 
                            set_ops.get(set_ops.size()-1) == STRINGS.S_SET_SYMDIFF_U.value)) {
                        // composite sets
                    	// sets with the intersection/symmetric different operators
                    	// haven't really tested this.
                    	System.out.println("Warning: intersection and symmetric difference with sets may not work correctly");
                    	
                        final HashSet<CgCompositeTag.AnyTag> a = result.getSet(sets.get(sets.size()-1)).getTagList(result);
                        final HashSet<CgCompositeTag.AnyTag> b = result.getSet(sets.get(sets.size()-2)).getTagList(result);
                    
                        ArrayList<CgCompositeTag.AnyTag> r = new ArrayList<CgCompositeTag.AnyTag>();
                        if (set_ops.get(set_ops.size()-1) == STRINGS.S_SET_ISECT_U.value) {
                            HashSet<CgCompositeTag.AnyTag> c = new HashSet<CgCompositeTag.AnyTag>();
                            c.addAll(a);
                            c.addAll(b);
                            for (CgCompositeTag.AnyTag itag : c) {
                                r.add(itag);
                            }
                        }
                        else if (set_ops.get(set_ops.size()-1) == STRINGS.S_SET_SYMDIFF_U.value) {
                            for (CgCompositeTag.AnyTag itag : a) {
                                if (!b.contains(itag)) {
                                    r.add(itag);
                                }
                            }
                            for (CgCompositeTag.AnyTag itag : b) {
                                if (!a.contains(itag)) {
                                    r.add(itag);
                                }
                            }
                        }
                        set_ops.remove(set_ops.size()-1);
                        sets.remove(sets.size()-1);
                        sets.remove(sets.size()-1);
                        
                        CgSet set_c = result.allocateSet();
                        set_c.line = result.lines;
                        set_c.setName(sets_counter++);
                        // TODO: this section needs to be reviewed
                        for (int i=0;i<r.size();i++) {
                            if (r.get(i).which == CgCompositeTag.ANYTAG_TYPE.ANYTAG_TAG.value) {
                                CgTag t = r.get(i).getTag();
                                set_c.addTag(t);
                            }
                            else {
                                CgCompositeTag t = r.get(i).getCompositeTag();
                                set_c.addCompositeTag(t);
                            }
                        }
                        result.addSet(set_c);
                        sets.add(set_c.hash);
                    }
                    wantop = true;
                }
                else {
                    nindex = index;
                    result.lines += SKIPTOWS_N((char)0, true);
                    StringBuilder sb = new StringBuilder();
                    for (int i=index;i<nindex;i++) {
                        sb.append(inArray[i]);
                    }
                    int sop = ux_isSetOp(sb.toString());
                    if (sop != STRINGS.S_IGNORE.value) {
                        set_ops.add(sop);
                        wantop = false;
                        index = nindex;
                    }
                    else {
                        break;
                    }                    
                }
            }
        }
        if (s != null) {
            s.sets = sets;
            s.set_ops = set_ops;
        }
        else if (sets.size() == 1) {
            s = result.getSet(sets.get(sets.size()-1));
        }
        else {
            s = result.allocateSet();
            s.sets = sets;
            s.set_ops = set_ops;
        }
        
        return s;
    }
    
    // has no arguments because it uses the global variable index
    private CgSet parseSetInlineWrapper() {
        int tmpLines = result.lines;
        CgSet s = parseSetInline(null);
        if (s.line == 0) {
            s.line = tmpLines;
        }
        if (s.name == null || s.name.isEmpty()) {
            s.setName(sets_counter++);
        }
        result.addSet(s);
        return s;
    }
    
    /**
     * Parses the beginning of a contextual test.
     * E.g. the "-1C" part of (-1C Noun)
     */
    private void parseContextualTestPosition() {
        boolean negative = false;
        boolean had_digits = false;
        int tries = 0;
        while (inArray[index] != ' ' && inArray[index] != '(' && tries < 100) {
            ++tries;
            if (inArray[index] == '*' && inArray[index+1] == '*') {
                currentTest.pos.add(POS.POS_SCANALL.value);
                index += 2;
            }
            if (inArray[index] == '*') {
                currentTest.pos.add(POS.POS_SCANFIRST.value);
                ++index;
            }
            if (inArray[index] == 'C') {
                currentTest.pos.add(POS.POS_CAREFUL.value);
                ++index;
            }
            if (inArray[index] == 'c') {
                currentTest.pos.add(POS.POS_DEP_CHILD.value);
                ++index;
            }
            if (inArray[index] == 'p') {
                currentTest.pos.add(POS.POS_DEP_PARENT.value);
                ++index;
            }
            if (inArray[index] == 's') {
                currentTest.pos.add(POS.POS_DEP_SIBLING.value);
                ++index;
            }
            if (inArray[index] == 'S') {
                currentTest.pos.add(POS.POS_SELF.value);
                ++index;
            }
            if (inArray[index] == '<') {
                currentTest.pos.add(POS.POS_SPAN_LEFT.value);
                ++index;
            }
            if (inArray[index] == '>') {
                currentTest.pos.add(POS.POS_SPAN_RIGHT.value);
                ++index;
            }
            if (inArray[index] == 'W') {
                currentTest.pos.add(POS.POS_SPAN_BOTH.value);
                ++index;
            }
            if (inArray[index] == '@') {
                currentTest.pos.add(POS.POS_ABSOLUTE.value);
                ++index;
            }
            if (inArray[index] == 'O') {
                currentTest.pos.add(POS.POS_NO_PASS_ORIGIN.value);
                ++index;
            }
            if (inArray[index] == 'o') {
                currentTest.pos.add(POS.POS_PASS_ORIGIN.value);
                ++index;
            }
            if (inArray[index] == 'L') {
                currentTest.pos.add(POS.POS_LEFT_PAR.value);
                ++index;
            }
            if (inArray[index] == 'R') {
                currentTest.pos.add(POS.POS_RIGHT_PAR.value);
                ++index;
            }
            if (inArray[index] == 'X') {
                currentTest.pos.add(POS.POS_MARK_SET.value);
                ++index;
            }
            if (inArray[index] == 'x') {
                currentTest.pos.add(POS.POS_MARK_JUMP.value);
                ++index;
            }
            if (inArray[index] == 'D') {
                currentTest.pos.add(POS.POS_LOOK_DELETED.value);
                ++index;
            }
            if (inArray[index] == 'd') {
                currentTest.pos.add(POS.POS_LOOK_DELAYED.value);
                ++index;
            }
            if (inArray[index] == 'A') {
                currentTest.pos.add(POS.POS_ATTACH_TO.value);
                ++index;
            }
            if (inArray[index] == '?') {
                currentTest.pos.add(POS.POS_UNKNOWN.value);
                ++index;
            }
            if (inArray[index] == '-') {
                negative = true;
                ++index;
            }
            if (Character.isDigit(inArray[index])) {
                had_digits = true;
                while (inArray[index] >= '0' && inArray[index] <= '9') {
                    currentTest.offset = (currentTest.offset*10) + (inArray[index] - '0');
                    ++index;
                }
            }
            if (inArray[index] == 'r' && inArray[index+1] == ':') {
                currentTest.pos.add(POS.POS_RELATION.value);
                index += 2;
                nindex = index;
                SKIPTOWS_N('(', true);
                StringBuilder sb = new StringBuilder();
                for (int i=index;i<nindex;i++) {
                    sb.append(inArray[i]);
                }
                CgTag tag = result.allocateTag(sb.toString(), true);
                currentTest.relation = tag.hash;
                index = nindex;
            }
        }
        if (negative) {
            currentTest.offset = (-1) * Math.abs(currentTest.offset);
        }
        if ((currentTest.pos.contains(POS.POS_DEP_CHILD.value) || currentTest.pos.contains(POS.POS_DEP_SIBLING.value)) &&
             currentTest.pos.contains(POS.POS_SCANFIRST.value) || currentTest.pos.contains(POS.POS_SCANALL.value)) {
            if (currentTest.pos.contains(POS.POS_SCANFIRST.value)) {
                currentTest.pos.remove(POS.POS_SCANFIRST.value);
            }
            if (currentTest.pos.contains(POS.POS_SCANALL.value)) {
                currentTest.pos.remove(POS.POS_SCANALL.value);
            }
            currentTest.pos.add(POS.POS_DEP_DEEP.value);
        }
        if ((currentTest.pos.contains(POS.POS_DEP_CHILD.value) || currentTest.pos.contains(POS.POS_DEP_SIBLING.value)) &&
             currentTest.pos.contains(POS.POS_CAREFUL.value)) {
            System.out.println("Warning: deprecated conversion from C to ALL on line " + result.lines);
            if (currentTest.pos.contains(POS.POS_CAREFUL.value)) {
                currentTest.pos.remove(POS.POS_CAREFUL.value);
            }
            currentTest.pos.add(POS.POS_ALL.value);
        }
        if ((currentTest.pos.contains(POS.POS_DEP_CHILD.value) || currentTest.pos.contains(POS.POS_DEP_SIBLING.value)) &&
                currentTest.pos.contains(POS.POS_NOT.value)) {
            System.out.println("Warning: deprecated conversion from NOT to NONE on line " + result.lines);
            if (currentTest.pos.contains(POS.POS_NOT.value)) {
                currentTest.pos.remove(POS.POS_NOT.value);
            }
            currentTest.pos.add(POS.POS_NONE.value);
        }
        if (currentTest.pos.contains(POS.POS_RELATION.value) && (currentTest.pos.contains(POS.POS_CAREFUL.value))) {
            System.out.println("Warning: deprecated conversion from C to ALL on line " + result.lines);
            currentTest.pos.remove(POS.POS_CAREFUL.value);
            currentTest.pos.add(POS.POS_NONE.value);
        }
        if (currentTest.pos.contains(POS.POS_RELATION.value) && (currentTest.pos.contains(POS.POS_NOT.value))) {
            System.out.println("Warning: deprecated from NOT to NONE on line " + result.lines);
            currentTest.pos.remove(POS.POS_NOT.value);
            currentTest.pos.add(POS.POS_NONE.value);
        }
        
        if (tries >= 5) {
            System.out.println("Warning: Position on line " + result.lines + " took too many loops");
        }
        if (tries >= 100) {
            System.err.println("Error: invalid position on line " + result.lines + " caused endless loop");
            System.exit(1);
        }
        if (had_digits) {
            if (currentTest.pos.contains(POS.POS_DEP_CHILD.value) || currentTest.pos.contains(POS.POS_DEP_SIBLING.value) ||
                currentTest.pos.contains(POS.POS_DEP_PARENT.value)) {
                System.err.println("Error: invalid position on line " + result.lines + " - cannot combine offsets with dependency");
                System.exit(1);
            }
            if (currentTest.pos.contains(POS.POS_LEFT_PAR.value) || currentTest.pos.contains(POS.POS_RIGHT_PAR.value)) {
                System.err.println("Error: invalid position on line " + result.lines + " - cannot combine offsets with enclosures");
                System.exit(1);
            }
            if (currentTest.pos.contains(POS.POS_RELATION.value)) {
                System.err.println("Error: invalid position on line " + result.lines + " - cannot combine offsets with relations");
                System.exit(1);
            }
        }
        if ((currentTest.pos.contains(POS.POS_LEFT_PAR.value) || currentTest.pos.contains(POS.POS_RIGHT_PAR.value)) &&
            (currentTest.pos.contains(POS.POS_SCANFIRST.value) || currentTest.pos.contains(POS.POS_SCANALL.value))) {
            System.err.println("Error: invalid position on line " + result.lines + " - cannot have both enclosure and scan");
            System.exit(1);
        }
        if (currentTest.pos.contains(POS.POS_PASS_ORIGIN.value) && currentTest.pos.contains(POS.POS_NO_PASS_ORIGIN.value)) {
            System.err.println("Error: invalid position on line " + result.lines + " - cannot have both O and o");
            System.exit(1);
        }
        if (currentTest.pos.contains(POS.POS_LEFT_PAR.value) && currentTest.pos.contains(POS.POS_RIGHT_PAR.value)) {
            System.err.println("Error: invalid position on line " + result.lines + " - cannot have both L and R");
            System.exit(1);
        }
        if (currentTest.pos.contains(POS.POS_ALL.value) && currentTest.pos.contains(POS.POS_NONE.value)) {
            System.err.println("Error: invalid position on line " + result.lines + " - cannot have both NONE and ALL");
            System.exit(1);
        }
        if (currentTest.pos.contains(POS.POS_UNKNOWN.value) && (currentTest.pos.size() == 1 || had_digits)) {
            System.err.println("Error: invalid position on line " + result.lines + " - ? cannot be combined with anything else");
            System.exit(1);
        }
        if (currentTest.pos.contains(POS.POS_SCANALL.value) && currentTest.pos.contains(POS.POS_NOT.value)) {
            System.out.println("Warning: we don't think mixing NOT and ** makes sense on line " + result.lines);
        }
    }

    /**
     * Takes a CgRule and parses the contextual test at the current position, adding the 
     * test to the rule and returning the modified rule
     * @param rule
     * @return
     */
    private CgRule parseContextualTestList(CgRule rule) {
        if (inLinkedTest) {
        	// if there's a previous linked test, link them together and add them to the HashMap
        	if (!linkedTests.isEmpty()) {
        		CgContextualTest lastTest = rule.test_map.get(linkedTests.get(linkedTests.size()-1));
        		lastTest.next = currentTest.hashCode();
        		linkedTests.remove(linkedTests.get(linkedTests.size()-1));
        		linkedTests.add(lastTest.hashCode());
        		currentTest.prev = lastTest.hashCode();
        		rule.test_map.put(currentTest.hashCode(), currentTest);
        		rule.test_map.put(lastTest.hashCode(),lastTest);
        	} 
            linkedTests.add(currentTest.hashCode());
            rule.test_map.put(currentTest.hashCode(), currentTest);
        } else {
            linkedTests = new ArrayList<Integer>();
        }
        currentTest = new CgContextualTest();
        
        currentTest.line = result.lines;
        result.lines += SKIPWS((char)0,(char)0);
        
        // NEGATE (the c++ code does this using stringbits, but i'm not sure how)
        if (ISCHR(index,0,'N','n') && ISCHR(index,5,'E','e') && ISCHR(index,1,'E','e') && ISCHR(index,2,'G','g') &&
                ISCHR(index,3,'A','a') && ISCHR(index,4,'T','t')) {
            index += 6;
            currentTest.pos.add(POS.POS_NEGATE.value);
        }
        // ALL
        if (ISCHR(index,0,'A','a') && ISCHR(index,2,'L','l') && ISCHR(index,1,'L','l')) {
            index += 3;
            currentTest.pos.add(POS.POS_ALL.value);
        }
        // NONE
        if (ISCHR(index,0,'N','n') && ISCHR(index,3,'E','e') && ISCHR(index,1,'O','o') && ISCHR(index,2,'N','n')) {
            index += 4;
            currentTest.pos.add(POS.POS_NONE.value);
        }
        // NOT
        if (ISCHR(index,0,'N','n') && ISCHR(index,2,'T','t') && ISCHR(index,1,'O','o')) {
            index += 3;
            currentTest.pos.add(POS.POS_NOT.value);
        }
        result.lines += SKIPWS((char)0,(char)0);
        nindex = index;
        result.lines += SKIPTOWS_N('(', false);
        StringBuilder buf = new StringBuilder();
        for (int i=index;i<nindex;i++) {
            buf.append(inArray[i]);
        }
        String str = buf.toString();
        if (ux_isEmpty(str)) {
            index = nindex;
            // if it's empty, there's going to be another contextual test, like in the case
            // ((NOT 1 NOUN) OR (2 VERB))
            if (parentTest != null) {
            	System.err.println("Can't have two nested tests on line " + result.lines + "\nTry splitting it up.");
            	System.exit(1);
            }
            parentTest = currentTest;
            inParentTest = true;
            for (;;) {
                if (inArray[index] != '(') {
                    System.err.println("Error: expected ( but found " + inArray[index] + " at line " + result.lines);
                    System.exit(1);
                }
                ++index;
                rule = parseContextualTestList(rule);
                ++index;
                // add either the currentTest or the head of the linked tests to the 
                // parentTest's "ors" list
                if (linkedTests.isEmpty()) {
                	parentTest.ors.add(currentTest.hashCode());
                } else {
                	parentTest.ors.add(linkedTests.get(0));
                }
                result.lines += SKIPWS((char)0,(char)0);
                // OR
                if (ISCHR(index,0,'O','o') && ISCHR(index,1,'R','r')) {
                    index += 2;
                }
                else {
                    inParentTest = false;
                    linkedTests = new ArrayList<Integer>();
                    break;
                }
                result.lines += SKIPWS((char)0,(char)0);
            }
        }
        else if (str.compareToIgnoreCase("[") == 0) {
            System.out.println("Warning: this feature may not work correctly in this implementation");
        	++index;
            result.lines += SKIPWS((char)0,(char)0);
            CgSet s1 = parseSetInlineWrapper();
            currentTest.offset = 1;
            currentTest.target = s1.hash;
            result.lines += SKIPWS((char)0,(char)0);
            while (inArray[index] == ',' ) {
                ++index;
                result.lines += SKIPWS((char)0,(char)0);
                CgContextualTest lnk = currentTest.allocateContextualTest();
                CgSet s = parseSetInlineWrapper();
                lnk.offset = 1;
                lnk.target = s.hash;
                currentTest.linked = lnk.hashCode();
                currentTest = lnk;
                result.lines += SKIPWS((char)0,(char)0);
            }
            if (inArray[index] != ']') {
                System.err.println("Error: expected ] on line " + result.lines + " but found " + inArray[index]);
                System.exit(1);
            }
            ++index;
        }
        else if (str.charAt(0) == 'T' && str.charAt(1) == ':') {
            System.err.println("Templates not supported in this CG parser. Sorry");
            System.exit(1);
            //TODO: add this if this project goes further
        }
        else {
            parseContextualTestPosition();
            index = nindex;
            if (currentTest.pos.contains(POS.POS_DEP_CHILD.value) || currentTest.pos.contains(POS.POS_DEP_CHILD.value) || 
                currentTest.pos.contains(POS.POS_DEP_SIBLING.value)) {
                result.has_dep = true;
            }
            result.lines += SKIPWS((char)0,(char)0);
            if (inArray[index] == 'T' && inArray[index + 1] == ':') {
                // again, templates not supported
                System.err.println("Templates not supported in this CG parser. Sorry");
                System.exit(1);
            }
            else {
                CgSet s = parseSetInlineWrapper();
                currentTest.target = s.hash;
            }
            result.lines += SKIPWS((char)0,(char)0);
            // CBARRIER
            if (ISCHR(index,0,'C','c') && ISCHR(index,7,'R','r') && ISCHR(index,1,'B','b') && ISCHR(index,2,'A','a') &&
                    ISCHR(index,3,'R','r') && ISCHR(index,4,'R','r') && ISCHR(index,5,'I','i') && ISCHR(index,6,'E','e')) {
                index += 8;
                result.lines += SKIPWS((char)0,(char)0);
                CgSet s = parseSetInlineWrapper();
                currentTest.cbarrier = s.hash;
            }
            result.lines += SKIPWS((char)0,(char)0);
            // BARRIER
            if (ISCHR(index,0,'B','b') && ISCHR(index,6,'R','r') && ISCHR(index,1,'A','a') && ISCHR(index,2,'R','r') &&
                    ISCHR(index,3,'R','r') && ISCHR(index,4,'I','i') && ISCHR(index,5,'E','e')) {
                index += 7;
                result.lines += SKIPWS((char)0,(char)0);
                CgSet s = parseSetInlineWrapper();
                currentTest.barrier = s.hash;
            }
            result.lines += SKIPWS((char)0,(char)0);
        }
        // This is kind of the "after the test is done being parsed" section
        boolean linked = false;
        result.lines += SKIPWS((char)0,(char)0);
        // AND
        if (ISCHR(index,0,'A','a') && ISCHR(index,2,'D','d') && ISCHR(index,1,'N','n')) {
            System.err.println("AND is deprecated. Use LINK 0 or operator +. Found on line " + result.lines);
            System.exit(1);
        }
        // LINK
        if (ISCHR(index,0,'L','l') && ISCHR(index,3,'K','k') && ISCHR(index,1,'I','i') && ISCHR(index,2,'N','n')) {
            index += 4;
            linked = true;
        }
        result.lines += SKIPWS((char)0,(char)0);
        
        if (linked) {
            if (currentTest.pos.contains(POS.POS_NONE.value)) {
                System.err.println("Error: it does not make sense to link from a NONE test.");
                System.exit(1);
            }
            inLinkedTest = true;
            rule = parseContextualTestList(rule);
            inLinkedTest = false;
            return rule;
        }
        
        if (rule != null) {
            if (rule.flags.contains(RFLAGS.RF_LOOKDELETED.value)) {
                currentTest.pos.add(POS.POS_LOOK_DELETED.value);
            }
            if (rule.flags.contains(RFLAGS.RF_LOOKDELAYED.value)) {
                currentTest.pos.add(POS.POS_LOOK_DELAYED.value);
            }
        }
        
        // if we're in a linked test environment, add the current test to the linkedTests
        // list and link them up correctly
        if (!linkedTests.isEmpty()) {
        	CgContextualTest lastTest = rule.test_map.get(linkedTests.get(linkedTests.size()-1));
        	lastTest.next = currentTest.hashCode();
        	linkedTests.remove(linkedTests.get(linkedTests.size()-1));
        	linkedTests.add(lastTest.hashCode());
        	currentTest.prev = lastTest.hashCode();
        	rule.test_map.put(currentTest.hashCode(), currentTest);
        	rule.test_map.put(lastTest.hashCode(),lastTest);
        	linkedTests.add(currentTest.hashCode());
        	// if you're not in a parent test, add the first of the linked
        	// tests to test_heads
        	if (!inParentTest) {
        		for (int testint : linkedTests) {
                    rule.all_tests.add(rule.test_map.get(testint));
                }
                rule.test_heads.add(rule.test_map.get(linkedTests.get(0)));
        	}
            
        }
        // if we're in a parentTest situation, but done with the child tests
        if (parentTest != null && !inParentTest) {
            // only add the parent test when we're done with all the children
            if (option_vislcg_compat && currentTest.pos.contains(POS.POS_NOT.value)) {
                currentTest.pos.remove(POS.POS_NOT.value);
                currentTest.pos.add(POS.POS_NEGATE.value);
            }
            rule.test_map.put(parentTest.hashCode(), parentTest);
            rule.test_heads.add(parentTest);    // parentTest should contain all the OR'ed child tests already
            linkedTests = new ArrayList<Integer>();	// reset the linkedTests
        } 
        
        if (!inParentTest && !inLinkedTest && parentTest == null) {
            if (option_vislcg_compat && currentTest.pos.contains(POS.POS_NOT.value)) {
                currentTest.pos.remove(POS.POS_NOT.value);
                currentTest.pos.add(POS.POS_NEGATE.value);
            }
            rule.all_tests.add(currentTest);
            rule.test_heads.add(currentTest);
            rule.test_map.put(currentTest.hashCode(), currentTest);
        }
        
        if (inParentTest && !inLinkedTest) {
        	if (option_vislcg_compat && currentTest.pos.contains(POS.POS_NOT.value)) {
                currentTest.pos.remove(POS.POS_NOT.value);
                currentTest.pos.add(POS.POS_NEGATE.value);
            }
        	rule.all_tests.add(currentTest);
        	rule.test_map.put(currentTest.hashCode(), currentTest);
        }
        
        return rule;
    }
    
    /**
     * Wrapper for parsing contextual tests
     * @param rule
     * @return
     */
    private CgRule parseContextualTests(CgRule rule) {
        currentTest = null;
        parentTest = null;
        inParentTest = false;
        inLinkedTest= false;
        linkedTests = new ArrayList<Integer>();
        return parseContextualTestList(rule);
    }
    
    /**
     * For parsing dependency tests; NOT TESTED
     * @param rule
     * @return
     */
    private CgRule parseContextualDependencyTests(CgRule rule) {
        // if there are no dependency rules this shouldn't be an issue, right?
    	System.out.println("Warning: dependency tests not tested in this implementation");
        return parseContextualTestList(rule);
    }
    
    /**
     * After the key is set (e.g. REMOVE or SELECT), parses the rule according the some specific guidelines
     * @param key
     */
    private void parseRule(KEYWORDS key) {
        CgRule rule = result.allocateRule();
        rule.line = result.lines;
        rule.type = key;
        
        lpindex = index;
        BACKTONL_LP();
        result.lines += SKIPWS_LP((char)0,(char)0);
        
        if (lpindex != index && lpindex < index) {
            nindex = lpindex;
            if (inArray[nindex] == '"' ) {
                nindex++;
                result.lines += SKIPTO_NOSPAN_N('"');
                if (inArray[nindex] != '"') {
                    System.err.println("Error: missing closing \" on line " + result.lines);
                    System.exit(1);
                }
            }
            result.lines += SKIPTOWS_N((char)0, true);
            StringBuilder sb = new StringBuilder();
            for (int i=lpindex;i<nindex;i++) {
                sb.append(inArray[i]);
            }
            CgTag wform = result.allocateTag(sb.toString(), false);
            rule.wordform = wform.hash;
        }
        index += keywords[key.value].length();
        result.lines += SKIPWS((char)0,(char)0);
        
        // sets the name of the rule, if there is one
        if (inArray[index] == ':') {
            ++index;
            nindex = index;
            result.lines += SKIPTOWS_N('(', false);
            StringBuilder strname = new StringBuilder();
            for (int i=index;i<nindex;i++) {
                strname.append(inArray[i]);
            }
            rule.setName(strname.toString());
            index = nindex;
        }
        result.lines += SKIPWS((char)0,(char)0);
        
        if (key == KEYWORDS.K_EXTERNAL) {
            // ONCE
            if (ISCHR(index,0,'O','o') && ISCHR(index,3,'E','e') && ISCHR(index,1,'N','n') && ISCHR(index,2,'C','c')) {
                index += 4;
                rule.type = KEYWORDS.K_EXTERNAL_ONCE;
            }
            // ALWAYS
            else if (ISCHR(index,0,'A','a') && ISCHR(index,5,'S','s') && ISCHR(index,1,'L','l') && ISCHR(index,2,'W','w') &&
                    ISCHR(index,3,'A','a') && ISCHR(index,4,'Y','y')) {
                index += 6;
                rule.type = KEYWORDS.K_EXTERNAL_ALWAYS;
            }
            else {
                System.err.println("Error: missing keyword ONCE or ALWAYS on line " + result.lines);
                System.exit(1);
            }
            result.lines += SKIPWS((char)0,(char)0);
            
            nindex = index;
            if (inArray[nindex] == '"') {
                ++nindex;
                result.lines += SKIPTO_NOSPAN_N('"');
                if (inArray[nindex] != '"') {
                    System.err.println("Error: missing closing \" on line " + result.lines);
                    System.exit(1);
                }
            }
            result.lines += SKIPTOWS_N((char)0, true);
            StringBuilder varname = new StringBuilder();
            if (inArray[index] == '"') {
                for (int i=index+1;i<nindex-1;i++) {
                    varname.append(inArray[i]);
                }
            }
            else {
                for (int i=index;i<nindex;i++) {
                    varname.append(inArray[i]);
                }
            }
            CgTag ext = result.allocateTag(varname.toString(), true);
            rule.varname = ext.hash;
            index = nindex;
        }
        
        boolean setflag = true;
        while (setflag) {
            setflag = false;
            for (SFLAGS fl : SFLAGS.values()) {
                if (ux_simplecasecmp(fl.name())) {
                    index += fl.name().length();
                    rule.flags.add((1 << fl.value));
                    setflag = true;
                }
                result.lines += SKIPWS((char)0,(char)0);
                // if any of these is the next char, there can't possibly be more rule options
                if (inArray[index] == '(' || inArray[index] == 'T' || inArray[index] == 't' || inArray[index] == ';') {
                    break;
                }
            }
        }
        //TODO: there might be a problem here with the names of RFLAGS vs the names that actually get put in the 
        // CG file. Like, do you write ENCL_OUTER, or RF_ENCL_OUTER?
        if (rule.flags.contains(RFLAGS.RF_ENCL_OUTER.value) && rule.flags.contains(RFLAGS.RF_ENCL_INNER.value)) {
            System.err.println("Error: Line " + result.lines + " ENCL_OUTER and ENCL_INNER are mutually exclusive");
            System.exit(1);
        }
        
        if (rule.flags.contains(RFLAGS.RF_VARYORDER.value) && rule.flags.contains(RFLAGS.RF_KEEPORDER.value)) {
            System.err.println("Error: Line " + result.lines + " KEEPORDER and VARYORDER are mutually exclusive");
            System.exit(1);
        }
        if (rule.flags.contains(RFLAGS.RF_RESETX.value) && rule.flags.contains(RFLAGS.RF_REMEMBERX.value)) {
            System.err.println("Error: Line " + result.lines + " REMEMBERX and RESETX are mutually exclusive");
            System.exit(1);
        }
        if (rule.flags.contains(RFLAGS.RF_ALLOWLOOP.value) && rule.flags.contains(RFLAGS.RF_NEAREST.value)) {
            System.err.println("Error: Line " + result.lines + " NEAREST and ALLOWLOOP are mutually exclusive");
            System.exit(1);
        }
        if (rule.flags.contains(RFLAGS.RF_UNSAFE.value) && rule.flags.contains(RFLAGS.RF_SAFE.value)) {
            System.err.println("Error: Line " + result.lines + " UNSAFE and SAFE are mutually exclusive");
            System.exit(1);
        }
        if (rule.flags.contains(RFLAGS.RF_SAFE.value) && rule.flags.contains(RFLAGS.RF_UNMAPLAST.value)) {
            System.err.println("Error: Line " + result.lines + " UNMAPLAST and SAFE are mutually exclusive");
            System.exit(1);
        }
        if (rule.flags.contains(RFLAGS.RF_DELAYED.value) && rule.flags.contains(RFLAGS.RF_IMMEDIATE.value)) {
            System.err.println("Error: Line " + result.lines + " DELAYED and IMMEDIATE are mutually exclusive");
            System.exit(1);
        }
        if (rule.flags.contains(RFLAGS.RF_NOCHILD.value) && rule.flags.contains(RFLAGS.RF_WITHCHILD.value)) {
            System.err.println("Error: Line " + result.lines + " NO_CHILD and WITH_CHILD are mutually exclusive");
            System.exit(1);
        }
        if (rule.flags.contains(RFLAGS.RF_NOITERATE.value) && rule.flags.contains(RFLAGS.RF_ITERATE.value)) {
            System.err.println("Error: Line " + result.lines + " NOITERATE and ITERATE are mutually exclusive");
            System.exit(1);
        }
        
        if (!(rule.flags.contains(RFLAGS.RF_ITERATE.value) || rule.flags.contains(RFLAGS.RF_NOITERATE.value))) {
            if (key != KEYWORDS.K_SELECT && key != KEYWORDS.K_REMOVE && key != KEYWORDS.K_IFF &&
                    key != KEYWORDS.K_DELIMIT && key != KEYWORDS.K_REMCOHORT && key != KEYWORDS.K_MOVE &&
                    key != KEYWORDS.K_SWITCH) {
                rule.flags.add(RFLAGS.RF_NOITERATE.value);
            }
        }
        if (key == KEYWORDS.K_UNMAP && !(rule.flags.contains(RFLAGS.RF_SAFE.value) || rule.flags.contains(RFLAGS.RF_UNSAFE.value))) {
            rule.flags.add(RFLAGS.RF_SAFE.value);
        }
        if (rule.flags.contains(RFLAGS.RF_UNMAPLAST.value)) {
            rule.flags.add(RFLAGS.RF_UNSAFE.value);
        }
        if (rule.flags.contains(RFLAGS.RF_ENCL_FINAL.value)) {
            result.has_encl_final = true;
        }
        result.lines += SKIPWS((char)0,(char)0);
        
        if (rule.flags.contains(RFLAGS.RF_WITHCHILD.value)) {
            result.has_dep = true;
            CgSet s = parseSetInlineWrapper();
            rule.childset1 = s.hash;
            result.lines += SKIPWS((char)0,(char)0);
        }
        else if (rule.flags.contains(RFLAGS.RF_NOCHILD.value)) {
            rule.childset1 = 0;
        }
        
        if (key == KEYWORDS.K_JUMP || key == KEYWORDS.K_EXECUTE) {
            nindex = index;
            result.lines += SKIPTOWS_N('(', false);
            if (!ux_isalnum(inArray[index])) {
                System.err.println("Error: Anchor name for " + key.name() + " must be alphanumeric on line " + result.lines);
                System.exit(1);
            }
            StringBuilder sb = new StringBuilder();
            for (int i=index;i<nindex;i++) {
                sb.append(inArray[i]);
            }
            String jumpstart = sb.toString();
            rule.jumpstart = jumpstart.hashCode();
            index = nindex;
        }
        result.lines += SKIPWS((char)0,(char)0);
        
        if (key == KEYWORDS.K_EXECUTE) {
            nindex = index;
            result.lines += SKIPTOWS_N('(',false);
            if (!ux_isalnum(inArray[index])) {
                System.err.println("Error: anchor name for at line " + result.lines + " must be alphanumeric");
                System.exit(1);
            }
            StringBuilder sb = new StringBuilder();
            for (int i=index;i<nindex;i++) {
                sb.append(inArray[i]);
            }
            String str = sb.toString();
            int sh = str.hashCode();
            rule.jumpend = sh;
            index = nindex;
        }
        result.lines += SKIPWS((char)0,(char)0);
        
        if (key == KEYWORDS.K_SUBSTITUTE) {
            CgSet s = parseSetInlineWrapper();
            System.out.println("Warning: substitute behavior not tested");
            s.reindex(result);  // need to define this
            rule.sublist = s;
            if (s.isEmpty()) {
                System.err.println("Error: empty substitute set on line " + result.lines);
                System.exit(1);
            }
            if (s.tags_list.isEmpty() && !(s.type.contains(ST.ST_TAG_UNIFY.value) ||
                                           s.type.contains(ST.ST_SET_UNIFY.value) ||
                                           s.type.contains(ST.ST_CHILD_UNIFY.value))) {
                System.err.println("Error: substitute set on line " + result.lines + " was neither unified nor of LIST type");
                System.exit(1);
            }
        }
        
        result.lines += SKIPWS((char)0,(char)0);
        if (key == KEYWORDS.K_MAP || key == KEYWORDS.K_ADD || key == KEYWORDS.K_REPLACE || 
                key == KEYWORDS.K_APPEND || key == KEYWORDS.K_SUBSTITUTE || key == KEYWORDS.K_COPY
                || key == KEYWORDS.K_ADDRELATIONS || key == KEYWORDS.K_ADDRELATION
                || key == KEYWORDS.K_SETRELATIONS || key == KEYWORDS.K_SETRELATION
                || key == KEYWORDS.K_REMRELATIONS || key == KEYWORDS.K_REMRELATION
                || key == KEYWORDS.K_ADDCOHORT) {
            CgSet s = parseSetInlineWrapper();
            s.reindex(result);
            rule.maplist = s;
            if (s.isEmpty()) {
                System.err.println("Error: Empty mapping set on line " + result.lines);
                System.exit(1);
            }
            if ((s.tags_list.isEmpty() && s.single_tags.isEmpty()) && !(s.type.contains(ST.ST_TAG_UNIFY.value) ||
                                           s.type.contains(ST.ST_SET_UNIFY.value) ||
                                           s.type.contains(ST.ST_CHILD_UNIFY.value))) {
                System.err.println("Error: substitute set on line " + result.lines + " was neither unified nor of LIST type");
                System.exit(1);
            }
        }
        
        result.lines += SKIPWS((char)0,(char)0);
        if (key == KEYWORDS.K_ADDRELATIONS || key == KEYWORDS.K_SETRELATIONS || key == KEYWORDS.K_REMRELATIONS) {
            CgSet s = parseSetInlineWrapper();
            s.reindex(result);
            rule.sublist = s;
            if (s.isEmpty()) {
                System.err.println("Error: Empty mapping set on line " + result.lines);
                System.exit(1);
            }
            if (s.tags_list.isEmpty() && !(s.type.contains(ST.ST_TAG_UNIFY.value) ||
                                           s.type.contains(ST.ST_SET_UNIFY.value) ||
                                           s.type.contains(ST.ST_CHILD_UNIFY.value))) {
                System.err.println("Error: substitute set on line " + result.lines + " was neither unified nor of LIST type");
                System.exit(1);
            }
        }
        
        if (key == KEYWORDS.K_ADDCOHORT) {
            if (ux_simplecasecmp(stringbits[STRINGS.S_AFTER.value])) {
                index += stringbits[STRINGS.S_AFTER.value].length();
                rule.type = KEYWORDS.K_ADDCOHORT_AFTER;
            }
            else if (ux_simplecasecmp(stringbits[STRINGS.S_BEFORE.value])) {
                index += stringbits[STRINGS.S_BEFORE.value].length();
                rule.type = KEYWORDS.K_ADDCOHORT_BEFORE;
            }
            else {
                System.err.println("Error: missing position keyword AFTER or BEFORE on line " + result.lines);
                System.exit(1);
            }
        }
        
        result.lines += SKIPWS((char)0,(char)0);
        if (ux_simplecasecmp(stringbits[STRINGS.S_TARGET.value])) {
            index += stringbits[STRINGS.S_TARGET.value].length();
        }
        result.lines += SKIPWS((char)0,(char)0);
        
        CgSet s = parseSetInlineWrapper();
        rule.target = s.hash;
        
        result.lines += SKIPWS((char)0,(char)0);
        if (ux_simplecasecmp(stringbits[STRINGS.S_IF.value])) {
            index += stringbits[STRINGS.S_IF.value].length();
        }
        result.lines += SKIPWS((char)0,(char)0);
        
        while (notDone() && inArray[index] == '(') {
            ++index;
            result.lines += SKIPWS((char)0,(char)0);
            rule = parseContextualTests(rule);
            result.lines += SKIPWS((char)0,(char)0);
            if (inArray[index] != ')') {
                System.err.println("Error: missing closing ) on line " + result.lines);
                System.exit(1);
            }
            ++index;
            result.lines += SKIPWS((char)0,(char)0);
        }
        
        if (key == KEYWORDS.K_SETPARENT || key == KEYWORDS.K_SETCHILD
                || key == KEYWORDS.K_ADDRELATIONS || key == KEYWORDS.K_ADDRELATION
                || key == KEYWORDS.K_SETRELATIONS || key == KEYWORDS.K_SETRELATION
                || key == KEYWORDS.K_REMRELATIONS || key == KEYWORDS.K_REMRELATION
                || key == KEYWORDS.K_MOVE || key == KEYWORDS.K_SWITCH) {
            result.lines += SKIPWS((char)0,(char)0);
            if (key == KEYWORDS.K_MOVE) {
                if (ux_simplecasecmp(stringbits[STRINGS.S_AFTER.value])) {
                    index += stringbits[STRINGS.S_AFTER.value].length();
                    rule.type = KEYWORDS.K_MOVE_AFTER;
                }
                else if (ux_simplecasecmp(stringbits[STRINGS.S_BEFORE.value])) {
                    index += stringbits[STRINGS.S_BEFORE.value].length();
                    rule.type = KEYWORDS.K_MOVE_BEFORE;
                }
                else {
                    System.err.println("Error: missing movement keyword AFTER or BEFORE on line " + result.lines);
                    System.exit(1);
                }
            }
            else if (key == KEYWORDS.K_SWITCH) {
                if (ux_simplecasecmp(stringbits[STRINGS.S_WITH.value])) {
                    index += stringbits[STRINGS.S_WITH.value].length();
                }
                else {
                    System.err.println("Error: missing movement keyword WITH on line " + result.lines);
                    System.exit(1);
                }
            }
            else {
                if (ux_simplecasecmp(stringbits[STRINGS.S_TO.value])) {
                    index += stringbits[STRINGS.S_TO.value].length();
                }
                else if (ux_simplecasecmp(stringbits[STRINGS.S_FROM.value])) {
                    index += stringbits[STRINGS.S_FROM.value].length();
                    rule.flags.add(RFLAGS.RF_REVERSE.value);
                }
                else {
                    System.err.println("Error: missing dependency keyword TO or FROM on line " + result.lines);
                    System.exit(1);
                }
            }
            result.lines += SKIPWS((char)0,(char)0);
            
            if (key == KEYWORDS.K_MOVE) {
                if (ux_simplecasecmp(flags[SFLAGS.FL_WITHCHILD.value])) {
                    index += flags[SFLAGS.FL_WITHCHILD.value].length();
                    result.has_dep = true;
                    CgSet s2 = parseSetInlineWrapper();
                    rule.childset2 = s2.hash;
                    result.lines += SKIPWS((char)0,(char)0);
                }
                else if (ux_simplecasecmp(flags[SFLAGS.FL_NOCHILD.value])) {
                    index += flags[SFLAGS.FL_NOCHILD.value].length();
                    rule.childset2 = 0;
                    result.lines += SKIPWS((char)0,(char)0);
                }
            }
            
            while (notDone() && inArray[index] != '(') {
                ++index;
                result.lines += SKIPWS((char)0,(char)0);
                parseContextualDependencyTests(rule);
                result.lines += SKIPWS((char)0,(char)0);
                if (inArray[index] != ')') {
                    System.err.println("Error: missing closing ) on line " + result.lines);
                    System.exit(1);
                }
                ++index;
                result.lines += SKIPWS((char)0,(char)0);
            }
            if (!(rule.dep_test_head != null)) {
                System.err.println("Error: missing dependency target on line " + result.lines);
                System.exit(1);
            }
            rule.dep_target = rule.dep_test_head;
            // this might be a problematic loop
            // TODO: dependency rules not really supported/tested yet
            while (rule.dep_target.next != 0) {
                rule.dep_target = rule.test_map.get(rule.dep_target.next);
            }
//            rule.dep_target.detach();
            if (rule.dep_target == rule.dep_test_head) {
                rule.dep_test_head = null;
            }
        }
        if (key == KEYWORDS.K_SETPARENT || key == KEYWORDS.K_SETCHILD) {
            result.has_dep = true;
        }
        
//        rule.reverseContextualTests();
        // This works in the simpler cases (REMOVE, ADD, SELECT), but may have problems with untested, more involved rules
        addRuleToGrammar(rule);
    }
    
    // ** TESTING METHODS FOR CURRENT CHARACTER/STRING **
    
    private static boolean ux_isalnum(char c) {
        return Character.isLetterOrDigit(c);
    }
    
    /**
     * Returns true if the given string is empty (all whitespace or length 0)
     * @param s
     * @return
     */
    private static boolean ux_isEmpty(String s) {
        int l = s.length();
        if (l > 0) {
            for (char c : s.toCharArray()) {
                if (!Character.isWhitespace(c)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Returns the integer value of the given set operator 
     * @param s
     * @return
     */
    private static int ux_isSetOp(String s) {
        int retval = STRINGS.S_IGNORE.value;
        if (s.compareToIgnoreCase(stringbits[STRINGS.S_OR.value]) == 0 ||
                s.compareToIgnoreCase(stringbits[STRINGS.S_PIPE.value]) == 0) {
            retval = STRINGS.S_OR.value;
        }
        else if (s.compareTo(stringbits[STRINGS.S_PLUS.value]) == 0) {
            retval = STRINGS.S_PLUS.value;
        }
        else if (s.compareTo(stringbits[STRINGS.S_MINUS.value]) == 0) {
            retval = STRINGS.S_MINUS.value;
        }
        else if (s.compareTo(stringbits[STRINGS.S_MULTIPLY.value]) == 0) {
            retval = STRINGS.S_MULTIPLY.value;
        }
        else if (s.compareTo(stringbits[STRINGS.S_FAILFAST.value]) == 0) {
            retval = STRINGS.S_FAILFAST.value;
        }
        else if (s.compareTo(stringbits[STRINGS.S_NOT.value]) == 0) {
            retval = STRINGS.S_NOT.value;
        }
        else if (s.compareTo(stringbits[STRINGS.S_SET_ISECT_U.value]) == 0) {
            retval = STRINGS.S_SET_ISECT_U.value;
        }
        else if (s.compareTo(stringbits[STRINGS.S_SET_SYMDIFF_U.value]) == 0) {
            retval = STRINGS.S_SET_SYMDIFF_U.value;
        }
        return retval;
    }
    
    // ** HELPER METHODS TO CHECK CHARACTER VALUES AND MOVE THE POINTER AROUND (contained in the inlines.h file) **
    
    /**
	 * Returns true if the next characters in the array match the given string
     * @param s
     * @return
     */
    private boolean ux_simplecasecmp(String s) {
        char[] ar = s.toCharArray();
        for (int i=0;i<ar.length;i++) {
            if (inArray[index + i] != ar[i] && inArray[index+i] != ar[i]+(char)32) {    // this +32 thing is probably something i should check
                return false;
            }
        }
        return true;
    }
    
    private boolean ISSTRING(int position, int offset) {
        if (inArray[position-1] == '"' && inArray[position+offset+1] == '"') {
            return true;
        }
        if (inArray[position-1] == '<' && inArray[position+offset+1] == '>') {
            return true;
        }
        return false;
    }
    
    private boolean ISCHR(int position, int offset, char c, char d) {
        if (position + offset >= length) {
            return false;
        }
        return inArray[position + offset] == c || inArray[position + offset] == d ;
    }
    
    
    private void BACKTONL_LP() {
        while (lpindex < inArray.length && !ISNL(lpindex) && (inArray[lpindex] != ';' || ISESC(lpindex))) {
            lpindex--;
        }
        lpindex++;
        
    }
    
    private int SKIPWS(char a,char b) {
        char s = (char)0;
        while (notDone() && inArray[index] != a && inArray[index] != b) {
            if (ISNL(index)) {
                ++s;
            }
            if (inArray[index] == '#' && !ISESC(index)) {
                s += SKIPLN();
                index--;
            }
            if (!ISSPACE(index)) {
                break;
            }
            ++index;
        }
        return s;
    }
    
    private int SKIPWS_S(char a, char b) {
        char d = (char)0;
        while ((sindex < inArray.length) && inArray[sindex] != a && inArray[sindex] != b) {
            if (ISNL(sindex)) {
                ++d;
            }
            if (inArray[sindex] == '#' && !ISESC(sindex)) {
                d += SKIPLN_S();
                sindex--;
            }
            if (!ISSPACE(sindex)) {
                break;
            }
            ++sindex;
        }
        return d;
    }
    
    private int SKIPWS_N(char a, char b) {
        char d = (char)0;
        while ((nindex < inArray.length) && inArray[nindex] != a && inArray[nindex] != b) {
            if (ISNL(nindex)) {
                ++d;
            }
            if (inArray[nindex] == '#' && !ISESC(nindex)) {
                d += SKIPLN_N();
                nindex--;
            }
            if (!ISSPACE(nindex)) {
                break;
            }
            ++nindex;
        }
        return d;
    }
    
    private int SKIPWS_LP(char a, char b) {
        char d = (char)0;
        while ((lpindex < inArray.length) && inArray[lpindex] != a && inArray[lpindex] != b) {
            if (ISNL(lpindex)) {
                ++d;
            }
            if (inArray[lpindex] == '#' && !ISESC(lpindex)) {
                d += SKIPLN_N();
                lpindex--;
            }
            if (!ISSPACE(lpindex)) {
                break;
            }
            ++lpindex;
        }
        return d;
    }
    
    private int SKIPTOWS_N(char b, boolean allowhash) {
        int s = 0;
        while ((nindex < inArray.length) && !ISSPACE(nindex)) {
            if (!allowhash && inArray[nindex] == '#' && !ISESC(nindex)) {
                s += SKIPLN_N();
                nindex--;
            }
            if (ISNL(nindex)) {
                ++s;
                ++nindex;
            }
            if (inArray[nindex] == ';' && !ISESC(nindex)) {
                break;
            }
            if (inArray[nindex] == b && !ISESC(nindex)) {
                break;
            }
            ++nindex;
        }
        return s;
    }
    
    private int SKIPTOWS_P(char b, boolean allowhash) {
        int s = 0;
        while (notDone() && !ISSPACE(index)) {
            if (!allowhash && inArray[index] == '#' && !ISESC(index)) {
                s += SKIPLN();
                index--;
            }
            if (ISNL(index)) {
                ++s;
                ++index;
            }
            if (inArray[index] == ';' && !ISESC(index)) {
                break;
            }
            if (inArray[index] == b && !ISESC(index)) {
                break;
            }
            ++index;
        }
        return s;
    }
    
    private int SKIPTO_NOSPAN_N(char b) {
        int s = 0;
        while ((nindex < inArray.length) && (inArray[nindex] != b || ISESC(nindex))) {
            if (ISNL(nindex)) {
                break;
            }
            ++nindex;
        }
        return s;
    }
    
    private int SKIPTO_NOSPAN_P(char b) {
        int s = 0;
        while (notDone() && (inArray[index] != b || ISESC(index))) {
            if (ISNL(index)) {
                break;
            }
            ++index;
        }
        return s;
    }
    
    private int SKIPLN() {
        while (notDone() && !ISNL(index)) {
            ++index;
        }
        ++index;
        return 1;
    }
    
    private int SKIPLN_S() {
        while ((sindex < inArray.length) && !ISNL(sindex)) {
            ++sindex;
        }
        ++sindex;
        return 1;
    }
    
    private int SKIPLN_N() {
        while ((nindex < inArray.length) && !ISNL(nindex)) {
            ++nindex;
        }
        ++nindex;
        return 1;
    }
    
    private boolean ISSPACE(int position) {
        char c = inArray[position];
        return (c == (char)0x20 || c == (char)0x09 || Character.isWhitespace(c));
    }
    
    private boolean ISNL(int position) {
        char c = inArray[position];
        return (c == (char)0x2028L || 
                c == (char)0x2029L ||
                c == (char)0x0085L ||
                c == (char)0x000CL ||
                c == (char)0x00AL);
    }

    private boolean ISESC(int position) {
        int a = 1;
        while (((position - a) < inArray.length) && (inArray[position - a] == '\\')) {
            a++;
        }
        return (a%2==0);
    }

    // ** STRING helper definitions
    
    public static String _S_SET_ISECT_U = "\u2229";
    public static String _S_SET_SYMDIFF_U = "\u2206";
    
    public static String[] stringbits = {
            "1f283fc29adb937a892e09bbc124b85c this is a dummy string to hold position 0",
            "|",
            "TO",
            "OR",
            "+",
            "-",
            "*",
            "**",
            "^",
            "\\",
            "#",
            "!",
            "NOT",
            "NEGATE",
            "ALL",
            "NONE",
            "LINK",
            "BARRIER",
            "CBARRIER",
            "<STREAMCMD:FLUSH>",
            "<STREAMCMD:EXIT>",
            "<STREAMCMD:IGNORE>",
            "<STREAMCMD:RESUME>",
            "TARGET",
            "AND",
            "IF",
            "_S_DELIMITERS_",
            "_S_SOFT_DELIMITERS_",
            ">>>",
            "<<<",
            " LINK 0 ",
            " ",
            "_LEFT_",
            "_RIGHT_",
            "_PAREN_",
            "_TARGET_",
            "_MARK_",
            "_ATTACHTO_",
            "<.*>",
            "\".*\"",
            "\"<.*>\"",
            "AFTER",
            "BEFORE",
            "WITH",
            "?",
            "$1",
            "$2",
            "$3",
            "$4",
            "$5",
            "$6",
            "$7",
            "$8",
            "$9",
            "%u",
            "%U",
            "%l",
            "%L",
            "_G_",
            "POSITIVE",
            "NEGATIVE",
            "ONCE",
            "ALWAYS",
            _S_SET_ISECT_U,
            _S_SET_SYMDIFF_U,
            "FROM"    
    };
    
    public String[] flags = {
        "NEAREST",
        "ALLOWLOOP",
        "DELAYED",
        "IMMEDIATE",
        "LOOKDELETED",
        "LOOKDELAYED",
        "UNSAFE",
        "SAFE",
        "REMEMBERX",
        "RESETX",
        "KEEPORDER",
        "VARYORDER",
        "ENCL_INNER",
        "ENCL_OUTER",
        "ENCL_FINAL",
        "ENCL_ANY",
        "ALLOWCROSS",
        "WITHCHILD",
        "NOCHILD",
        "ITERATE",
        "NOITERATE",
        "UNMAPLAST",
        "REVERSE"
    };
    
    public String[] keywords = {
            "1f283fc29adb937a892e09bbc124b85c this is a dummy keyword to hold position 0",
            "SETS",
            "LIST",
            "SET",
            "DELIMITERS",
            "SOFT-DELIMITERS",
            "PREFERRED-TARGETS",
            "MAPPING-PREFIX",
            "MAPPINGS",
            "CONSTRAINTS",
            "CORRECTIONS",
            "SECTION",
            "BEFORE-SECTIONS",
            "AFTER-SECTIONS",
            "NULL-SECTION",
            "ADD",
            "MAP",
            "REPLACE",
            "SELECT",
            "REMOVE",
            "IFF",
            "APPEND",
            "SUBSTITUTE",
            "START",
            "END",
            "ANCHOR",
            "EXECUTE",
            "JUMP",
            "REMVARIABLE",
            "SETVARIABLE",
            "DELIMIT",
            "MATCH",
            "SETPARENT",
            "SETCHILD",
            "ADDRELATION",
            "SETRELATION",
            "REMRELATION",
            "ADDRELATIONS",
            "SETRELATIONS",
            "REMRELATIONS",
            "TEMPLATE",
            "MOVE",
            "MOVE-AFTER",
            "MOVE-BEFORE",
            "SWITCH",
            "REMCOHORT",
            "STATIC-SETS",
            "UNMAP",
            "COPY",
            "ADDCOHORT",
            "ADDCOHORT-AFTER",
            "ADDCOHORT-BEFORE",
            "EXTERNAL",
            "EXTERNAL-ONCE",
            "EXTERNAL-ALWAYS"
    };
    
    public static STRINGS S_ASTERISK = STRINGS.S_MULTIPLY;
    
}

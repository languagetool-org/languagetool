package de.danielnaber.languagetool.dev.conversion.cg;

import java.util.HashSet;
import java.util.regex.Pattern;

import de.danielnaber.languagetool.dev.conversion.cg.CgStrings.STRINGS;

public class CgTag {

    public HashSet<Integer> type;
    public int hash;
    public int plain_hash;
    public int comparison_hash;
    public int seed;
    public String tag;
    
    public Pattern regexp;
    
    public CgTag() {
        this.tag = "";
        this.type = new HashSet<Integer>();
        this.hash = 0;
        this.plain_hash = 0;
        this.comparison_hash = 0;
        this.seed = 0;
    }
    
    public CgTag parseTagRaw(CgTag tag, String to) {
        tag.type = new HashSet<Integer>();
        if (to.length() > 0) {
            String tmp = to;
            int len = tmp.length();
            if (tmp.charAt(0) != (char)0 && (tmp.charAt(0) == '"' || tmp.charAt(0) == '<')) {
                if (((tmp.charAt(0) == '"') && (tmp.charAt(len-1) == '"')) || ((tmp.charAt(0) == '<') && (tmp.charAt(len-1)) == '>')) {
                    tag.type.add(TAGS.T_TEXTUAL.value);
                    if (tmp.charAt(0) == '"' && tmp.charAt(len-1) == '"') {
                        if (tmp.charAt(1) == '<' && tmp.charAt(len-2) == '>') {
                            tag.type.add(TAGS.T_WORDFORM.value);
                        }
                        else {
                            tag.type.add(TAGS.T_BASEFORM.value);
                        }
                    }
                }
            }
            tag.tag = tmp;
            if (!tag.tag.isEmpty() && tag.tag.charAt(0) == '<' && tag.tag.charAt(len-1) == '>') {
                tag = parseNumeric(tag); // TODO: this isn't right; won't work
            }
            if (!tag.tag.isEmpty() && tag.tag.charAt(0) == '#') {
                // something with dependency tags. I don't want to deal with these. Let's assume we're not going to deal with dependency stuff
            }
        }
        if (tag.type.contains(TAGS.T_SPECIAL.value)) {
            tag.type.remove(TAGS.T_SPECIAL.value);
        }
        if (tag.type.contains(TAGS.T_NUMERICAL.value)) {
            tag.type.add(TAGS.T_SPECIAL.value);
        }
        return tag;
    }
    
    public CgTag parseTag(CgTag tag, String to, CgGrammar grammar) {
        // some long and semi-complicated function in Tag.cpp that I'll probably eventually need to write
        tag.type = new HashSet<Integer>();;
        if (to != null && to.length() > 0) {
            char[] tmp = to.toCharArray();
            int tmpIndex = 0;
            while (tmp.length > 0 && (tmp[tmpIndex] == '!' || tmp[tmpIndex] == '^')) {
                if (tmp[tmpIndex] == '!') {
                    tag.type.add(TAGS.T_NEGATIVE.value);
                    tmpIndex++;
                }
                if (tmp[tmpIndex] == '^') {
                    tag.type.add(TAGS.T_FAILFAST.value);
                    tmpIndex++;
                }
            }
            if (tmp[tmpIndex] == 'T' && tmp[tmpIndex+1] == ':') {
                System.out.println("Warning: the tag on line " + grammar.lines + " looks like a misplaced template marker.");
            }
            
            if (tmp[tmpIndex] == 'M' && tmp[tmpIndex+1] == 'E' && tmp[tmpIndex+2] == 'T' && tmp[tmpIndex+3] == 'A'&&
                    tmp[tmpIndex+4] == ':') {
                tag.type.add(TAGS.T_META.value);
                tmpIndex += 5;
            }
            if (tmp[tmpIndex] == 'V' && tmp[tmpIndex+1] == 'A' && tmp[tmpIndex+2] == 'R' && tmp[tmpIndex+3] == ':') {
                tag.type.add(TAGS.T_VARIABLE.value);
                tmpIndex += 4;
            }
            if (tmp[tmpIndex] == 'S' && tmp[tmpIndex+1] == 'E' && tmp[tmpIndex+2] == 'T' && tmp[tmpIndex+3] == ':') {
                tag.type.add(TAGS.T_SET.value);
                tmpIndex += 4;
            }
            if (tmp[tmpIndex] == 'V' && tmp[tmpIndex+1] == 'S' && tmp[tmpIndex+2] == 'T' && tmp[tmpIndex+3] == 'R' &&
                    tmp[tmpIndex+4] == ':') {
                tag.type.add(TAGS.T_VARSTRING.value);
                tag.type.add(TAGS.T_VSTR.value);
                tmpIndex += 5;
                
                StringBuilder sb = new StringBuilder();
                for (int i=tmpIndex;i<tmp.length;i++) {
                    sb.append(tmp[i]);
                }
                tag.tag = sb.toString();
                // whole goto label_isVarString here, leaving this out. just don't use VSTR: label
            }
            if (tmpIndex < tmp.length && (tmp[tmpIndex] == '"' || tmp[tmpIndex] == '<')) {
                // parse suffixes r, i, and v, but only one of each
                int endIndex = tmp.length - 1;
                while (tmp[endIndex] == 'i' || tmp[endIndex] == 'r' || tmp[endIndex] == 'v') {
                    if (!tag.type.contains(TAGS.T_VARSTRING.value) && tmp[endIndex] == 'v') {
                        tag.type.add(TAGS.T_VARSTRING.value);
                        endIndex--;
                        continue;
                    }
                    if (!tag.type.contains(TAGS.T_REGEXP.value) && tmp[endIndex] == 'r') {
                        tag.type.add(TAGS.T_REGEXP.value);
                        endIndex--;
                        continue;
                    }
                    if (!tag.type.contains(TAGS.T_CASE_INSENSITIVE.value) && tmp[endIndex] == 'i') {
                        tag.type.add(TAGS.T_CASE_INSENSITIVE.value);
                        endIndex--;
                        continue;
                    }
                    break;
                }
                if (tmp[tmpIndex] == '"' && tmp[endIndex] == '"') {
                    if (tmp[tmpIndex+1] == '<' && tmp[endIndex-1] == '>') {
                        tag.type.add(TAGS.T_WORDFORM.value);
                    }
                    else {
                        tag.type.add(TAGS.T_BASEFORM.value);
                    }
                }
                
                if ((tmp[tmpIndex] == '"' && tmp[endIndex] == '"') || (tmp[tmpIndex] == '<' && tmp[endIndex] == '>')) {
                    tag.type.add(TAGS.T_TEXTUAL.value);
                } else {
                    if (tag.type.contains(TAGS.T_VARSTRING.value)) {
                        tag.type.remove(TAGS.T_VARSTRING.value);
                    }
                    if (tag.type.contains(TAGS.T_REGEXP.value)) {
                        tag.type.remove(TAGS.T_REGEXP.value);
                    }
                    if (tag.type.contains(TAGS.T_CASE_INSENSITIVE.value)) {
                        tag.type.remove(TAGS.T_CASE_INSENSITIVE.value);
                    }
                    if (tag.type.contains(TAGS.T_WORDFORM.value)) {
                        tag.type.remove(TAGS.T_WORDFORM.value);
                    }
                    if (tag.type.contains(TAGS.T_BASEFORM.value)) {
                        tag.type.remove(TAGS.T_BASEFORM.value);
                    }
                    endIndex = tmp.length - 1;
                }
            }
            for (int i=0;i<tmp.length;++i) {
                if (tmp[i] == '\\') {
                    ++i;
                }
                if (i >= tmp.length) {
                    break;
                }
                tag.tag = tag.tag.concat(Character.toString(tmp[i]));
            }
            if (tag.tag.isEmpty()) {
                System.err.println("Error: parsing tag on line " + grammar.lines + " resulted in an empty tag.");
                System.exit(1);
            }
            tag.comparison_hash = CgStrings.hash_sdbm_uchar(tag.tag, 0, 0);
            if (!tag.tag.isEmpty() && tag.tag.charAt(0) == '<' && tag.tag.charAt(tag.tag.length() - 1) == '>') {
                tag = parseNumeric(tag);
            }
            
            if (tag.tag.equals(CgTextualParser.stringbits[STRINGS.S_MULTIPLY.value])) {
                tag.type.add(TAGS.T_ANY.value);
            }
            else if (tag.tag.equals(CgTextualParser.stringbits[STRINGS.S_UU_LEFT.value])) {
                tag.type.add(TAGS.T_PAR_LEFT.value);
            }
            else if (tag.tag.equals(CgTextualParser.stringbits[STRINGS.S_UU_RIGHT.value])) {
                tag.type.add(TAGS.T_PAR_RIGHT.value);
            }
            else if (tag.tag.equals(CgTextualParser.stringbits[STRINGS.S_UU_TARGET.value])) {
                tag.type.add(TAGS.T_TARGET.value);
            }
            else if (tag.tag.equals(CgTextualParser.stringbits[STRINGS.S_UU_MARK.value])) {
                tag.type.add(TAGS.T_MARK.value);
            }
            else if (tag.tag.equals(CgTextualParser.stringbits[STRINGS.S_UU_ATTACHTO.value])) {
                tag.type.add(TAGS.T_ATTACHTO.value);
            }
            
            if (tag.type.contains(TAGS.T_REGEXP.value)) {
                if (tag.tag.equals(CgTextualParser.stringbits[STRINGS.S_RXTEXT_ANY.value]) ||
                    tag.tag.equals(CgTextualParser.stringbits[STRINGS.S_RXBASE_ANY.value]) ||
                    tag.tag.equals(CgTextualParser.stringbits[STRINGS.S_RXWORD_ANY.value])) {
                    tag.type.add(TAGS.T_REGEXP_ANY.value);
                    if (tag.type.contains(TAGS.T_REGEXP.value)) {
                        tag.type.remove(TAGS.T_REGEXP.value);
                    }
                }
                else {
                    String rt = "^";
                    rt = rt.concat(tag.tag);
                    rt = rt.concat("$");
                    
                    if (tag.type.contains(TAGS.T_CASE_INSENSITIVE.value)) {
                        regexp = Pattern.compile(rt,Pattern.CASE_INSENSITIVE);
                    } else {
                        regexp = Pattern.compile(rt);
                    }
                    // some error handling here. don't know how RE works in C++
                    
                }
            }
        }
        if (tag.type.contains(TAGS.T_SPECIAL.value)) {
            tag.type.remove(TAGS.T_SPECIAL.value);
        }
        if (tag.type.contains(TAGS.T_ANY.value) || tag.type.contains(TAGS.T_TARGET.value) || tag.type.contains(TAGS.T_MARK.value) ||
                tag.type.contains(TAGS.T_ATTACHTO.value) || tag.type.contains(TAGS.T_PAR_LEFT.value) || tag.type.contains(TAGS.T_PAR_RIGHT.value) ||
                tag.type.contains(TAGS.T_NUMERICAL.value) || tag.type.contains(TAGS.T_VARIABLE.value) || tag.type.contains(TAGS.T_META.value) ||
                tag.type.contains(TAGS.T_NEGATIVE.value) || tag.type.contains(TAGS.T_FAILFAST.value) || tag.type.contains(TAGS.T_CASE_INSENSITIVE.value) ||
                tag.type.contains(TAGS.T_REGEXP.value) || tag.type.contains(TAGS.T_REGEXP_ANY.value) || tag.type.contains(TAGS.T_VARSTRING.value) ||
                tag.type.contains(TAGS.T_SET.value)) {
            tag.type.add(TAGS.T_SPECIAL.value);
        }
        if (tag.type.contains(TAGS.T_VARSTRING.value) &&
                (tag.type.contains(TAGS.T_REGEXP.value) || tag.type.contains(TAGS.T_REGEXP_ANY.value) || 
                 tag.type.contains(TAGS.T_VARIABLE.value) || tag.type.contains(TAGS.T_META.value))) {
            System.err.println("Error: cannot mix varstring with any other special features on line " + grammar.lines);
            System.exit(1);
        }
       
        return tag;
        
    }
    
    public CgTag parseNumeric(CgTag tag) {
        //TODO: this'll have to be filled in, once I know what "this.tag" contains
        // this method fills in the fields:
        // comparison_op; comparison_val; comparison_hash; type
        return tag;
    }
    
    public int rehash() {
        this.hash = 0;
        this.plain_hash = 0;
        if (this.type.contains(TAGS.T_NEGATIVE.value)) {
            this.hash = CgStrings.hash_sdbm_char("!", this.hash, 0);
        }
        if (this.type.contains(TAGS.T_FAILFAST.value)) {
            this.hash = CgStrings.hash_sdbm_char("^", this.hash, 0);
        }
        if (this.type.contains(TAGS.T_META.value)) {
            this.hash = CgStrings.hash_sdbm_char("META:", this.hash, 0);
        }
        if (this.type.contains(TAGS.T_VARIABLE.value)) {
            this.hash = CgStrings.hash_sdbm_char("VAR:", this.hash, 0);
        }
        if (this.type.contains(TAGS.T_SET.value)) {
            this.hash = CgStrings.hash_sdbm_char("SET:", this.hash, 0);
        }
        if (this.type.contains(TAGS.T_NEGATIVE.value)) {
            this.hash = CgStrings.hash_sdbm_char("!", this.hash, 0);
        }
        this.plain_hash = CgStrings.hash_sdbm_uchar(this.tag,0,0);
        if (this.hash != 0) {
            this.hash = CgStrings.hash_sdbm_uint32_t(this.plain_hash,this.hash);
        }
        else {
            this.hash = this.plain_hash;
        }
        if (this.type.contains(TAGS.T_CASE_INSENSITIVE.value)) {
            this.hash = CgStrings.hash_sdbm_char("i", this.hash, 0);
        }
        if (this.type.contains(TAGS.T_REGEXP.value)) {
            this.hash = CgStrings.hash_sdbm_char("r", this.hash, 0);
        }
        if (this.type.contains(TAGS.T_VARSTRING.value)) {
            CgStrings.hash_sdbm_char("v", this.hash, 0);
        }
        
        if (this.seed != 0) {
            hash += this.seed;
        }
        if (this.type.contains(TAGS.T_SPECIAL.value)) {
            this.type.remove(TAGS.T_SPECIAL.value);
        }
        if (this.type.contains(TAGS.T_ANY.value) || this.type.contains(TAGS.T_TARGET.value) || this.type.contains(TAGS.T_MARK.value) ||
            this.type.contains(TAGS.T_ATTACHTO.value) || this.type.contains(TAGS.T_PAR_LEFT.value) ||
            this.type.contains(TAGS.T_PAR_RIGHT.value) || this.type.contains(TAGS.T_NUMERICAL.value) ||
            this.type.contains(TAGS.T_VARIABLE.value) || this.type.contains(TAGS.T_META.value) ||
            this.type.contains(TAGS.T_NEGATIVE.value) || this.type.contains(TAGS.T_FAILFAST.value) ||
            this.type.contains(TAGS.T_CASE_INSENSITIVE.value) || this.type.contains(TAGS.T_REGEXP.value) ||
            this.type.contains(TAGS.T_REGEXP_ANY.value) || this.type.contains(TAGS.T_VARSTRING.value) ||
            this.type.contains(TAGS.T_SET.value)) {
            this.type.add(TAGS.T_SPECIAL.value);
        }
        
        return this.hash;
        
    }
    
    public enum TAGS {
        T_ANY         (1 <<  0), 
        T_NUMERICAL   (1 <<  1), 
        T_MAPPING     (1 <<  2), 
        T_VARIABLE    (1 <<  3), 
        T_META        (1 <<  4), 
        T_WORDFORM    (1 <<  5), 
        T_BASEFORM    (1 <<  6), 
        T_TEXTUAL     (1 <<  7), 
        T_DEPENDENCY  (1 <<  8), 
        T_NEGATIVE    (1 <<  9), 
        T_FAILFAST    (1 << 10),
        T_CASE_INSENSITIVE  (1 << 11),
        T_REGEXP      (1 << 12),
        T_PAR_LEFT    (1 << 13),
        T_PAR_RIGHT   (1 << 14),
        T_REGEXP_ANY  (1 << 15),
        T_VARSTRING   (1 << 16),
        T_TARGET      (1 << 17),
        T_MARK        (1 << 18),
        T_ATTACHTO    (1 << 19),
        T_SPECIAL     (1 << 20),
        T_USED        (1 << 21),
        T_GRAMMAR     (1 << 22),
        T_SET         (1 << 23),
        T_VSTR        (1 << 24);
        public int value;
        TAGS(int v) {
            this.value = v;
        }
    }
    
}

package de.danielnaber.languagetool.dev.conversion.cg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import de.danielnaber.languagetool.dev.conversion.cg.CgStrings.STRINGS;
import de.danielnaber.languagetool.dev.conversion.cg.CgTag.TAGS;
import de.danielnaber.languagetool.dev.conversion.cg.CgSet.ST;

public class CgGrammar {
    
    public boolean has_dep;
    public boolean has_encl_final;
    public boolean is_binary;
    public int grammar_size;
    public String mapping_prefix;
    public int lines;
    public int verbosity_level;
    public double total_time;
    public int tag_any;
    
    ArrayList<CgTag> single_tags_list = new ArrayList<CgTag>();
    HashMap<Integer,CgTag> single_tags = new HashMap<Integer,CgTag>();
    ArrayList<CgCompositeTag> tags_list = new ArrayList<CgCompositeTag>();
    HashMap<Integer,CgCompositeTag> tags = new HashMap<Integer,CgCompositeTag>();
    
    ArrayList<CgSet> sets_list = new ArrayList<CgSet>();
    HashSet<CgSet> sets_all = new HashSet<CgSet>();
    HashMap<Integer,CgSet> sets_by_name = new HashMap<Integer,CgSet>(); // indexed by a hash of the set's name
    HashMap<Integer,Integer> set_name_seeds = new HashMap<Integer,Integer>();
    HashMap<Integer,CgSet> sets_by_contents = new HashMap<Integer,CgSet>(); // indexed by a hash of the set's contents
    HashMap<Integer,Integer> set_alias = new HashMap<Integer,Integer>();
    HashMap<Integer,HashSet<Integer>> sets_by_tag = new HashMap<Integer,HashSet<Integer>>();
    HashSet<Integer> sets_any = new HashSet<Integer>();
    
    ArrayList<String> static_sets = new ArrayList<String>();
    
    ArrayList<CgContextualTest> template_list = new ArrayList<CgContextualTest>();
    HashMap<Integer,CgContextualTest> templates = new HashMap<Integer,CgContextualTest>();
    
    HashMap<Integer,ArrayList<Integer>> rules_by_set = new HashMap<Integer,ArrayList<Integer>>();
    HashMap<Integer,ArrayList<Integer>> rules_by_tag = new HashMap<Integer,ArrayList<Integer>>();
    ArrayList<Integer> rules_any = new ArrayList<Integer>();
    public ArrayList<CgRule> rule_by_number = new ArrayList<CgRule>();
    public ArrayList<CgRule> rules = new ArrayList<CgRule>();
    
    ArrayList<Integer> preferred_targets;
    HashMap<Integer,Integer> parentheses = new HashMap<Integer,Integer>();
    HashMap<Integer,Integer> parentheses_reverse = new HashMap<Integer,Integer>();
    
    HashMap<Integer,Integer> anchor_by_hash = new HashMap<Integer,Integer>();
    HashMap<Integer,CgAnchor> anchor_by_line = new HashMap<Integer,CgAnchor>();
    
    ArrayList<Integer> sections = new ArrayList<Integer>();
    ArrayList<CgRule> before_sections = new ArrayList<CgRule>();    
    ArrayList<CgRule> after_sections = new ArrayList<CgRule>();
    ArrayList<CgRule> null_section = new ArrayList<CgRule>();
    
    CgSet delimiters;
    CgSet soft_delimiters;
    
    public CgGrammar() {
        
    }
    
    public void addPreferredTarget(String s) {
        CgTag tag = allocateTag(s, false);
        preferred_targets.add(tag.hash);
    }
    
    public void addSet(CgSet to) {
//        String s = CgTextualParser.stringbits[STRINGS.S_DELIMITSET.value];

        if (this.delimiters == null && to.name.equals(CgTextualParser.stringbits[STRINGS.S_DELIMITSET.value])) {
            this.delimiters = to;
        }
        else if (this.soft_delimiters == null && to.name.equals(CgTextualParser.stringbits[STRINGS.S_SOFTDELIMITSET.value])) {
            this.soft_delimiters = to;
        }

        to.hashContents();
        if (!this.sets_all.contains(to)) {
            this.sets_list.add(to);
        }
        this.sets_all.add(to);
        this.sets_by_name.put(to.hash, to);
        this.sets_by_contents.put(to.chash, to);  
    }
    
    public CgSet getSet(int which) {

    	// return the set if you give it the name hash
        if (this.sets_by_name.containsKey(which)) {
            return this.sets_by_name.get(which);
        }
        return null;
    }
    
    public CgSet allocateSet(CgSet from) {
        CgSet ns = null;
        if (from != null) {
            ns = new CgSet(from);
        } else {
            ns = new CgSet();
        }
        this.sets_all.add(ns);   // this might not be the right idea, could put the set in the set of sets too soon
        return ns;
        
    }
    
    public CgSet allocateSet() {
        return new CgSet();
    }
    
    public void destroySet(CgSet s) {
        if (this.sets_all.contains(s)) {
            this.sets_all.remove(s);
        }
    }
    
    public void addSetToList(CgSet s) {
        if (s.number == 0) {
            if (this.sets_list.isEmpty() || this.sets_list.get(0) != s) {
                if (!s.sets.isEmpty()) {
                    for (int setID : s.sets) {
                        this.addSetToList(this.getSet(setID));
                    }
                }
                s.number = this.sets_list.size();   // so the set's number is the last one in the (new, augmented) sets_list
                this.sets_list.add(s);
            }
        }
    }
    
    public CgSet parseSet(String name) {
        int sh = name.hashCode();
        // some check if the set is S_IGNORE has been left out here
        if (name.length() > 2 && (name.charAt(0) == '$' && name.charAt(1) == '$') ||
                              (name.charAt(0) == '&' && name.charAt(1) == '&')) {
            String wname = name.substring(2);
            int wrap = wname.hashCode();
            CgSet wtmp = this.getSet(wrap);
            if (wtmp == null) {
                System.err.println("Error: attempted to reference undefined set " + wname + " on line " + this.lines);
                System.exit(1);
            }
            CgSet tmp = this.getSet(sh);
            if (tmp == null) {
                CgSet ns = this.allocateSet();
                ns.line = this.lines;
                ns.setName(name);
                ns.sets.add(wtmp.hash);
                if (name.charAt(0) == '$' && name.charAt(1) == '$') {
                    ns.type.add(ST.ST_TAG_UNIFY.value);
                }
                else if (name.charAt(0) == '&' && name.charAt(1) == '&') {
                    ns.type.add(ST.ST_SET_UNIFY.value);
                }
                this.addSet(ns);
            }
        }
        // (the aliases are currently not being set anywhere)
        if (this.set_alias.containsKey(sh)) {
            sh = this.set_alias.get(sh);
        }
        CgSet tmp = this.getSet(sh);
        if (tmp == null) {
            System.err.println("Error: attempted to reference undefined set " + name + " on line " + this.lines);
            System.exit(1);
        }
        return tmp;
    }
    
    public void addAnchor(String s, int line) {
        int ah = s.hashCode();
        if (this.anchor_by_hash.containsKey(ah)) {
            System.err.println("Error: attempted to redefine anchor on line " + line);
            System.exit(1);
        }
        CgAnchor anc = new CgAnchor();
        anc.setName(s);
        anc.line = line;
        this.anchor_by_hash.put(ah,line);
        this.anchor_by_line.put(line,anc);
    }
    
    public CgTag allocateTag(String txt, boolean raw) {
        if (txt.startsWith("(")) {
            System.err.println("Error: tag cannot start with (");
            System.exit(1);
        }
        CgTag tag = new CgTag();
        if (raw) {
            tag = tag.parseTagRaw(tag,txt);
        }
        else {
            tag = tag.parseTag(tag,txt,this);
        }
        tag.type.add(TAGS.T_GRAMMAR.value);
        
        int hash = tag.rehash();	// the hashing stuff isn't fully correctly supported in this implementation 
        // skipped a bunch of things just to see if it runs
        this.single_tags_list.add(tag);
        this.single_tags.put(hash, tag);
        return this.single_tags.get(hash);
        
    }
    
    public CgTag allocateTag() {
        return new CgTag();
    }
    
    public void destroyTag(CgTag tag) {
        // don't need this method (no memory management!)
    }
    
    public CgCompositeTag addTagToCompositeTag(CgTag simpletag, CgCompositeTag tag) {
        if (simpletag != null && !(simpletag.tag == null || simpletag.tag == "")) {
            tag.addTag(simpletag);
        }
        else {
            System.err.println("Error: attempted to add empty tag to grammar on line " + this.lines);
            System.exit(1);
        }
        return tag;
    }
    
    // don't think this method currently gets called anywhere
    public CgSet addTagToSet(CgTag rtag, CgSet set) {
        set.single_tags.add(rtag);
        set.single_tags_hash.add(rtag.hashCode());
        if (rtag.type.contains(TAGS.T_ANY.value)) {
            set.type.add(ST.ST_ANY.value);
        } 
        if (rtag.type.contains(TAGS.T_SPECIAL.value)) {
            set.type.add(ST.ST_SPECIAL.value);
        } 
        if (rtag.type.contains(TAGS.T_FAILFAST.value)) {
            set.ff_tags.add(rtag);
        } 
        return set;
    }
    
    public CgCompositeTag addCompositeTag(CgCompositeTag tag) {
        if (tag != null && tag.tags.size() > 0) {
            tag.rehash();
            if (this.tags.containsKey(tag.hash)) {
                // if it's already in the list, do nothing
            }
            else {
                tag.number = this.tags_list.size();
                this.tags.put(tag.hash,tag);
                this.tags_list.add(tag);
            }
        } else {
            System.err.println("Error: attempted to add empty composite tag to grammar on line " + this.lines);
            System.exit(1);
        }
        
        return this.tags.get(tag.hash);
    }
    
    public CgSet addCompositeTagToSet(CgSet set, CgCompositeTag tag) {
        if (tag != null && !tag.tags.isEmpty()) {
            if (tag.tags.size() == 1) {
                CgTag rtag = tag.tags.get(0);
                CgSet s = this.addTagToSet(rtag, set);
            }
            else {
                tag = addCompositeTag(tag);
                set.tags.add(tag);
                if (tag.is_special) {
                    set.type.add(ST.ST_SPECIAL.value);
                }
            }
        } else {
            System.err.println("Error: attempted to add empty composite tag to set on line " + this.lines);
            System.exit(1);
        }
        this.addCompositeTag(tag);
        return set;
    }
    
    public CgCompositeTag allocateCompositeTag() {
        return new CgCompositeTag();
    }
    
    public void destroyCompositeTag(CgCompositeTag tag) {
        // don't need this method either
    }
    
    public CgRule allocateRule() {
        return new CgRule();
    }
    
    public void addRule(CgRule rule) {
        rule.number = this.rule_by_number.size();
        this.rule_by_number.add(rule);
    }
    
    public void destroyRule(CgRule rule) {
        // don't need this method
    }
    
    // these are kind of duplicate methods, can just call new ContextualTest() where I need to
    public CgContextualTest allocateContextualTest() {
        return new CgContextualTest();
    }
    
    public void addContextualTest(CgContextualTest test, String name) {
        int cn = name.hashCode();
        if (this.templates.containsKey(cn)) {
            System.err.println("Error: attempted to redefine template " + name + " on line " + this.lines);
            System.exit(1);
        }
        this.templates.put(cn, test);
        this.template_list.add(test);
    }
    
    public void resetStatistics() {
        // not implemented yet
    }
    
    public void reindex(boolean unused_sets) {
        // not implemented yet
    }
    
    public void renameAllRules() {
        // not implemented yet
    }
    
    
    
    
    
}

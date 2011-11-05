package de.danielnaber.languagetool.dev.conversion.cg;

import java.util.HashMap;
import java.util.HashSet;

import de.danielnaber.languagetool.dev.conversion.cg.CgStrings.KEYWORDS;

public class CgRule {

    public HashSet<Integer> flags = new HashSet<Integer>();
    public int line;
    public KEYWORDS type;
    public int wordform;
    public int varname;
    public int childset1;
    public int childset2;
    public int jumpstart;
    public int jumpend;
    public int section;
    public int target;
    public int number;
    
    public CgSet sublist;
    public CgSet maplist;
    
    public CgContextualTest dep_test_head;
    public CgContextualTest dep_target;
    public HashSet<CgContextualTest> all_tests = new HashSet<CgContextualTest>();
    public HashMap<Integer,CgContextualTest> test_map = new HashMap<Integer,CgContextualTest>();
    public HashSet<CgContextualTest> test_heads = new HashSet<CgContextualTest>();
    
    public String name;
    
    public void setName(String name) {
        this.name = name;
    }
    
    public CgRule() {
    	
    }
    
    public CgRule(CgRule rule) {
    	this.flags = new HashSet<Integer>(rule.flags);
    	this.line = rule.line;
    	this.type = rule.type;
    	this.wordform = rule.wordform;
    	this.varname = rule.varname;
    	this.childset1 = rule.childset1;
    	this.childset2 = rule.childset2;
    	this.jumpstart = rule.jumpstart;
    	this.jumpend = rule.jumpend;
    	this.section = rule.section;
    	this.target = rule.target;
    	this.number = rule.number;
    	this.sublist = new CgSet(rule.sublist);
    	this.maplist = new CgSet(rule.maplist);
    	this.dep_target = new CgContextualTest(rule.dep_target);
    	this.dep_test_head = new CgContextualTest(rule.dep_test_head);
    	this.all_tests = new HashSet<CgContextualTest>();
    	for (CgContextualTest test : rule.all_tests) {
    		this.all_tests.add(new CgContextualTest(test));
    	}
    	this.test_map = new HashMap<Integer,CgContextualTest>();
    	for (Integer index : rule.test_map.keySet()) {
    		this.test_map.put(index,new CgContextualTest(rule.test_map.get(index)));
    	}
    	this.test_heads = new HashSet<CgContextualTest>();
    	for (CgContextualTest test : rule.test_heads) {
    		this.test_heads.add(new CgContextualTest(test));
    	}
    }
    
    public CgContextualTest allocateContextualTest() {
        return new CgContextualTest();
    }
    
    public void addContextualTest(CgContextualTest t, CgContextualTest head) {
        // this method needs to be more fully defined
    	this.all_tests.add(t);
    }
    
    public enum RFLAGS {
        RF_NEAREST       (1 <<  0),
        RF_ALLOWLOOP     (1 <<  1),
        RF_DELAYED       (1 <<  2),
        RF_IMMEDIATE     (1 <<  3),
        RF_LOOKDELETED   (1 <<  4),
        RF_LOOKDELAYED   (1 <<  5),
        RF_UNSAFE        (1 <<  6),
        RF_SAFE          (1 <<  7),
        RF_REMEMBERX     (1 <<  8),
        RF_RESETX        (1 <<  9),
        RF_KEEPORDER     (1 << 10),
        RF_VARYORDER     (1 << 11),
        RF_ENCL_INNER    (1 << 12),
        RF_ENCL_OUTER    (1 << 13),
        RF_ENCL_FINAL    (1 << 14),
        RF_ENCL_ANY      (1 << 15),
        RF_ALLOWCROSS    (1 << 16),
        RF_WITHCHILD     (1 << 17),
        RF_NOCHILD       (1 << 18),
        RF_ITERATE       (1 << 19),
        RF_NOITERATE     (1 << 20),
        RF_UNMAPLAST     (1 << 21),
        RF_REVERSE       (1 << 22);
        public int value;
        RFLAGS(int v) {
            this.value = v;
        }
    }
    
}

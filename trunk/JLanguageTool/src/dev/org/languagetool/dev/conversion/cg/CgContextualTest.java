package de.danielnaber.languagetool.dev.conversion.cg;

import java.util.ArrayList;
import java.util.HashSet;

public class CgContextualTest {

    public int line;
    public int name;
    public HashSet<Integer> pos = new HashSet<Integer>();
    public int offset;
    public int relation;
    public int target;
    public int cbarrier;
    public int barrier;
    
    public int linked = 0;
    public int next = 0;
    public int prev = 0;
    
    public ArrayList<Integer> ors = new ArrayList<Integer>();
    
    public boolean isParentTest() {
    	return !this.ors.isEmpty();
    }
    
    public boolean isLinkedTest() {
    	return this.next != 0;
    }
    
    public boolean isNormalTest() {
    	return (this.ors.isEmpty() && this.next == 0);
    }
    
    
    
    public void rehash() {
    	// this doesn't currently get called, which is a good thing because it's not defined :)
    }
    
    public CgContextualTest allocateContextualTest() {
        return new CgContextualTest();
    }
    
    public CgContextualTest() {
        this.barrier = 0;
        this.cbarrier = 0;
        this.line = 0;
        this.name = 0;
        this.offset = 0;
        this.target = 0;
        this.pos = new HashSet<Integer>();
        this.relation = 0;
    }
    
    public CgContextualTest(CgContextualTest test) {
    	if (test == null) {
    		this.barrier = 0;
            this.cbarrier = 0;
            this.line = 0;
            this.name = 0;
            this.offset = 0;
            this.target = 0;
            this.pos = new HashSet<Integer>();
            this.relation = 0;
    	} else {
    		this.barrier = test.barrier;
        	this.cbarrier = test.cbarrier;
        	this.line = test.line;
        	this.name = test.name;
        	this.offset = test.offset;
        	this.target = test.target;
        	this.pos = new HashSet<Integer>(test.pos);
        	this.relation = test.relation;
        	this.ors = new ArrayList<Integer>(test.ors);
    	}
    }
    
    public enum POS {
        POS_CAREFUL         (1 <<  0),
        POS_NEGATE          (1 <<  1),
        POS_NOT             (1 <<  2),
        POS_SCANFIRST       (1 <<  3),
        POS_SCANALL         (1 <<  4),
        POS_ABSOLUTE        (1 <<  5),
        POS_SPAN_RIGHT      (1 <<  6),
        POS_SPAN_LEFT       (1 <<  7),
        POS_SPAN_BOTH       (1 <<  8),
        POS_DEP_PARENT      (1 <<  9),
        POS_DEP_SIBLING     (1 << 10),
        POS_DEP_CHILD       (1 << 11),
        POS_PASS_ORIGIN     (1 << 12),
        POS_NO_PASS_ORIGIN  (1 << 13),
        POS_LEFT_PAR        (1 << 14),
        POS_RIGHT_PAR       (1 << 15),
        POS_SELF            (1 << 16),
        POS_NONE            (1 << 17),
        POS_ALL             (1 << 18),
        POS_DEP_DEEP        (1 << 19),
        POS_MARK_SET        (1 << 20),
        POS_MARK_JUMP       (1 << 21),
        POS_LOOK_DELETED    (1 << 22),
        POS_LOOK_DELAYED    (1 << 23),
        POS_TMPL_OVERRIDE   (1 << 24),
        POS_UNKNOWN         (1 << 25),
        POS_RELATION        (1 << 26),
        POS_ATTACH_TO       (1 << 27);
        public int value;
        POS(int v) {
            this.value = v;
        }
    }
    
}

package de.danielnaber.languagetool.dev.conversion.cg;

import java.util.ArrayList;
import java.util.HashSet;

import de.danielnaber.languagetool.dev.conversion.CgRuleConverter;

public class CgCompositeTag {

    public boolean is_used;
    public boolean is_special;
    public int hash;
    public int number;
    public HashSet<CgTag> tags_set = new HashSet<CgTag>();
    public ArrayList<CgTag> tags = new ArrayList<CgTag>();
    
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("(");
    	for (int i=0;i<tags.size();i++) {
    		if (i == tags.size() - 1) {
    			sb.append(tags.get(i).tag + ")");
    		} else {
    			sb.append(tags.get(i).tag + " ");
    		}
    	}
    	return sb.toString();
    }
    
    public void addTag(CgTag t) {
        this.tags_set.add(t);
        this.tags.add(t);
    }
    
    public String getPostags() {
    	return CgRuleConverter.compositePostagToString(this);
    }
    
    // should only be at most one baseform in a composite tag
    public String getBaseform() {
    	String baseform = "";
    	for (CgTag tag : this.tags) {
    		if (CgRuleConverter.isBaseForm(tag.tag)) {
    			baseform = tag.tag;
    		}
    	}
    	return baseform;
    }
    
    // should also be at most one surfaceform
    public String getSurfaceform() {
    	String surfaceform = "";
    	for (CgTag tag : this.tags) {
    		if (CgRuleConverter.isSurfaceForm(tag.tag)) {
    			surfaceform = tag.tag;
    		}
    	}
    	return surfaceform;
    }
    
    public enum ANYTAG_TYPE {
        ANYTAG_TAG (0),
        ANYTAG_COMPOSITE (1),
        NUM_ANYTAG (2);
        public final int value;
        ANYTAG_TYPE(int v) {
            value = v;
        }
    }
    
    public int rehash() {
        int retval = 0;
        for (CgTag t : this.tags) {
            retval = CgStrings.hash_sdbm_uint32_t(t.hash, retval);
        }
        this.hash = retval;
        return retval;
    }
    
    // this class is a holdover that I'm not using for my LT purposes
    // if I want to extend/complete the Java CG port, I'd have to extend this
    public class AnyTag {
        int which;
        CgTag tag;
        CgCompositeTag ct;
        public CgTag getTag() {
            return tag;
        }
        public CgCompositeTag getCompositeTag() {
            return ct;
        }
    }
    
    public boolean isEmpty() {
    	return (this.tags.size() == 0);
    }
    
}

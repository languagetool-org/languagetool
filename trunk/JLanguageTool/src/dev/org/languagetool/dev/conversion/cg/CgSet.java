package de.danielnaber.languagetool.dev.conversion.cg;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.Random;

import de.danielnaber.languagetool.dev.conversion.cg.CgTag.TAGS;
import de.danielnaber.languagetool.dev.conversion.CgRuleConverter;

public class CgSet {

    public int line;
    public int hash;   // will always contain a hash of the Set's name
    public int chash;   // contains a hash of the set's contents
    public String name;
    // store anything that gets bit shifted as a HashSet of integers
    public HashSet<Integer> type;
    public int number;

    public HashSet<CgCompositeTag> tags;
    public HashSet<CgTag> single_tags;
    public HashSet<Integer> single_tags_hash;
    public ArrayList<Integer> sets;
    public ArrayList<Integer> set_ops;
    public ArrayList<CgCompositeTag.AnyTag> tags_list;
    public HashSet<CgTag> ff_tags;
    
    
    // default constructor - sets none of the important fields
    public CgSet() {
        this.number = 0;
        this.line = 0;
        this.hash = 0;
        this.name = null;
        this.setName(0);
        this.type = new HashSet<Integer>();
        this.chash = 0;
        this.tags = new HashSet<CgCompositeTag>();
        this.single_tags = new HashSet<CgTag>();
        this.single_tags_hash = new HashSet<Integer>();
        this.sets = new ArrayList<Integer>();
        this.set_ops = new ArrayList<Integer>();
        this.tags_list = new ArrayList<CgCompositeTag.AnyTag>();
        this.ff_tags = new HashSet<CgTag>();
    }
    
    // copy constructor
    public CgSet(CgSet from) {
    	if (from == null) {
    		this.number = 0;
            this.line = 0;
            this.hash = 0;
            this.name = null;
            this.setName(0);
            this.type = new HashSet<Integer>();
            this.chash = 0;
            this.tags = new HashSet<CgCompositeTag>();
            this.single_tags = new HashSet<CgTag>();
            this.single_tags_hash = new HashSet<Integer>();
            this.sets = new ArrayList<Integer>();
            this.set_ops = new ArrayList<Integer>();
            this.tags_list = new ArrayList<CgCompositeTag.AnyTag>();
            this.ff_tags = new HashSet<CgTag>();
    	} else {
    		this.tags_list = from.tags_list;	// i never use this anyway
            this.tags = new HashSet<CgCompositeTag>(from.tags);
            this.single_tags = new HashSet<CgTag>(from.single_tags);
            this.single_tags_hash = from.single_tags_hash;
            this.ff_tags = from.ff_tags;
            this.set_ops = from.set_ops;
            this.sets = from.sets;

            this.hash = this.hashCode();
            this.number = from.number;
            this.name = from.name;
            this.type = from.type;
            this.line = from.line;
            this.hashContents();
    	}
    	
    }
    
    // Get methods used by CgRuleConverter class
    
    public CgCompositeTag[] getCompositeTags() {
    	ArrayList<CgCompositeTag> tags = new ArrayList<CgCompositeTag>();
    	if (!this.tags.isEmpty()) {
    		for (CgCompositeTag ctag : this.tags) {
    			if (!CgRuleConverter.isCompositePostag(ctag)) {
    				tags.add(ctag);
    			}
    		}
    	}
    	return tags.toArray(new CgCompositeTag[tags.size()]);
    }
    
    public CgCompositeTag[] getCompositePostags() {
    	ArrayList<CgCompositeTag> postags = new ArrayList<CgCompositeTag>();
    	if (!this.tags.isEmpty()) {
    		for (CgCompositeTag ctag : this.tags) {
    			if (CgRuleConverter.isCompositePostag(ctag)) {
    				postags.add(ctag);
    			}
    		}
    	}
    	return postags.toArray(new CgCompositeTag[postags.size()]);
    }
    
    public String[] getSingleTagPostagsString() {
    	ArrayList<String> postags = new ArrayList<String>();
    	if (!this.single_tags.isEmpty()) {
    		for (CgTag tag : this.single_tags) {
    			if (CgRuleConverter.isPostag(tag.tag)) {
    				postags.add(tag.tag);
    			}
    		}
    	}
    	return postags.toArray(new String[postags.size()]);
    }
    
    public CgTag[] getSingleTagPostags() {
    	ArrayList<CgTag> postags = new ArrayList<CgTag>();
    	if (!this.single_tags.isEmpty()) {
    		for (CgTag tag : this.single_tags) {
    			if (CgRuleConverter.isPostag(tag.tag)) postags.add(tag);
    		}
    	}
    	return postags.toArray(new CgTag[postags.size()]);
    }
    
    public String[] getSingleTagBaseformsString() {
    	ArrayList<String> forms = new ArrayList<String>();
    	if (!this.single_tags.isEmpty()) {
    		for (CgTag tag : this.single_tags) {
    			String tagtag = tag.tag;
    			if (CgRuleConverter.isBaseForm(tagtag)) forms.add(tagtag);
    		}
    	}
    	return forms.toArray(new String[forms.size()]);
    }
    
    public CgTag[] getSingleTagBaseforms() {
    	ArrayList<CgTag> forms = new ArrayList<CgTag>();
    	if (!this.single_tags.isEmpty()) {
    		for (CgTag tag : this.single_tags) {
    			if (CgRuleConverter.isBaseForm(tag.tag)) forms.add(tag);
    		}
    	}
    	return forms.toArray(new CgTag[forms.size()]);
    }
    
    public String[] getSingleTagSurfaceformsString() {
    	ArrayList<String> forms = new ArrayList<String>();
    	if (!this.single_tags.isEmpty()) {
    		for (CgTag tag : this.single_tags) {
    			String tagtag = tag.tag;
    			if (CgRuleConverter.isSurfaceForm(tagtag)) forms.add(tagtag);
    		}
    	}
    	return forms.toArray(new String[forms.size()]);
    }
    
    public CgTag[] getSingleTagSurfaceforms() {
    	ArrayList<CgTag> forms = new ArrayList<CgTag>();
    	if (!this.single_tags.isEmpty()) {
    		for (CgTag tag : this.single_tags) {
    			String tagtag = tag.tag;
    			if (CgRuleConverter.isSurfaceForm(tagtag)) forms.add(tag);
    		}
    	}
    	return forms.toArray(new CgTag[forms.size()]);
    }
    
    public String[] getPostagsString() {
    	ArrayList<String> tags = new ArrayList<String>();
    	if (!this.single_tags.isEmpty()) {
    		for (CgTag tag : this.single_tags) {
    			if (CgRuleConverter.isPostag(tag.tag)) {
    				tags.add(CgRuleConverter.tagToString(tag));
    			}
    		}
    	}
    	if (!this.tags.isEmpty()) {
    		for (CgCompositeTag ctag : this.tags) {
    			if (CgRuleConverter.isCompositePostag(ctag)) {
    				tags.add(CgRuleConverter.compositePostagToString(ctag));
    			}
    		}
    	}
    	return tags.toArray(new String[tags.size()]);
    }
    
    
    // these should never really be used
    
    public ArrayList<String> getSurfaceFormsString(CgGrammar grammar) {
    	ArrayList<String> forms = new ArrayList<String>();
    	if (!this.single_tags.isEmpty()) {
    		for (CgTag tag : this.single_tags) {
    			String tagtag = tag.tag;
    			if (CgRuleConverter.isSurfaceForm(tagtag)) {
    				forms.add(tagtag);
    			}
    		}
    	}
    	if (!this.tags.isEmpty()) {
    		for (CgCompositeTag ctag : this.tags) {
    			for (CgTag tag : ctag.tags) {
    				String tagtag = tag.tag;
    				if (CgRuleConverter.isSurfaceForm(tagtag)) {
    					forms.add(tagtag);
    				}
    			}
    		}
    	}
    	if (!this.sets.isEmpty()) {
    		for (int setint : this.sets) {
    			ArrayList<String> setForms = grammar.getSet(setint).getSurfaceFormsString(grammar);
    			for (String setForm : setForms) {
    				forms.add(setForm);
    			}
    		}
    	}
    	return forms;
    }
    
    public ArrayList<String> getBaseformsString(CgGrammar grammar) {
    	ArrayList<String> forms = new ArrayList<String>();
    	if (!this.single_tags.isEmpty()) {
    		for (CgTag tag : this.single_tags) {
    			String tagtag = tag.tag;
    			if (CgRuleConverter.isBaseForm(tagtag)) {
    				forms.add(tagtag);
    			}
    		}
    	}
    	if (!this.tags.isEmpty()) {
    		for (CgCompositeTag ctag : this.tags) {
    			for (CgTag tag : ctag.tags) {
    				String tagtag = tag.tag;
    				if (CgRuleConverter.isBaseForm(tagtag)) {
    					forms.add(tagtag);
    				}
    			}
    		}
    	}
    	if (!this.sets.isEmpty()) {
    		for (int setint : this.sets) {
    			ArrayList<String> setForms = grammar.getSet(setint).getBaseformsString(grammar);
    			for (String setForm : setForms) {
    				forms.add(setForm);
    			}
    		}
    	}
    	return forms;
    }
    
    public ArrayList<String> getPostagsString(CgGrammar grammar) {
    	ArrayList<String> postags = new ArrayList<String>();
    	if (!this.single_tags.isEmpty()) {
    		for (CgTag tag : this.single_tags) {
    			if (CgRuleConverter.isPostag(tag.tag)) {
    				postags.add(tag.tag);
    			}
    		}
    	}
    	if (!this.tags.isEmpty()) {
    		for (CgCompositeTag ctag : this.tags) {
    			if (CgRuleConverter.isCompositePostag(ctag)) {
    				postags.add(ctag.toString());
    			}
    		}
    	}
    	return postags;
    }
    
    public String toString(CgGrammar grammar) {
        StringBuilder sb = new StringBuilder();
        if (!this.tags.isEmpty()) {
            for (CgCompositeTag ctag : this.tags) {
                sb.append("(");
                for (CgTag stag : ctag.tags ) {
                    sb.append(stag + " ");
                }
                sb.append(")");
            }
        } else if (!this.single_tags.isEmpty()) {
            for (CgTag stag : this.single_tags) {
                sb.append("(");
                sb.append(stag.tag);
                sb.append(")");
            }
            
        } else if (!this.sets.isEmpty()) {
            for (int s2 : this.sets) {
                sb.append(grammar.getSet(s2).toString(grammar));
            }
        }
        return sb.toString();
    }
    
    public void setName(String name) {
        this.name = name;
        this.hash = name.hashCode();
    }
    
    public void setName(int to) {
        if (to == 0) {
            Random gen = new Random();
            to = gen.nextInt();
        }
        this.name = "_G_" + line + "_" + to;
        this.hash = this.name.hashCode();
    }
    
    public boolean isEmpty() {
        return (this.single_tags.isEmpty() && this.tags.isEmpty() && this.sets.isEmpty());
    }
    
    // adds a tag to this set
    public void addTag(CgTag t) {
        this.single_tags.add(t);
        this.hashContents();
    }
    
    // add a composite tag to this set
    public void addCompositeTag(CgCompositeTag t) {
        this.tags.add(t);
    }
    
    // AnyTags not handled too well here.
    public HashSet<CgCompositeTag.AnyTag> getTagList(final CgGrammar grammar) {
        HashSet<CgCompositeTag.AnyTag> theTags = new HashSet<CgCompositeTag.AnyTag>();
        if (sets.isEmpty()) {
            for (int i=0;i<sets.size();i++) {
                HashSet<CgCompositeTag.AnyTag> recursiveTags = grammar.getSet(i).getTagList(grammar);
                for (CgCompositeTag.AnyTag t : recursiveTags) {
                    theTags.add(t);
                }
            }
        } else {
            for (CgCompositeTag.AnyTag t : this.tags_list) {
                theTags.add(t);
            }
        }
        return theTags;
    }
    
    public void reindex(CgGrammar grammar) {
        if (this.type.contains(ST.ST_SPECIAL.value)) {
            this.type.remove(ST.ST_SPECIAL.value);
        }
        if (this.type.contains(ST.ST_CHILD_UNIFY.value)) {
            this.type.remove(ST.ST_CHILD_UNIFY.value);
        }
        if (this.sets.isEmpty()) {
            for (CgTag tomp_iter : this.single_tags) {
                if (tomp_iter.type.contains(TAGS.T_SPECIAL.value)) {
                    this.type.add(ST.ST_SPECIAL.value);
                }
                if (tomp_iter.type.contains(TAGS.T_MAPPING.value)) {
                    this.type.add(ST.ST_MAPPING.value);
                }
            }
            for (CgCompositeTag comp_iter : this.tags) {
                for (CgTag tag_iter : comp_iter.tags) {
                    if (tag_iter.type.contains(TAGS.T_SPECIAL.value)) {
                        this.type.add(ST.ST_SPECIAL.value);
                    }
                    if (tag_iter.type.contains(TAGS.T_MAPPING.value)) {
                        this.type.add(ST.ST_MAPPING.value);
                    }
                }
            }
        }
        
    }
    
    public void hashContents() {
        int h = this.name.hashCode();
        if (!this.tags.isEmpty()) {
            for (CgCompositeTag t : this.tags) {
                h = h << t.hashCode();
            }
        }
        this.chash = h;
    }
    
    public void rehash() {
        this.hashContents();
        this.hash = this.hash << this.chash;
    }

    public enum ST {
         ST_ANY  (1 << 0),
         ST_SPECIAL  (1 << 1),
         ST_TAG_UNIFY  (1 << 2),
         ST_SET_UNIFY  (1 << 3),
         ST_CHILD_UNIFY  (1 << 4),
         ST_MAPPING  (1 << 5),
         ST_USED  (1 << 6),
         ST_STATIC  (1 << 7);
         public int value;
         ST(int v) {
             this.value = v;
         }
    }
    
}

package de.danielnaber.languagetool.dev.conversion.cg;

public class CgAnchor {

    public int line;
    public String name;
    public int hash;
    
    public void setName(String n) {
        this.name = n;
        this.hash = n.hashCode();
    }
    
}

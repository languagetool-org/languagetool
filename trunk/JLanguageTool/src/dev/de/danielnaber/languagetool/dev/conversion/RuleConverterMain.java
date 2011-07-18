package de.danielnaber.languagetool.dev.conversion;

import java.io.*;
import java.util.*;

import de.danielnaber.languagetool.Language;

public class RuleConverterMain {
    
    private String grammarfile;
    private String specificFiletype;
    private RuleConverter rc;
    
    private static String[] supportedGeneralFiletypes = {"atd"};
    private static String[] supportedSpecificFiletypes = {"avoid","default"};
    
    private static void exitWithUsageMessage() {
        System.out
        .println("Usage: java de.danielnaber.languagetool.tools.RuleConverterMain "
            + "[-h|--help] [-g|--generalFiletype] [-s|--specificFiletype] <incoming-rule-file> <outgoing-grammar-file>");
        System.exit(1);
      }
    
    private RuleConverterMain(String infilename, String grammarfile, String generalFiletype, String specificFiletype) {
        this.grammarfile  = grammarfile;
        this.specificFiletype = specificFiletype;
        if (generalFiletype.equals("atd")) {
            rc = new AtdRuleConverter(infilename, grammarfile, specificFiletype);
            
        }
    }
    
    private void run() throws IOException {
        // get the rules
        List<String> rules = rc.getRulesAsString();
        // regular rules
        ArrayList<ArrayList<String>> ltRules = new ArrayList<ArrayList<String>>(); 
        // false alarm rules
        ArrayList<ArrayList<String>> killedRules = new ArrayList<ArrayList<String>>();
        for (String r : rules) {
            HashMap<String,String> hm = rc.parseRule(r);
            ArrayList<String> ltRule = (ArrayList<String>) rc.ltRuleAsList(hm, 
                    RuleConverter.getSuitableID(hm), RuleConverter.getSuitableName(hm), specificFiletype);
            if (notKilledRule(hm)) {
            	ltRules.add(ltRule);
            } else {
            	killedRules.add(ltRule);
            }
        }

        // write the new grammar file out (as ".xml.backup" for now)
        //TODO: write the false alarm rules to another file
        PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(grammarfile),"UTF-8"));
        w.write(RuleConverter.xmlHeader);
        w.write("<category name=\"Auto-generated AtD rules\" id=\"ATD_RULES\">\n");
        for (ArrayList<String> ltRule : ltRules) {
            for (String line : ltRule) {
                w.write(line + '\n');
            }
        }
        w.write("</category>\n");
        w.write("</rules>");
        w.close();
        RuleCoverage checker = new RuleCoverage(Language.ENGLISH);
        checker.evaluateRules(grammarfile);
        
        // for now, write the disambiguation rules to a default file
        w = new PrintWriter(new OutputStreamWriter(new FileOutputStream("disambiguationtest.xml"), "UTF-8"));
        for (ArrayList<String> killedRule : killedRules) {
        	for (String line : killedRule) {
        		w.write(line + '\n');
        	}
        }
        w.close();
        
    }
    
    public static void main(String[] args) throws IOException {
        String grammarfile = null;
        String rulefile = null;
        String specificFiletype = null; // type of rule, specific to filetype (e.g. "avoid")
        String generalFiletype= null; // type of file, like the syntax of the other system (e.g. "atd") - i know this is confusing and needs to be fixed
        
        if (args.length < 2) {
            exitWithUsageMessage();
        }
        
        for (int i=0;i<args.length;i++) {
            if (args[i].equals("-h") || args[i].equals("--help") ||
                    args[i].equals("--?") || args[i].equals("-help")) {
                exitWithUsageMessage();
            }
            else if (args[i].equals("-s") || args[i].equals("--specificFiletype")) {
                specificFiletype = getSpecificFiletypeOrExit(args[++i]);
            }
            else if (args[i].equals("-g") || args[i].equals("--generalFiletype")) {
                generalFiletype = getGeneralFiletypeOrExit(args[++i]);
            }
            else if (i == args.length - 2) {
                rulefile = args[i];
            }
            else if (i == args.length - 1) {
                grammarfile = args[i];
            }
            else {
                System.err.println("Unknown option: " + args[i]);
                exitWithUsageMessage();
            }       
        }
        if (specificFiletype == null) {
            specificFiletype = "default";
        }
        if (generalFiletype == null) {
            generalFiletype = "atd";
        }
        RuleConverterMain prg = new RuleConverterMain(rulefile, grammarfile, generalFiletype, specificFiletype);
        prg.run();       
        
    }
    
    public static String getSpecificFiletypeOrExit(String arg) {
        String type = null;
        boolean foundtype = false;
        for (String s : supportedSpecificFiletypes) {
            if (arg.equals(s)) {
                type = s;
                foundtype = true;
                break;
            }
        }
        if (!foundtype) {
            System.out.println("Unknown specific filetype " + arg);
            System.out.print("Supported filetypes are");
            for (String s : supportedSpecificFiletypes) {
                System.out.print(" " + s);
            }
            System.out.println();
            exitWithUsageMessage();
        }
        return type;
    }
    
    public static String getGeneralFiletypeOrExit(String arg) {
        String type = null;
        boolean foundtype = false;
        for (String s : supportedGeneralFiletypes) {
            if (arg.equals(s)) {
                type = s;
                foundtype = true;
                break;
            }
        }
        if (!foundtype) {
            System.out.println("Unknown general filetype " + arg);
            System.out.print("Supported filetypes are");
            for (String s : supportedGeneralFiletypes) {
                System.out.print(" " + s);
            }
            System.out.println();
            exitWithUsageMessage();
        }
        return type;
    }
    

    /**
     * Applies to the AtD false alarm rules
     * @param rule
     * @return
     */
    public boolean notKilledRule(HashMap<String,String> rule) {
    	if (rule.containsKey("filter")) {
    		if (rule.get("filter").equals("kill") || rule.get("filter").equals("die")) {
    			return false;
    		}
    	}
    	return true;
    }
    
}

/* LanguageTool, a natural language style checker 
 * Copyright (C) 2010 Daniel Naber (http://www.languagetool.org)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */

package de.danielnaber.languagetool.dev.conversion;

import java.io.*;
import java.util.*;

import de.danielnaber.languagetool.Language;

public class RuleConverterMain {
    
    private String grammarfile;
    private String specificFiletype;
    private String discardfile;
    private String disambigfile;
    private RuleConverter rc;
    
    private static String[] supportedGeneralFiletypes = {"atd","cg"};
    private static String[] supportedSpecificFiletypes = {"avoid","default"};
    
    private static void exitWithUsageMessage() {
        System.out
        .println("Usage: java de.danielnaber.languagetool.tools.RuleConverterMain "
            + "[-h|--help] [-g|--generalFiletype] [-s|--specificFiletype] [-i|--inputFile] [-a|--disambigFile] " +
            "[-d|--discardFile] [-o|--outputFile]");
        System.exit(1);
      }
    
    private RuleConverterMain(String infilename, String grammarfile, String discardfile, String disambigfile, String generalFiletype, String specificFiletype) {
        this.grammarfile  = grammarfile;
        this.specificFiletype = specificFiletype;
        this.disambigfile = disambigfile;
        this.discardfile = discardfile;
        if (generalFiletype.equals("atd")) {
            rc = new AtdRuleConverter(infilename, grammarfile, specificFiletype);
        } else if (generalFiletype.equals("cg")) {
        	rc = new CgRuleConverter(infilename, grammarfile, specificFiletype);
        }
    }
    
    private void run() throws IOException {
        rc.parseRuleFile();

        // write out the grammar rules to grammarfile
        PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(grammarfile),"UTF-8"));
        
        w.write("<rules>\n");
        w.write("<category name=\"Auto-generated rules\">\n");
        for (List<String> ltRule : rc.ltRules) {
            for (String line : ltRule) {
                w.write(line + '\n');
            }
        }
        w.write("</category>\n");
        w.write("</rules>");
        w.close();
        
        /*
         * this will have to be added back in once I have the general format figured out
        // evaluate the rules to see which are already covered
        RuleCoverage checker = new RuleCoverage(Language.ENGLISH);
        checker.splitOutCoveredRules(grammarfile,discardfile);
        */
        // for now, write the disambiguation rules to a default file
        if (rc.disambiguationRules.size() > 0) {
        	w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(disambigfile), "UTF-8"));
            for (List<String> killedRule : rc.disambiguationRules) {
            	for (String line : killedRule) {
            		w.write(line + '\n');
            	}
            }
            w.close();
            System.out.println(Integer.toString(rc.disambiguationRules.size()) + " disambiguation rules written to " + disambigfile);
        }
        
        
    }
    
    public static void main(String[] args) throws IOException {
        String grammarfile = null;
        String rulefile = null;
        String specificFiletype = null; // type of rule, specific to filetype (e.g. "avoid")
        String generalFiletype= null; // type of file, like the syntax of the other system (e.g. "atd") - i know this is confusing and needs to be fixed
        String discardfile = null;
        String disambigfile = null;
        
        // this doesn't work because the output rules file is not a legit rules file 
        //TODO: fix this so we can have this functionality
        try {
        	if (args[0].equals("--check")) {
        		RuleCoverage checker = new RuleCoverage(Language.ENGLISH);
        		String inFile = args[1];
        		checker.evaluateRules(inFile);
        		System.exit(1);
        	}
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        if (args.length < 4) {
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
            else if (args[i].equals("--outputFile") || args[i].equals("-o")) {
            	grammarfile = args[++i];
            }
            else if (args[i].equals("--discardFile") || args[i].equals("-d")) {
            	discardfile = args[++i];
            }
            else if (args[i].equals("--disambigFile") || args[i].equals("-a")) {
            	disambigfile = args[++i];
            }
            else if (args[i].equals("--inputFile") || args[i].equals("-i")) {
            	rulefile = args[++i];
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
        if (grammarfile == null) {
        	System.err.println("Need to specify a grammar file");
        	exitWithUsageMessage();
        }
        if (rulefile == null) {
        	System.err.println("Need to specify a rule file");
        	exitWithUsageMessage();
        }
        if (disambigfile == null) {
        	disambigfile = "disambig.xml";
        }
        if (discardfile == null) {
        	discardfile = "discard.xml";
        }
        RuleConverterMain prg = new RuleConverterMain(rulefile, grammarfile, discardfile, disambigfile, generalFiletype, specificFiletype);
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
    

  
    
}

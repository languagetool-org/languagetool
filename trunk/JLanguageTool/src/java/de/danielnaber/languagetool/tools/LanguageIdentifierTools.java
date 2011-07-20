package de.danielnaber.languagetool.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.tika.language.*;

import de.danielnaber.languagetool.JLanguageTool;



public class LanguageIdentifierTools {

	static String[] additionalLanguages = {"be", "ca", "eo", "gl", "ro", "sk", "sl", "uk"};
	
	public static void addLtProfiles() throws Exception {
		for (String language : additionalLanguages) {
			addProfile(language);
		}
	}
	
    private static void addProfile(String language) throws Exception {
        final String PROFILE_SUFFIX = ".ngp";
        final String PROFILE_ENCODING = "UTF-8";
    
        try {
            LanguageProfile profile = new LanguageProfile();
            final String languageFilePath = JLanguageTool.getDataBroker().getResourceDir() + 
            	"/" + language + "/" + language + PROFILE_SUFFIX;
             
            InputStream stream = JLanguageTool.class.getResourceAsStream(languageFilePath);
            
            try {
                BufferedReader reader =
                    new BufferedReader(new InputStreamReader(stream, PROFILE_ENCODING));
                String line = reader.readLine();
                while (line != null) {
                    if (line.length() > 0 && !line.startsWith("#")) {
                        int space = line.indexOf(' ');
                        profile.add(
                                line.substring(0, space),
                                Long.parseLong(line.substring(space + 1)));
                    }   
                    line = reader.readLine();
                }   
            } finally {
                stream.close();
            }   

            LanguageIdentifier.addProfile(language, profile);
        } catch (Throwable t) {
            throw new Exception("Failed trying to load language profile for language \""+language+"\". Error: "+t.getMessage());
        }
    }

	
	
}

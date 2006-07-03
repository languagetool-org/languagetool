/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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

package de.danielnaber.languagetool;

/**
 * An array of (@link AnalyzedToken) used to store multiple POS tags and lemmas
 * for a given single token.
 * 
 * @author Marcin Milkowski
 * 
 */
public class AnalyzedTokenReadings {
	protected AnalyzedToken[] ATreadings;
	protected int startPos;
	protected String token; 
//	protected List readings = new ArrayList();
	
	public AnalyzedTokenReadings (AnalyzedToken[] r) {
		ATreadings = r;
		token = ATreadings[0].getToken();
		this.startPos=ATreadings[0].getStartPos();
	}
	
	public AnalyzedTokenReadings (AnalyzedToken at){
		ATreadings = new AnalyzedToken[1];
		ATreadings[0]=at;
		token = ATreadings[0].getToken();
		startPos=at.getStartPos();
	}
	public AnalyzedToken getAnalyzedToken(int i) {
		return ATreadings[i];
	}
	
	public int getStartPos() {
		return startPos;
	}
	
	public String getToken(){
		return token;
	}
	
	public int getReadingslength () {
		return ATreadings.length;
	}
	
	public void setReading(int i, AnalyzedToken tok) {
		ATreadings[i]=tok;
	}
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < ATreadings.length; i++) {
      sb.append(ATreadings[i]);
    }
    return sb.toString();
  }
	
}

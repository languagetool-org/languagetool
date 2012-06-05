/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
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


/**
 * Script to check versions of Windows DLLs, should be called during
 * Windows ANT test... 
 */

var fso = WScript.CreateObject("Scripting.FileSystemObject");

var version1 = fso.GetFileVersion("../../../libs/native-lib/hunspell-win-x86-64.dll");
var version2 = fso.GetFileVersion("../../../libs/native-lib/hunspell-win-x86-32.dll");

if (version1 != version2) {
	WScript.Echo("Error: version mismatch: " + version1 + " != " + version2);
	WScript.Quit(-1);
}

WScript.Quit();
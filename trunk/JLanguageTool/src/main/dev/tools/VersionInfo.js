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

/* this function does not work... the attribute numbers differ in various
 * Windows versions
 
function getProductVersion (filePath, program) {
if (fso.FileExists(filePath + "/" + program)) {    
	var objShell = WScript.CreateObject("shell.application");
    var objFolder = new Object;
    
    objFolder = objShell.NameSpace(filePath);
    if (objFolder == null) {
    	WScript.Echo(filePath);	
    }
    var objFolderItem = new Object;
    objFolderItem = objFolder.ParseName(program);    
    for (var i = 0; i < 300; i++) {
        var arrHeaders = objFolder.GetDetailsOf(objFolderItem, i);
        WScript.Echo(i + "- " + arrHeaders + ": " + objFolder.GetDetailsOf(objFolderItem, i));
        if (arrHeaders.toLowerCase() == "product version") {
            return objFolder.GetDetailsOf(objFolderItem, i);
        	}
        }
     }
  }
*/

var fso = WScript.CreateObject("Scripting.FileSystemObject");

var version1 = fso.GetFileVersion("../../../libs/native-lib/hunspell-win-x86-64.dll");
var version2 = fso.GetFileVersion("../../../libs/native-lib/hunspell-win-x86-32.dll");

if (version1 != version2) {
	WScript.Echo("Error: Hunspell Windows DLLs have different version numbers! \r\n 64-bit library has number: " + version1 + " 32-bit library has number: " + version2);
	WScript.Quit(-1);
}

var versionNumber = version1.replace(/\./g, "");

if (versionNumber < 1310) {
	WScript.Echo("Error: minimum version number is 1.3.1.0. This DLL has version: " + version1);
	WScript.Quit(-1);
}

WScript.Quit();
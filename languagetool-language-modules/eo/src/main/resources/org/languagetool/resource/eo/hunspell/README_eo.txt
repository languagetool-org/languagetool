Esperanto Dictionary.

Source: Hunspell dictionary from Esperantilo OpenOffice extension in (files eo_ilo.{aff,doc})
in esperantilo.oxt available http://extensions.openoffice.org/en/download/4561

The follow patch was applied by Dominique Pellé:

*** eo_ilo.aff  2010-02-18 21:58:58.000000000 +0100
--- /home/pel/sb/languagetool/languagetool-language-modules/eo/src/main/resources/org/languagetool/resource/eo/hunspell/eo.aff  2013-11-20 23:29:56.133731423 +0100
***************
*** 28,36 ****
  KEY qwertyuiop|asdfghjkl|zxcvbnm
  
  # this is not useful for OpenOffice, which has its own mechanism for treating interpunction
! BREAK 2
  BREAK -
  BREAK --
  
  # common two-letter surrogates for the special Esperanto characters: the "h-convention" and the "x-convention"
  # (this list includes "uh", which is actually not a part of the h-convention, but used (incorrectly) by some authors)
--- 28,41 ----
  KEY qwertyuiop|asdfghjkl|zxcvbnm
  
  # this is not useful for OpenOffice, which has its own mechanism for treating interpunction
! # D. Pelle (20130102): added BREAK rules for apostrophes/quotes.
! BREAK 6
  BREAK -
  BREAK --
+ BREAK ^'
+ BREAK ^?
+ BREAK '$
+ BREAK ?$
  
  # common two-letter surrogates for the special Esperanto characters: the "h-convention" and the "x-convention"
  # (this list includes "uh", which is actually not a part of the h-convention, but used (incorrectly) by some authors)


License of the file eo_ilo.{aff,dic} in esperantilo.oxt:
===

Tiu dosiero estas publikigita laŭ GPL Permesilo
legu plu sur: http://www.gnu.org

This file is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

Autoro: Artur Trzewik mail@xdobry.de 
2010-01

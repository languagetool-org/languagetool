package org.languagetool.tokenizers.el;

%%

%integer
%final
%public
%class GreekWordTokenizerImpl
//%implements StandardTokenizerInterface
%function getNextToken
%char

//Delim = [\u0020\u00A0\u115f\u1160\u1680\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200A\u200B\u200c\u200d\u200e\u200f\u2028\u2029\u202a\u202b\u202c\u202d\u202e\u202f\u205F\u2060\u2061\u2062\u2063\u206A\u206b\u206c\u206d\u206E\u206F\u3000\u3164\ufeff\uffa0\ufff9\ufffa\ufffb,\.;\(\)\[\]\{\}\!:\"'’‘„“”…\\\/\t\n]
Delim = (\u0020 | \u00A0 | \u115f | \u1160 | \u1680 | \u2000 | \u2001 | \u2002
 | \u2003 | \u2004 | \u2005 | \u2006 | \u2007 | \u2008 | \u2009 | \u200A
 | \u200B | \u200c | \u200d | \u200e | \u200f | \u2028 | \u2029 | \u202a
 | \u202b | \u202c | \u202d | \u202e | \u202f | \u205F | \u2060 | \u2061
 | \u2062 | \u2063 | \u206A | \u206b | \u206c | \u206d | \u206E | \u206F
 | \u3000 | \u3164 | \ufeff | \uffa0 | \ufff9 | \ufffa | \ufffb
 | "," | "." | ";" | "(" | ")" | "[" | "]" | "{" | "}" | "!" | ":" | "\"" | "'"
 | "·" //Greek Ano Teleia
 | "’" | "‘" | "„" | "“" | "”" | "…" | "«" | "»" | "\\" | "/" | \t | \n)
Word = ("ό,τι" | (!(![^] | {Delim}))*  | {Delim})

%{

  public final int yychar()
  {
    return yychar;
  }

  public final String getText() {
    return new String(zzBuffer, zzStartRead, zzMarkedPos-zzStartRead);
  }
%}

%%

<<EOF>> { return -1; }

{Word} { return 0; }

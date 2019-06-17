README

Morfologik is a project aiming at generating Polish morphosyntactic 
dictionaries (hence the name) used for part-of-speech tagging and
part-of-speech synthesis.

VERSION: 2.0 PoliMorf

BUILD:  8 mar 2013 12:05:02

LICENCE

Copyright (c) 2013, Marcin Miłkowski
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met: 

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution. 

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

1. morfologik.txt is a tab-separated file, containing the following format:

inflected-formHTbase-formHTtags

where HT means a horizontal tab.

2. polish.dict is a binary dictionary file for morphological analysis in 
fsa_morph program by Jan Daciuk 
(see http://www.eti.pg.gda.pl/katedry/kiw/pracownicy/Jan.Daciuk/personal/fsa.html), 
usable also in LanguageTool grammar checker.

3. polish_synth.dict is a binary file for grammatical synthesis, usable
by morfologik-stemming library. To get an inflected word, use the 
following syntax in fsa_morph:

<word>|<tag>

For example:

niemiecki|adjp

gives "niemiecku+".

4. polish.info and polish_synth.info are required for using the binary
dictionaries in morfologik-stemming Java library.

TAGSET

The tagset used is roughly similar to IPI/NKJP corpus tagset, and described in more detail in the tagset.txt file.
See also www.nkjp.pl.

Morfologik, (c) 2007-2013 Marcin Miłkowski.

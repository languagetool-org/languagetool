<?php
$page = "development";
$title = "LanguageTool";
$title2 = "Development";
$lastmod = "2011-08-14 13:05:00 CET";
include("../../include/header.php");
include('../../include/geshi/geshi.php');
?>


<p class="firstpara">This is a collection of the developer documentation available for LanguageTool.
It's intended for people who want to understand LanguageTool so
they can write their own rules or even add support for a new language.
Software developers might also be interested in LanguageTool's
<?=show_link("API", "api/", 0)?>.</p>

<ul>
	<li><a href="#helpwanted">Help wanted!</a></li>
    <li><a href="#checkout">Checkout (Java developers only)</a></li>
	<li><a href="#installation">Installation and usage</a></li>
	<li><a href="#process">Language checking process</a></li>
	<li><a href="#xmlrules">Adding new XML rules</a></li>
	<li><a href="#javarules">Adding new Java rules</a></li>
	<li><a href="#translation">Translating the user interface</a></li>
	<li><a href="#newlanguage">Adding support for a new language</a></li>
	<li><a href="#background">Background</a></li>
</ul>

<p><a name="helpwanted"><strong>Help wanted!</strong></a><br />
We're looking for people who support us writing new rules so LanguageTool can
detect more errors. Also see <?=show_link("the list of supported languages", "../languages/", 0)?>.</p>

<p>How can you help?</p>

<ol>
	<li>Read this page</li>
	<li>Subscribe to the <?=show_link("mailing list",
		"http://lists.sourceforge.net/lists/listinfo/languagetool-devel", 1)?></li>
	<li>Try writing rules. For English and German, see the lists of errors
		on the <?=show_link("Links page", "/links/", 0)?>. Many of those
		errors are not yet detected.</li>
    <li><?=show_link("See the wiki", "http://languagetool.wikidot.com/", 0)?> for 
        more tips and tricks</li>
</ol>

<p><a name="checkout"><strong>Checkout (Java developers only)</strong></a><br />
If you are a Java developer and you want to extend LanguageTool or if you
want to use the latest development version, check out LanguageTool from subversion:</p>

<code>
svn co https://languagetool.svn.sourceforge.net/svnroot/languagetool/trunk/JLanguageTool languagetool
</code>

<p>You can then run the test with <tt>ant test</tt> or build the code with <tt>ant</tt>.</p>

<p><a name="installation"><strong>Installation and usage</strong></a><br />
Please see the README file that comes with LanguageTool and the 
<?=show_link("Usage page", "/usage/", 0) ?>.</p>

<p><a name="process"><strong>Language checking process</strong></a><br />
<ol>
	<li>The text to be checked is split into sentences</li>
	<li>Each sentence is split into words</li>
	<li>Each word is assigned its part-of-speech tag(s) (e.g. <em>cars</em>
		= plural noun, <em>talked</em> = simple past verb)</li>
	<li>The analyzed text is then matched against the built-in rules and against
		the rules loaded from the grammar.xml file</li>
</ol>

<p><a name="xmlrules"><strong>Adding new XML rules</strong></a><br />
Many rules are contained in <tt>rules/xx/grammar.xml</tt>, whereas <tt>xx</tt> is
a language code like <tt>en</tt> or <tt>de</tt>. A rule is basically a pattern
which shows an error message to the user if the pattern matches. A pattern can
address words or part-of-speech tags.
Here are some examples of patterns that can be used in that file:</p>

<ul class="largelist">
	<li><?php hl('<token bla="x">think</token>', "xmlcodeNoIndent"); ?>
		matches the word <em>think</em></li>
	<li><?php hl('<token regexp="yes">think|say</token>', "xmlcodeNoIndent"); ?>
		matches the regular expression
		<tt>think|say</tt>, i.e. the word <em>think</em> or <em>say</em></li>
	<li><?php hl('<token postag="VB" /> <token>house</token>', "xmlcodeNoIndent"); ?>
		matches a base form verb followed by the word <em>house</em>.
		See resource/en/tagset.txt for a list of possible part-of-speech tags (these are specific to English).</li>
	<li><?php hl('<token>cause</token> <token regexp="yes" negate="yes">and|to</token>', "xmlcodeNoIndent"); ?>
		matches the word <em>cause</em> followed
		by any word that is not <em>and</em> or <em>to</em></li>
	<li><?php hl('<token postag="SENT_START" /> <token>foobar</token>', "xmlcodeNoIndent"); ?>
		matches the word <em>foobar</em> only
		at the beginning of a sentence</li>
</ul>

<p>Pattern's terms are matched case-insensitively by default, this can be changed
by setting the <tt>case_sensitive</tt> attribute to <tt>yes</tt>.

<p>Here's an example of a complete rule that marks "bed English", "bat attitude"
etc as an error:</p>

<?php hl('<rule id="BED_ENGLISH" name="Possible typo &apos;bed/bat(bad) English/...&apos;">
    <pattern mark_from="0" mark_to="-1">
      <token regexp="yes">bed|bat</token>
      <token regexp="yes">English|attitude</token>
    </pattern>
    <message>Did you mean
      <suggestion>bad</suggestion>?
    </message>
    <example type="correct">
      Sorry for my <marker>bad</marker> English.
    </example>
    <example type="incorrect">
      Sorry for my <marker>bed</marker> English.
    </example>
</rule>'); ?>

<p>A short description of the elements and their attributes:</p>

<ul class="largelist">
	<li>element <tt>rule</tt>, attribute <tt>id</tt>: an internal identifier used to address this rule</li>
	<li>element <tt>rule</tt>, attribute <tt>name</tt>: the text displayed in the configuration</li>
	<li>element <tt>pattern</tt>, attributes <tt>mark_from</tt> and <tt>mark_to</tt>: what part of the original 
		text should be marked. The default, <tt>mark_from="0"</tt> and <tt>mark_to="0"</tt>, means to mark
		the complete matching token. For example, if the pattern contains three token
		elements that match the input text, those three matching words will be marked in the text.
		<tt>mark_to="-1"</tt> in the example above means that the last token of the match will not
		be marked.</li>
	<li>element <tt>token</tt>, attribute <tt>regexp</tt>: interpret the given token
		as a regular expression</li>
	<li>element <tt>message</tt>: The text displayed to the user if this rule matches.
		Use sub-element <tt>suggestion</tt> to suggest a possible replacement that corrects the error.</li>
	<li>element <tt>example</tt>: At least two examples that with one correct and one incorrect sentence.
		The incorrect sentence is supposed to be matched by this rule. The position of the error
		must be marked up with the sub-element <tt>marker</tt>. This is used by the 
		automatic test cases that can be run using <tt>ant test</tt>.</li>
</ul>

<p>There are more features not used in the example above:</p>

<ul class="largelist">
    
    <li>element <tt>token</tt>, attribute <tt>inflected</tt> is used to match not only the given form but 
    also all of its inflected forms. For example <tt>&lt;token inflected="yes"&gt;bicycle&lt;/token&gt;</tt> will
    match <em>bicycle</em>, <em>bicycles</em>, <em>bicycling</em> etc.</li>
    
	<li>element <tt>token</tt>, attribute <tt>skip</tt> is used
	in two situations:
	
	<br /><br />
	<p><strong>1. Simulate a simple chunker</strong> for languages with flexible word order, 
	e.g., for matching errors of rection; we could for example skip possible 
	adverbs in some rule. <tt>skip="1"</tt> works exactly as two rules, i.e.</p>

	<?php hl('<token skip="1">A</token>
<token>B</token>'); ?>

	<p>is equivalent to the pair of rules:</p>
	
	<?php hl('<token>A</token>
<token/>
<token>B</token>

<token>A</token>
<token>B</token>'); ?>

	<p>Using negative value, we can match until the B is found, no matter how 
	many tokens are skipped. This cannot be easily encoded using empty 
	tokens as above because the sentence could be of any length.</p>

	<br />
	<p><strong>2. Match coordinated words</strong>, for example to match
	"both... as well" we could write:</p>
	
	<?php hl('<token skip="-1">both<exception scope="next">and</exception></token>
<token>as</token>
<token>well</token>'); ?>

	<p>Here the exception is applied only to the skipped tokens.</p>
	
	<p>The scope attribute of the exception is used to make exception valid 
	only for the token the exception is specified (scope="current") or for 
	skipped tokens (scope="next"). Default behavior is scope="current". 
	Using scopes is useful where several different exceptions should be 
	applied to avoid false alarms. In some cases, it's useful to use 
	<tt>scope="previous"</tt> in rules that already have <tt>skip="-1"</tt>.
	This way, you can set an exception against a single token that immediately
	preceeds the matched token. For example, we want to match "tak" after "jak"
	which is not preceeded by a comma:</p>
	
	<? hl('<token>tak</token>
<token skip="-1">jak</token>
<token>tak<exception scope="previous">,</exception></token>'); ?>
	
	<p>In this case, the rule excludes all sentences, where there is a comma 
	before "tak". Note that it's very hard to make such an exclusion otherwise.	
	</p>

	<p><strong>3. Using variables in rules</strong>
	
	<p>In XML rules, you can refer to previously matched tokens in the pattern. For example:</p>
	
	<?php hl('<pattern mark_from="2">
 <token regexp="yes" skip="-1">ani|ni|i|lub|albo|czy|oraz<exception scope="next">,</exception></token>
 <token><match no="0"/></token>
</pattern>'); ?>
	
	<p>This rule matches sequences like <b>ani... ani, ni... ni, i... i</b> but you don't have to 
	write all these cases explicitly. The first match (matches are numbered from zero, so it's 
	&lt;match no="0"/&gt;) is automatically inserted into the second token. Note 
	that this rule will match sentences like:
	
	<tt>Nie	kupiłem ani gruszek ani jabłek. Kupię to lub to lub tamto.</tt></p>
	
	<p>A similar mechanism could be used in suggestions, however there are more features, and tokens are
	numbered from 1 (for compatibility with the older notation \1 for the first matched token). For example:</p>
	
	<?php hl('<suggestion><match no="1"/></suggestion>'); ?>

	<p>A more complicated example:</p>

	<?php hl('<pattern>
<token regexp="yes">^(\p{Lu}{2}+[i]*\p{Lu}+[\p{L}&amp;
&amp;[^\p{Lu}]]{1,4}+)</token>
</pattern>
<message>Prawdopodobny błąd zapisu odmiany;
  skrótowce odmieniamy z dywizem:
  <suggestion><match no="1" regexp_match="^(\p{Lu}{2}+[i]*\p{Lu}+)([\p{L}&amp;
&amp;[^\p{Lu}]]{1,4}+)" regexp_replace="$1-$2"/></suggestion></message>'); ?>
		
	<p>This rule matches Polish inflected acronyms such as "SMSem" that should be written with 
	a hyphen: "SMS-em". So the acronym is matched with a complicated regular expression, and the 
	match replaces the match using Java regular expression notation. Basically, the regular expression 
	only shows two parts and inserts a hyphen between them.</p>
	
	<p>For some languages (currently Polish and English), element &lt;match/&gt; can be used to 
	insert an inflected matched token (or another word with a specified part of speech 
	tag). For example:</p>
	
	<?php hl('<pattern mark_from="1" mark_to="-1">
 <token regexp="yes">has|have</token>
 <token postag="VBD|VBP|VB" postag_regexp="yes"><exception postag="VBN|NN:U.*|JJ.*|RB" postag_regexp="yes"/></token>
 <token><exception postag="VBG"/></token>
</pattern>
<message>Possible agreement error -- use past participle here: <suggestion><match no="2" postag="VBN"/></suggestion>.</message>'); ?>
		
	<p>The above rule takes the second verb with a POS tag "VBN", "VBP" or "VB" and displays its 
	form with a POS tag "VBN" in the suggestion. You can also specify POS tags using 
	regular expressions (<tt>postag_regexp="yes"</tt>) and replace POS tags – just like 
	in the above example with acronyms. This is useful for large and complicated 
	tagsets (for many examples, see Polish rule file: <tt>rules/pl/grammar.xml</tt>).</p>
	
	<p>Sometimes the rule should change the case of the matched word. For this purpose, 
	you can use <tt>case_conversion</tt> attribute values: <tt>startlower</tt>, <tt>startupper</tt>,
	<tt>allupper</tt> and <tt>alllower</tt>.
	
	<p>Another useful thing is that &lt;match&gt; can refer to a token, but apply its POS 
	to another word. This is useful for suggesting another word with the same part 
	of speech. There is a special abbreviated syntax used for this purpose:</p>
	
	<?php hl('<match no="1" postag="verb:.*perf">kierować</match>'); ?>
	
	<p>This syntax means: take the POS tag of the first matched token that matches the regular expression specified 
	in the <tt>postag</tt> attribute, and then apply this POS tag to the verb "kierować". This way the verb 
	will be inflected just the way the matched verb was originally inflected. The reason why you 
	need to specify the POS tag is that the matched token can have several POS tags (several readings).</p>
	
	<p>Note that by default <tt>&lt;match&gt;</tt> element inside the <tt>&lt;token&gt;</tt> element inserts only a string – 
	so it matches a string, and not part of speech tags. So even if it refers to 
	a token with a POS tag, it copies the matched token, and not its POS token. However, 
	you can use all above attributes to change the form of the token.</p>
	<p>You can however use the <tt>&lt;match&gt;</tt> element to copy POS tags alone but to do so,
	you must use the attribute <tt>setpos="yes"</tt>. All other attributes can be applied so that
	the POS could be converted appropriately. This can be useful for creating rules specifying grammatical 
	agreement. Currently, such rules must be quite wordy, somewhat more terse syntax is in 
	development.</p>
	<p><strong>4. Turning the rule off</strong></p>
	<p>Some rules can be optional, useful only in specific registers,
	or very sensitive. You can turn them off by default by using an 
	attribute <tt>default="off"</tt>. The user can turn the rule in the
	Options dialog box, and this setting is being saved in the configuration
	file.</p>
	</li>
</ul>

<p><a name="javarules"><strong>Adding new Java rules</strong></a><br />
Rules that cannot be expressed with a simple pattern in <tt>grammar.xml</tt>
can be developed as a Java class. See 
<tt><a href="http://languagetool.svn.sourceforge.net/viewvc/languagetool/trunk/JLanguageTool/src/java/de/danielnaber/languagetool/rules/WordRepeatRule.java?revision=4635&amp;content-type=text%2Fplain">rules/WordRepeatRule.java</a></tt>
for a simple
example which you can use to develop your own rules. You will also need to
add your rule's id to <tt>&lt;YourLanguage&gt;.java</tt> to activate it.</p>

<p><a name="translation"><strong>Translating the user interface</strong></a><br />
To translate the user interface, just copy <tt>MessagesBundle_en.properties</tt>
to <tt>MessagesBundle_xx.properties</tt> (whereas <tt>xx</tt> is the code of your
language) and translate the text. Note that hot keys for menu items are specified
with the <tt>&amp;</tt> character (for example, <tt>&amp;File</tt>).
The next time you start LanguageTool, it should show your translation (assuming your computer is configured to use your 
language -- if
that's not the case, start LanguageTool with <tt>java -Duser.language=xx -jar LanguageToolGUI.jar</tt>).
</p>

<p><a name="newlanguage"><strong>Adding support for a new language</strong></a><br />
Adding a new language requires some changes to the Java source files. You should check out
the "JLanguageTool" module from subversion (see <a href="#checkout">above</a> or the <a href="http://sourceforge.net/scm/?type=svn&amp;group_id=110216">sourceforge 
help</a>). You may then call <tt><a href="http://ant.apache.org/">ant</a></tt> to
build LanguageTool (this is optional, it's okay to work only inside Eclipse). Ant should compile
a file named like <tt>LanguageTool-1.x.y-dev.oxt</tt> in the <tt>dist</tt> directory.</p>

<ul>

<li><p><tt>Language.java</tt> contains 
the information about supported languages. You can add a new language by creating
a new <tt>Language</tt> object in this class and providing a part-of-speech tagger
for it, similar to <tt>de/danielnaber/languagetool/tagging/en/EnglishTagger.java</tt>. The tagger
must implement the <tt>Tagger</tt> interface, any implementation details (i.e. how
to actually assign tags to words) are up to you -- the easiest thing is probably
to just copy the English tagger.</p>

<p>A trivial tagger that only assigns
null tags to words is <tt>DemoTagger</tt>. This is enough for rules that refer
to words but not to part-of-speech tags. You can add those rules to a file
<tt>rules/xy/grammar.xml</tt>, whereas <tt>xy</tt> is the short name for your language.
You will also need to add the short name of your language to <tt>rules.dtd</tt>.</p>

<p>The test cases run by "ant test" will automatically include your new language
and its rules, based on the "example" elements of each rule.</p>

<p>To add part-of-speech tags, please have a look at <tt>resource/en/make-dict-en.sh</tt>
(note: this file is only in subversion, not in the released OXT). First try to make it work
for English. You need the
<?=show_link("fsa", "http://www.eti.pg.gda.pl/katedry/kiw/pracownicy/Jan.Daciuk/personal/fsa.html", 1) ?> 
package. Install it and add its installation directory to your PATH. Once it works for English,
create your own version of <tt>manually_added.txt</tt> and use that to create a <tt>.dict</tt> file,
then adapt your tagger to use it (e.g. copy <tt>EnglishTagger.java</tt> and change the 
<tt>getFileName()</tt> implementation). More details about building dictionaries
are <?=show_link("in the Wiki.", "http://languagetool.wikidot.com/developing-a-tagger-dictionary", 0) ?>
</p></li>

<li>Adapt <tt>openoffice/Addons.xcu</tt> and <tt>openoffice/description.xml</tt> to translate the user
interface of LanguageTool into your language when used in OpenOffice.org.</li>

<li>Adapt <tt>build.xml</tt>. Just search for "/en/"
in that file and copy those lines, adapting them to your language.</li>

<li>Add a two-letter code of your language to <tt>MessageBundle.properties</tt>.</li>

<li>Copy <tt>MessagesBundle.properties</tt> to <tt>MessagesBundle_xx.properties</tt>,
whereas <tt>xx</tt> is the code of your new language and translate all values (i.e. the strings
on the right of the "=" sign).</li>

</ul>

<p><a name="background"><strong>Background</strong></a><br />
For background information, my diploma thesis 
about LanguageTool is available (note that this refers to an earlier version of LanguageTool
which was written in Python):<br />
<?=show_link("PDF, 650 KB", "http://www.danielnaber.de/languagetool/download/style_and_grammar_checker.pdf", 0) ?>
<br /><?=show_link("Postscript (.ps.gz), 630 KB", "http://www.danielnaber.de/languagetool/download/style_and_grammar_checker.ps.gz", 0) ?>
</p>

<?php
include("../../include/footer.php");
?>

<?php
$page = "other";
$title = "LanguageTool";
$title2 = "Common Problems";
$lastmod = "2011-09-15 22:30:00 CET";
include("../../include/header.php");
?>

<h2><a name="commonproblems">Checklist in Case of Problems</a></h2>

<ul class="largelist">
<li>Did you restart OpenOffice.org - including the QuickStarter - after installation of LanguageTool? This is required,
	even if OpenOffice.org doesn't say so. (<a href="http://qa.openoffice.org/issues/show_bug.cgi?id=88692">Issue 88692</a>)</li>
<li>Make sure <a href="http://www.java.com/en/download/manual.jsp">Java 5.0 or later from Oracle/Sun</a>
	is installed on your system. Java versions which are not from Oracle/Sun may not work.</li>
<li>Make sure this version of Java is selected in OpenOffice.org
	(under <em>Tools -&gt; Options -&gt; Java</em>).</li>
<li>If LanguageTool doesn't start and you see no error message, please
	check if the extension is enabled in the Extension manager
	(under <em>Tools -&gt; Extension Manager</em>).</li>
</ul>

<h2>Common problems with OpenOffice.org/LibreOffice integration</a></h2>

<ul class="largelist">
<li><strong>Freeze on startup</strong>: for some people, LanguageTool freezes LibreOffice or OpenOffice.org on startup for seconds to minutes.
 We don't have a solution yet, neither can we reproduce the problem. <a href="https://sourceforge.net/tracker/?func=detail&amp;aid=3153545&amp;group_id=110216&amp;atid=655717">Bug report</a>.
</li>
<li>When you get an <strong>error message during installation on Ubuntu</strong>, you might need to
	install the <tt>openoffice.org-java-common</tt> package. See
	<a href="http://nancib.wordpress.com/2008/05/03/fixing-the-openofficeorg-grammar-glitch-in-ubuntu-hardy/">this blog posting</a>
	for details.</li>
<li>If you get <strong>"This media-type is not supported: application/vnd.sun.star.package-bundle2.0.00"</strong> during installation, please consider
    <a href="http://user.services.openoffice.org/en/forum/viewtopic.php?p=58403#p58403">resetting your OpenOffice user profile</a>.</li>
<li>If you get <strong>"Could not create Java implementation loader"</strong>, try this:
    Got to <em>Tools -> Options -> Java</em>, uncheck "Use a Java runtime environment", exit OpenOffice.org and start it again,
    check "Use a Java runtime environment" again and try to install/activate the LanguageTool extension
    (<a href="http://sourceforge.net/projects/opencards/forums/forum/707158/topic/1886832">source</a>) - please
    let us know if this worked for you</li>
<li>If you get <strong>Failed to load rules for language ... Caused by java.lang.ClassNotFoundException: Loading rules failed: Duplicate class definition</strong>:
    For some reason LanguageTool is installed twice as an extension in OpenOffice.org. You can try deleting the directories listed
    in the error message after making a backup. (The directories to be deleted have random names like "EE31.tmp_" or similar - exit OpenOffice.org
    before deleting anything).</li>
<li>The <strong>menu items in LibreOffice/OpenOffice.org get mixed up</strong> when both <a href="http://open.afterthedeadline.com/">After the Deadline</a>
	and LanguageTool are installed. This issue is tracked as <a href="http://openatd.trac.wordpress.org/ticket/215">ticket #215 at After the Deadline</a>.</li>

<li>If you are using an older version of LanguageTool and/or OpenOffice.org, these issues may affect you:
    <ul>
        <li>LanguageTool installation fails if the name of your user account contains
            special characters. The only workaround so far seems to be to use a different
            user account. (<a href="http://qa.openoffice.org/issues/show_bug.cgi?id=95162">Issue 95162</a>)</li>
        <li>If you get a message "Can not activate the factory for com.sun.star.help.HelpIndexer because java.lang.NoClassDefFoundError: org/apache/lucene/analysis/cjk/CJKAnalyzer":
            this was a bug In OpenOffice.org 3.1, it was fixed in version 3.2 (<a href="http://qa.openoffice.org/issues/show_bug.cgi?id=98680">Issue 98680</a>)</li>
        <li>If you get "Failed to load rules for language English" when opening the configuration dialog, try the latest version of LanguageTool, as this should be fixed in 1.3
            (problem occurred on openSUSE 11.3 with LanguageTool 1.2 pre-installed)
            <!-- 2011-03-18 --></li>
        <li>LanguageTool didn't work together with the <a href="http://extensions.services.openoffice.org/en/project/DeltaXMLODTCompare">DeltaXML
            ODT Compare</a> extension - use version 1.2.0 of DeltaXML ODT Compare, which fixes the problem.</li>
    </ul>
</li>
</ul>

<h2>Known Limitations</h2>

<ul>
    <li>Some errors are not detected: LanguageTool uses rules to detect errors, i.e. it will only complain about errors for which there 
        is a rule that detects it. Please consider learning <a href="../development/">how to write rules</a> and help make LanguageTool 
        better by contributing your rules.</li>
    <li>For some rules there are a lot of false alarms, i.e., LanguageTool complains about text which is actually correct</li>
    <li>LanguageTool doesn't work correctly with documents that contain revisions
        (<a hreF="https://issues.apache.org/ooo/show_bug.cgi?id=92013">Issue 92013</a>,
        <a href="https://bugs.freedesktop.org/show_bug.cgi?id=36540">LibreOffice issue 36540</a>)
    </li>
</ul>

<h2>Still need Help?</h2>

<p>If LanguageTool still doesn't properly work for you, please email <strong>naber at danielnaber de</strong> describing the problem
and letting me know which version of LanguageTool, LibreOffice/OpenOffice.org and which operating system you are using.</p>

<?php
include("../../include/footer.php");
?>

<?xml version="1.0" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 version="1.0">
<!-- XSLT stylesheet to pretty print grammar.xml

usage:

java -jar saxon8.jar grammar.xml print.xsl

-->
<xsl:output method="html" encoding="windows-1250" indent="yes"/>
 
 <xsl:template match="text()"/>
 
 <xsl:template match="//category">
 <p><strong><xsl:value-of select="@name"/></strong></p>
 <ol>
 <xsl:apply-templates select="*"/>
 </ol>
</xsl:template>
 
 
 <xsl:template match="//rule[@id!='']">
 <li><xsl:value-of select="@name"/></li>
 <ul>
<xsl:apply-templates select="*"/>
</ul>
 </xsl:template>
 
 <xsl:template match="//rulegroup">
 <li><xsl:value-of select="@name"/></li>
<ul>
<xsl:apply-templates select="*"/>
</ul>
 </xsl:template>
 
 
 <xsl:template match="//rule/example[@type='incorrect']">
<li>
 <xsl:apply-templates select="*|text()"/>
 </li>
</xsl:template>

 <xsl:template match="//rule/example[@type='incorrect']/text()">
 <xsl:copy-of select="."/>
</xsl:template>

 <xsl:template match="//rule/example[@type='incorrect']/marker">
 <strong style="color: rgb(255, 0, 0);">
 <xsl:value-of select="./text()"/>
 </strong>
</xsl:template>

</xsl:stylesheet>
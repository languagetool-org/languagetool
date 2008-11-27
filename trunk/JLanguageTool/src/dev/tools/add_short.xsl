<?xml version="1.0" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 version="1.0">

 <!-- A simple stylesheet that adds "short" element with category name to  grammar files 
 (c) Marcin Miłkowski 2008, LGPL
Note: remove DOCTYPE declaration before conversion and add after it. Otherwise, you'd get
all default values in the grammar.xml!!! 

Usage:

java -jar saxon8.jar grammar.xml add_short.xsl >new_grammar.xml

Then rename new_grammar.xml to grammar.xml, after making a backup of grammar.xml -->

 <xsl:output method="xml" encoding="utf-8" indent="no"/>
 
 <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

 <xsl:template match="@xml:space"/>  
  
 <xsl:template match="message">
	<xsl:copy>
		<xsl:apply-templates select="@*|node()"/>
	</xsl:copy>
	<xsl:text>
	</xsl:text>
	<xsl:element name="short">
	<xsl:choose>
	<xsl:when test="name(../..)='rulegroup'">
	<xsl:value-of select="../../../@name"></xsl:value-of>
	</xsl:when>
	<xsl:otherwise><xsl:value-of select="../../@name"/></xsl:otherwise>
	</xsl:choose>
	</xsl:element>	
</xsl:template>

</xsl:stylesheet>
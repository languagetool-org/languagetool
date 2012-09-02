<?xml version="1.0" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 version="1.0">
<!--
 A simple stylesheet that adds "short" element with category name to grammar files 
 Copyright (C) 2008 Marcin Miłkowski
 
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
 
Note: remove DOCTYPE declaration before conversion and add after it. Otherwise, you'd get
all default values in the grammar.xml!!! 

Usage:

java -jar saxon8.jar grammar.xml add_short.xsl >new_grammar.xml

Then rename new_grammar.xml to grammar.xml, after making a backup of grammar.xml
-->

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
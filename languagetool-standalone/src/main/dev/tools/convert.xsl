<?xml version="1.0" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 version="1.0">
<!-- XSLT stylesheet to convert grammar.xml <em> elements

 Copyright (C) 2008 Marcin Miłkowski.

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

Note: it's obsolete and useless for current grammar.xml files. 

usage:

java -jar saxon8.jar grammar.xml convert.xsl

-->
 <xsl:output method="xml" encoding="utf-8" indent="yes"/>
 
 <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

 <xsl:template match="//message/em">
 <xsl:element name="suggestion">
 <xsl:value-of select="./text()"/>
 </xsl:element>
</xsl:template>

 <xsl:template match="//example/em">
 <xsl:element name="marker">
 <xsl:value-of select="./text()"/>
 </xsl:element>
</xsl:template>

</xsl:stylesheet>
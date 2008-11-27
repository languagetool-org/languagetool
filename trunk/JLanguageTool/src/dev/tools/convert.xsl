<?xml version="1.0" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 version="1.0">
<!-- XSLT stylesheet to convert grammar.xml <em> elements

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
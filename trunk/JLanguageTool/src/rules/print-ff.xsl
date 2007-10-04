<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">
	<!-- XSLT stylesheet to pretty print false-friends.xml
		
		usage:
		
		java -jar saxon8.jar false-friends.xml print-ff.xsl
		
	-->
	<xsl:output method="html" encoding="UTF-8" indent="no" />

	<xsl:template match="text()" />

	<xsl:template match="*">	
			<xsl:apply-templates select="*">
				<xsl:sort select="@lang"/>
			</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template match="token">
	<strong style="color: #339900;">		
		<xsl:value-of select="translate(.,'|',',')"/>
		<xsl:text> </xsl:text>
	</strong>
	</xsl:template>

	<xsl:template match="pattern">
		<xsl:apply-templates select="*"/>
		(<xsl:value-of select="@lang"/>)
	</xsl:template>

	<xsl:template match="//rule">
		<xsl:apply-templates select="*">
			<xsl:sort select="pattern/token"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template match="translation">
		<ul>
		<li>
			<xsl:value-of select="."/>			
			<xsl:text> (</xsl:text><xsl:value-of select="@lang"/><xsl:text>)</xsl:text>			
		</li>
		</ul>
	</xsl:template>

	<xsl:template match="//rules">	
	<html>
	<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
	<body>	
			<xsl:apply-templates select="*">
				<xsl:sort select="token"/>
			</xsl:apply-templates>
	</body>
	</html>
	</xsl:template>	
	
</xsl:stylesheet>
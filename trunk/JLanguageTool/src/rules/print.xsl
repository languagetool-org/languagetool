<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">
	<!-- XSLT stylesheet to pretty print grammar.xml
		
		usage:
		
		java -jar saxon8.jar grammar.xml print.xsl
		
	-->
	<xsl:output method="html" encoding="UTF-8" indent="yes" />

	<xsl:template match="text()" />

	<xsl:template match="*">	
			<xsl:apply-templates select="*">
				<xsl:sort select="@name"/>
			</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template match="//category">
		<p>
			<xsl:element name="strong">
				<xsl:value-of select="@name" />
			</xsl:element>
		</p>
		<ol>			
			<xsl:apply-templates select="*">
				<xsl:sort select="@name"/>
			</xsl:apply-templates>
		</ol>
	</xsl:template>


	<xsl:template match="//rule[@id!='']">
		<li>
			<xsl:value-of select="@name" />			
		</li>
		<ul>
			<xsl:apply-templates select="*" />
		</ul>
	</xsl:template>

	<xsl:template match="//rulegroup">
		<li>
			<xsl:value-of select="@name" />
		</li>
		<ul>
			<xsl:apply-templates select="*" />
		</ul>
	</xsl:template>


	<xsl:template match="//rule/example[@type='incorrect']">
		<li>
			<xsl:apply-templates select="*|text()" /> <br/>
			<xsl:if test="@correction !=''">
			<xsl:choose>
			<xsl:when test="//rules[@lang='pl']">Poprawnie: </xsl:when>
			<xsl:when test="//rules[@lang='en']">Correctly: </xsl:when>
			<xsl:when test="//rules[@lang='de']">Korrekt: </xsl:when>
			<xsl:when test="//rules[@lang='fr']">Correctement : </xsl:when>
			<xsl:when test="//rules[@lang='nl']">Correct: </xsl:when>
			<xsl:when test="//rules[@lang='es']">Correctamente: </xsl:when>
			</xsl:choose>
				<strong style="color: #339900;">
					<xsl:value-of select="@correction"/>
				</strong>				
			</xsl:if>
		</li>
	</xsl:template>

	<xsl:template match="//rule/example[@type='incorrect']/text()">
		<xsl:copy-of select="." />
	</xsl:template>

	<xsl:template match="//rule/example[@type='incorrect']/marker">
		<strong style="color: rgb(255, 0, 0);">
			<xsl:value-of select="./text()" />
		</strong>
	</xsl:template>
	
	<xsl:template match="//rules">	
	<html>
	<body>
		<xsl:choose>
		<xsl:when test="//rules[@lang='pl']">Liczba reguł: </xsl:when>
		<xsl:when test="//rules[@lang='en']">Number of rules: </xsl:when>
		<xsl:when test="//rules[@lang='de']">Regeln: </xsl:when>
		<xsl:otherwise>Number of rules: </xsl:otherwise>
		</xsl:choose>
		<strong>
			<xsl:value-of select="count(//rule)"/>
		</strong>
		<br/>
		<xsl:choose>
		<xsl:when test="//rules[@lang='pl']">W tym z podpowiedziami: </xsl:when>
		<xsl:when test="//rules[@lang='en']">Rules with suggestions: </xsl:when>
		<xsl:otherwise>Rules with suggestions: </xsl:otherwise>
		</xsl:choose>
		<strong>
			<xsl:value-of select="count(//message[suggestion!=''])"/>
		</strong>
			<xsl:apply-templates select="*">
				<xsl:sort select="@name"/>
			</xsl:apply-templates>
	</body>
	</html>
	</xsl:template>	
	
</xsl:stylesheet>
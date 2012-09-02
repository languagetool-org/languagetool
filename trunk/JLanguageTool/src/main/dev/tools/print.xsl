<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="2.0">
	<!-- XSLT stylesheet to pretty print grammar.xml
		
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
 		
		usage:
		
		java -jar saxon8.jar grammar.xml print.xsl
		
		This version doesn't work in Firefox, unfortunately...
		
	-->
	<xsl:output method="html" encoding="UTF-8" indent="no" />

	<xsl:template match="text()" />

	<xsl:template match="*">	
			<xsl:apply-templates select="*">
				<xsl:sort select="@name"/>
			</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template match="//category">		
		<xsl:variable name="category_name" select="@name"/>
		<xsl:variable name="cat_id" select="generate-id()"/>
		<xsl:element name="div">
		<xsl:attribute name="id"><xsl:copy-of select="$cat_id"/></xsl:attribute>
		<xsl:attribute name="style">display:none</xsl:attribute>			
		<h4>		
		<xsl:element name="a">
		<xsl:attribute name="href">javascript:;</xsl:attribute>
		<xsl:attribute name="onmousedown">toggleDiv('<xsl:copy-of select="$cat_id"/>');</xsl:attribute>
		<xsl:value-of select="$category_name"/>
		</xsl:element>
        (<xsl:value-of select="count(rule[@id!=''])+count(rulegroup[@id!=''])"/>)
		</h4>
		<ol>			
			<xsl:apply-templates select="*">
				<xsl:sort select="@name"/>
			</xsl:apply-templates>
		</ol>
		</xsl:element>
		<h4>		
		<xsl:element name="a">
		<xsl:attribute name="href">javascript:;</xsl:attribute>
		<xsl:attribute name="onmousedown">toggleDiv('<xsl:copy-of select="$cat_id"/>');</xsl:attribute>
		<xsl:value-of select="$category_name"/>
		</xsl:element>
        (<xsl:value-of select="count(rule[@id!=''])+count(rulegroup[@id!=''])"/>)
		</h4>
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
			<xsl:if test="../short/text()!=''">
			<xsl:value-of select="../short/text()"/>. 
			</xsl:if> 
			<xsl:if test="@correction !=''">
			<xsl:choose>
			<xsl:when test="not(contains(@correction, '|')) and not(contains(../message/text()[1], '\')) and count(../message/text()) &lt; 3">
			<xsl:copy-of select="../message/text()[1]"/>
			<strong style="color: #339900;"><xsl:value-of select="@correction"/></strong>
			<xsl:copy-of select="../message/text()[2]"/>
			</xsl:when>
			<xsl:otherwise>
<!--
Remaining problem: replace \1 in message text with pattern/token[1]

 
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
				 --> 
											
				<xsl:variable name="message" select="../message/text()"/>
					<xsl:for-each select="tokenize(@correction,'\|')">
					<xsl:variable name="message_cnt" select="position()"/>					
					<xsl:value-of select="$message[$message_cnt]"/>					
					<strong style="color: #339900;">					
					<xsl:value-of select="."/>
					</strong>
					<xsl:if test="position()=last()">
						<xsl:variable name="last" select="last()+1"/>
						<xsl:value-of select="$message[$last]"/>
					</xsl:if>										  
					</xsl:for-each>				
			 </xsl:otherwise>
			 </xsl:choose>
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
	<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
	<head>
	<script language="javascript">
	<xsl:text>
	  function toggleDiv(divid){
    	if(document.getElementById(divid).style.display == 'none'){
	      document.getElementById(divid).style.display = 'block';
    	}else{
	      document.getElementById(divid).style.display = 'none';
    	}
	  }
	 </xsl:text>
	</script>
	</head>
	<body>
        <noscript><p><strong>Note:</strong> this page requires Javascript to work</p></noscript>
		<xsl:choose>
		<xsl:when test="//rules[@lang='pl']">Łączna liczba reguł: </xsl:when>
		<xsl:otherwise>Total number of rules: </xsl:otherwise>
		</xsl:choose>
		<strong>
			<xsl:value-of select="count(//rule)"/>
		</strong>		
		<br/>
		<xsl:choose>
		<xsl:when test="//rules[@lang='pl']">W tym z podpowiedziami: </xsl:when>
		<xsl:otherwise>Rules with suggestions: </xsl:otherwise>
		</xsl:choose>
		<strong>
			<xsl:value-of select="count(//message[suggestion!=''])"/>
		</strong>
		<br/>
		<xsl:choose>
		<xsl:when test="//rules[@lang='pl']">Liczba widocznych typów reguł: </xsl:when>
		<xsl:otherwise>Total number of visible rule types: </xsl:otherwise>
		</xsl:choose>
		<strong>
			<xsl:value-of select="count(//rule[@id!=''])+count(//rulegroup[@id!=''])"/>
		</strong>		
		<br/>
	
			<xsl:apply-templates select="*">
				<xsl:sort select="@name"/>
			</xsl:apply-templates>
	</body>
	</html>
	</xsl:template>	
	
</xsl:stylesheet>
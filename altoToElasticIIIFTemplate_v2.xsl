<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:alto="http://www.loc.gov/standards/alto/ns-v2#" version="1.0">
    <xsl:output method="text"/>
    <!-- This needs to resolve to the annotation list: -->
    <xsl:param name="annoURI" select="'http://localhost/IIIFAltoConvertor/canvas-FLPID.json'"/>
    <!--
        The ALTO may have been generated from the TIFF, if so the jp2 or IIIF image might be a different size. If so
        use the following ratios to reduce the TIFF coordinators to the IIIF image coordinates:
-->    
    <xsl:param name="xRatio" select="'XRAT'"/>
    <xsl:param name="yRatio" select="'YRAT'"/>
    <!-- Links to the canvas for the annotation and the manifest for the within -->
    <xsl:param name="canvasURI" select="'https://lib.is/IEPID/canvas/canvas-FLPID.json'" />
    <xsl:param name="modificationdate" select="'MODIFICATIONDATE'" />
    <!--
        Include this if you want to have a within link in the annotation. For example:
        <xsl:param name="manifestURI" select="'http://dams.llgc.org.uk/iiif/3100186/manifest.json'"/>
    -->
    <xsl:param name="manifestURI" select="'https://lib.is/IEPID/manifest'"/>
    <xsl:variable name="quote">'</xsl:variable>
    <xsl:variable name="doubleqoute">"</xsl:variable>
    <xsl:template match="/">
        <xsl:for-each select="//alto:String">
<xsl:variable name="text">
                                <xsl:choose>
                                    <xsl:when test="name(.) = 'String'">
                                        <xsl:value-of select="./@CONTENT"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:apply-templates/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:variable>
{ "index": { "_index": "annotations" } }
{ "chars":"<xsl:call-template name="replace-string">
                                <xsl:with-param name="text" select="normalize-space($text)" />
                            <xsl:with-param name="text">
                                    <xsl:call-template name="replace-string">
                                    <xsl:with-param name="text" select="normalize-space($text)" />
                                    <xsl:with-param name="replace" select="'\'"/>
                                    <xsl:with-param name="with" select="'\\'"/>
                                </xsl:call-template>

                            </xsl:with-param>
                            <xsl:with-param name="replace" select="$doubleqoute"/>
                                <xsl:with-param name="with" select="concat('\', $doubleqoute)"/>
                            </xsl:call-template>", "on":"<xsl:value-of select="$canvasURI"/>#xywh=<xsl:value-of select="floor((@HPOS*(@HPOS >=0) - @HPOS*(@HPOS &lt; 0)) div $xRatio)"/>,<xsl:value-of select="floor((@VPOS*(@VPOS >=0) - @VPOS*(@VPOS &lt; 0)) div $yRatio)"/>,<xsl:value-of select="floor((@WIDTH*(@WIDTH >=0) - @WIDTH*(@WIDTH &lt; 0)) div $xRatio)"/>,<xsl:value-of select="floor((@HEIGHT*(@HEIGHT >=0) - @HEIGHT*(@HEIGHT &lt; 0)) div $yRatio)"/>","modificationdate": "<xsl:value-of select="$modificationdate"/>" }<xsl:text>&#10;</xsl:text>
<xsl:if test="position() != last()"></xsl:if>
                </xsl:for-each>
		
    </xsl:template>
    <xsl:template match="alto:String">
        <xsl:value-of select="./@CONTENT"/>
    </xsl:template>
    <xsl:template match="alto:SP">
        <xsl:text> </xsl:text>
    </xsl:template>
    <xsl:template name="replace-string">
        <xsl:param name="text"/>
        <xsl:param name="replace"/>
        <xsl:param name="with"/>
        <xsl:choose>
            <xsl:when test="contains($text,$replace)">
                <xsl:value-of select="substring-before($text,$replace)"/>
                <xsl:value-of select="$with"/>
                <xsl:call-template name="replace-string">
                    <xsl:with-param name="text"
                        select="substring-after($text,$replace)"/>
                    <xsl:with-param name="replace" select="$replace"/>
                    <xsl:with-param name="with" select="$with"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$text"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:svg="http://www.w3.org/2000/svg"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  version="2.0">

    <xsl:template match="@xlink:show">
        <xsl:attribute name="xlink:show">new</xsl:attribute>
    </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="svg:svg">
<xsl:copy>

      <xsl:apply-templates select="@*"/>
<svg:style type="text/css">
a {
   text-decoration: underline;
   color:blue;
}
</svg:style>

      <xsl:apply-templates select="node()"/>
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>

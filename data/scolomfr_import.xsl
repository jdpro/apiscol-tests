<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:lom="http://ltsc.ieee.org/xsd/LOM"
	xmlns:lomfr="http://www.lom-fr.fr/xsd/LOMFR" xmlns:scolomfr="http://www.lom-fr.fr/xsd/SCOLOMFR">
	<xsl:output method="html" omit-xml-declaration="yes"
		encoding="UTF-8" indent="yes" />
	<!-- Define user language here : it will be used to select a langstring 
		value -->
	<xsl:variable name="user.language" select="'fr'" />
	<!-- Start point -->
	<xsl:template match="/lom:lom">
		<add>
			<doc>
				<xsl:apply-templates />
			</doc>
		</add>
	</xsl:template>
	<!-- Identifier is a technical element, will not be indexed -->
	<xsl:template match="lom:general/lom:identifier" />
	<!-- Title will be indexed as full text -->
	<xsl:template match="lom:general/lom:title">
		<xsl:element name="field">
			<xsl:attribute name="name"><xsl:value-of select="'general.title'"></xsl:value-of></xsl:attribute>
			<xsl:call-template name="langstring" />
		</xsl:element>
	</xsl:template>
	<!-- Language is indexed for faceted search -->
	<xsl:template match="lom:general/lom:language">
		<xsl:element name="field">
			<xsl:attribute name="name"><xsl:value-of select="'general.language'"></xsl:value-of></xsl:attribute>
			<xsl:value-of select="." />
		</xsl:element>
	</xsl:template>
	<!-- Description will be indexed as full text -->
	<xsl:template match="lom:general/lom:description">
		<xsl:element name="field">
			<xsl:attribute name="name"><xsl:value-of
				select="'general.description'"></xsl:value-of></xsl:attribute>
			<xsl:call-template name="langstring" />
		</xsl:element>
	</xsl:template>
	<!-- Description will be indexed as full text -->
	<xsl:template match="lom:general/lom:keyword">
		<xsl:call-template name="splitkeywords">
			<xsl:with-param name="string">
				<xsl:call-template name="langstring" />
			</xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	<!-- Coverage will be indexed as full text -->
	<xsl:template match="lom:general/lom:coverage">
		<xsl:element name="field">
			<xsl:attribute name="name"><xsl:value-of select="'general.coverage'"></xsl:value-of></xsl:attribute>
			<xsl:call-template name="langstring" />
		</xsl:element>
	</xsl:template>
	<xsl:template match="lom:general/scolomfr:generalResourceType">
		<xsl:element name="field">
			<xsl:attribute name="name"><xsl:value-of
				select="'general.generalresourcetype'"></xsl:value-of></xsl:attribute>
			<xsl:value-of select="scolomfr:value" />
		</xsl:element>
	</xsl:template>
	<xsl:template match="lom:lifeCycle/lom:status">
		<xsl:element name="field">
			<xsl:attribute name="name"><xsl:value-of select="'lifecycle.status'"></xsl:value-of></xsl:attribute>
			<xsl:value-of select="lom:value" />
		</xsl:element>
	</xsl:template>
	<xsl:template match="lom:lifeCycle/lom:contribute">
		<xsl:variable name="role" select="lom:role/lom:value" />
		<xsl:apply-templates select="lom:entity">
			<xsl:with-param name="role">
				<xsl:value-of select="$role"></xsl:value-of>
			</xsl:with-param>
			<xsl:with-param name="type">
				<xsl:value-of select="'lifecycle'"></xsl:value-of>
			</xsl:with-param>
		</xsl:apply-templates>
		<xsl:apply-templates select="lom:date">
			<xsl:with-param name="role">
				<xsl:value-of select="$role"></xsl:value-of>
			</xsl:with-param>
			<xsl:with-param name="type">
				<xsl:value-of select="'lifecycle'"></xsl:value-of>
			</xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>
	<xsl:template match="lom:metaMetadata/lom:contribute">
		<xsl:variable name="role" select="lom:role/lom:value" />
		<xsl:apply-templates select="lom:entity">
			<xsl:with-param name="role">
				<xsl:value-of select="$role"></xsl:value-of>
			</xsl:with-param>
			<xsl:with-param name="type">
				<xsl:value-of select="'metameta'"></xsl:value-of>
			</xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>
	<xsl:template match="lom:entity">
		<xsl:param name="role" />
		<xsl:param name="type" />
		<xsl:element name="field">
			<xsl:attribute name="name"><xsl:value-of
				select="concat($type,'.contributor.',$role)"></xsl:value-of></xsl:attribute>
			<xsl:call-template name="extract-from-vcard">
				<xsl:with-param name="vcard">
					<xsl:value-of select="." />
				</xsl:with-param>
			</xsl:call-template>
		</xsl:element>
	</xsl:template>
	<xsl:template match="lom:date">
		<xsl:param name="role" />
		<xsl:param name="type" />
		<xsl:variable name="datecontent" select="lom:dateTime" />
		<xsl:choose>
			<xsl:when test="not(normalize-space($datecontent))">
			</xsl:when>
			<xsl:otherwise>
				<xsl:element name="field">
					<xsl:attribute name="name"><xsl:value-of
						select="concat($type,'.date.',$role)"></xsl:value-of></xsl:attribute>
					<xsl:value-of select="concat($datecontent,'T00:00:00Z')" />
				</xsl:element>
			</xsl:otherwise>
		</xsl:choose>


	</xsl:template>
	<xsl:template match="lom:metaMetadata/lom:identifier">
		<xsl:element name="field">
			<xsl:attribute name="name"><xsl:value-of select="'id'"></xsl:value-of></xsl:attribute>
			<xsl:value-of select="lom:entry" />
		</xsl:element>
	</xsl:template>
	<xsl:template match="lom:technical/lom:location">
	</xsl:template>
	<xsl:template match="lom:technical/lom:format">
		<xsl:element name="field">
			<xsl:attribute name="name"><xsl:value-of select="'technical.format'"></xsl:value-of></xsl:attribute>
			<xsl:value-of select="." />
		</xsl:element>
	</xsl:template>
	<xsl:template match="lom:technical/lom:size">
		<xsl:element name="field">
			<xsl:attribute name="name"><xsl:value-of select="'technical.size'"></xsl:value-of></xsl:attribute>
			<xsl:if test=". = ''">
				<xsl:value-of select="'0'" />
			</xsl:if>
			<xsl:value-of select="." />

		</xsl:element>
	</xsl:template>
	<xsl:template match="lom:technical/lom:installationRemarks">
		<xsl:element name="field">
			<xsl:attribute name="name"><xsl:value-of
				select="'technical.installationremarks'"></xsl:value-of></xsl:attribute>
			<xsl:call-template name="langstring" />
		</xsl:element>
	</xsl:template>
	<xsl:template match="lom:technical/lom:otherPlatformRequirements">
		<xsl:element name="field">
			<xsl:attribute name="name"><xsl:value-of
				select="'technical.otherplatformrequirements'"></xsl:value-of></xsl:attribute>
			<xsl:call-template name="langstring" />
		</xsl:element>
	</xsl:template>
	<xsl:template match="lom:educational/lom:learningResourceType">
		<xsl:element name="field">
			<xsl:attribute name="name"><xsl:value-of
				select="'educational.learningresourcetype'"></xsl:value-of></xsl:attribute>
			<xsl:value-of select="lom:value" />
		</xsl:element>
	</xsl:template>
	<xsl:template match="lom:educational/lom:intendedEndUserRole">
		<xsl:element name="field">
			<xsl:attribute name="name"><xsl:value-of
				select="'educational.intendedenduserrole'"></xsl:value-of></xsl:attribute>
			<xsl:value-of select="lom:value" />
		</xsl:element>
	</xsl:template>
	<xsl:template match="lom:educational/lom:context">
		<xsl:element name="field">
			<xsl:attribute name="name"><xsl:value-of
				select="'educational.context'"></xsl:value-of></xsl:attribute>
			<xsl:value-of select="lom:value" />
		</xsl:element>
	</xsl:template>
	<xsl:template match="lom:educational/lomfr:activity">
		<xsl:element name="field">
			<xsl:attribute name="name"><xsl:value-of
				select="'educational.activity'"></xsl:value-of></xsl:attribute>
			<xsl:value-of select="lomfr:value" />
		</xsl:element>
	</xsl:template>
	<xsl:template match="lom:educational/scolomfr:place">
		<xsl:element name="field">
			<xsl:attribute name="name"><xsl:value-of select="'educational.place'"></xsl:value-of></xsl:attribute>
			<xsl:value-of select="scolomfr:value" />
		</xsl:element>
	</xsl:template>
	<xsl:template match="lom:educational/scolomfr:educationalMethod">
		<xsl:element name="field">
			<xsl:attribute name="name"><xsl:value-of
				select="'educational.educationalmethod'"></xsl:value-of></xsl:attribute>
			<xsl:value-of select="scolomfr:value" />
		</xsl:element>
	</xsl:template>
	<xsl:template match="lom:educational/scolomfr:tool">
		<xsl:element name="field">
			<xsl:attribute name="name"><xsl:value-of select="'educational.tool'"></xsl:value-of></xsl:attribute>
			<xsl:value-of select="scolomfr:value" />
		</xsl:element>
	</xsl:template>
	<xsl:template match="lom:classification">
		<xsl:apply-templates select="lom:taxonPath">
			<xsl:with-param name="purpose">
				<xsl:value-of select="lom:purpose/lom:value" />
			</xsl:with-param>
		</xsl:apply-templates>
	</xsl:template>
	<xsl:template match="lom:rights/lom:cost">
		<xsl:element name="field">
			<xsl:attribute name="name"><xsl:value-of select="'rights.costs'"></xsl:value-of></xsl:attribute>
			<xsl:call-template name="convert-to-boolean">
				<xsl:with-param name="natural">
					<xsl:value-of select="lom:value" />
				</xsl:with-param>
			</xsl:call-template>
		</xsl:element>
	</xsl:template>
	<xsl:template match="lom:rights/lom:copyrightAndOtherRestrictions">
		<xsl:element name="field">
			<xsl:attribute name="name"><xsl:value-of
				select="'rights.copyrightandotherrestrictions'"></xsl:value-of></xsl:attribute>
			<xsl:call-template name="convert-to-boolean">
				<xsl:with-param name="natural">
					<xsl:value-of select="lom:value" />
				</xsl:with-param>
			</xsl:call-template>
		</xsl:element>
	</xsl:template>
	<!-- rights description will be indexed as full text -->
	<xsl:template match="lom:rights/lom:description">
		<xsl:element name="field">
			<xsl:attribute name="name"><xsl:value-of
				select="'rights.description'"></xsl:value-of></xsl:attribute>
			<xsl:call-template name="langstring" />
		</xsl:element>
	</xsl:template>
	<xsl:template match="lom:relation/lom:resource/lom:description">
		<xsl:element name="field">
			<xsl:attribute name="name"><xsl:value-of
				select="'relation.resource.description'"></xsl:value-of></xsl:attribute>
			<xsl:call-template name="langstring" />
		</xsl:element>
	</xsl:template>
	<xsl:template match="lom:taxonPath">
		<xsl:param name="purpose" />
		<xsl:variable name="fieldname" select="concat($purpose, '-taxon')" />
		<xsl:variable name="source" select="lom:source/lom:string" />
		<xsl:apply-templates select="lom:taxon[1]">
			<xsl:with-param name="purpose" select="$purpose" />
			<xsl:with-param name="fieldname" select="$fieldname" />
			<xsl:with-param name="source" select="$source" />
			<xsl:with-param name="previousid" select="''" />
		</xsl:apply-templates>
	</xsl:template>
	<xsl:template match="lom:taxon">
		<xsl:param name="purpose" />
		<xsl:param name="fieldname" />
		<xsl:param name="source" />
		<xsl:param name="previousid" />
		<xsl:variable name="taxonid">
			<xsl:choose>
				<xsl:when test="$previousid=''">
					<xsl:value-of select="lom:id" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="concat($previousid, '::^|^::', lom:id)" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="label">
			<xsl:choose>
				<xsl:when test="lom:entry/lom:string[@language=$user.language]">
					<xsl:value-of select="lom:entry/lom:string[@language=$user.language]" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="lom:entry/lom:string" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:element name="field">
			<xsl:attribute name="name"><xsl:value-of
				select="translate($fieldname,' ','_')"></xsl:value-of></xsl:attribute>
			<xsl:value-of select="concat($source,'!_!',$taxonid,'(__',$label,'__)')" />
		</xsl:element>
		<xsl:apply-templates select="following-sibling::lom:taxon[1]">
			<xsl:with-param name="purpose" select="$purpose" />
			<xsl:with-param name="fieldname" select="$fieldname" />
			<xsl:with-param name="source" select="$source" />
			<xsl:with-param name="previousid" select="$taxonid" />
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="lom:metadataSchema" />
	<xsl:template match="lom:metaMetadata/lom:language" />
	<xsl:template match="lom:educational/lom:description">
		<xsl:element name="field">
			<xsl:attribute name="name"><xsl:value-of
				select="'educational.description'"></xsl:value-of></xsl:attribute>
			<xsl:call-template name="langstring" />
		</xsl:element>
	</xsl:template>
	<xsl:template match="lom:educational/lom:typicalLearningTime">
		<xsl:element name="field">
			<xsl:attribute name="name"><xsl:value-of
				select="'educational.typicallearningtime'"></xsl:value-of></xsl:attribute>
			<xsl:call-template name="convert-duration-to-iso">
				<xsl:with-param name="duration">
					<xsl:value-of select="lom:duration" />
				</xsl:with-param>
			</xsl:call-template>
		</xsl:element>
	</xsl:template>
	<xsl:template match="lom:educational/lom:language">
		<xsl:element name="field">
			<xsl:attribute name="name"><xsl:value-of
				select="'educational.language'"></xsl:value-of></xsl:attribute>
			<xsl:value-of select="." />
		</xsl:element>
	</xsl:template>
	<!-- Utility functions -->
	<xsl:template name="langstring">
		<xsl:choose>
			<xsl:when test="lom:string[@language=$user.language]">
				<xsl:value-of select="lom:string[@language=$user.language]" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="lom:string" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="convert-to-boolean">
		<xsl:param name="natural" />
		<xsl:choose>
			<!--Les deux sont admis par http://www.lom-fr.fr/scolomfr/fileadmin/user_upload/fiches_vocabulaire/2011-06-20_027-Droitauteur_vdex.xml -->
			<xsl:when test="($natural='yes') or ($natural='oui')">
				<xsl:value-of select="'true'" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="'false'" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="convert-duration-to-iso">
		<xsl:param name="duration" />
		<xsl:variable name="hours"
			select="format-number(substring-before(substring-after($duration, 'T'), 'H'),'00')"></xsl:variable>
		<xsl:variable name="minutes"
			select="format-number(substring-before(substring-after($duration, 'H'), 'M'),'00')"></xsl:variable>

		<xsl:variable name="controlledhours">
			<xsl:choose>
				<xsl:when test="($hours='') or ($hours='NaN')">
					<xsl:value-of select="'00'" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$hours" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="controlledminutes">
			<xsl:choose>
				<xsl:when test="($minutes='') or ($minutes='NaN')">
					<xsl:value-of select="'00'" />
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$minutes" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:value-of
			select="concat('2012-12-12T',$controlledhours,':',$controlledminutes,':00Z' )"></xsl:value-of>
	</xsl:template>
	<!-- Does nothing -->
	<xsl:template name="extract-from-vcard">
		<!-- No regular expression is possible because of solr xslt1.0 engine. 
			Insted we put it in solr metadata core shema vcard field type. -->
		<xsl:value-of select="." />
	</xsl:template>
	<!-- Split keywords recursively when string contains comas -->
	<xsl:template name="splitkeywords">
		<xsl:param name="string" />
		<xsl:choose>
			<xsl:when test="contains($string,',')">
				<field name="general.keyword">
					<xsl:value-of select="normalize-space(substring-before($string,','))" />
				</field>
				<xsl:call-template name="splitkeywords">
					<xsl:with-param name="string">
						<xsl:value-of select="substring-after($string,',')" />
					</xsl:with-param>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="normalize-space($string) != ''">
					<field name="general.keyword">
						<xsl:value-of select="normalize-space($string)" />
					</field>
				</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>

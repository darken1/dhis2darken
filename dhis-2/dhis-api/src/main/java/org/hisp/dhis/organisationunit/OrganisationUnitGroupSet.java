package org.hisp.dhis.organisationunit;

/*
 * Copyright (c) 2004-2014, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.attribute.AttributeValue;
import org.hisp.dhis.common.BaseDimensionalObject;
import org.hisp.dhis.common.BaseIdentifiableObject;
import org.hisp.dhis.common.DxfNamespaces;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.NameableObject;
import org.hisp.dhis.common.adapter.JacksonOrganisationUnitGroupSymbolSerializer;
import org.hisp.dhis.common.annotation.Scanned;
import org.hisp.dhis.common.comparator.IdentifiableObjectNameComparator;
import org.hisp.dhis.common.view.DetailedView;
import org.hisp.dhis.common.view.DimensionalView;
import org.hisp.dhis.common.view.ExportView;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * @author Kristian Nordal
 */
@JacksonXmlRootElement( localName = "organisationUnitGroupSet", namespace = DxfNamespaces.DXF_2_0 )
public class OrganisationUnitGroupSet
    extends BaseDimensionalObject
{
    /**
     * Determines if a de-serialized file is compatible with this class.
     */
    private static final long serialVersionUID = -221220579471558683L;

    private boolean compulsory;

    @Scanned
    private Set<OrganisationUnitGroup> organisationUnitGroups = new HashSet<OrganisationUnitGroup>();

    private boolean dataDimension = true;

    /**
     * Set of the dynamic attributes values that belong to this data element.
     */
    private Set<AttributeValue> attributeValues = new HashSet<AttributeValue>();

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public OrganisationUnitGroupSet()
    {
        setAutoFields();
    }

    public OrganisationUnitGroupSet( String name, String description, boolean compulsory )
    {
        this();
        this.name = name;
        this.description = description;
        this.compulsory = compulsory;
    }

    public OrganisationUnitGroupSet( String name, String description, boolean compulsory, boolean dataDimension )
    {
        this( name, description, compulsory );
        this.dataDimension = dataDimension;
    }

    // -------------------------------------------------------------------------
    // Logic
    // -------------------------------------------------------------------------

    public void addOrganisationUnitGroup( OrganisationUnitGroup organisationUnitGroup )
    {
        organisationUnitGroups.add( organisationUnitGroup );
        organisationUnitGroup.setGroupSet( this );
    }

    public void removeOrganisationUnitGroup( OrganisationUnitGroup organisationUnitGroup )
    {
        organisationUnitGroups.remove( organisationUnitGroup );
        organisationUnitGroup.setGroupSet( null );
    }

    public void removeAllOrganisationUnitGroups()
    {
        for ( OrganisationUnitGroup organisationUnitGroup : organisationUnitGroups )
        {
            organisationUnitGroup.setGroupSet( null );
        }

        organisationUnitGroups.clear();
    }

    public Collection<OrganisationUnit> getOrganisationUnits()
    {
        List<OrganisationUnit> units = new ArrayList<OrganisationUnit>();

        for ( OrganisationUnitGroup group : organisationUnitGroups )
        {
            units.addAll( group.getMembers() );
        }

        return units;
    }

    public boolean isMemberOfOrganisationUnitGroups( OrganisationUnit organisationUnit )
    {
        for ( OrganisationUnitGroup group : organisationUnitGroups )
        {
            if ( group.getMembers().contains( organisationUnit ) )
            {
                return true;
            }
        }

        return false;
    }

    public boolean hasOrganisationUnitGroups()
    {
        return organisationUnitGroups != null && organisationUnitGroups.size() > 0;
    }

    public OrganisationUnitGroup getGroup( OrganisationUnit unit )
    {
        for ( OrganisationUnitGroup group : organisationUnitGroups )
        {
            if ( group.getMembers().contains( unit ) )
            {
                return group;
            }
        }

        return null;
    }

    public List<OrganisationUnitGroup> getSortedGroups()
    {
        List<OrganisationUnitGroup> sortedGroups = new ArrayList<OrganisationUnitGroup>( organisationUnitGroups );

        Collections.sort( sortedGroups, IdentifiableObjectNameComparator.INSTANCE );

        return sortedGroups;
    }

    // -------------------------------------------------------------------------
    // Dimensional object
    // -------------------------------------------------------------------------

    @Override
    @JsonProperty
    @JsonSerialize( contentAs = BaseIdentifiableObject.class )
    @JsonView( { DetailedView.class, DimensionalView.class } )
    @JacksonXmlElementWrapper( localName = "items", namespace = DxfNamespaces.DXF_2_0 )
    @JacksonXmlProperty( localName = "item", namespace = DxfNamespaces.DXF_2_0 )
    public List<NameableObject> getItems()
    {
        return new ArrayList<NameableObject>( organisationUnitGroups );
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    @Override
    public String getShortName()
    {
        if ( getName().length() <= 50 )
        {
            return getName();
        }
        else
        {
            return getName().substring( 0, 49 );
        }
    }

    @Override
    public boolean isAutoGenerated()
    {
        return name != null && (name.equals( "Type" ) || name.equals( "Ownership" ));
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public boolean isCompulsory()
    {
        return compulsory;
    }

    public void setCompulsory( boolean compulsory )
    {
        this.compulsory = compulsory;
    }

    @JsonProperty( value = "organisationUnitGroups" )
    // @JsonSerialize(contentAs = BaseIdentifiableObject.class)
    @JsonSerialize( contentUsing = JacksonOrganisationUnitGroupSymbolSerializer.class )
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlElementWrapper( localName = "organisationUnitGroups", namespace = DxfNamespaces.DXF_2_0 )
    @JacksonXmlProperty( localName = "organisationUnitGroup", namespace = DxfNamespaces.DXF_2_0 )
    public Set<OrganisationUnitGroup> getOrganisationUnitGroups()
    {
        return organisationUnitGroups;
    }

    public void setOrganisationUnitGroups( Set<OrganisationUnitGroup> organisationUnitGroups )
    {
        this.organisationUnitGroups = organisationUnitGroups;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public boolean isDataDimension()
    {
        return dataDimension;
    }

    public void setDataDimension( boolean dataDimension )
    {
        this.dataDimension = dataDimension;
    }

    @JsonProperty( value = "attributeValues" )
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlElementWrapper( localName = "attributeValues", namespace = DxfNamespaces.DXF_2_0)
    @JacksonXmlProperty( localName = "attributeValue", namespace = DxfNamespaces.DXF_2_0)
    public Set<AttributeValue> getAttributeValues()
    {
        return attributeValues;
    }

    public void setAttributeValues( Set<AttributeValue> attributeValues )
    {
        this.attributeValues = attributeValues;
    }

    @Override
    public void mergeWith( IdentifiableObject other )
    {
        super.mergeWith( other );

        if ( other.getClass().isInstance( this ) )
        {
            OrganisationUnitGroupSet organisationUnitGroupSet = (OrganisationUnitGroupSet) other;

            compulsory = organisationUnitGroupSet.isCompulsory();
            description = organisationUnitGroupSet.getDescription() == null ? description : organisationUnitGroupSet.getDescription();
            dataDimension = organisationUnitGroupSet.isDataDimension();

            removeAllOrganisationUnitGroups();

            for ( OrganisationUnitGroup organisationUnitGroup : organisationUnitGroupSet.getOrganisationUnitGroups() )
            {
                addOrganisationUnitGroup( organisationUnitGroup );
            }

            attributeValues.clear();
            attributeValues.addAll( organisationUnitGroupSet.getAttributeValues() );
        }
    }
}

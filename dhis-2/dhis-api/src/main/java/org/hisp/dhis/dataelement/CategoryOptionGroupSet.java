package org.hisp.dhis.dataelement;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.apache.commons.collections.CollectionUtils;
import org.hisp.dhis.common.BaseDimensionalObject;
import org.hisp.dhis.common.BaseIdentifiableObject;
import org.hisp.dhis.common.DxfNamespaces;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.NameableObject;
import org.hisp.dhis.common.annotation.Scanned;
import org.hisp.dhis.common.view.DetailedView;
import org.hisp.dhis.common.view.DimensionalView;
import org.hisp.dhis.common.view.ExportView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Lars Helge Overland
 */
@JacksonXmlRootElement( localName = "categoryOptionGroupSet", namespace = DxfNamespaces.DXF_2_0 )
public class CategoryOptionGroupSet
    extends BaseDimensionalObject
{
    @Scanned
    private List<CategoryOptionGroup> members = new ArrayList<CategoryOptionGroup>();

    private boolean dataDimension = true;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public CategoryOptionGroupSet()
    {
        setAutoFields();
    }

    public CategoryOptionGroupSet( String name )
    {
        this();
        this.name = name;
    }

    // -------------------------------------------------------------------------
    // Logic
    // -------------------------------------------------------------------------

    // TODO link group set to category to avoid conflicting grouping of category
    // option combos

    public CategoryOptionGroup getGroup( DataElementCategoryOptionCombo optionCombo )
    {
        Set<DataElementCategoryOption> categoryOptions = optionCombo.getCategoryOptions();

        for ( CategoryOptionGroup group : members )
        {
            if ( !CollectionUtils.intersection( group.getMembers(), categoryOptions ).isEmpty() )
            {
                return group;
            }
        }

        return null;
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
        return new ArrayList<NameableObject>( members );
    }
    
    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    @JsonProperty( value = "categoryOptionGroups" )
    @JsonSerialize( contentAs = BaseIdentifiableObject.class )
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlElementWrapper( localName = "categoryOptionGroups", namespace = DxfNamespaces.DXF_2_0 )
    @JacksonXmlProperty( localName = "categoryOptionGroup", namespace = DxfNamespaces.DXF_2_0 )
    public List<CategoryOptionGroup> getMembers()
    {
        return members;
    }

    public void setMembers( List<CategoryOptionGroup> members )
    {
        this.members = members;
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

    // -------------------------------------------------------------------------
    // Logic
    // -------------------------------------------------------------------------

    public void addCategoryOptionGroup( CategoryOptionGroup categoryOptionGroup )
    {
        members.add( categoryOptionGroup );
        categoryOptionGroup.setGroupSet( this );
    }

    public void removeCategoryOptionGroup( CategoryOptionGroup categoryOptionGroup )
    {
        members.remove( categoryOptionGroup );
        categoryOptionGroup.setGroupSet( null );
    }

    @Override
    public void mergeWith( IdentifiableObject other )
    {
        super.mergeWith( other );

        if ( other.getClass().isInstance( this ) )
        {
            CategoryOptionGroupSet categoryOptionGroupSet = (CategoryOptionGroupSet) other;
            dataDimension = categoryOptionGroupSet.isDataDimension();

            members.clear();

            for ( CategoryOptionGroup categoryOptionGroup : categoryOptionGroupSet.getMembers() )
            {
                addCategoryOptionGroup( categoryOptionGroup );
            }
        }
    }
}

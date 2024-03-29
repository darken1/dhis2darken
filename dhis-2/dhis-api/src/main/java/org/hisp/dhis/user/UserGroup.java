package org.hisp.dhis.user;

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.hisp.dhis.attribute.AttributeValue;
import org.hisp.dhis.common.BaseIdentifiableObject;
import org.hisp.dhis.common.DxfNamespaces;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.annotation.Scanned;
import org.hisp.dhis.common.view.DetailedView;
import org.hisp.dhis.common.view.ExportView;

import java.util.HashSet;
import java.util.Set;

@JacksonXmlRootElement(localName = "userGroup", namespace = DxfNamespaces.DXF_2_0)
public class UserGroup
    extends BaseIdentifiableObject
{
    /**
     * Determines if a de-serialized file is compatible with this class.
     */
    private static final long serialVersionUID = 347909584755616508L;

    /**
     * Set of related users
     */
    @Scanned
    private Set<User> members = new HashSet<User>();

    /**
     * Set of the dynamic attributes values that belong to this user group.
     */
    private Set<AttributeValue> attributeValues = new HashSet<AttributeValue>();

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------     

    public UserGroup()
    {
    }

    public UserGroup( String name )
    {
        this.name = name;
    }

    public UserGroup( String name, Set<User> members )
    {
        this.name = name;
        this.members = members;
    }

    // -------------------------------------------------------------------------
    // Logic
    // -------------------------------------------------------------------------

    public void addUser( User user )
    {
        members.add( user );
        user.getGroups().add( this );
    }

    public void removeUser( User user )
    {
        members.remove( user );
        user.getGroups().remove( this );
    }

    public void updateUsers( Set<User> updates )
    {
        for ( User user : new HashSet<User>( members ) )
        {
            if ( !updates.contains( user ) )
            {
                removeUser( user );
            }
        }

        for ( User user : updates )
        {
            addUser( user );
        }
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    @Override
    public boolean haveUniqueNames()
    {
        return false;
    }

    @JsonIgnore
    public User getUser()
    {
        return user;
    }

    @JsonIgnore
    public void setUser( User user )
    {
        this.user = user;
    }

    @JsonProperty(value = "users")
    @JsonSerialize(contentAs = BaseIdentifiableObject.class)
    @JsonView({ DetailedView.class, ExportView.class })
    @JacksonXmlElementWrapper(localName = "users", namespace = DxfNamespaces.DXF_2_0)
    @JacksonXmlProperty(localName = "user", namespace = DxfNamespaces.DXF_2_0)
    public Set<User> getMembers()
    {
        return members;
    }

    public void setMembers( Set<User> members )
    {
        this.members = members;
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
            UserGroup userGroup = (UserGroup) other;

            members.clear();
            members.addAll( userGroup.getMembers() );

            attributeValues.clear();
            attributeValues.addAll( userGroup.getAttributeValues() );
        }
    }
}

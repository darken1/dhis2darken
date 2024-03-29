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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.apache.commons.collections.CollectionUtils;
import org.hisp.dhis.attribute.AttributeValue;
import org.hisp.dhis.common.BaseIdentifiableObject;
import org.hisp.dhis.common.DxfNamespaces;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.IdentifiableObjectUtils;
import org.hisp.dhis.common.annotation.Scanned;
import org.hisp.dhis.common.view.DetailedView;
import org.hisp.dhis.common.view.ExportView;
import org.hisp.dhis.organisationunit.OrganisationUnit;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Nguyen Hong Duc
 */
@JacksonXmlRootElement(localName = "user", namespace = DxfNamespaces.DXF_2_0)
public class User
    extends BaseIdentifiableObject
{
    /**
     * Determines if a de-serialized file is compatible with this class.
     */
    private static final long serialVersionUID = 859837727604102353L;

    /**
     * Required.
     */
    private String surname;

    private String firstName;

    /**
     * Optional.
     */
    private String email;

    private String phoneNumber;

    private String jobTitle;

    private String introduction;

    private String gender;

    private Date birthday;

    private String nationality;

    private String employer;

    private String education;

    private String interests;

    private String languages;

    private Date lastCheckedInterpretations;

    private UserCredentials userCredentials;

    private Set<UserGroup> groups = new HashSet<UserGroup>();

    /**
     * Organisation units for data input and data capture / write operations.
     * TODO move to UserCredentials.
     */
    @Scanned
    private Set<OrganisationUnit> organisationUnits = new HashSet<OrganisationUnit>();

    /**
     * Organisation units for data output and data analysis / read operations.
     */
    @Scanned
    private Set<OrganisationUnit> dataViewOrganisationUnits = new HashSet<OrganisationUnit>();

    /**
     * Set of the dynamic attributes values that belong to this User.
     */
    private Set<AttributeValue> attributeValues = new HashSet<AttributeValue>();

    /**
     * Ordered favorite apps.
     */
    private List<String> apps = new ArrayList<String>();

    // -------------------------------------------------------------------------
    // Logic
    // -------------------------------------------------------------------------

    public void addOrganisationUnit( OrganisationUnit unit )
    {
        organisationUnits.add( unit );
        unit.getUsers().add( this );
    }

    public void removeOrganisationUnit( OrganisationUnit unit )
    {
        organisationUnits.remove( unit );
        unit.getUsers().remove( this );
    }

    public void updateOrganisationUnits( Set<OrganisationUnit> updates )
    {
        for ( OrganisationUnit unit : new HashSet<OrganisationUnit>( organisationUnits ) )
        {
            if ( !updates.contains( unit ) )
            {
                removeOrganisationUnit( unit );
            }
        }

        for ( OrganisationUnit unit : updates )
        {
            addOrganisationUnit( unit );
        }
    }

    /**
     * Returns the concatenated first name and surname.
     */
    @Override
    public String getName()
    {
        return firstName + " " + surname;
    }

    /**
     * Checks whether the profile has been filled, which is defined as three
     * not-null properties out of all optional properties.
     */
    public boolean isProfileFilled()
    {
        Object[] props = { jobTitle, introduction, gender, birthday,
            nationality, employer, education, interests, languages };

        int count = 0;

        for ( Object prop : props )
        {
            count = prop != null ? (count + 1) : count;
        }

        return count > 3;
    }

    /**
     * Returns the first of the organisation units associated with the user.
     * Null is returned if the user has no organisation units. Which
     * organisation unit to return is undefined if the user has multiple
     * organisation units.
     */
    public OrganisationUnit getOrganisationUnit()
    {
        return CollectionUtils.isEmpty( organisationUnits ) ? null : organisationUnits.iterator().next();
    }

    public boolean hasOrganisationUnit()
    {
        return !CollectionUtils.isEmpty( organisationUnits );
    }

    public boolean hasDataViewOrganisationUnit()
    {
        return !CollectionUtils.isEmpty( dataViewOrganisationUnits );
    }

    public OrganisationUnit getDataViewOrganisationUnit()
    {
        return CollectionUtils.isEmpty( dataViewOrganisationUnits ) ? null : dataViewOrganisationUnits.iterator().next();
    }

    public boolean hasDataViewOrganisationUnitWithFallback()
    {
        return hasDataViewOrganisationUnit() || hasOrganisationUnit();
    }

    /**
     * Returns the first of the data view organisation units associated with the
     * user. If none, returns the first of the data capture organisation units.
     * If none, return nulls.
     */
    public OrganisationUnit getDataViewOrganisationUnitWithFallback()
    {
        OrganisationUnit dataViewOrgUnit = getDataViewOrganisationUnit();
        return dataViewOrgUnit != null ? dataViewOrgUnit : getOrganisationUnit();
    }

    public String getOrganisationUnitsName()
    {
        return IdentifiableObjectUtils.join( organisationUnits );
    }

    public String getUsername()
    {
        return userCredentials != null ? userCredentials.getUsername() : null;
    }

    public boolean isSuper()
    {
        return userCredentials != null && userCredentials.isSuper();
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    @Override
    public boolean haveUniqueNames()
    {
        return false;
    }

    @JsonProperty
    @JsonView({ DetailedView.class, ExportView.class })
    @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName( String firstName )
    {
        this.firstName = firstName;
    }

    @JsonProperty
    @JsonView({ DetailedView.class, ExportView.class })
    @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
    public String getSurname()
    {
        return surname;
    }

    public void setSurname( String surname )
    {
        this.surname = surname;
    }

    @JsonProperty
    @JsonView({ DetailedView.class, ExportView.class })
    @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
    public String getEmail()
    {
        return email;
    }

    public void setEmail( String email )
    {
        this.email = email;
    }

    @JsonProperty
    @JsonView({ DetailedView.class, ExportView.class })
    @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
    public String getJobTitle()
    {
        return jobTitle;
    }

    public void setJobTitle( String jobTitle )
    {
        this.jobTitle = jobTitle;
    }

    @JsonProperty
    @JsonView({ DetailedView.class, ExportView.class })
    @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    public void setPhoneNumber( String phoneNumber )
    {
        this.phoneNumber = phoneNumber;
    }

    @JsonProperty
    @JsonView({ DetailedView.class, ExportView.class })
    @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
    public String getIntroduction()
    {
        return introduction;
    }

    public void setIntroduction( String introduction )
    {
        this.introduction = introduction;
    }

    @JsonProperty
    @JsonView({ DetailedView.class, ExportView.class })
    @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
    public String getGender()
    {
        return gender;
    }

    public void setGender( String gender )
    {
        this.gender = gender;
    }

    @JsonProperty
    @JsonView({ DetailedView.class, ExportView.class })
    @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
    public Date getBirthday()
    {
        return birthday;
    }

    public void setBirthday( Date birthday )
    {
        this.birthday = birthday;
    }

    @JsonProperty
    @JsonView({ DetailedView.class, ExportView.class })
    @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
    public String getNationality()
    {
        return nationality;
    }

    public void setNationality( String nationality )
    {
        this.nationality = nationality;
    }

    @JsonProperty
    @JsonView({ DetailedView.class, ExportView.class })
    @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
    public String getEmployer()
    {
        return employer;
    }

    public void setEmployer( String employer )
    {
        this.employer = employer;
    }

    @JsonProperty
    @JsonView({ DetailedView.class, ExportView.class })
    @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
    public String getEducation()
    {
        return education;
    }

    public void setEducation( String education )
    {
        this.education = education;
    }

    @JsonProperty
    @JsonView({ DetailedView.class, ExportView.class })
    @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
    public String getInterests()
    {
        return interests;
    }

    public void setInterests( String interests )
    {
        this.interests = interests;
    }

    @JsonProperty
    @JsonView({ DetailedView.class, ExportView.class })
    @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
    public String getLanguages()
    {
        return languages;
    }

    public void setLanguages( String languages )
    {
        this.languages = languages;
    }

    @JsonProperty
    @JsonView({ DetailedView.class, ExportView.class })
    @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
    public Date getLastCheckedInterpretations()
    {
        return lastCheckedInterpretations;
    }

    public void setLastCheckedInterpretations( Date lastCheckedInterpretations )
    {
        this.lastCheckedInterpretations = lastCheckedInterpretations;
    }

    @JsonProperty
    @JsonView({ DetailedView.class, ExportView.class })
    @JacksonXmlProperty(namespace = DxfNamespaces.DXF_2_0)
    public UserCredentials getUserCredentials()
    {
        return userCredentials;
    }

    public void setUserCredentials( UserCredentials userCredentials )
    {
        this.userCredentials = userCredentials;
    }

    @JsonProperty
    @JsonSerialize(contentAs = BaseIdentifiableObject.class)
    @JsonView({ DetailedView.class })
    @JacksonXmlElementWrapper(localName = "userGroups", namespace = DxfNamespaces.DXF_2_0)
    @JacksonXmlProperty(localName = "userGroup", namespace = DxfNamespaces.DXF_2_0)
    public Set<UserGroup> getGroups()
    {
        return groups;
    }

    public void setGroups( Set<UserGroup> groups )
    {
        this.groups = groups;
    }

    @JsonProperty
    @JsonSerialize(contentAs = BaseIdentifiableObject.class)
    @JsonView({ DetailedView.class, ExportView.class })
    @JacksonXmlElementWrapper(localName = "organisationUnits", namespace = DxfNamespaces.DXF_2_0)
    @JacksonXmlProperty(localName = "organisationUnit", namespace = DxfNamespaces.DXF_2_0)
    public Set<OrganisationUnit> getOrganisationUnits()
    {
        return organisationUnits;
    }

    public void setOrganisationUnits( Set<OrganisationUnit> organisationUnits )
    {
        this.organisationUnits = organisationUnits;
    }

    @JsonProperty
    @JsonSerialize(contentAs = BaseIdentifiableObject.class)
    @JsonView({ DetailedView.class, ExportView.class })
    @JacksonXmlElementWrapper(localName = "dataViewOrganisationUnits", namespace = DxfNamespaces.DXF_2_0)
    @JacksonXmlProperty(localName = "dataViewOrganisationUnit", namespace = DxfNamespaces.DXF_2_0)
    public Set<OrganisationUnit> getDataViewOrganisationUnits()
    {
        return dataViewOrganisationUnits;
    }

    public void setDataViewOrganisationUnits( Set<OrganisationUnit> dataViewOrganisationUnits )
    {
        this.dataViewOrganisationUnits = dataViewOrganisationUnits;
    }

    @JsonProperty(value = "attributeValues")
    @JsonView({ DetailedView.class, ExportView.class })
    @JacksonXmlElementWrapper(localName = "attributeValues", namespace = DxfNamespaces.DXF_2_0)
    @JacksonXmlProperty(localName = "attributeValue", namespace = DxfNamespaces.DXF_2_0)
    public Set<AttributeValue> getAttributeValues()
    {
        return attributeValues;
    }

    public void setAttributeValues( Set<AttributeValue> attributeValues )
    {
        this.attributeValues = attributeValues;
    }

    public List<String> getApps()
    {
        return apps;
    }

    public void setApps( List<String> apps )
    {
        this.apps = apps;
    }

    @Override
    public void mergeWith( IdentifiableObject other )
    {
        super.mergeWith( other );

        if ( other.getClass().isInstance( this ) )
        {
            User user = (User) other;

            email = user.getEmail();
            phoneNumber = user.getPhoneNumber();
            jobTitle = user.getJobTitle();
            introduction = user.getIntroduction();
            gender = user.getGender();
            birthday = user.getBirthday();
            nationality = user.getNationality();
            employer = user.getEmployer();
            education = user.getEducation();
            interests = user.getInterests();
            languages = user.getLanguages();
            lastCheckedInterpretations = user.getLastCheckedInterpretations();

            userCredentials = user.getUserCredentials() == null ? userCredentials : user.getUserCredentials();

            attributeValues.clear();
            attributeValues.addAll( user.getAttributeValues() );

            organisationUnits.clear();
            organisationUnits.addAll( user.getOrganisationUnits() );
        }
    }

    @Override
    public String toString()
    {
        return "User{" +
            "surname='" + surname + '\'' +
            ", firstName='" + firstName + '\'' +
            ", email='" + email + '\'' +
            ", phoneNumber='" + phoneNumber + '\'' +
            ", jobTitle='" + jobTitle + '\'' +
            ", introduction='" + introduction + '\'' +
            ", gender='" + gender + '\'' +
            ", birthday=" + birthday +
            ", nationality='" + nationality + '\'' +
            ", employer='" + employer + '\'' +
            ", education='" + education + '\'' +
            ", interests='" + interests + '\'' +
            ", languages='" + languages + '\'' +
            ", lastCheckedInterpretations=" + lastCheckedInterpretations +
            ", userCredentials=" + userCredentials +
            ", groups=" + groups +
            ", organisationUnits=" + organisationUnits +
            ", dataViewOrganisationUnits=" + dataViewOrganisationUnits +
            ", attributeValues=" + attributeValues +
            ", apps=" + apps +
            '}';
    }
}

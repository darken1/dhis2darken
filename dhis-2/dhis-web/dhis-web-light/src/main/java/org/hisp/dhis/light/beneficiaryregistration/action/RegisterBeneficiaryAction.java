package org.hisp.dhis.light.beneficiaryregistration.action;

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

import java.util.Collection;

import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.setting.SystemSettingManager;
import org.hisp.dhis.trackedentity.TrackedEntity;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityAttributeService;

import com.opensymphony.xwork2.Action;

public class RegisterBeneficiaryAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private TrackedEntityAttributeService patientAttributeService;

    public void setPatientAttributeService( TrackedEntityAttributeService patientAttributeService )
    {
        this.patientAttributeService = patientAttributeService;
    }

    private ProgramService programService;

    public ProgramService getProgramService()
    {
        return programService;
    }

    public void setProgramService( ProgramService programService )
    {
        this.programService = programService;
    }

    private SystemSettingManager systemSettingManager;

    public SystemSettingManager getSystemSettingManager()
    {
        return systemSettingManager;
    }

    public void setSystemSettingManager( SystemSettingManager systemSettingManager )
    {
        this.systemSettingManager = systemSettingManager;
    }

    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------

    private Integer orgUnitId;

    public Integer getOrgUnitId()
    {
        return orgUnitId;
    }

    public void setOrgUnitId( Integer orgUnitId )
    {
        this.orgUnitId = orgUnitId;
    }

    private Collection<TrackedEntityAttribute> patientAttributes;

    public Collection<TrackedEntityAttribute> getPatientAttributes()
    {
        return patientAttributes;
    }

    public void setPatientAttributes( Collection<TrackedEntityAttribute> patientAttributes )
    {
        this.patientAttributes = patientAttributes;
    }

    // Register person on-the-fly

    private Integer originalPatientId;

    public Integer getOriginalPatientId()
    {
        return originalPatientId;
    }

    public void setOriginalPatientId( Integer originalPatientId )
    {
        this.originalPatientId = originalPatientId;
    }

    private Integer relationshipTypeId;

    public Integer getRelationshipTypeId()
    {
        return relationshipTypeId;
    }

    public void setRelationshipTypeId( Integer relationshipTypeId )
    {
        this.relationshipTypeId = relationshipTypeId;
    }

    private String phoneNumberAreaCode;

    public String getPhoneNumberAreaCode()
    {
        return phoneNumberAreaCode;
    }

    public void setPhoneNumberAreaCode( String phoneNumberAreaCode )
    {
        this.phoneNumberAreaCode = phoneNumberAreaCode;
    }

    private Collection<TrackedEntity> trackedEntities;

    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------

    public Collection<TrackedEntity> getTrackedEntities()
    {
        return trackedEntities;
    }

    public void setTrackedEntities( Collection<TrackedEntity> trackedEntities )
    {
        this.trackedEntities = trackedEntities;
    }

    @Override
    public String execute()
        throws Exception
    {
        patientAttributes = patientAttributeService.getAllTrackedEntityAttributes();

        phoneNumberAreaCode = (String) systemSettingManager
            .getSystemSetting( SystemSettingManager.KEY_PHONE_NUMBER_AREA_CODE );
        if ( phoneNumberAreaCode == null )
            phoneNumberAreaCode = "";
        Collection<Program> programs = programService.getAllPrograms();

        for ( Program program : programs )
        {
            patientAttributes.removeAll( program.getAttributes() );
        }
        return SUCCESS;
    }
}

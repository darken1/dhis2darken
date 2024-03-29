package org.hisp.dhis.caseentry.action.trackedentity;

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

import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.relationship.Relationship;
import org.hisp.dhis.relationship.RelationshipService;
import org.hisp.dhis.relationship.RelationshipType;
import org.hisp.dhis.relationship.RelationshipTypeService;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceService;

import com.opensymphony.xwork2.Action;

/**
 * @author Abyot Asalefew Gizaw
 * @version $Id$
 */
public class SaveRelationshipAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private TrackedEntityInstanceService entityInstanceService;

    public void setEntityInstanceService( TrackedEntityInstanceService entityInstanceService )
    {
        this.entityInstanceService = entityInstanceService;
    }

    private RelationshipTypeService relationshipTypeService;

    public void setRelationshipTypeService( RelationshipTypeService relationshipTypeService )
    {
        this.relationshipTypeService = relationshipTypeService;
    }

    private RelationshipService relationshipService;

    public void setRelationshipService( RelationshipService relationshipService )
    {
        this.relationshipService = relationshipService;
    }

    // -------------------------------------------------------------------------
    // Input/Output
    // -------------------------------------------------------------------------

    private Integer entityInstanceId;
    
    public void setEntityInstanceId( Integer entityInstanceId )
    {
        this.entityInstanceId = entityInstanceId;
    }

    private String partnerId;

    public void setPartnerId( String partnerId )
    {
        this.partnerId = partnerId;
    }
    
    private Integer relationshipTypeId;

    public void setRelationshipTypeId( Integer relationshipTypeId )
    {
        this.relationshipTypeId = relationshipTypeId;
    }

    private String relationshipName;

    public void setRelationshipName( String relationshipName )
    {
        this.relationshipName = relationshipName;
    }
    
    private String message;

    public String getMessage()
    {
        return message;
    }

    private I18n i18n;

    public void setI18n( I18n i18n )
    {
        this.i18n = i18n;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
        TrackedEntityInstance entityInstance = entityInstanceService.getTrackedEntityInstance( entityInstanceId );

        TrackedEntityInstance partner = entityInstanceService.getTrackedEntityInstance( partnerId );

        RelationshipType relationshipType = relationshipTypeService.getRelationshipType( relationshipTypeId );

        Relationship relationship = relationshipService.getRelationship( entityInstance, partner, relationshipType );

        if ( relationship != null )
        {
            message = i18n.getString( "the_relationship_already_exists" );

            return INPUT;
        }

        // ---------------------------------------------------------------------
        // Validation success
        // ---------------------------------------------------------------------

        relationship = new Relationship();

        if ( relationshipType.getaIsToB().equalsIgnoreCase( relationshipName ) )
        {
            relationship.setEntityInstanceA( partner );
            relationship.setEntityInstanceB( entityInstance );
        }
        else if ( relationshipType.getbIsToA().equalsIgnoreCase( relationshipName ) )
        {
            relationship.setEntityInstanceA( entityInstance );
            relationship.setEntityInstanceB( partner );
        }

        relationship.setRelationshipType( relationshipType );
        relationshipService.addRelationship( relationship );
        
        return SUCCESS;
    }
}

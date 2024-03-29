package org.hisp.dhis.importexport.importer;

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

import org.amplecode.quick.BatchHandler;
import org.hisp.dhis.importexport.GroupMemberAssociation;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;

/**
 * @author Lars Helge Overland
 * @version $Id: AbstractOrganisationUnitRelationshipConverter.java 4646 2008-02-26 14:54:29Z larshelg $
 */
public class OrganisationUnitRelationshipImporter
    extends AbstractImporter<GroupMemberAssociation>
{
    protected OrganisationUnitService organisationUnitService;
    
    protected BatchHandler<OrganisationUnit> organisationUnitBatchHandler;

    public OrganisationUnitRelationshipImporter()
    {
    }
    
    public OrganisationUnitRelationshipImporter( BatchHandler<OrganisationUnit> batchHandler, OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitBatchHandler = batchHandler;
        this.organisationUnitService = organisationUnitService;
    }
    
    // -------------------------------------------------------------------------
    // Overridden methods
    // -------------------------------------------------------------------------

    protected void importUnique( GroupMemberAssociation object )
    {
        organisationUnitService.updateOrganisationUnitParent( object.getMemberId(), object.getGroupId() );
    }

    protected void importMatching( GroupMemberAssociation object, GroupMemberAssociation match )
    {
        // Not in use
    }
    
    protected GroupMemberAssociation getMatching( GroupMemberAssociation object )
    {
        return null;
    }
    
    protected boolean isIdentical( GroupMemberAssociation object, GroupMemberAssociation existing )
    {
        return true;
    }
}


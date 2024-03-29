package org.hisp.dhis.importexport.dhis14.xml.converter;

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

import org.amplecode.staxwax.reader.XMLReader;
import org.amplecode.staxwax.writer.XMLWriter;
import org.hisp.dhis.importexport.ExportParams;
import org.hisp.dhis.importexport.ImportParams;
import org.hisp.dhis.importexport.XMLConverter;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
import org.hisp.dhis.organisationunit.OrganisationUnitService;

import static org.hisp.dhis.importexport.dhis14.util.Dhis14TypeHandler.convertBooleanToDhis14;

/**
 * @author Lars Helge Overland
 * @version $Id: OrganisationUnitGroupMemberConverter.java 6455 2008-11-24 08:59:37Z larshelg $
 */
public class OrganisationUnitGroupMemberConverter
    implements XMLConverter
{
    public static final String ELEMENT_NAME = "OrgUnitGroupMember";
    
    private static final String FIELD_GROUP_ID = "OrgUnitGroupID";
    private static final String FIELD_UNIT_ID = "OrgUnitID";
    private static final String FIELD_ACTIVE = "Active";
    private static final String FIELD_LAST_USER = "LastUserID";
    private static final String FIELD_LAST_UPDATED = "LastUpdatedID";
    
    private OrganisationUnitGroupService organisationUnitGroupService;
    
    private OrganisationUnitService organisationUnitService;
    
    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Constructor for write operations.
     */
    public OrganisationUnitGroupMemberConverter( OrganisationUnitGroupService organisationUnitGroupService,
        OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitGroupService = organisationUnitGroupService;
        this.organisationUnitService = organisationUnitService;
    }

    // -------------------------------------------------------------------------
    // XMLConverter implementation
    // -------------------------------------------------------------------------
    
    public void write( XMLWriter writer, ExportParams params )
    {
        Collection<OrganisationUnitGroup> groups = organisationUnitGroupService.getOrganisationUnitGroups( params.getOrganisationUnitGroups() );
        
        Collection<OrganisationUnit> units = organisationUnitService.getOrganisationUnits( params.getOrganisationUnits() );
        
        if ( groups != null && groups.size() > 0 && units != null && units.size() > 0 )
        {
            for ( OrganisationUnitGroup group : groups )
            {
                if ( group.getMembers() != null )
                {
                    for ( OrganisationUnit unit : group.getMembers() )
                    {
                        if ( units.contains( unit ) )
                        {
                            writer.openElement( ELEMENT_NAME );
                            
                            writer.writeElement( FIELD_GROUP_ID, String.valueOf( group.getId() ) );
                            writer.writeElement( FIELD_UNIT_ID, String.valueOf( unit.getId() ) );
                            writer.writeElement( FIELD_ACTIVE, convertBooleanToDhis14( unit.isActive() ) );
                            writer.writeElement( FIELD_LAST_USER, "");
                            writer.writeElement( FIELD_LAST_UPDATED, "");
                            
                            writer.closeElement();
                        }
                    }
                }
            }
        }
    }
    
    public void read( XMLReader reader, ImportParams params )
    {
        // Not implemented        
    }
}

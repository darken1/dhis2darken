package org.hisp.dhis.importexport.dxf.converter;

import java.util.Collection;
import java.util.Map;

import org.amplecode.staxwax.reader.XMLReader;
import org.amplecode.staxwax.writer.XMLWriter;
import org.hisp.dhis.importexport.ExportParams;
import org.hisp.dhis.importexport.ImportObjectService;
import org.hisp.dhis.importexport.ImportParams;
import org.hisp.dhis.importexport.XMLConverter;
import org.hisp.dhis.importexport.importer.OrganisationUnitLevelImporter;
import org.hisp.dhis.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.organisationunit.OrganisationUnitService;

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

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
public class OrganisationUnitLevelConverter
    extends OrganisationUnitLevelImporter implements XMLConverter
{
    public static final String COLLECTION_NAME = "organisationUnitLevels";
    public static final String ELEMENT_NAME = "organisationUnitLevel";
    
    private static final String FIELD_ID = "id";
    private static final String FIELD_UID = "uid";
    private static final String FIELD_CODE = "code";
    private static final String FIELD_LEVEL = "level";
    private static final String FIELD_NAME = "name";

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Constructor for write operations.
     */
    public OrganisationUnitLevelConverter( OrganisationUnitService organisationUnitService )
    {   
        this.organisationUnitService = organisationUnitService;
    }
    
    /**
     * Constructor for read operations.
     * 
     * @param organisationUnitService the organisationUnitService to use.
     * @param importObjectService the importObjectService to use.
     */
    public OrganisationUnitLevelConverter( OrganisationUnitService organisationUnitService,
        ImportObjectService importObjectService )
    {
        this.organisationUnitService = organisationUnitService;
        this.importObjectService = importObjectService;
    }

    // -------------------------------------------------------------------------
    // XMLConverter implementation
    // -------------------------------------------------------------------------

    public void write( XMLWriter writer, ExportParams params )
    {
        Collection<OrganisationUnitLevel> levels = organisationUnitService.getOrganisationUnitLevels( params.getOrganisationUnitLevels() );
        
        if ( levels != null && levels.size() > 0 )
        {
            writer.openElement( COLLECTION_NAME );
            
            for ( OrganisationUnitLevel level : levels )
            {
                writer.openElement( ELEMENT_NAME );
                
                writer.writeElement( FIELD_ID, String.valueOf( level.getId() ) );
                writer.writeElement( FIELD_UID, level.getUid() );
                writer.writeElement( FIELD_CODE, level.getCode() );
                writer.writeElement( FIELD_LEVEL, String.valueOf( level.getLevel() ) );
                writer.writeElement( FIELD_NAME, level.getName() );
                
                writer.closeElement();
            }
            
            writer.closeElement();
        }
    }
    
    public void read( XMLReader reader, ImportParams params )
    {
        while ( reader.moveToStartElement( ELEMENT_NAME, COLLECTION_NAME ) )
        {
            final Map<String, String> values = reader.readElements( ELEMENT_NAME );
            
            final OrganisationUnitLevel level = new OrganisationUnitLevel();
            
            level.setId( Integer.parseInt( values.get( FIELD_ID ) ) );

            if ( params.minorVersionGreaterOrEqual( "1.3") )
            {
                level.setUid( values.get( FIELD_UID ) );
                level.setCode( values.get( FIELD_CODE) );
            }

            level.setLevel( Integer.parseInt( values.get( FIELD_LEVEL ) ) );
            level.setName( values.get( FIELD_NAME ) );
            
            importObject( level, params );
        }
    }
}

package org.hisp.dhis.importexport.dhis14.xml.converter.xsd;

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

import org.amplecode.staxwax.reader.XMLReader;
import org.amplecode.staxwax.writer.XMLWriter;
import org.hisp.dhis.importexport.ExportParams;
import org.hisp.dhis.importexport.ImportParams;

/**
 * @author Lars Helge Overland
 * @version $Id: UserXSDConverter.java 6455 2008-11-24 08:59:37Z larshelg $
 */
public class UserXSDConverter
    extends AbstractXSDConverter
{
    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Constructor for write operations.
     */
    public UserXSDConverter()
    {   
    }

    // -------------------------------------------------------------------------
    // XMLConverter implementation
    // -------------------------------------------------------------------------
    
    public void write( XMLWriter writer, ExportParams params )
    {
        writer.openElement( "xsd:element", "name", "UserName" );

        writeAnnotation( writer );
        
        writer.openElement( "xsd:complexType" );
        
        writer.openElement( "xsd:sequence" );
        
        writeInteger( writer, "UserID", 1, false );
        
        writeText( writer, "UID", 1, true, 11 );
        
        writeText( writer, "UserName", 1, true, 25 );
        
        writeText( writer, "Surname", 0, false, 230 );
        
        writeText( writer, "Firstname", 0, false, 230 );        
        
        writeInteger( writer, "InfoRoleID", 1, true );
        
        writeText( writer, "UserRoleUserDefined", 0, false, 100 );
        
        writeText( writer, "TelephoneNumber", 0, false, 15 );

        writeText( writer, "FaxNumber", 0, false, 15 );

        writeText( writer, "CellNumber", 0, false, 15 );
        
        writeText( writer, "EmailAddress", 0, false, 30 );

        writeUrl( writer, "UserURL", 0, 536870910 );
        
        writeInteger( writer, "Active", 1, true );
        
        writeInteger( writer, "Selected", 1, true );
        
        writeDateTime( writer, "FirstRegistered", 1, true );
        
        writeDateTime( writer, "LastLogOnDate", 1, true );
        
        writeDateTime( writer, "LastUpdated", 1, true );

        writer.closeElement();
        
        writer.closeElement();
        
        writer.closeElement();
    }    

    public void read( XMLReader reader, ImportParams params )
    {
        // Not implemented        
    }
}

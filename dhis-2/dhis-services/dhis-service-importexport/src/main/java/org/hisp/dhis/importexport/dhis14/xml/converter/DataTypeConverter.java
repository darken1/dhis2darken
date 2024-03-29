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

import org.amplecode.staxwax.reader.XMLReader;
import org.amplecode.staxwax.writer.XMLWriter;
import org.hisp.dhis.importexport.ExportParams;
import org.hisp.dhis.importexport.ImportParams;
import org.hisp.dhis.importexport.XMLConverter;

/**
 * @author Lars Helge Overland
 * @version $Id: DataTypeConverter.java 6455 2008-11-24 08:59:37Z larshelg $
 */
public class DataTypeConverter
    implements XMLConverter
{
    public static final String ELEMENT_NAME = "DataType";
    
    private static final String FIELD_ID = "DataTypeID";
    private static final String FIELD_NAME = "DataTypeName";
    private static final String FIELD_DISPLAY = "DataTypeDisplay";
    private static final String FIELD_ML = "DataType_ML";
    private static final String FIELD_UID = "UID";

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Constructor for write operations.
     */
    public DataTypeConverter()
    {   
    }

    // -------------------------------------------------------------------------
    // XMLConverter implementation
    // -------------------------------------------------------------------------
    
    public void write( XMLWriter writer, ExportParams params )
    {
        // ---------------------------------------------------------------------
        // Date
        // ---------------------------------------------------------------------
     
        writer.openElement( ELEMENT_NAME );
        
        writer.writeElement( FIELD_ID, String.valueOf( 1 ) );
        writer.writeElement( FIELD_NAME, "Date" );
        writer.writeElement( FIELD_DISPLAY, "Date" );
        writer.writeElement( FIELD_ML, "1175" );
        writer.writeElement( FIELD_UID, "" );
        
        writer.closeElement();

        // ---------------------------------------------------------------------
        // Memo
        // ---------------------------------------------------------------------
     
        writer.openElement( ELEMENT_NAME );
        
        writer.writeElement( FIELD_ID, String.valueOf( 2 ) );
        writer.writeElement( FIELD_NAME, "Memo" );
        writer.writeElement( FIELD_DISPLAY, "Memo" );
        writer.writeElement( FIELD_ML, "4316" );
        writer.writeElement( FIELD_UID, "" );
        
        writer.closeElement();

        // ---------------------------------------------------------------------
        // Number
        // ---------------------------------------------------------------------
     
        writer.openElement( ELEMENT_NAME );
        
        writer.writeElement( FIELD_ID, String.valueOf( 3 ) );
        writer.writeElement( FIELD_NAME, "Number" );
        writer.writeElement( FIELD_DISPLAY, "Number" );
        writer.writeElement( FIELD_ML, "1174" );
        writer.writeElement( FIELD_UID, "" );
        
        writer.closeElement();

        // ---------------------------------------------------------------------
        // String
        // ---------------------------------------------------------------------
     
        writer.openElement( ELEMENT_NAME );
        
        writer.writeElement( FIELD_ID, String.valueOf( 4 ) );
        writer.writeElement( FIELD_NAME, "String" );
        writer.writeElement( FIELD_DISPLAY, "Text" );
        writer.writeElement( FIELD_ML, "1173" );
        writer.writeElement( FIELD_UID, "" );
        
        writer.closeElement();

        // ---------------------------------------------------------------------
        // Yes/No
        // ---------------------------------------------------------------------
     
        writer.openElement( ELEMENT_NAME );
        
        writer.writeElement( FIELD_ID, String.valueOf( 5 ) );
        writer.writeElement( FIELD_NAME, "YesNo" );
        writer.writeElement( FIELD_DISPLAY, "Yes/No" );
        writer.writeElement( FIELD_ML, "3077" );
        writer.writeElement( FIELD_UID, "" );
        
        writer.closeElement();

        // ---------------------------------------------------------------------
        // Object
        // ---------------------------------------------------------------------
     
        writer.openElement( ELEMENT_NAME );
        
        writer.writeElement( FIELD_ID, String.valueOf( 6 ) );
        writer.writeElement( FIELD_NAME, "Object" );
        writer.writeElement( FIELD_DISPLAY, "OLE Object" );
        writer.writeElement( FIELD_ML, "4317" );
        writer.writeElement( FIELD_UID, "" );
        
        writer.closeElement();
    }

    public void read( XMLReader reader, ImportParams params )
    {
        // Not implemented        
    }
}

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
import java.util.Map;

import org.amplecode.staxwax.reader.XMLReader;
import org.amplecode.staxwax.writer.XMLWriter;
import org.hisp.dhis.importexport.ExportParams;
import org.hisp.dhis.importexport.ImportObjectService;
import org.hisp.dhis.importexport.ImportParams;
import org.hisp.dhis.importexport.XMLConverter;
import org.hisp.dhis.importexport.importer.IndicatorTypeImporter;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.indicator.IndicatorType;

/**
 * @author Lars Helge Overland
 * @version $Id: IndicatorTypeConverter.java 6455 2008-11-24 08:59:37Z larshelg $
 */
public class IndicatorTypeConverter
    extends IndicatorTypeImporter implements XMLConverter
{
    public static final String ELEMENT_NAME = "IndicatorType";
    
    private static final String FIELD_ID = "IndicatorTypeID";
    private static final String FIELD_NAME = "IndicatorTypeName";
    private static final String FIELD_FACTOR = "Factor";
    
    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Constructor for write operations.
     */
    public IndicatorTypeConverter( IndicatorService indicatorService )
    {   
        this.indicatorService = indicatorService;
    }

    /**
     * Constructor for read operations.
     * 
     * @param indicatorService the indicatorService to use.
     * @param importObjectService the importObjectService to use.
     */
    public IndicatorTypeConverter( ImportObjectService importObjectService, 
        IndicatorService indicatorService )
    {
        this.importObjectService = importObjectService;
        this.indicatorService = indicatorService;
    }    

    // -------------------------------------------------------------------------
    // XMLConverter implementation
    // -------------------------------------------------------------------------
    
    public void write( XMLWriter writer, ExportParams params )
    {
        Collection<IndicatorType> indicatorTypes = indicatorService.getIndicatorTypes( params.getIndicatorTypes() );
        
        if ( indicatorTypes != null && indicatorTypes.size() > 0 )
        {
            for ( IndicatorType object : indicatorTypes )
            {
                writer.openElement( ELEMENT_NAME );
                
                writer.writeElement( FIELD_ID, String.valueOf( object.getId() ) );
                writer.writeElement( FIELD_NAME, object.getName() );
                writer.writeElement( FIELD_FACTOR, String.valueOf( object.getFactor() ) );
                
                writer.closeElement();
            }
        }
    }
    
    public void read( XMLReader reader, ImportParams params )
    {
        final IndicatorType type = new IndicatorType();

        Map<String, String> values = reader.readElements( ELEMENT_NAME );
        
        type.setId( Integer.parseInt( values.get( FIELD_ID ) ) );
        type.setName( values.get( FIELD_NAME ) );
        type.setFactor( Integer.parseInt( values.get( FIELD_FACTOR ) ) );
           
        importObject( type, params );
    }
}

package org.hisp.dhis.importexport.dxf.converter;

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
import org.hisp.dhis.dataelement.DataElementCategoryCombo;
import org.hisp.dhis.dataelement.DataElementCategoryOption;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.importexport.ExportParams;
import org.hisp.dhis.importexport.ImportObjectService;
import org.hisp.dhis.importexport.ImportParams;
import org.hisp.dhis.importexport.XMLConverter;
import org.hisp.dhis.importexport.importer.DataElementCategoryOptionComboImporter;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
public class DataElementCategoryOptionComboConverter
    extends DataElementCategoryOptionComboImporter implements XMLConverter
{

    public static final String COLLECTION_NAME = "categoryOptionCombos";

    public static final String ELEMENT_NAME = "categoryOptionCombo";

    private static final String FIELD_ID = "id";

    private static final String FIELD_UID = "uid";

    private static final String FIELD_CODE = "code";

    private static final String FIELD_NAME = "name";

    private static final String SUB_ELEMENT_NAME = "categoryCombo";

    private static final String SUB_COLLECTION_NAME = "categoryOptions";

    private static final String SUB_COLLECTION_ELEMENT_NAME = "categoryOption";

    // -------------------------------------------------------------------------
    // Properties
    // -------------------------------------------------------------------------
    private Map<Object, Integer> categoryComboMapping;

    private Map<Object, Integer> categoryOptionMapping;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------
    /**
     * Constructor for write operations.
     */
    public DataElementCategoryOptionComboConverter( DataElementCategoryService categoryService )
    {
        this.categoryService = categoryService;
    }

    /**
     * Constructor for read operations.
     * 
     * @param importObjectService the importObjectService to use.
     * @param categoryOptionService the dataElementCategoryOptionService to use.
     */
    public DataElementCategoryOptionComboConverter( ImportObjectService importObjectService,
        Map<Object, Integer> categoryComboMapping,
        Map<Object, Integer> categoryOptionMapping,
        DataElementCategoryService categoryService )
    {
        this.importObjectService = importObjectService;
        this.categoryComboMapping = categoryComboMapping;
        this.categoryOptionMapping = categoryOptionMapping;
        this.categoryService = categoryService;
    }

    // -------------------------------------------------------------------------
    // XMLConverter implementation
    // -------------------------------------------------------------------------
    public void write( XMLWriter writer, ExportParams params )
    {
        Collection<DataElementCategoryOptionCombo> categoryOptionCombos =
            categoryService.getDataElementCategoryOptionCombos( params.getCategoryOptionCombos() );

        if ( categoryOptionCombos != null && categoryOptionCombos.size() > 0 )
        {
            writer.openElement( COLLECTION_NAME );

            for ( DataElementCategoryOptionCombo categoryOptionCombo : categoryOptionCombos )
            {
                writer.openElement( ELEMENT_NAME );

                writer.writeElement( FIELD_ID, String.valueOf( categoryOptionCombo.getId() ) );

                writer.writeElement( FIELD_UID,  categoryOptionCombo.getUid() );

                writer.writeElement( FIELD_CODE, categoryOptionCombo.getCode() );

                // -------------------------------------------------------------
                // Write CategoryCombo
                // -------------------------------------------------------------

                writer.openElement( SUB_ELEMENT_NAME );

                writer.writeElement( FIELD_ID, String.valueOf( categoryOptionCombo.getCategoryCombo().getId() ) );
                writer.writeElement( FIELD_NAME, categoryOptionCombo.getCategoryCombo().getName() );

                writer.closeElement();

                // -------------------------------------------------------------
                // Write CategoryOptions
                // -------------------------------------------------------------

                writer.openElement( SUB_COLLECTION_NAME );

                for ( DataElementCategoryOption categoryOption : categoryOptionCombo.getCategoryOptions() )
                {
                    writer.openElement( SUB_COLLECTION_ELEMENT_NAME );

                    writer.writeElement( FIELD_ID, String.valueOf( categoryOption.getId() ) );
                    writer.writeElement( FIELD_NAME, String.valueOf( categoryOption.getName() ) );

                    writer.closeElement();
                }

                writer.closeElement();

                writer.closeElement();
            }

            writer.closeElement();
        }
    }

    public void read( XMLReader reader, ImportParams params )
    {
        while ( reader.moveToStartElement( ELEMENT_NAME, COLLECTION_NAME ) )
        {
            final DataElementCategoryOptionCombo categoryOptionCombo = new DataElementCategoryOptionCombo();

            reader.moveToStartElement( FIELD_ID );
            categoryOptionCombo.setId( Integer.parseInt( reader.getElementValue() ) );

            if ( params.minorVersionGreaterOrEqual( "1.3") )
            {
                reader.moveToStartElement( FIELD_UID );
                categoryOptionCombo.setUid( reader.getElementValue() );
                reader.moveToStartElement( FIELD_CODE );
                categoryOptionCombo.setCode( reader.getElementValue() );
            }


            reader.moveToStartElement( FIELD_ID );
            int categoryComboId = Integer.parseInt( reader.getElementValue() );

            reader.moveToStartElement( FIELD_NAME );
            String categoryComboName = reader.getElementValue();

            log.debug( categoryComboName + ": " + categoryComboId + "-" + categoryOptionCombo.getId() );
            // -----------------------------------------------------------------
            // Setting the persisted CategoryCombo on the CategoryOptionCombo
            // if not in preview
            // -----------------------------------------------------------------

            DataElementCategoryCombo categoryCombo = new DataElementCategoryCombo();

            if ( params.isPreview() )
            {
                categoryCombo.setId( categoryComboId );
                categoryCombo.setName( categoryComboName );
            } else
            {
                categoryCombo = categoryService.getDataElementCategoryCombo( categoryComboMapping.get( categoryComboId ) );
            }

            categoryOptionCombo.setCategoryCombo( categoryCombo );

            // -----------------------------------------------------------------
            // Setting the persisted CategoryOptions on the CategoryOptionCombo
            // if not in preview
            // -----------------------------------------------------------------

            while ( reader.moveToStartElement( SUB_COLLECTION_ELEMENT_NAME, SUB_COLLECTION_NAME ) )
            {
                DataElementCategoryOption categoryOption = new DataElementCategoryOption();

                reader.moveToStartElement( FIELD_ID );
                int categoryOptionId = Integer.parseInt( reader.getElementValue() );

                reader.moveToStartElement( FIELD_NAME );
                String categoryOptionName = reader.getElementValue();

                if ( params.isPreview() )
                {
                    categoryOption.setId( categoryOptionId );
                    categoryOption.setName( categoryOptionName );
                } else
                {
                    categoryOption = categoryService.getDataElementCategoryOption( categoryOptionMapping.get( categoryOptionId ) );
                }

                categoryOptionCombo.getCategoryOptions().add( categoryOption );
            }

            importObject( categoryOptionCombo, params );
        }
    }
}

package org.hisp.dhis.importexport.dhis14.file.rowhandler;

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

import java.util.Map;

import org.amplecode.quick.BatchHandler;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.importexport.ImportDataValue;
import org.hisp.dhis.importexport.ImportParams;
import org.hisp.dhis.importexport.dhis14.object.Dhis14SemiPermanentDataValue;
import org.hisp.dhis.importexport.dhis14.util.Dhis14TypeHandler;
import org.hisp.dhis.importexport.importer.DataValueImporter;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.Period;

import com.ibatis.sqlmap.client.event.RowHandler;

/**
 * @author Lars Helge Overland
 * @version $Id: SemiPermanentDataValueRowHandler.java 5946 2008-10-16 15:46:43Z
 *          larshelg $
 */
public class SemiPermanentDataValueRowHandler
    extends DataValueImporter
    implements RowHandler
{
    private Map<Object, Integer> dataElementMapping;

    private Map<Period, Integer> periodMapping;

    private Map<Object, Integer> organisationUnitMapping;

    private DataElementCategoryOptionCombo categoryOptionCombo;

    private DataElement element;

    private OrganisationUnit source;

    private DataValue value;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public SemiPermanentDataValueRowHandler( BatchHandler<DataValue> batchHandler,
        BatchHandler<ImportDataValue> importDataValueBatchHandler, DataValueService dataValueService,
        Map<Object, Integer> dataElementMapping, Map<Period, Integer> periodMapping,
        Map<Object, Integer> organisationUnitMapping, DataElementCategoryOptionCombo categoryOptionCombo,
        ImportParams params )
    {
        this.batchHandler = batchHandler;
        this.importDataValueBatchHandler = importDataValueBatchHandler;
        this.dataValueService = dataValueService;
        this.dataElementMapping = dataElementMapping;
        this.periodMapping = periodMapping;
        this.organisationUnitMapping = organisationUnitMapping;
        this.categoryOptionCombo = categoryOptionCombo;
        this.params = params;

        this.element = new DataElement();
        this.source = new OrganisationUnit();
        this.value = new DataValue();
    }

    // -------------------------------------------------------------------------
    // RowHandler implementation
    // -------------------------------------------------------------------------

    public void handleRow( Object object )
    {
        final Dhis14SemiPermanentDataValue dhis14Value = (Dhis14SemiPermanentDataValue) object;

        final Integer dataElementId = dataElementMapping.get( dhis14Value.getDataElementId() );
        final Integer organisationUnitId = organisationUnitMapping.get( dhis14Value.getOrganisationUnitId() );

        if ( dataElementId == null )
        {
            log.warn( "Data element does not exist for identifier: " + dhis14Value.getDataElementId() );
            return;
        }
        if ( organisationUnitId == null )
        {
            log.warn( "Organisation unit does not exist for identifier: " + dhis14Value.getOrganisationUnitId() );
            return;
        }

        final Period period = new Period();

        period.setPeriodType( dhis14Value.getPeriodType() );
        period.setStartDate( dhis14Value.getStartDate() );
        period.setEndDate( dhis14Value.getEndDate() );

        element.setId( dataElementId );
        source.setId( organisationUnitId );
        period.setId( periodMapping.get( period ) );

        value.setDataElement( element );
        value.setCategoryOptionCombo( categoryOptionCombo );
        value.setPeriod( period );
        value.setSource( source );

        if ( dhis14Value.getValue() != null )
        {
            value.setValue( String.valueOf( dhis14Value.getValue() ) );
        }
        else if ( dhis14Value.getYesNo() != null )
        {
            value.setValue( Dhis14TypeHandler.convertYesNoFromDhis14( dhis14Value.getYesNo() ) );
        }

        if ( value.getDataElement() != null && value.getPeriod() != null && value.getSource() != null
            && value.getValue() != null )
        {
            importObject( value, params );
        }
    }
}

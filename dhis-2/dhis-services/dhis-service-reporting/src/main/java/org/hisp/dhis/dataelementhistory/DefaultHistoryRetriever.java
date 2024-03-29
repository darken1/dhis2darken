package org.hisp.dhis.dataelementhistory;

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

import java.util.List;

import org.apache.commons.math.util.MathUtils;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.minmax.MinMaxDataElement;
import org.hisp.dhis.minmax.MinMaxDataElementService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;

/**
 * @author Torgeir Lorange Ostby
 */
public class DefaultHistoryRetriever
    implements HistoryRetriever
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private MinMaxDataElementService minMaxDataElementService;

    public void setMinMaxDataElementService( MinMaxDataElementService minMaxDataElementService )
    {
        this.minMaxDataElementService = minMaxDataElementService;
    }

    private DataValueService dataValueService;

    public void setDataValueService( DataValueService dataValueService )
    {
        this.dataValueService = dataValueService;
    }

    private PeriodService periodService;

    public void setPeriodService( PeriodService periodService )
    {
        this.periodService = periodService;
    }

    // -------------------------------------------------------------------------
    // HistoryRetriever implementation
    // -------------------------------------------------------------------------

    public DataElementHistory getHistory( DataElement dataElement, DataElementCategoryOptionCombo optionCombo,
        OrganisationUnit organisationUnit, Period lastPeriod, int historyLength )
    {
        if ( !dataElement.getType().equals( DataElement.VALUE_TYPE_INT ) )
        {
            return null; // TODO
        }

        // ---------------------------------------------------------------------
        // Initialise history
        // ---------------------------------------------------------------------

        DataElementHistory history = new DataElementHistory();
        history.setDataElement( dataElement );
        history.setOptionCombo( optionCombo );
        history.setOrganisationUnit( organisationUnit );
        history.setHistoryLength( historyLength );
        addMinMaxLimits( organisationUnit, dataElement, optionCombo, history );

        // ---------------------------------------------------------------------
        // Create history points
        // ---------------------------------------------------------------------

        List<Period> periods = periodService.getPeriods( lastPeriod, historyLength );

        double max = 1;
        double average = 0;
        double total = 0;
        int count = 0;

        if ( history.getMaxLimit() != null )
        {
            max = Math.max( max, history.getMaxLimit() );
        }

        for ( Period period : periods )
        {
            DataElementHistoryPoint historyPoint = new DataElementHistoryPoint();
            historyPoint.setPeriod( period );

            Double value = getValue( dataElement, optionCombo, organisationUnit, period );

            if ( value != null )
            {
                historyPoint.setValue( value );
            }

            if ( historyPoint.getValue() != null )
            {
                max = Math.max( max, historyPoint.getValue() );
                total += historyPoint.getValue();
                average = total / ++count;
                average = MathUtils.round( average, 1 );
            }

            historyPoint.setAverage( average );

            history.getHistoryPoints().add( historyPoint );
        }

        history.setMaxHistoryValue( max );

        double maxValue = getMaxValue( history );

        if ( maxValue != Double.NEGATIVE_INFINITY )
        {
            history.setMaxValue( maxValue );

            double minValue = getMinValue( history );
            history.setMinValue( minValue );
        }

        return history;
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private void addMinMaxLimits( OrganisationUnit organisationUnit, DataElement dataElement,
        DataElementCategoryOptionCombo optionCombo, DataElementHistory history )
    {
        MinMaxDataElement minMaxDataElement = minMaxDataElementService.getMinMaxDataElement( organisationUnit,
            dataElement, optionCombo );

        if ( minMaxDataElement != null )
        {
            history.setMaxLimit( minMaxDataElement.getMax() );
            history.setMinLimit( minMaxDataElement.getMin() );
        }
    }

    /**
     * Finds the lowest value entered in the periode given by
     * history.historyLenght.
     * 
     * @param history DataElementHistory
     * @return the lowest Double value entred. If no values are entred,
     *         Double.MAX_VALUE is returned
     */
    private Double getMinValue( DataElementHistory history )
    {
        double value = Double.MAX_VALUE;
        List<DataElementHistoryPoint> historyPoints = history.getHistoryPoints();

        for ( DataElementHistoryPoint DEPoint : historyPoints )
        {
            if ( DEPoint.getValue() != null )
            {
                if ( DEPoint.getValue() < value )
                {
                    value = DEPoint.getValue();
                }
            }
        }

        return value;
    }

    /**
     * Finds the highest value entered in the periode given by
     * history.historyLenght.
     * 
     * @param history DataElementHistory
     * @return the highest entred value. If no value is entred
     *         Double.NEGATIVE_INFINITY is returned
     */
    private Double getMaxValue( DataElementHistory history )
    {
        double value = Double.NEGATIVE_INFINITY;
        List<DataElementHistoryPoint> historyPoints = history.getHistoryPoints();

        for ( DataElementHistoryPoint DEPoint : historyPoints )
        {
            if ( DEPoint.getValue() != null )
            {
                if ( DEPoint.getValue() > value )
                {
                    value = DEPoint.getValue();
                }
            }
        }

        return value;
    }

    private Double getValue( DataElement dataElement, DataElementCategoryOptionCombo optionCombo,
        OrganisationUnit organisationUnit, Period period )
    {
        DataValue dataValue = dataValueService.getDataValue( dataElement, period, organisationUnit, optionCombo );

        if ( dataValue != null )
        {
            if ( dataValue.getValue() != null )
            {
                return parseValue( dataValue.getValue() );

            }
        }

        return null;
    }

    private Double parseValue( String value )
    {
        try
        {
            return Double.parseDouble( value );
        }
        catch ( NumberFormatException e )
        {
            throw new RuntimeException( "Failed to parse double: " + value, e );
        }
    }
}

package org.hisp.dhis.reporttable;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hisp.dhis.DhisSpringTest;
import org.hisp.dhis.common.GenericIdentifiableObjectStore;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorService;
import org.hisp.dhis.indicator.IndicatorType;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.MonthlyPeriodType;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.period.RelativePeriods;
import org.junit.Test;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
@SuppressWarnings( "unchecked" )
public class ReportTableStoreTest
    extends DhisSpringTest
{
    private GenericIdentifiableObjectStore<ReportTable> reportTableStore;
    
    private IndicatorType indicatorType;

    private List<DataElement> dataElements;
    private List<Indicator> indicators;
    private List<DataSet> dataSets;
    private List<Period> periods;
    private List<OrganisationUnit> units;
    
    private PeriodType periodType;

    private DataElement dataElementA;
    private DataElement dataElementB;
        
    private Indicator indicatorA;
    private Indicator indicatorB;
    
    private DataSet dataSetA;
    private DataSet dataSetB;
    
    private Period periodA;
    private Period periodB;
    
    private OrganisationUnit unitA;
    private OrganisationUnit unitB;

    private RelativePeriods relativesA;
    private RelativePeriods relativesB;
    private RelativePeriods relativesC;
        
    @Override
    public void setUpTest()
        throws Exception
    {
        dataElements = new ArrayList<DataElement>();
        indicators = new ArrayList<Indicator>();
        dataSets = new ArrayList<DataSet>();
        periods = new ArrayList<Period>();
        units = new ArrayList<OrganisationUnit>();
        
        reportTableStore = (GenericIdentifiableObjectStore<ReportTable>) getBean( "org.hisp.dhis.reporttable.ReportTableStore" );

        dataElementService = (DataElementService) getBean( DataElementService.ID );
        
        indicatorService = (IndicatorService) getBean( IndicatorService.ID );
        
        dataSetService = (DataSetService) getBean( DataSetService.ID );
        
        periodService = (PeriodService) getBean( PeriodService.ID );
        
        organisationUnitService = (OrganisationUnitService) getBean( OrganisationUnitService.ID );
        
        indicatorType = createIndicatorType( 'A' );
        
        indicatorService.addIndicatorType( indicatorType );
        
        periodType = PeriodType.getPeriodTypeByName( MonthlyPeriodType.NAME );

        dataElementA = createDataElement( 'A' );
        dataElementB = createDataElement( 'B' );
        
        dataElementService.addDataElement( dataElementA );
        dataElementService.addDataElement( dataElementB );
                
        dataElements.add( dataElementA );
        dataElements.add( dataElementB );
        
        indicatorA = createIndicator( 'A', indicatorType );
        indicatorB = createIndicator( 'B', indicatorType );
        
        indicatorService.addIndicator( indicatorA );
        indicatorService.addIndicator( indicatorB );
        
        indicators.add( indicatorA );
        indicators.add( indicatorB );
        
        dataSetA = createDataSet( 'A', periodType );
        dataSetB = createDataSet( 'B', periodType );
        
        dataSetService.addDataSet( dataSetA );
        dataSetService.addDataSet( dataSetB );
        
        dataSets.add( dataSetA );
        dataSets.add( dataSetB );
        
        periodA = createPeriod( periodType, getDate( 2000, 1, 1 ), getDate( 2000, 1, 31 ) );
        periodB = createPeriod( periodType, getDate( 2000, 2, 1 ), getDate( 2000, 2, 28 ) );
        
        periodService.addPeriod( periodA );
        periodService.addPeriod( periodB );
        
        periods.add( periodA );
        periods.add( periodB );        

        unitA = createOrganisationUnit( 'A' );
        unitB = createOrganisationUnit( 'B' );        
        
        organisationUnitService.addOrganisationUnit( unitA );
        organisationUnitService.addOrganisationUnit( unitB );
        
        units.add( unitA );
        units.add( unitB );

        relativesA = new RelativePeriods();        
        relativesA.setReportingMonth( true );
        relativesA.setThisYear( true );

        relativesB = new RelativePeriods();        
        relativesB.setReportingMonth( true );
        relativesB.setThisYear( true );

        relativesC = new RelativePeriods();        
        relativesC.setReportingMonth( true );
        relativesC.setThisYear( true );    
    }

    @Test
    public void testSaveGetReportTable()
    {
        ReportTable reportTableA = new ReportTable( "Immunization",
            new ArrayList<DataElement>(), indicators, new ArrayList<DataSet>(), periods, units, 
            true, true, false, relativesA, null, "january_2000" );        
        ReportTable reportTableB = new ReportTable( "Prescriptions",
            dataElements, new ArrayList<Indicator>(), new ArrayList<DataSet>(), periods, units, 
            false, false, true, relativesB, null, "january_2000" );
        ReportTable reportTableC = new ReportTable( "Assualt",
            new ArrayList<DataElement>(), new ArrayList<Indicator>(), dataSets, periods, units, 
            false, false, true, relativesC, null, "january_2000" );
        
        int idA = reportTableStore.save( reportTableA );
        int idB = reportTableStore.save( reportTableB );
        int idC = reportTableStore.save( reportTableC );
        
        reportTableA = reportTableStore.get( idA );
        reportTableB = reportTableStore.get( idB );
        reportTableC = reportTableStore.get( idC );
        
        assertEquals( "Immunization", reportTableA.getName() );
        assertEquals( indicators, reportTableA.getIndicators() );
        assertEquals( periods, reportTableA.getPeriods() );
        assertEquals( units, reportTableA.getOrganisationUnits() );
        assertEquals( true, reportTableA.isDoIndicators() );
        assertEquals( true, reportTableA.isDoPeriods() );
        assertEquals( false, reportTableA.isDoUnits() );
        assertEquals( relativesA, reportTableA.getRelatives() );
        
        assertEquals( "Prescriptions", reportTableB.getName() );
        assertEquals( dataElements, reportTableB.getDataElements() );
        assertEquals( periods, reportTableB.getPeriods() );
        assertEquals( units, reportTableB.getOrganisationUnits() );
        assertEquals( false, reportTableB.isDoIndicators() );
        assertEquals( false, reportTableB.isDoPeriods() );
        assertEquals( true, reportTableB.isDoUnits() );
        assertEquals( relativesB, reportTableB.getRelatives() );

        assertEquals( "Assualt", reportTableC.getName() );
        assertEquals( dataSets, reportTableC.getDataSets() );
        assertEquals( periods, reportTableC.getPeriods() );
        assertEquals( units, reportTableC.getOrganisationUnits() );
        assertEquals( false, reportTableC.isDoIndicators() );
        assertEquals( false, reportTableC.isDoPeriods() );
        assertEquals( true, reportTableC.isDoUnits() );
        assertEquals( relativesC, reportTableC.getRelatives() );
    }

    @Test
    public void testDeleteReportTable()
    {
        ReportTable reportTableA = new ReportTable( "Immunization",
            new ArrayList<DataElement>(), indicators, new ArrayList<DataSet>(), periods, units, 
            true, true, false, relativesA, null, "january_2000" );        
        ReportTable reportTableB = new ReportTable( "Prescriptions",
            dataElements, new ArrayList<Indicator>(), new ArrayList<DataSet>(), periods, units, 
            false, false, true, relativesB, null, "january_2000" );
        
        int idA = reportTableStore.save( reportTableA );
        int idB = reportTableStore.save( reportTableB );
        
        assertNotNull( reportTableStore.get( idA ) );
        assertNotNull( reportTableStore.get( idB ) );
        
        reportTableStore.delete( reportTableA );

        assertNull( reportTableStore.get( idA ) );
        assertNotNull( reportTableStore.get( idB ) );
        
        reportTableStore.delete( reportTableB );

        assertNull( reportTableStore.get( idA ) );
        assertNull( reportTableStore.get( idB ) );
    }

    @Test
    public void testGetAllReportTables()
    {
        ReportTable reportTableA = new ReportTable( "Immunization",
            new ArrayList<DataElement>(), indicators, new ArrayList<DataSet>(), periods, units, 
            true, true, false, relativesA, null, "january_2000" );        
        ReportTable reportTableB = new ReportTable( "Prescriptions",
            dataElements, new ArrayList<Indicator>(), new ArrayList<DataSet>(), periods, units, 
            false, false, true, relativesB, null, "january_2000" );
        
        reportTableStore.save( reportTableA );
        reportTableStore.save( reportTableB );
        
        Collection<ReportTable> reportTables = reportTableStore.getAll();
        
        assertTrue( reportTables.contains( reportTableA ) );
        assertTrue( reportTables.contains( reportTableB ) );
    }

    @Test
    public void testGetReportTableByName()
    {
        ReportTable reportTableA = new ReportTable( "Immunization",
            new ArrayList<DataElement>(), indicators, new ArrayList<DataSet>(), periods, units, 
            true, true, false, relativesA, null, "january_2000" );        
        ReportTable reportTableB = new ReportTable( "Prescriptions",
            dataElements, new ArrayList<Indicator>(), new ArrayList<DataSet>(), periods, units, 
            false, false, true, relativesB, null, "january_2000" );
        
        reportTableStore.save( reportTableA );
        reportTableStore.save( reportTableB );
        
        ReportTable receivedReportTableA = reportTableStore.getByName( "Immunization" );
        
        assertNotNull( receivedReportTableA );
        assertEquals( reportTableA.getName(), receivedReportTableA.getName() );
    }
}

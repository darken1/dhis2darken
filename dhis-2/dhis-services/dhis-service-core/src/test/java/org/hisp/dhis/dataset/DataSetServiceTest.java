package org.hisp.dhis.dataset;

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

import org.hisp.dhis.DhisSpringTest;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.MonthlyPeriodType;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;

import static org.junit.Assert.*;

/**
 * @author Lars Helge Overland
 */
public class DataSetServiceTest
    extends DhisSpringTest
{
    private PeriodType periodType;

    private Period period;
    
    private OrganisationUnit unitA;
    private OrganisationUnit unitB;
    private OrganisationUnit unitC;
    private OrganisationUnit unitD;
    private OrganisationUnit unitE;
    private OrganisationUnit unitF;
    
    // -------------------------------------------------------------------------
    // Fixture
    // -------------------------------------------------------------------------

    @Override
    public void setUpTest()
        throws Exception
    {
        dataSetService = (DataSetService) getBean( DataSetService.ID );

        dataElementService = (DataElementService) getBean( DataElementService.ID );

        organisationUnitService = (OrganisationUnitService) getBean( OrganisationUnitService.ID );

        periodService = (PeriodService) getBean( PeriodService.ID );

        periodType = new MonthlyPeriodType();

        period = createPeriod( periodType, getDate( 2000, 3, 1 ), getDate( 2000, 3, 31 ) );
        periodService.addPeriod( period );
        
        unitA = createOrganisationUnit( 'A' );
        unitB = createOrganisationUnit( 'B' );
        unitC = createOrganisationUnit( 'C' );
        unitD = createOrganisationUnit( 'D' );
        unitE = createOrganisationUnit( 'E' );
        unitF = createOrganisationUnit( 'F' );
        
        organisationUnitService.addOrganisationUnit( unitA );
        organisationUnitService.addOrganisationUnit( unitB );
        organisationUnitService.addOrganisationUnit( unitC );
        organisationUnitService.addOrganisationUnit( unitD );
        organisationUnitService.addOrganisationUnit( unitE );
        organisationUnitService.addOrganisationUnit( unitF );
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private void assertEq( char uniqueCharacter, DataSet dataSet )
    {
        assertEquals( "DataSet" + uniqueCharacter, dataSet.getName() );
        assertEquals( "DataSetShort" + uniqueCharacter, dataSet.getShortName() );
        assertEquals( periodType, dataSet.getPeriodType() );
    }

    // -------------------------------------------------------------------------
    // DataSet
    // -------------------------------------------------------------------------

    @Test
    public void testAddDataSet()
    {
        DataSet dataSetA = createDataSet( 'A', periodType );
        DataSet dataSetB = createDataSet( 'B', periodType );

        int idA = dataSetService.addDataSet( dataSetA );
        int idB = dataSetService.addDataSet( dataSetB );

        dataSetA = dataSetService.getDataSet( idA );
        dataSetB = dataSetService.getDataSet( idB );

        assertEquals( idA, dataSetA.getId() );
        assertEq( 'A', dataSetA );

        assertEquals( idB, dataSetB.getId() );
        assertEq( 'B', dataSetB );
    }

    @Test
    public void testUpdateDataSet()
    {
        DataSet dataSet = createDataSet( 'A', periodType );

        int id = dataSetService.addDataSet( dataSet );

        dataSet = dataSetService.getDataSet( id );

        assertEq( 'A', dataSet );

        dataSet.setName( "DataSetB" );

        dataSetService.updateDataSet( dataSet );

        dataSet = dataSetService.getDataSet( id );

        assertEquals( dataSet.getName(), "DataSetB" );
    }

    @Test
    public void testDeleteAndGetDataSet()
    {
        DataSet dataSetA = createDataSet( 'A', periodType );
        DataSet dataSetB = createDataSet( 'B', periodType );

        int idA = dataSetService.addDataSet( dataSetA );
        int idB = dataSetService.addDataSet( dataSetB );

        assertNotNull( dataSetService.getDataSet( idA ) );
        assertNotNull( dataSetService.getDataSet( idB ) );

        dataSetService.deleteDataSet( dataSetService.getDataSet( idA ) );

        assertNull( dataSetService.getDataSet( idA ) );
        assertNotNull( dataSetService.getDataSet( idB ) );

        dataSetService.deleteDataSet( dataSetService.getDataSet( idB ) );

        assertNull( dataSetService.getDataSet( idA ) );
        assertNull( dataSetService.getDataSet( idB ) );
    }

    @Test
    public void testGetDataSetByName()
        throws Exception
    {
        DataSet dataSetA = createDataSet( 'A', periodType );
        DataSet dataSetB = createDataSet( 'B', periodType );

        int idA = dataSetService.addDataSet( dataSetA );
        int idB = dataSetService.addDataSet( dataSetB );

        assertEquals( dataSetService.getDataSetByName( "DataSetA" ).get( 0 ).getId(), idA );
        assertEquals( dataSetService.getDataSetByName( "DataSetB" ).get( 0 ).getId(), idB );
        assertTrue( dataSetService.getDataSetByName( "DataSetC" ).isEmpty() );
    }

    @Test
    public void testGetDataSetByShortName()
        throws Exception
    {
        DataSet dataSetA = createDataSet( 'A', periodType );
        DataSet dataSetB = createDataSet( 'B', periodType );

        int idA = dataSetService.addDataSet( dataSetA );
        int idB = dataSetService.addDataSet( dataSetB );

        assertEquals( dataSetService.getDataSetByShortName( "DataSetShortA" ).get( 0 ).getId(), idA );
        assertEquals( dataSetService.getDataSetByShortName( "DataSetShortB" ).get( 0 ).getId(), idB );
        assertTrue( dataSetService.getDataSetByShortName( "DataSetShortC" ).isEmpty() );
    }

    @Test
    public void testGetAllDataSets()
    {
        DataSet dataSetA = createDataSet( 'A', periodType );
        DataSet dataSetB = createDataSet( 'B', periodType );

        dataSetService.addDataSet( dataSetA );
        dataSetService.addDataSet( dataSetB );

        Collection<DataSet> dataSets = dataSetService.getAllDataSets();

        assertEquals( dataSets.size(), 2 );
        assertTrue( dataSets.contains( dataSetA ) );
        assertTrue( dataSets.contains( dataSetB ) );
    }

    @Test
    @Ignore
    public void testGetDataSetsBySources()
    {
        DataSet dataSetA = createDataSet( 'A', periodType );
        DataSet dataSetB = createDataSet( 'B', periodType );
        DataSet dataSetC = createDataSet( 'C', periodType );
        DataSet dataSetD = createDataSet( 'D', periodType );
        dataSetA.getSources().add( unitA );
        dataSetA.getSources().add( unitB );
        dataSetB.getSources().add( unitA );
        dataSetC.getSources().add( unitB );

        dataSetService.addDataSet( dataSetA );
        dataSetService.addDataSet( dataSetB );
        dataSetService.addDataSet( dataSetC );
        dataSetService.addDataSet( dataSetD );

        Collection<OrganisationUnit> sources = new HashSet<OrganisationUnit>();
        sources.add( unitA );
        sources.add( unitB );

        Collection<DataSet> dataSets = dataSetService.getDataSetsBySources( sources );

        assertEquals( 3, dataSets.size() );
        assertTrue( dataSets.contains( dataSetA ) );
        assertTrue( dataSets.contains( dataSetB ) );
        assertTrue( dataSets.contains( dataSetC ) );

        sources = new HashSet<OrganisationUnit>();
        sources.add( unitA );

        dataSets = dataSetService.getDataSetsBySources( sources );

        assertEquals( 2, dataSets.size() );
        assertTrue( dataSets.contains( dataSetA ) );
        assertTrue( dataSets.contains( dataSetB ) );
    }

    @Test
    public void testGetSourcesAssociatedWithDataSet()
    {
        DataSet dataSetA = createDataSet( 'A', periodType );
        DataSet dataSetB = createDataSet( 'B', periodType );

        dataSetA.getSources().add( unitA );
        dataSetA.getSources().add( unitB );
        dataSetB.getSources().add( unitE );

        dataSetB.getSources().add( unitC );
        dataSetB.getSources().add( unitD );
        dataSetB.getSources().add( unitE );

        dataSetService.addDataSet( dataSetA );
        dataSetService.addDataSet( dataSetB );

        Collection<OrganisationUnit> sources = new HashSet<OrganisationUnit>();

        sources.add( unitA );
        sources.add( unitB );
        sources.add( unitC );
        sources.add( unitD );

        assertEquals( 2, dataSetService.getSourcesAssociatedWithDataSet( dataSetA, sources ) );
        assertEquals( 2, dataSetService.getSourcesAssociatedWithDataSet( dataSetB, sources ) );
    }

    // -------------------------------------------------------------------------
    // LockException
    // -------------------------------------------------------------------------

    @Test
    public void testSaveGet()
    {
        Period period = periodType.createPeriod();
        DataSet dataSet = createDataSet( 'A', periodType );

        dataSetService.addDataSet( dataSet );

        LockException lockException = new LockException( period, unitA, dataSet );

        int id = dataSetService.addLockException( lockException );

        lockException = dataSetService.getLockException( id );

        assertNotNull( lockException );
        assertEquals( unitA, lockException.getOrganisationUnit() );
        assertEquals( period, lockException.getPeriod() );
        assertEquals( dataSet, lockException.getDataSet() );
    }

    @Test
    public void testIsLockedDataElement()
    {
        DataSet dataSetA = createDataSet( 'A', periodType );
        DataSet dataSetB = createDataSet( 'B', periodType );
        dataSetA.setExpiryDays( 20 );
        dataSetA.setTimelyDays( 15 );
        dataSetB.setExpiryDays( 10 );
        dataSetB.setTimelyDays( 15 );

        DataElement dataElementA = createDataElement( 'A' );
        DataElement dataElementB = createDataElement( 'B' );
        dataElementA.getDataSets().add( dataSetA );
        dataElementA.getDataSets().add( dataSetB );
        dataSetA.getDataElements().add( dataElementA );
        dataSetB.getDataElements().add( dataElementA );

        dataElementService.addDataElement( dataElementA );
        dataElementService.addDataElement( dataElementB );
        dataSetService.addDataSet( dataSetA );
        dataSetService.addDataSet( dataSetB );

        // ---------------------------------------------------------------------
        // Expiry days
        // ---------------------------------------------------------------------

        assertFalse( dataSetService.isLocked( dataElementA, period, unitA, getDate( 2000, 4, 1 ) ) );
        assertFalse( dataSetService.isLocked( dataElementA, period, unitA, getDate( 2000, 4, 5 ) ) );
        assertTrue( dataSetService.isLocked( dataElementA, period, unitA, getDate( 2000, 4, 15 ) ) );
        assertTrue( dataSetService.isLocked( dataElementA, period, unitA, getDate( 2000, 4, 25 ) ) );
        assertFalse( dataSetService.isLocked( dataElementB, period, unitA, getDate( 2000, 4, 25 ) ) );

        // ---------------------------------------------------------------------
        // Lock exception
        // ---------------------------------------------------------------------

        LockException lockException = new LockException( period, unitA, dataSetA );
        dataSetService.addLockException( lockException );

        assertFalse( dataSetService.isLocked( dataElementA, period, unitA, getDate( 2000, 4, 1 ) ) );
        assertFalse( dataSetService.isLocked( dataElementA, period, unitA, getDate( 2000, 4, 5 ) ) );
        assertFalse( dataSetService.isLocked( dataElementA, period, unitA, getDate( 2000, 4, 15 ) ) );
        assertFalse( dataSetService.isLocked( dataElementA, period, unitA, getDate( 2000, 4, 25 ) ) );
        assertFalse( dataSetService.isLocked( dataElementB, period, unitA, getDate( 2000, 4, 25 ) ) );
    }

    @Test
    public void testIsLockedDataSet()
    {
        DataSet dataSetA = createDataSet( 'A', periodType );
        DataSet dataSetB = createDataSet( 'B', periodType );
        dataSetA.setExpiryDays( 10 );
        dataSetA.setTimelyDays( 15 );
        dataSetB.setExpiryDays( 15 );
        dataSetB.setTimelyDays( 15 );

        dataSetService.addDataSet( dataSetA );
        dataSetService.addDataSet( dataSetB );

        // ---------------------------------------------------------------------
        // Expiry days
        // ---------------------------------------------------------------------

        assertFalse( dataSetService.isLocked( dataSetA, period, unitA, null, getDate( 2000, 4, 1 ) ) );
        assertFalse( dataSetService.isLocked( dataSetA, period, unitA, null, getDate( 2000, 4, 5 ) ) );
        assertTrue( dataSetService.isLocked( dataSetA, period, unitA, null, getDate( 2000, 4, 15 ) ) );
        assertTrue( dataSetService.isLocked( dataSetA, period, unitA, null, getDate( 2000, 4, 25 ) ) );
        assertFalse( dataSetService.isLocked( dataSetB, period, unitA, null, getDate( 2000, 4, 10 ) ) );
        assertTrue( dataSetService.isLocked( dataSetB, period, unitA, null, getDate( 2000, 4, 25 ) ) );

        // ---------------------------------------------------------------------
        // Lock exception
        // ---------------------------------------------------------------------

        LockException lockException = new LockException( period, unitA, dataSetA );
        dataSetService.addLockException( lockException );

        assertFalse( dataSetService.isLocked( dataSetA, period, unitA, null, getDate( 2000, 4, 1 ) ) );
        assertFalse( dataSetService.isLocked( dataSetA, period, unitA, null, getDate( 2000, 4, 5 ) ) );
        assertFalse( dataSetService.isLocked( dataSetA, period, unitA, null, getDate( 2000, 4, 15 ) ) );
        assertFalse( dataSetService.isLocked( dataSetA, period, unitA, null, getDate( 2000, 4, 25 ) ) );
        assertFalse( dataSetService.isLocked( dataSetB, period, unitA, null, getDate( 2000, 4, 10 ) ) );
        assertTrue( dataSetService.isLocked( dataSetB, period, unitA, null, getDate( 2000, 4, 25 ) ) );
    }
}

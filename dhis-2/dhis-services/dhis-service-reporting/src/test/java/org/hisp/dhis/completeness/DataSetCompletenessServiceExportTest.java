package org.hisp.dhis.completeness;

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

import static org.hisp.dhis.system.util.ConversionUtils.getIdentifiers;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;

import org.hisp.dhis.DhisSpringTest;
import org.hisp.dhis.dataset.CompleteDataSetRegistration;
import org.hisp.dhis.dataset.CompleteDataSetRegistrationService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.MonthlyPeriodType;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.period.QuarterlyPeriodType;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
@Ignore //TODO rewrite this test, takes too long
public class DataSetCompletenessServiceExportTest
    extends DhisSpringTest
{
    private DataSetCompletenessEngine completenessEngine;
    
    private DataSetCompletenessStore completenessStore;

    private CompleteDataSetRegistrationService registrationService;
    
    private PeriodType monthly;
    private PeriodType quarterly;
    
    private Period periodA;
    private Period periodB;
    private Period periodC;
    private Period periodD;
    
    private OrganisationUnit unitA;
    private OrganisationUnit unitB;
    private OrganisationUnit unitC;

    private DataSet dataSetA;
    
    private Collection<DataSet> dataSets;
    private Collection<Period> periods;
    private Collection<OrganisationUnit> units;
    
    @Override
    public void setUpTest()
    {
        completenessEngine = (DataSetCompletenessEngine) getBean( DataSetCompletenessEngine.ID );
        
        completenessStore = (DataSetCompletenessStore) getBean( DataSetCompletenessStore.ID );
        
        periodService = (PeriodService) getBean( PeriodService.ID );
        
        organisationUnitService = (OrganisationUnitService) getBean( OrganisationUnitService.ID );
        
        dataSetService = (DataSetService) getBean( DataSetService.ID );
        
        registrationService = (CompleteDataSetRegistrationService) getBean( CompleteDataSetRegistrationService.ID );

        dataSets = new ArrayList<DataSet>();
        periods = new ArrayList<Period>();
        units = new ArrayList<OrganisationUnit>();
        
        monthly = new MonthlyPeriodType();
        quarterly = new QuarterlyPeriodType();
        
        periodA = createPeriod( monthly, getDate( 2000, 1, 1 ), getDate( 2000, 1, 31 ) );
        periodB = createPeriod( monthly, getDate( 2000, 2, 1 ), getDate( 2000, 2, 28 ) );
        periodC = createPeriod( monthly, getDate( 2000, 3, 1 ), getDate( 2000, 3, 31 ) );
        periodD = createPeriod( quarterly, getDate( 2000, 1, 1 ), getDate( 2000, 3, 31 ) );
        
        periodService.addPeriod( periodA );
        periodService.addPeriod( periodB );
        periodService.addPeriod( periodC );
        periodService.addPeriod( periodD );
        
        periods.add( periodA );
        periods.add( periodB );
        periods.add( periodC );
        periods.add( periodD );
        
        unitA = createOrganisationUnit( 'A' );
        unitB = createOrganisationUnit( 'B' );
        unitC = createOrganisationUnit( 'C' );
        
        unitB.setParent( unitA );
        unitC.setParent( unitA );
        
        unitA.getChildren().add( unitB );
        unitA.getChildren().add( unitC );
        
        organisationUnitService.addOrganisationUnit( unitA );
        organisationUnitService.addOrganisationUnit( unitB );
        organisationUnitService.addOrganisationUnit( unitC );

        units.add( unitA );
        units.add( unitB );
        units.add( unitC );
        
        dataSetA = createDataSet( 'A', monthly );
        
        dataSetA.getSources().add( unitA );
        dataSetA.getSources().add( unitB );
        dataSetA.getSources().add( unitC );
        
        dataSetService.addDataSet( dataSetA );
        
        dataSets.add( dataSetA );
    }
    
    @Test
    public void testExportDataSetCompleteness()
    {
        registrationService.saveCompleteDataSetRegistration( new CompleteDataSetRegistration( dataSetA, periodA, unitB, null, null, "" ) );
        registrationService.saveCompleteDataSetRegistration( new CompleteDataSetRegistration( dataSetA, periodA, unitC, null, null, "" ) );
        registrationService.saveCompleteDataSetRegistration( new CompleteDataSetRegistration( dataSetA, periodB, unitB, null, null, "" ) );
        registrationService.saveCompleteDataSetRegistration( new CompleteDataSetRegistration( dataSetA, periodB, unitA, null, null, "" ) );
        registrationService.saveCompleteDataSetRegistration( new CompleteDataSetRegistration( dataSetA, periodC, unitA, null, null, "" ) );
        registrationService.saveCompleteDataSetRegistration( new CompleteDataSetRegistration( dataSetA, periodC, unitC, null, null, "" ) );
        
        completenessEngine.exportDataSetCompleteness( getIdentifiers( DataSet.class, dataSets ),
            getIdentifiers( Period.class, periods ), getIdentifiers( OrganisationUnit.class, units ), null );
        
        assertEquals( 100.0, completenessStore.getPercentage( dataSetA.getId(), periodA.getId(), unitB.getId() ), DELTA );
        assertEquals( 100.0, completenessStore.getPercentage( dataSetA.getId(), periodA.getId(), unitC.getId() ), DELTA );
        assertEquals( 66.7, completenessStore.getPercentage( dataSetA.getId(), periodA.getId(), unitA.getId() ), DELTA );
        
        assertEquals( 100.0, completenessStore.getPercentage( dataSetA.getId(), periodB.getId(), unitB.getId() ), DELTA );
        assertEquals( 0.0, completenessStore.getPercentage( dataSetA.getId(), periodB.getId(), unitC.getId() ), DELTA );
        assertEquals( 66.7, completenessStore.getPercentage( dataSetA.getId(), periodB.getId(), unitA.getId() ), DELTA );

        assertEquals( 0.0, completenessStore.getPercentage( dataSetA.getId(), periodC.getId(), unitB.getId() ), DELTA );
        assertEquals( 100.0, completenessStore.getPercentage( dataSetA.getId(), periodC.getId(), unitC.getId() ), DELTA );
        assertEquals( 66.7, completenessStore.getPercentage( dataSetA.getId(), periodC.getId(), unitA.getId() ), DELTA );
        
        assertEquals( 66.7, completenessStore.getPercentage( dataSetA.getId(), periodD.getId(), unitB.getId() ), DELTA );
        assertEquals( 66.7, completenessStore.getPercentage( dataSetA.getId(), periodD.getId(), unitC.getId() ), DELTA );
        assertEquals( 66.7, completenessStore.getPercentage( dataSetA.getId(), periodD.getId(), unitA.getId() ), DELTA );
    }
}

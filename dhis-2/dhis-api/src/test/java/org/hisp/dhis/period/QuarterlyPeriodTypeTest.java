package org.hisp.dhis.period;

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

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Lars Helge Overland
 */
public class QuarterlyPeriodTypeTest
{
    private Cal startCal;
    private Cal endCal;
    private Cal testCal;
    private QuarterlyPeriodType periodType;

    @Before
    public void before()
    {
        startCal = new Cal();
        endCal = new Cal();
        testCal = new Cal();
        periodType = new QuarterlyPeriodType();
    }

    @Test
    public void testCreatePeriod()
    {
        testCal.set( 2009, 8, 15 );

        startCal.set( 2009, 7, 1 );
        endCal.set( 2009, 9, 30 );

        Period period = periodType.createPeriod( testCal.time() );

        assertEquals( startCal.time(), period.getStartDate() );
        assertEquals( endCal.time(), period.getEndDate() );

        testCal.set( 2009, 4, 15 );

        startCal.set( 2009, 4, 1 );
        endCal.set( 2009, 6, 30 );

        period = periodType.createPeriod( testCal.time() );

        assertEquals( startCal.time(), period.getStartDate() );
        assertEquals( endCal.time(), period.getEndDate() );

        testCal.set( 2014, 11, 20 );

        startCal.set( 2014, 10, 1 );
        endCal.set( 2014, 12, 31 );

        period = periodType.createPeriod( testCal.time() );

        assertEquals( startCal.time(), period.getStartDate() );
        assertEquals( endCal.time(), period.getEndDate() );
    }

    @Test
    public void testCreatePeriodOverflow()
    {

    }

    @Test
    public void testGetNextPeriod()
    {
        testCal.set( 2009, 8, 15 );

        Period period = periodType.createPeriod( testCal.time() );

        period = periodType.getNextPeriod( period );

        startCal.set( 2009, 10, 1 );
        endCal.set( 2009, 12, 31 );

        assertEquals( startCal.time(), period.getStartDate() );
        assertEquals( endCal.time(), period.getEndDate() );
    }

    @Test
    public void testGetPreviousPeriod()
    {
        testCal.set( 2009, 8, 15 );

        Period period = periodType.createPeriod( testCal.time() );

        period = periodType.getPreviousPeriod( period );

        startCal.set( 2009, 4, 1 );
        endCal.set( 2009, 6, 30 );

        assertEquals( startCal.time(), period.getStartDate() );
        assertEquals( endCal.time(), period.getEndDate() );
    }

    @Test
    public void testGeneratePeriods()
    {
        testCal.set( 2009, 8, 15 );

        List<Period> periods = periodType.generatePeriods( testCal.time() );

        assertEquals( 4, periods.size() );
        assertEquals( periodType.createPeriod( new Cal( 2009, 1, 1 ).time() ), periods.get( 0 ) );
        assertEquals( periodType.createPeriod( new Cal( 2009, 4, 1 ).time() ), periods.get( 1 ) );
        assertEquals( periodType.createPeriod( new Cal( 2009, 7, 1 ).time() ), periods.get( 2 ) );
        assertEquals( periodType.createPeriod( new Cal( 2009, 10, 1 ).time() ), periods.get( 3 ) );
    }

    @Test
    public void testGenerateRollingPeriods()
    {
        testCal.set( 2009, 8, 15 );

        List<Period> periods = periodType.generateRollingPeriods( testCal.time() );

        assertEquals( 4, periods.size() );
        assertEquals( periodType.createPeriod( new Cal( 2008, 10, 1 ).time() ), periods.get( 0 ) );
        assertEquals( periodType.createPeriod( new Cal( 2009, 1, 1 ).time() ), periods.get( 1 ) );
        assertEquals( periodType.createPeriod( new Cal( 2009, 4, 1 ).time() ), periods.get( 2 ) );
        assertEquals( periodType.createPeriod( new Cal( 2009, 7, 1 ).time() ), periods.get( 3 ) );
    }

    @Test
    public void testGenerateLast5Years()
    {
        testCal.set( 2009, 8, 15 );

        List<Period> periods = periodType.generateLast5Years( testCal.time() );

        assertEquals( 20, periods.size() );
        assertEquals( periodType.createPeriod( new Cal( 2005, 1, 1 ).time() ), periods.get( 0 ) );
        assertEquals( periodType.createPeriod( new Cal( 2005, 4, 1 ).time() ), periods.get( 1 ) );
        assertEquals( periodType.createPeriod( new Cal( 2005, 7, 1 ).time() ), periods.get( 2 ) );
        assertEquals( periodType.createPeriod( new Cal( 2005, 10, 1 ).time() ), periods.get( 3 ) );
    }

    @Test
    public void testGeneratePeriodsBetweenDates()
    {
        startCal.set( 2009, 8, 15 );
        endCal.set( 2010, 2, 20 );

        List<Period> periods = periodType.generatePeriods( startCal.time(), endCal.time() );

        assertEquals( 3, periods.size() );
        assertEquals( periodType.createPeriod( new Cal( 2009, 7, 1 ).time() ), periods.get( 0 ) );
        assertEquals( periodType.createPeriod( new Cal( 2009, 10, 1 ).time() ), periods.get( 1 ) );
        assertEquals( periodType.createPeriod( new Cal( 2010, 1, 1 ).time() ), periods.get( 2 ) );
    }

    @Test
    public void testGetPeriodsBetween()
    {
        assertEquals( 1, periodType.createPeriod().getPeriodSpan( periodType ) );
        assertEquals( 2, new SixMonthlyPeriodType().createPeriod().getPeriodSpan( periodType ) );
        assertEquals( 4, new YearlyPeriodType().createPeriod().getPeriodSpan( periodType ) );
    }
}

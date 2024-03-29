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

import com.google.common.collect.Lists;
import org.hisp.dhis.calendar.DateUnit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The superclass of all PeriodTypes which represent typical calendar periods
 * like days, weeks, months, etc.
 *
 * @author Torgeir Lorange Ostby
 * @version $Id: CalendarPeriodType.java 2952 2007-03-01 23:40:10Z torgeilo $
 */
public abstract class CalendarPeriodType
    extends PeriodType
{
    /**
     * Determines if a de-serialized file is compatible with this class.
     */
    private static final long serialVersionUID = -2748690219217338988L;

    // -------------------------------------------------------------------------
    // CalendarPeriodType functionality
    // -------------------------------------------------------------------------

    /**
     * Returns a Period which is the next of the given Period. Only valid
     * Periods are returned. If the given Period is of different PeriodType than
     * the executing PeriodType, or the given Period is invalid, the returned
     * Period might overlap the given Period.
     *
     * @param period the Period to base the next Period on.
     * @return a Period which is the next of the given Period.
     */
    public abstract Period getNextPeriod( Period period );

    /**
     * Returns a Period which is the previous of the given Period. Only valid
     * Periods are returned. If the given Period is of different PeriodType than
     * the executing PeriodType, or the given Period is invalid, the returned
     * Period might overlap the given Period.
     *
     * @param period the Period to base the previous Period on.
     * @return a Period which is the previous of the given Period.
     */
    public abstract Period getPreviousPeriod( Period period );

    /**
     * Generates a list of Periods for a defined time span containing the given
     * Period. E.g. if the given Period is March 2007, and a monthly PeriodType
     * generates for a year, all months in 2007 should be generated and returned
     * in order.
     *
     * @param period the Period which touches the time span to generate Periods
     *               for.
     * @return a list of Periods for a defined time span.
     */
    public List<Period> generatePeriods( Period period )
    {
        return generatePeriods( period.getStartDate() );
    }

    /**
     * Generates a list of Periods for a defined time span containing the given
     * date. E.g. if the given date is March 2007, and a monthly PeriodType
     * generates for a year, all months in 2007 should be generated and returned
     * in order.
     *
     * @param date the date which touches the time span to generate Periods for.
     * @return a list of Periods for a defined time span.
     */
    public List<Period> generatePeriods( Date date )
    {
        return generatePeriods( createLocalDateUnitInstance( date ) );
    }

    public abstract List<Period> generatePeriods( DateUnit dateUnit );

    public List<Period> generateRollingPeriods( Date date )
    {
        return generateRollingPeriods( createLocalDateUnitInstance( date ) );
    }

    public abstract List<Period> generateRollingPeriods( DateUnit dateUnit );

    /**
     * Generates a list of Periods for the last 5 years. Must be overridden by
     * CalendarPeriodTypes which do not generate periods for the current year
     * only in their implementation of generatePeriods( Date ).
     *
     * @param date the date which touches the time span to generate Periods for.
     * @return a list of Periods for a defined time span.
     */
    public List<Period> generateLast5Years( Date date )
    {
        DateUnit dateUnit = createLocalDateUnitInstance( date );
        dateUnit = getCalendar().minusYears( dateUnit, 4 );
        List<Period> periods = Lists.newArrayList();

        for ( int i = 0; i < 5; i++ )
        {
            periods.addAll( generatePeriods( dateUnit ) );
            dateUnit = getCalendar().plusYears( dateUnit, 1 );
        }

        return periods;
    }

    /**
     * Generates a list of all Periods between the given start and end date. The
     * first period will span the start date. The last period will span the end
     * date.
     *
     * @param startDate the start date.
     * @param endDate   the end date.
     * @return a list of Periods for the defined time span.
     */
    public List<Period> generatePeriods( Date startDate, Date endDate )
    {
        List<Period> periods = new ArrayList<Period>();

        Period period = createPeriod( startDate );

        while ( period.getStartDate().before( endDate ) )
        {
            periods.add( period );
            period = getNextPeriod( period );
        }

        return periods;
    }
}

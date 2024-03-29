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

import org.hisp.dhis.calendar.CalendarService;
import org.hisp.dhis.calendar.DateUnit;
import org.hisp.dhis.calendar.impl.Iso8601Calendar;

import java.util.Calendar;
import java.util.Date;

/**
 * An abstraction over a calendar implementation, expects input to be in whatever the current
 * system calendar is using, and all output will be in ISO 8601.
 *
 * @author Lars Helge Overland
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class Cal
{
    private static CalendarService calendarService;

    public static void setCalendarService( CalendarService calendarService )
    {
        Cal.calendarService = calendarService;
    }

    public static org.hisp.dhis.calendar.Calendar getCalendar()
    {
        if ( calendarService != null )
        {
            return calendarService.getSystemCalendar();
        }

        return Iso8601Calendar.getInstance();
    }

    private DateUnit dateUnit = new DateUnit( 1, 1, 1 );

    public Cal()
    {
        dateUnit = getCalendar().today();
    }

    /**
     * @param year  the year starting at AD 1.
     * @param month the month starting at 1.
     * @param day   the day of the month starting at 1.
     */
    public Cal( int year, int month, int day )
    {
        dateUnit = new DateUnit( year, month, day );
    }

    /**
     * @param date the date.
     */
    public Cal( Date date )
    {
        dateUnit = DateUnit.fromJdkDate( date );
    }

    /**
     * Sets the time of the calendar to now.
     */
    public Cal now()
    {
        dateUnit = getCalendar().today();
        return this;
    }

    /**
     * Adds the given amount of time to the given calendar field.
     * @param field  the calendar field.
     * @param amount the amount of time.
     */
    public Cal add( int field, int amount )
    {
        switch ( field )
        {
            case Calendar.YEAR:
                getCalendar().plusYears( dateUnit, amount );
            case Calendar.MONTH:
                getCalendar().plusMonths( dateUnit, amount );
            case Calendar.DAY_OF_MONTH:
                getCalendar().plusDays( dateUnit, amount );
        }

        return this;
    }

    /**
     * Subtracts the given amount of time to the given calendar field.
     * @param field  the calendar field.
     * @param amount the amount of time.
     */
    public Cal subtract( int field, int amount )
    {
        switch ( field )
        {
            case Calendar.YEAR:
                getCalendar().minusYears( dateUnit, amount );
            case Calendar.MONTH:
                getCalendar().minusMonths( dateUnit, amount );
            case Calendar.DAY_OF_MONTH:
                getCalendar().minusDays( dateUnit, amount );
        }

        return this;
    }

    /**
     * Returns the value of the given calendar field.
     * @param field the field.
     */
    public int get( int field )
    {
        return getCalendar().toIso( dateUnit ).toJdkCalendar().get( field );
    }

    /**
     * Returns the current year.
     * @return current year
     */
    public int getYear()
    {
        return getCalendar().toIso( dateUnit ).toJdkCalendar().get( Calendar.YEAR );
    }

    /**
     * Sets the current time.
     * @param year  the year starting at AD 1.
     * @param month the month starting at 1.
     * @param day   the day of the month starting at 1.
     */
    public Cal set( int year, int month, int day )
    {
        dateUnit = new DateUnit( year, month, day );
        return this;
    }

    /**
     * Sets the current month and day.
     * @param month the month starting at 1.
     * @param day   the day of the month starting at 1.
     */
    public Cal set( int month, int day )
    {
        dateUnit.setMonth( month );
        dateUnit.setDay( day );
        return this;
    }

    /**
     * Sets the current time.
     * @param date the date to base time on.
     */
    public Cal set( Date date )
    {
        dateUnit = getCalendar().fromIso( DateUnit.fromJdkDate( date ) );
        return this;
    }

    /**
     * Returns the current date the cal.
     */
    public Date time()
    {
        return getCalendar().toIso( dateUnit ).toJdkDate();
    }
}

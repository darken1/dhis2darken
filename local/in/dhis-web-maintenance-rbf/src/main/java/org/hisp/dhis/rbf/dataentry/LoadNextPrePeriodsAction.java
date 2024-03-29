package org.hisp.dhis.rbf.dataentry;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.period.CalendarPeriodType;
import org.hisp.dhis.period.MonthlyPeriodType;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.period.QuarterlyPeriodType;
import org.hisp.dhis.system.filter.PastAndCurrentPeriodFilter;
import org.hisp.dhis.system.util.FilterUtils;
import org.hisp.dhis.util.SessionUtils;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 */

public class LoadNextPrePeriodsAction implements Action
{
   
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------
    
    private DataSetService dataSetService;
    
    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }
    
    private I18nFormat format;

    public void setFormat( I18nFormat format )
    {
        this.format = format;
    }

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------

    private int dataSetId;
    
    public void setDataSetId( int dataSetId )
    {
        this.dataSetId = dataSetId;
    }

    private int year;

    public void setYear( int year )
    {
        this.year = year;
    }

    // -------------------------------------------------------------------------
    // Output
    // -------------------------------------------------------------------------

    private List<Period> periods;

    public List<Period> getPeriods()
    {
        return periods;
    }

    private String periodType;
    
    public void setPeriodType( String periodType )
    {
        this.periodType = periodType;
    }
    
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()  throws Exception
    {
        
        DataSet dataset = dataSetService.getDataSet( dataSetId );
        
        if( dataset != null )
        {
            periodType = dataset.getPeriodType().getName();
        }
        else if ( periodType != null && periodType.equals( "Quarterly" ) )
        {
            //periodType = QuarterlyPeriodType.NAME;
        }
        else
        {
            periodType = MonthlyPeriodType.NAME;
        }
        
        //periodType = periodType != null && !periodType.isEmpty() ? periodType : MonthlyPeriodType.NAME;

        CalendarPeriodType _periodType = (CalendarPeriodType) CalendarPeriodType.getPeriodTypeByName( periodType );

        int thisYear = Calendar.getInstance().get( Calendar.YEAR );

        int currentYear = (Integer) SessionUtils.getSessionVar( SessionUtils.KEY_CURRENT_YEAR, thisYear );

        Calendar cal = PeriodType.createCalendarInstance();

        // Cannot go to next year if current year equals this year
        
        if ( !( currentYear == thisYear && year > 0 ) )
        {
            cal.set( Calendar.YEAR, currentYear );
            cal.add( Calendar.YEAR, year );

            SessionUtils.setSessionVar( SessionUtils.KEY_CURRENT_YEAR, cal.get( Calendar.YEAR ) );
        }

        periods = _periodType.generatePeriods( cal.getTime() );

        FilterUtils.filter( periods, new PastAndCurrentPeriodFilter() );

        Collections.reverse( periods );


        for ( Period period : periods )
        {
            period.setName( format.formatPeriod( period ) );
        }

        return SUCCESS;
    }


}

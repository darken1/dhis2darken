package org.hisp.dhis.caseentry.action.caseaggregation;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.caseaggregation.CaseAggregationCondition;
import org.hisp.dhis.caseaggregation.CaseAggregationConditionService;
import org.hisp.dhis.common.Grid;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.oust.manager.SelectionTreeManager;
import org.hisp.dhis.period.CalendarPeriodType;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodType;

import com.opensymphony.xwork2.Action;

public class CaseAggregationResultAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private SelectionTreeManager selectionTreeManager;

    public void setSelectionTreeManager( SelectionTreeManager selectionTreeManager )
    {
        this.selectionTreeManager = selectionTreeManager;
    }

    private DataSetService dataSetService;

    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }

    private CaseAggregationConditionService aggregationConditionService;

    public void setAggregationConditionService( CaseAggregationConditionService aggregationConditionService )
    {
        this.aggregationConditionService = aggregationConditionService;
    }

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    private I18nFormat format;

    public void setFormat( I18nFormat format )
    {
        this.format = format;
    }

    private I18n i18n;

    public void setI18n( I18n i18n )
    {
        this.i18n = i18n;
    }

    // -------------------------------------------------------------------------
    // Input/Output
    // -------------------------------------------------------------------------

    private String facilityLB;

    public void setFacilityLB( String facilityLB )
    {
        this.facilityLB = facilityLB;
    }

    private Integer dataSetId;

    public void setDataSetId( Integer dataSetId )
    {
        this.dataSetId = dataSetId;
    }

    private String startDate;

    public void setStartDate( String startDate )
    {
        this.startDate = startDate;
    }

    private String endDate;

    public void setEndDate( String endDate )
    {
        this.endDate = endDate;
    }

    private boolean autoSave;

    public void setAutoSave( boolean autoSave )
    {
        this.autoSave = autoSave;
    }

    public boolean isAutoSave()
    {
        return autoSave;
    }

    private List<Grid> grids = new ArrayList<Grid>();

    public List<Grid> getGrids()
    {
        return grids;
    }

    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
        // ---------------------------------------------------------------------
        // Get CaseAggregateCondition list
        // ---------------------------------------------------------------------

        DataSet selectedDataSet = dataSetService.getDataSet( dataSetId );

        Collection<CaseAggregationCondition> aggregationConditions = aggregationConditionService
            .getCaseAggregationCondition( selectedDataSet.getDataElements() );

        // ---------------------------------------------------------------------
        // Get selected periods list
        // ---------------------------------------------------------------------

        Date sDate = format.parseDate( startDate );
        Date eDate = format.parseDate( endDate );

        CalendarPeriodType periodType = (CalendarPeriodType) PeriodType.getPeriodTypeByName( selectedDataSet
            .getPeriodType().getName() );
        List<Period> periods = new ArrayList<Period>();
        periods.addAll( periodType.generatePeriods( sDate, eDate ) );

        // ---------------------------------------------------------------------
        // Get selected orgunits
        // ---------------------------------------------------------------------

        Set<Integer> orgunitIds = new HashSet<Integer>();

        OrganisationUnit selectedOrgunit = selectionTreeManager.getReloadedSelectedOrganisationUnit();

        if ( selectedOrgunit == null )
        {
            return SUCCESS;
        }

        if ( facilityLB.equals( "selected" ) )
        {
            orgunitIds.add( selectedOrgunit.getId() );
        }
        else if ( facilityLB.equals( "childrenOnly" ) )
        {
            orgunitIds.addAll( organisationUnitService.getOrganisationUnitHierarchy().getChildren(
                selectedOrgunit.getId() ) );
            orgunitIds.remove( selectedOrgunit.getId() );
        }
        else
        {
            orgunitIds.addAll( organisationUnitService.getOrganisationUnitHierarchy().getChildren(
                selectedOrgunit.getId() ) );
        }

        // ---------------------------------------------------------------------
        // Aggregation
        // ---------------------------------------------------------------------

        for ( CaseAggregationCondition condition : aggregationConditions )
        {
            for ( Period period : periods )
            {
                if ( autoSave )
                {
                    aggregationConditionService.insertAggregateValue( condition, orgunitIds, period );
                }
                else
                {
                    grids.add( aggregationConditionService.getAggregateValue( condition, orgunitIds, period, format,
                        i18n ) );
                }
            }
        }
        return SUCCESS;
    }
}

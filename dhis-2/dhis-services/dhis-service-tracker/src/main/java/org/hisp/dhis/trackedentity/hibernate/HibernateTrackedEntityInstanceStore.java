package org.hisp.dhis.trackedentity.hibernate;

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

import static org.hisp.dhis.common.IdentifiableObjectUtils.getIdentifiers;
import static org.hisp.dhis.system.util.DateUtils.getMediumDateString;
import static org.hisp.dhis.system.util.TextUtils.getCommaDelimitedString;
import static org.hisp.dhis.system.util.TextUtils.getTokens;
import static org.hisp.dhis.system.util.TextUtils.removeLastAnd;
import static org.hisp.dhis.system.util.TextUtils.removeLastComma;
import static org.hisp.dhis.system.util.TextUtils.removeLastOr;
import static org.hisp.dhis.trackedentity.TrackedEntityInstanceQueryParams.CREATED_ID;
import static org.hisp.dhis.trackedentity.TrackedEntityInstanceQueryParams.LAST_UPDATED_ID;
import static org.hisp.dhis.trackedentity.TrackedEntityInstanceQueryParams.ORG_UNIT_ID;
import static org.hisp.dhis.trackedentity.TrackedEntityInstanceQueryParams.TRACKED_ENTITY_ID;
import static org.hisp.dhis.trackedentity.TrackedEntityInstanceQueryParams.TRACKED_ENTITY_INSTANCE_ID;
import static org.hisp.dhis.trackedentity.TrackedEntityInstanceService.ERROR_DUPLICATE_IDENTIFIER;
import static org.hisp.dhis.trackedentity.TrackedEntityInstanceService.SEPARATOR;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hisp.dhis.common.OrganisationUnitSelectionMode;
import org.hisp.dhis.common.QueryFilter;
import org.hisp.dhis.common.QueryItem;
import org.hisp.dhis.common.QueryOperator;
import org.hisp.dhis.common.SetMap;
import org.hisp.dhis.common.hibernate.HibernateIdentifiableObjectStore;
import org.hisp.dhis.event.EventStatus;
import org.hisp.dhis.jdbc.StatementBuilder;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramInstance;
import org.hisp.dhis.program.ProgramStatus;
import org.hisp.dhis.system.util.SqlHelper;
import org.hisp.dhis.system.util.Timer;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceQueryParams;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceStore;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValue;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Abyot Asalefew Gizaw
 */
@Transactional
public class HibernateTrackedEntityInstanceStore
    extends HibernateIdentifiableObjectStore<TrackedEntityInstance>
    implements TrackedEntityInstanceStore
{
    private static final Map<ProgramStatus, Integer> PROGRAM_STATUS_MAP = new HashMap<ProgramStatus, Integer>()
    {
        {
            put( ProgramStatus.ACTIVE, ProgramInstance.STATUS_ACTIVE );
            put( ProgramStatus.COMPLETED, ProgramInstance.STATUS_COMPLETED );
            put( ProgramStatus.CANCELLED, ProgramInstance.STATUS_CANCELLED );
        }
    };

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private StatementBuilder statementBuilder;

    public void setStatementBuilder( StatementBuilder statementBuilder )
    {
        this.statementBuilder = statementBuilder;
    }

    // -------------------------------------------------------------------------
    // Implementation methods
    // -------------------------------------------------------------------------

    @Override
    public List<Map<String, String>> getTrackedEntityInstances( TrackedEntityInstanceQueryParams params )
    {
        SqlHelper hlp = new SqlHelper();

        // ---------------------------------------------------------------------
        // Select clause
        // ---------------------------------------------------------------------

        String sql = "select tei.uid as " + TRACKED_ENTITY_INSTANCE_ID + ", " + "tei.created as " + CREATED_ID + ", "
            + "tei.lastupdated as " + LAST_UPDATED_ID + ", " + "ou.uid as " + ORG_UNIT_ID + ", " + "te.uid as "
            + TRACKED_ENTITY_ID + ", ";

        for ( QueryItem item : params.getAttributes() )
        {
            String col = statementBuilder.columnQuote( item.getItemId() );

            sql += col + ".value as " + col + ", ";
        }

        sql = removeLastComma( sql ) + " ";

        // ---------------------------------------------------------------------
        // From and where clause
        // ---------------------------------------------------------------------

        sql += getFromWhereClause( params, hlp );

        // ---------------------------------------------------------------------
        // Paging clause
        // ---------------------------------------------------------------------

        if ( params.isPaging() )
        {
            sql += "limit " + params.getPageSizeWithDefault() + " offset " + params.getOffset();
        }

        // ---------------------------------------------------------------------
        // Query
        // ---------------------------------------------------------------------

        Timer t = new Timer().start();

        SqlRowSet rowSet = jdbcTemplate.queryForRowSet( sql );

        t.getTime( "Tracked entity instance query SQL: " + sql );

        List<Map<String, String>> list = new ArrayList<Map<String, String>>();

        while ( rowSet.next() )
        {
            final Map<String, String> map = new HashMap<String, String>();

            map.put( TRACKED_ENTITY_INSTANCE_ID, rowSet.getString( TRACKED_ENTITY_INSTANCE_ID ) );
            map.put( CREATED_ID, rowSet.getString( CREATED_ID ) );
            map.put( LAST_UPDATED_ID, rowSet.getString( LAST_UPDATED_ID ) );
            map.put( ORG_UNIT_ID, rowSet.getString( ORG_UNIT_ID ) );
            map.put( TRACKED_ENTITY_ID, rowSet.getString( TRACKED_ENTITY_ID ) );

            for ( QueryItem item : params.getAttributes() )
            {
                map.put( item.getItemId(), rowSet.getString( item.getItemId() ) );
            }

            list.add( map );
        }

        return list;
    }

    @Override
    public int getTrackedEntityInstanceCount( TrackedEntityInstanceQueryParams params )
    {
        SqlHelper hlp = new SqlHelper();

        // ---------------------------------------------------------------------
        // Select clause
        // ---------------------------------------------------------------------

        String sql = "select count(tei.uid) as " + TRACKED_ENTITY_INSTANCE_ID + " ";

        // ---------------------------------------------------------------------
        // From and where clause
        // ---------------------------------------------------------------------

        sql += getFromWhereClause( params, hlp );

        // ---------------------------------------------------------------------
        // Query
        // ---------------------------------------------------------------------

        Timer t = new Timer().start();

        Integer count = jdbcTemplate.queryForObject( sql, Integer.class );

        t.getTime( "Tracked entity instance count SQL: " + sql );

        return count;
    }

    /**
     * From, join and where clause. For attribute params, restriction is set in
     * inner join. For query params, restriction is set in where clause.
     */
    private String getFromWhereClause( TrackedEntityInstanceQueryParams params, SqlHelper hlp )
    {
        final String regexp = statementBuilder.getRegexpMatch();
        final String wordStart = statementBuilder.getRegexpWordStart();
        final String wordEnd = statementBuilder.getRegexpWordEnd();
        final String anyChar = "\\.*?";

        String sql = "from trackedentityinstance tei " + 
            "inner join trackedentity te on tei.trackedentityid = te.trackedentityid " +
            "inner join organisationunit ou on tei.organisationunitid = ou.organisationunitid ";

        for ( QueryItem item : params.getAttributesAndFilters() )
        {
            final String col = statementBuilder.columnQuote( item.getItemId() );

            final String joinClause = item.hasFilter() ? "inner join" : "left join";

            sql += joinClause + " " + 
                "trackedentityattributevalue as " + col + " " + "on " + col + ".trackedentityinstanceid = tei.trackedentityinstanceid " + 
                "and " + col + ".trackedentityattributeid = " + item.getItem().getId() + " ";

            if ( !params.isOrQuery() && item.hasFilter() )
            {
                for ( QueryFilter filter : item.getFilters() )
                {
                    final String encodedFilter = statementBuilder.encode( filter.getFilter(), false );

                    final String queryCol = item.isNumeric() ? (col + ".value") : "lower(" + col + ".value)";

                    sql += "and " + queryCol + " " + filter.getSqlOperator() + " "
                        + StringUtils.lowerCase( filter.getSqlFilter( encodedFilter ) ) + " ";
                }
            }
        }

        if ( params.isOrganisationUnitMode( OrganisationUnitSelectionMode.DESCENDANTS ) )
        {
            sql += "left join _orgunitstructure ous on tei.organisationunitid = ous.organisationunitid ";
        }

        if ( params.hasTrackedEntity() )
        {
            sql += hlp.whereAnd() + " tei.trackedentityid = " + params.getTrackedEntity().getId() + " ";
        }

        if ( params.isOrganisationUnitMode( OrganisationUnitSelectionMode.DESCENDANTS ) )
        {
            SetMap<Integer, OrganisationUnit> levelOuMap = params.getLevelOrgUnitMap();

            for ( Integer level : levelOuMap.keySet() )
            {
                sql += hlp.whereAnd() + " ous.idlevel" + level + " in ("
                    + getCommaDelimitedString( getIdentifiers( levelOuMap.get( level ) ) ) + ") or ";
            }

            sql = removeLastOr( sql );
        }
        else if ( params.isOrganisationUnitMode( OrganisationUnitSelectionMode.ALL ) )
        {
        }
        else // SELECTED (default)
        {
            sql += hlp.whereAnd() + " tei.organisationunitid in ("
                + getCommaDelimitedString( getIdentifiers( params.getOrganisationUnits() ) ) + ") ";
        }

        if ( params.hasProgram() )
        {
            sql += hlp.whereAnd() + " exists (" +
                "select pi.trackedentityinstanceid " +
                "from programinstance pi ";

            if ( params.hasEventStatus() )
            {
                sql += 
                    "left join programstageinstance psi " +
                    "on pi.programinstanceid = psi.programinstanceid ";
            }

            sql += 
                "where pi.trackedentityinstanceid = tei.trackedentityinstanceid " +
                "and pi.programid = " + params.getProgram().getId() + " ";

            if ( params.hasProgramStatus() )
            {
                sql += "and pi.status = " + PROGRAM_STATUS_MAP.get( params.getProgramStatus() ) + " ";
            }

            if ( params.hasFollowUp() )
            {
                sql += "and pi.followup = " + params.getFollowUp() + " ";
            }

            if ( params.hasProgramStartDate() )
            {
                sql += "and pi.enrollmentdate >= '" + getMediumDateString( params.getProgramStartDate() ) + "' ";
            }

            if ( params.hasProgramEndDate() )
            {
                sql += "and pi.enrollmentdate <= '" + getMediumDateString( params.getProgramEndDate() ) + "' ";
            }

            if ( params.hasEventStatus() )
            {
                sql += getEventStatusWhereClause( params );
            }

            sql += ") ";
        }

        if ( params.isOrQuery() && params.hasAttributesOrFilters() )
        {
            final String start = params.getQuery().isOperator( QueryOperator.LIKE ) ? anyChar : wordStart;
            final String end = params.getQuery().isOperator( QueryOperator.LIKE ) ? anyChar : wordEnd;
            
            sql += hlp.whereAnd() + " (";

            List<String> queryTokens = getTokens( params.getQuery().getFilter() );

            for ( String queryToken : queryTokens )
            {
                final String query = statementBuilder.encode( queryToken, false );

                sql += "(";

                for ( QueryItem item : params.getAttributesAndFilters() )
                {
                    final String col = statementBuilder.columnQuote( item.getItemId() );

                    sql += 
                        col + ".value " + regexp + " '" + start + 
                        StringUtils.lowerCase( query ) + end + "' or ";
                }

                sql = removeLastOr( sql ) + ") and ";
            }

            sql = removeLastAnd( sql ) + ") ";
        }

        return sql;
    }

    private String getEventStatusWhereClause( TrackedEntityInstanceQueryParams params )
    {
        String start = getMediumDateString( params.getEventStartDate() );
        String end = getMediumDateString( params.getEventEndDate() );

        String sql = StringUtils.EMPTY;

        if ( params.isEventStatus( EventStatus.COMPLETED ) )
        {
            sql = 
                "and psi.executiondate >= '" + start + "' and psi.executiondate <= '" + end + "' " +
                "and psi.status = '" + EventStatus.COMPLETED.name() + "' ";
        }
        else if ( params.isEventStatus( EventStatus.VISITED ) )
        {
            sql = 
                "and psi.executiondate >= '" + start + "' and psi.executiondate <= '" + end + "' " + 
                "and psi.status = '" + EventStatus.ACTIVE.name() + "' ";
        }
        else if ( params.isEventStatus( EventStatus.SCHEDULE ) )
        {
            sql = 
                "and psi.executiondate is null and psi.duedate >= '" + start + "' and psi.duedate <= '" + end + "' " +
                "and psi.status is not null and date(now()) <= date(psi.duedate) ";
        }
        else if ( params.isEventStatus( EventStatus.OVERDUE ) )
        {
            sql = 
                "and psi.executiondate is null and psi.duedate >= '" + start + "' and psi.duedate <= '" + end + "' " +
                "and psi.status is not null and date(now()) > date(psi.duedate) ";
        }
        else if ( params.isEventStatus( EventStatus.SKIPPED ) )
        {
            sql = 
                "and psi.duedate >= '" + start + "' and psi.duedate <= '" + end + "' " +
                "and psi.status = '" + EventStatus.SKIPPED.name() + "' ";
        }

        return sql;
    }

    @Override
    public String validate( TrackedEntityInstance instance, TrackedEntityAttributeValue attributeValue, Program program )
    {
        TrackedEntityAttribute attribute = attributeValue.getAttribute();

        if ( attribute.isUnique() )
        {
            Criteria criteria = getCriteria();
            criteria.add( Restrictions.ne( "id", instance.getId() ) );
            criteria.createAlias( "attributeValues", "attributeValue" );
            criteria.createAlias( "attributeValue.attribute", "attribute" );
            criteria.add( Restrictions.eq( "attributeValue.value", attributeValue.getValue() ) );
            criteria.add( Restrictions.eq( "attributeValue.attribute", attribute ) );

            if ( attribute.getId() != 0 )
            {
                criteria.add( Restrictions.ne( "id", attribute.getId() ) );
            }

            if ( attribute.getOrgunitScope() )
            {
                criteria.add( Restrictions.eq( "organisationUnit", instance.getOrganisationUnit() ) );
            }

            if ( program != null && attribute.getProgramScope() )
            {
                criteria.createAlias( "programInstances", "programInstance" );
                criteria.add( Restrictions.eq( "programInstance.program", program ) );
            }

            Number rs = (Number) criteria.setProjection( Projections.projectionList().add( 
                Projections.property( "attribute.id" ) ) ).uniqueResult();

            if ( rs != null && rs.intValue() > 0 )
            {
                return ERROR_DUPLICATE_IDENTIFIER + SEPARATOR + rs.intValue();
            }
        }
        
        return null;
    }
}

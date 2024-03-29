package org.hisp.dhis.program.hibernate;

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

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hisp.dhis.common.hibernate.HibernateIdentifiableObjectStore;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramInstance;
import org.hisp.dhis.program.ProgramInstanceStore;
import org.hisp.dhis.program.SchedulingProgramObject;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceReminder;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

/**
 * @author Abyot Asalefew
 */
public class HibernateProgramInstanceStore
    extends HibernateIdentifiableObjectStore<ProgramInstance>
    implements ProgramInstanceStore
{
    // -------------------------------------------------------------------------
    // Implemented methods
    // -------------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public Collection<ProgramInstance> get( Program program )
    {
        return getCriteria( Restrictions.eq( "program", program ) ).list();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<ProgramInstance> get( Collection<Program> programs )
    {
        if ( programs == null || programs.isEmpty() )
        {
            return new ArrayList<ProgramInstance>();
        }

        return getCriteria( Restrictions.in( "program", programs ) ).list();
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Collection<ProgramInstance> get( Collection<Program> programs, OrganisationUnit organisationUnit )
    {
        if ( programs == null || programs.isEmpty() )
        {
            return new ArrayList<ProgramInstance>();
        }

        return getCriteria( 
            Restrictions.in( "program", programs ) ).
            createAlias( "entityInstance", "entityInstance" ).
            add( Restrictions.eq( "entityInstance.organisationUnit", organisationUnit ) ).list();
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public Collection<ProgramInstance> get( Collection<Program> programs, OrganisationUnit organisationUnit, int status )
    {
        if ( programs == null || programs.isEmpty() )
        {
            return new ArrayList<ProgramInstance>();
        }

        return getCriteria( 
            Restrictions.eq( "status", status ), 
            Restrictions.in( "program", programs ) ).
            createAlias( "entityInstance", "entityInstance" ).
            add( Restrictions.eq( "entityInstance.organisationUnit", organisationUnit ) ).list();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<ProgramInstance> get( Program program, Integer status )
    {
        return getCriteria( Restrictions.eq( "program", program ), Restrictions.eq( "status", status ) ).list();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<ProgramInstance> get( Collection<Program> programs, Integer status )
    {
        if ( programs == null || programs.isEmpty() )
        {
            return new ArrayList<ProgramInstance>();
        }

        return getCriteria( Restrictions.in( "program", programs ), Restrictions.eq( "status", status ) ).list();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<ProgramInstance> get( TrackedEntityInstance entityInstance, Integer status )
    {
        return getCriteria( Restrictions.eq( "entityInstance", entityInstance ), Restrictions.eq( "status", status ) ).list();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<ProgramInstance> get( TrackedEntityInstance entityInstance, Program program )
    {
        return getCriteria( Restrictions.eq( "entityInstance", entityInstance ), Restrictions.eq( "program", program ) ).list();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<ProgramInstance> get( TrackedEntityInstance entityInstance, Program program, Integer status )
    {
        return getCriteria( Restrictions.eq( "entityInstance", entityInstance ), Restrictions.eq( "program", program ),
            Restrictions.eq( "status", status ) ).list();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<ProgramInstance> get( Program program, OrganisationUnit organisationUnit, Integer min, Integer max )
    {
        Criteria criteria = getCriteria( 
            Restrictions.eq( "program", program ), Restrictions.isNull( "endDate" ) ).
            add( Restrictions.eq( "entityInstance.organisationUnit", organisationUnit ) ).
            createAlias( "entityInstance", "entityInstance" ).
            addOrder( Order.asc( "entityInstance.id" ) );

        if ( min != null )
        {
            criteria.setFirstResult( min );
        }
        
        if ( max != null )
        {
            criteria.setMaxResults( max );
        }
        
        return criteria.list();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<ProgramInstance> get( Program program, Collection<Integer> orgunitIds, Date startDate,
        Date endDate, Integer min, Integer max )
    {
        Criteria criteria = getCriteria( Restrictions.eq( "program", program ),
            Restrictions.ge( "enrollmentDate", startDate ), Restrictions.le( "enrollmentDate", endDate ) )
            .createAlias( "entityInstance", "entityInstance" ).createAlias( "entityInstance.organisationUnit", "organisationUnit" )
            .add( Restrictions.in( "organisationUnit.id", orgunitIds ) ).addOrder( Order.asc( "entityInstance.id" ) );

        if ( min != null )
        {
            criteria.setFirstResult( min );
        }
        
        if ( max != null )
        {
            criteria.setMaxResults( max );
        }
        
        return criteria.list();
    }

    public int count( Program program, OrganisationUnit organisationUnit )
    {
        Number rs = (Number) getCriteria( 
            Restrictions.eq( "program", program ), Restrictions.isNull( "endDate" ) ).
            createAlias( "entityInstance", "entityInstance" ).
            add( Restrictions.eq( "entityInstance.organisationUnit", organisationUnit ) ).
            setProjection( Projections.rowCount() ).uniqueResult();
        
        return rs != null ? rs.intValue() : 0;
    }

    public int count( Program program, Collection<Integer> orgunitIds, Date startDate, Date endDate )
    {
        Number rs = (Number) getCriteria( 
            Restrictions.eq( "program", program ),
            Restrictions.ge( "enrollmentDate", startDate ), 
            Restrictions.le( "enrollmentDate", endDate ) ).
            createAlias( "entityInstance", "entityInstance" ).
            createAlias( "entityInstance.organisationUnit", "organisationUnit" ).
            add( Restrictions.in( "organisationUnit.id", orgunitIds ) ).
            setProjection( Projections.rowCount() ).uniqueResult();

        return rs != null ? rs.intValue() : 0;
    }

    public int countByStatus( Integer status, Program program, Collection<Integer> orgunitIds, Date startDate, Date endDate )
    {
        Number rs = (Number) getCriteria( 
            Restrictions.eq( "program", program ),
            Restrictions.between( "enrollmentDate", startDate, endDate ) ).
            createAlias( "entityInstance", "entityInstance" ).
            createAlias( "entityInstance.organisationUnit", "organisationUnit" ).
            add( Restrictions.in( "organisationUnit.id", orgunitIds ) ).
            add( Restrictions.eq( "status", status ) ).
            setProjection( Projections.rowCount() ).uniqueResult();

        return rs != null ? rs.intValue() : 0;
    }

    @SuppressWarnings( "unchecked" )
    public Collection<ProgramInstance> getByStatus( Integer status, Program program, Collection<Integer> orgunitIds,
        Date startDate, Date endDate )
    {
        return getCriteria( 
            Restrictions.eq( "program", program ),
            Restrictions.between( "enrollmentDate", startDate, endDate ) ).
            createAlias( "entityInstance", "entityInstance" ).
            createAlias( "entityInstance.organisationUnit", "organisationUnit" ).
            add( Restrictions.in( "organisationUnit.id", orgunitIds ) ).
            add( Restrictions.eq( "status", status ) ).list();
    }
    
    //TODO from here this class must be rewritten

    public Collection<SchedulingProgramObject> getSendMesssageEvents( String dateToCompare )
    {
        String sql = " ( " + sendMessageToTrackedEntityInstanceSql( dateToCompare ) + " ) ";

        sql += " UNION ( " + sendMessageToOrgunitRegisteredSql( dateToCompare ) + " ) ";

        sql += " UNION ( " + sendMessageToUsersSql( dateToCompare ) + " ) ";

        sql += " UNION ( " + sendMessageToUserGroupsSql( dateToCompare ) + " ) ";

        SqlRowSet rs = jdbcTemplate.queryForRowSet( sql );

        int cols = rs.getMetaData().getColumnCount();

        Collection<SchedulingProgramObject> schedulingProgramObjects = new HashSet<SchedulingProgramObject>();

        while ( rs.next() )
        {
            String message = "";
            for ( int i = 1; i <= cols; i++ )
            {
                message = rs.getString( "templatemessage" );
                String organisationunitName = rs.getString( "orgunitName" );
                String programName = rs.getString( "programName" );
                String incidentDate = rs.getString( "dateofincident" ).split( " " )[0];
                String daysSinceIncidentDate = rs.getString( "days_since_incident_date" );
                String erollmentDate = rs.getString( "enrollmentdate" ).split( " " )[0];
                String daysSinceEnrollementDate = rs.getString( "days_since_erollment_date" );

                message = message.replace( TrackedEntityInstanceReminder.TEMPLATE_MESSSAGE_PROGRAM_NAME, programName );
                message = message.replace( TrackedEntityInstanceReminder.TEMPLATE_MESSSAGE_ORGUNIT_NAME, organisationunitName );
                message = message.replace( TrackedEntityInstanceReminder.TEMPLATE_MESSSAGE_INCIDENT_DATE, incidentDate );
                message = message.replace( TrackedEntityInstanceReminder.TEMPLATE_MESSSAGE_ENROLLMENT_DATE, erollmentDate );
                message = message.replace( TrackedEntityInstanceReminder.TEMPLATE_MESSSAGE_DAYS_SINCE_ENROLLMENT_DATE,
                    daysSinceEnrollementDate );
                message = message.replace( TrackedEntityInstanceReminder.TEMPLATE_MESSSAGE_DAYS_SINCE_INCIDENT_DATE,
                    daysSinceIncidentDate );
            }

            SchedulingProgramObject schedulingProgramObject = new SchedulingProgramObject();
            schedulingProgramObject.setProgramInstanceId( rs.getInt( "programinstanceid" ) );
            schedulingProgramObject.setPhoneNumber( rs.getString( "phonenumber" ) );
            schedulingProgramObject.setMessage( message );

            schedulingProgramObjects.add( schedulingProgramObject );
        }

        return schedulingProgramObjects;
    }

    private String sendMessageToTrackedEntityInstanceSql( String dateToCompare )
    {
        return "SELECT pi.programinstanceid, pav.value as phonenumber, prm.templatemessage, "
            + "         org.name as orgunitName, " + "         pg.name as programName, pi.dateofincident , "
            + "         pi.enrollmentdate,(DATE(now()) - DATE(pi.enrollmentdate) ) as days_since_erollment_date, "
            + "         (DATE(now()) - DATE(pi.dateofincident) ) as days_since_incident_date "
            + "       FROM trackedentityinstance p INNER JOIN programinstance pi "
            + "              ON p.trackedentityinstanceid=pi.trackedentityinstanceid INNER JOIN program pg "
            + "              ON pg.programid=pi.programid INNER JOIN organisationunit org "
            + "              ON org.organisationunitid = p.organisationunitid INNER JOIN trackedentityinstancereminder prm "
            + "              ON prm.programid = pi.programid INNER JOIN trackedentityattributevalue pav "
            + "              ON pav.trackedentityinstanceid=p.trackedentityinstanceid INNER JOIN trackedentityattribute pa "
            + "              ON pa.trackedentityattributeid=pav.trackedentityattributeid " + "       WHERE pi.status= "
            + ProgramInstance.STATUS_ACTIVE
            + "         and prm.templatemessage is not NULL and prm.templatemessage != ''   "
            + "         and pg.type=1 and prm.daysallowedsendmessage is not null and pa.valuetype='phoneNumber' "
            + "         and ( DATE(now()) - DATE(pi." + dateToCompare + ") ) = prm.daysallowedsendmessage "
            + "         and prm.whenToSend is null and prm.dateToCompare='" + dateToCompare + "' and prm.sendto = "
            + TrackedEntityInstanceReminder.SEND_TO_TRACKED_ENTITY_INSTANCE;
    }

    private String sendMessageToOrgunitRegisteredSql( String dateToCompare )
    {
        return "SELECT pi.programinstanceid, org.phonenumber, prm.templatemessage, org.name as orgunitName, "
            + "   pg.name as programName, pi.dateofincident, pi.enrollmentdate,(DATE(now()) - DATE(pi.enrollmentdate) ) as days_since_erollment_date, "
            + "       (DATE(now()) - DATE(pi.dateofincident) ) as days_since_incident_date "
            + "    FROM trackedentityinstance p INNER JOIN programinstance pi "
            + "           ON p.trackedentityinstanceid=pi.trackedentityinstanceid INNER JOIN program pg "
            + "           ON pg.programid=pi.programid INNER JOIN organisationunit org "
            + "           ON org.organisationunitid = p.organisationunitid INNER JOIN trackedentityinstancereminder prm "
            + "           ON prm.programid = pi.programid " + "    WHERE pi.status = " + ProgramInstance.STATUS_ACTIVE
            + "      and org.phonenumber is not NULL and org.phonenumber != '' "
            + "      and prm.templatemessage is not NULL and prm.templatemessage != '' "
            + "      and pg.type=1 and prm.daysallowedsendmessage is not null " + "      and ( DATE(now()) - DATE( pi."
            + dateToCompare + " ) ) = prm.daysallowedsendmessage " + "      and prm.dateToCompare='" + dateToCompare
            + "'     and prm.whenToSend is null and prm.sendto =  " + TrackedEntityInstanceReminder.SEND_TO_ORGUGNIT_REGISTERED;
    }

    private String sendMessageToUsersSql( String dateToCompare )
    {
        return "SELECT pi.programinstanceid, uif.phonenumber, prm.templatemessage, org.name as orgunitName, pg.name as programName, pi.dateofincident ,"
            + "pi.enrollmentdate,(DATE(now()) - DATE(pi.enrollmentdate) ) as days_since_erollment_date, "
            + "(DATE(now()) - DATE(pi.dateofincident) ) as days_since_incident_date "
            + "FROM trackedentityinstance p INNER JOIN programinstance pi "
            + "    ON p.trackedentityinstanceid=pi.trackedentityinstanceid INNER JOIN program pg "
            + "    ON pg.programid=pi.programid INNER JOIN organisationunit org "
            + "    ON org.organisationunitid = p.organisationunitid INNER JOIN trackedentityinstancereminder prm "
            + "    ON prm.programid = pi.programid INNER JOIN usermembership ums "
            + "    ON ums.organisationunitid = p.organisationunitid INNER JOIN userinfo uif "
            + "    ON uif.userinfoid = ums.userinfoid "
            + "WHERE pi.status= "
            + ProgramInstance.STATUS_ACTIVE
            + "         and uif.phonenumber is not NULL and uif.phonenumber != '' "
            + "         and prm.templatemessage is not NULL and prm.templatemessage != '' "
            + "         and pg.type=1 and prm.daysallowedsendmessage is not null "
            + "         and ( DATE(now()) - DATE( "
            + dateToCompare
            + " ) ) = prm.daysallowedsendmessage "
            + "         and prm.dateToCompare='"
            + dateToCompare
            + "'        and prm.sendto = "
            + TrackedEntityInstanceReminder.SEND_TO_ALL_USERS_IN_ORGUGNIT_REGISTERED;
    }

    private String sendMessageToUserGroupsSql( String dateToCompare )
    {
        return "select pi.programinstanceid, uif.phonenumber,prm.templatemessage, org.name as orgunitName ,"
            + " pg.name as programName, pi.dateofincident, pi.enrollmentdate, (DATE(now()) - DATE(pi.enrollmentdate) ) as days_since_erollment_date, "
            + "(DATE(now()) - DATE(pi.dateofincident) ) as days_since_incident_date "
            + "  from trackedentityinstance p INNER JOIN programinstance pi " + "       ON p.trackedentityinstanceid=pi.trackedentityinstanceid "
            + "   INNER JOIN program pg " + "       ON pg.programid=pi.programid "
            + "   INNER JOIN organisationunit org " + "       ON org.organisationunitid = p.organisationunitid "
            + "   INNER JOIN trackedentityinstancereminder prm " + "       ON prm.programid = pg.programid "
            + "   INNER JOIN usergroupmembers ugm " + "       ON ugm.usergroupid = prm.usergroupid "
            + "   INNER JOIN userinfo uif " + "       ON uif.userinfoid = ugm.userid " + "  WHERE pi.status= "
            + ProgramInstance.STATUS_ACTIVE + "       and uif.phonenumber is not NULL and uif.phonenumber != '' "
            + "       and prm.templatemessage is not NULL and prm.templatemessage != '' "
            + "       and pg.type=1 and prm.daysallowedsendmessage is not null " + "       and (  DATE(now()) - DATE("
            + dateToCompare + ") ) = prm.daysallowedsendmessage " + "       and prm.whentosend is null "
            + "       and prm.sendto = " + TrackedEntityInstanceReminder.SEND_TO_USER_GROUP;
    }
}

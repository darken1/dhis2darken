package org.hisp.dhis.trackedentitydatavalue.hibernate;

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

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.hibernate.HibernateGenericStore;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValue;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValueStore;

/**
 * @author Abyot Asalefew Gizaw
 * @version $Id$
 */
public class HibernateTrackedEntityDataValueStore
    extends HibernateGenericStore<TrackedEntityDataValue>
    implements TrackedEntityDataValueStore
{
    public void saveVoid( TrackedEntityDataValue dataValue )
    {
        sessionFactory.getCurrentSession().save( dataValue );
    }
   
    public int detele( ProgramStageInstance programStageInstance )
    {
        Query query = getQuery( "delete from TrackedEntityDataValue where programStageInstance = :programStageInstance" );
        query.setEntity( "programStageInstance", programStageInstance );
        return query.executeUpdate();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<TrackedEntityDataValue> get( ProgramStageInstance programStageInstance )
    {
        return getCriteria( Restrictions.eq( "programStageInstance", programStageInstance ) ).list();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<TrackedEntityDataValue> get( ProgramStageInstance programStageInstance,
        Collection<DataElement> dataElements )
    {
        if ( dataElements == null || dataElements.isEmpty() )
        {
            return new ArrayList<TrackedEntityDataValue>();
        }
        
        return getCriteria( Restrictions.in( "dataElement", dataElements ), Restrictions.eq( "programStageInstance", programStageInstance ) ).list();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<TrackedEntityDataValue> get( Collection<ProgramStageInstance> programStageInstances )
    {
        if ( programStageInstances == null || programStageInstances.isEmpty() )
        {
            return new ArrayList<TrackedEntityDataValue>();
        }

        return getCriteria( Restrictions.in( "programStageInstance", programStageInstances ) ).list();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<TrackedEntityDataValue> get( DataElement dataElement )
    {
        return getCriteria( Restrictions.eq( "dataElement", dataElement ) ).list();
    }

    @SuppressWarnings( "unchecked" )
    public Collection<TrackedEntityDataValue> get( TrackedEntityInstance entityInstance, Collection<DataElement> dataElements, Date startDate,
        Date endDate )
     {
        if ( dataElements == null || dataElements.isEmpty() )
        {
            return new ArrayList<TrackedEntityDataValue>();
        }
        
        Criteria criteria = getCriteria();
        criteria.createAlias( "programStageInstance", "programStageInstance" );
        criteria.createAlias( "programStageInstance.programInstance", "programInstance" );
        criteria.add( Restrictions.in( "dataElement", dataElements ) );
        criteria.add( Restrictions.eq( "programInstance.entityInstance", entityInstance ) );
        criteria.add( Restrictions.between( "programStageInstance.executionDate", startDate, endDate ) );
        return criteria.list();
    }

    public TrackedEntityDataValue get( ProgramStageInstance programStageInstance, DataElement dataElement )
    {
        return (TrackedEntityDataValue) getCriteria( Restrictions.eq( "programStageInstance", programStageInstance ),
            Restrictions.eq( "dataElement", dataElement ) ).uniqueResult();
    }

}

package org.hisp.dhis.trackedentitydatavalue;

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

import java.util.Collection;
import java.util.Date;

import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValue;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValueService;
import org.hisp.dhis.trackedentitydatavalue.TrackedEntityDataValueStore;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Abyot Asalefew Gizaw
 */
@Transactional
public class DefaultTrackedEntityDataValueService
    implements TrackedEntityDataValueService
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private TrackedEntityDataValueStore dataValueStore;

    public void setDataValueStore( TrackedEntityDataValueStore dataValueStore )
    {
        this.dataValueStore = dataValueStore;
    }

    // -------------------------------------------------------------------------
    // Implementation methods
    // -------------------------------------------------------------------------

    public void saveTrackedEntityDataValue( TrackedEntityDataValue dataValue )
    {
        if ( dataValue.getValue() != null )
        {
            dataValueStore.saveVoid( dataValue );
        }
    }

    public void deleteTrackedEntityDataValue( TrackedEntityDataValue dataValue )
    {
        dataValueStore.delete( dataValue );
    }

    public void deleteTrackedEntityDataValue( ProgramStageInstance programStageInstance )
    {
        dataValueStore.detele( programStageInstance );
    }

    public void updateTrackedEntityDataValue( TrackedEntityDataValue dataValue )
    {
        if ( dataValue.getValue() == null )
        {
            dataValueStore.delete( dataValue );
        }
        else
        {
            dataValueStore.update( dataValue );
        }
    }

    public Collection<TrackedEntityDataValue> getTrackedEntityDataValues( ProgramStageInstance programStageInstance )
    {
        return dataValueStore.get( programStageInstance );
    }

    public Collection<TrackedEntityDataValue> getTrackedEntityDataValues( ProgramStageInstance programStageInstance,
        Collection<DataElement> dataElements )
    {
        return dataValueStore.get( programStageInstance, dataElements );
    }

    public Collection<TrackedEntityDataValue> getTrackedEntityDataValues( Collection<ProgramStageInstance> programStageInstances )
    {
        return dataValueStore.get( programStageInstances );
    }

    public Collection<TrackedEntityDataValue> getTrackedEntityDataValues( DataElement dataElement )
    {
        return dataValueStore.get( dataElement );
    }

    public Collection<TrackedEntityDataValue> getTrackedEntityDataValues( TrackedEntityInstance entityInstance,
        Collection<DataElement> dataElements, Date startDate, Date endDate )
    {
        return dataValueStore.get( entityInstance, dataElements, startDate, endDate );
    }

    public TrackedEntityDataValue getTrackedEntityDataValue( ProgramStageInstance programStageInstance,
        DataElement dataElement )
    {
        return dataValueStore.get( programStageInstance, dataElement );
    }
}

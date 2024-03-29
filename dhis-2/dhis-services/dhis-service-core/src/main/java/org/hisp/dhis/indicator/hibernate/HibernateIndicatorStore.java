package org.hisp.dhis.indicator.hibernate;

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

import org.hibernate.Query;
import org.hisp.dhis.common.hibernate.HibernateIdentifiableObjectStore;
import org.hisp.dhis.indicator.Indicator;
import org.hisp.dhis.indicator.IndicatorStore;
import org.hisp.dhis.mapping.MapLegendSet;

import java.util.Collection;

/**
 * @author Lars Helge Overland
 * @version $Id: HibernateIndicatorStore.java 3287 2007-05-08 00:26:53Z larshelg $
 */
public class HibernateIndicatorStore
    extends HibernateIdentifiableObjectStore<Indicator>
    implements IndicatorStore
{
    // -------------------------------------------------------------------------
    // Indicator
    // -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public Collection<Indicator> getIndicatorsWithGroupSets()
    {
        final String hql = "from Indicator d where d.groupSets.size > 0";

        return getQuery( hql ).setCacheable( true ).list();
    }

    @SuppressWarnings("unchecked")
    public Collection<Indicator> getIndicatorsWithoutGroups()
    {
        final String hql = "from Indicator d where d.groups.size = 0";

        return getQuery( hql ).setCacheable( true ).list();
    }

    @SuppressWarnings("unchecked")
    public Collection<Indicator> getIndicatorsWithDataSets()
    {
        final String hql = "from Indicator d where d.dataSets.size > 0";

        return getQuery( hql ).setCacheable( true ).list();
    }

    @Override
    public int countMapLegendSetIndicators( MapLegendSet mapLegendSet )
    {
        Query query = getQuery( "select count(distinct c) from Indicator c where c.legendSet=:mapLegendSet" );
        query.setEntity( "mapLegendSet", mapLegendSet );

        return ((Long) query.uniqueResult()).intValue();
    }
}

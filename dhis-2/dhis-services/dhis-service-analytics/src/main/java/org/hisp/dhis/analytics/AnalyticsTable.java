package org.hisp.dhis.analytics;

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

import java.util.List;

import org.hisp.dhis.period.Period;
import org.hisp.dhis.program.Program;

/**
 * @author Lars Helge Overland
 */
public class AnalyticsTable
{
    private String baseName;

    private List<String[]> dimensionColumns;
    
    private Period period;
    
    private Program program;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public AnalyticsTable()
    {
    }

    public AnalyticsTable( String baseName, List<String[]> dimensionColumns )
    {
        this.baseName = baseName;
        this.dimensionColumns = dimensionColumns;
    }
    
    public AnalyticsTable( String baseName, List<String[]> dimensionColumns, Period period )
    {
        this.baseName = baseName;
        this.dimensionColumns = dimensionColumns;
        this.period = period;
    }
    
    public AnalyticsTable( String baseName, List<String[]> dimensionColumns, Period period, Program program )
    {
        this.baseName = baseName;
        this.dimensionColumns = dimensionColumns;
        this.period = period;
        this.program = program;
    }

    // -------------------------------------------------------------------------
    // Logic
    // -------------------------------------------------------------------------

    public String getTableName()
    {
        String name = baseName;
        
        if ( period != null )
        {
            name += "_" + period.getIsoDate();
        }
        
        if ( program != null )
        {
            name += "_" + program.getUid();
        }
        
        return name;
    }
    
    public String getTempTableName()
    {
        String name = baseName + AnalyticsTableManager.TABLE_TEMP_SUFFIX;

        if ( period != null )
        {
            name += "_" + period.getIsoDate();
        }
        
        if ( program != null )
        {
            name += "_" + program.getUid();
        }
        
        return name;
    }

    public boolean hasPeriod()
    {
        return period != null;
    }
    
    public boolean hasProgram()
    {
        return program != null;
    }
    
    @Override
    public String toString()
    {
        return getTableName();
    }

    // -------------------------------------------------------------------------
    // Getters and setters
    // -------------------------------------------------------------------------

    public String getBaseName()
    {
        return baseName;
    }

    public void setBaseName( String baseName )
    {
        this.baseName = baseName;
    }

    public List<String[]> getDimensionColumns()
    {
        return dimensionColumns;
    }

    public void setDimensionColumns( List<String[]> dimensionColumns )
    {
        this.dimensionColumns = dimensionColumns;
    }

    public Period getPeriod()
    {
        return period;
    }

    public void setPeriod( Period period )
    {
        this.period = period;
    }

    public Program getProgram()
    {
        return program;
    }

    public void setProgram( Program program )
    {
        this.program = program;
    }
}

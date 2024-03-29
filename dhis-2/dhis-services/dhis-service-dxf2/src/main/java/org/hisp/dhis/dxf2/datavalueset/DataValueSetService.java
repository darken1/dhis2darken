package org.hisp.dhis.dxf2.datavalueset;

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

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dxf2.importsummary.ImportSummary;
import org.hisp.dhis.dxf2.metadata.ImportOptions;
import org.hisp.dhis.node.types.RootNode;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.scheduling.TaskId;

public interface DataValueSetService
{
    void writeDataValueSet( String dataSet, String period, String orgUnit, OutputStream out );

    void writeDataValueSet( Set<String> dataSets, Date startDate, Date endDate, Set<String> orgUnits, OutputStream out );

    void writeDataValueSetJson( String ds, String period, String ou, OutputStream outputStream );

    void writeDataValueSetJson( Set<String> dataSet, Date startDate, Date endDate, Set<String> ous, OutputStream outputStream );

    void writeDataValueSetCsv( Set<String> dataSets, Date startDate, Date endDate, Set<String> orgUnits, Writer writer );

    RootNode getDataValueSetTemplate( DataSet dataSet, Period period, List<String> orgUnits,
        boolean writeComments, String ouScheme, String deScheme );

    ImportSummary saveDataValueSet( InputStream in );

    ImportSummary saveDataValueSetJson( InputStream in );

    ImportSummary saveDataValueSet( InputStream in, ImportOptions importOptions );

    ImportSummary saveDataValueSetJson( InputStream in, ImportOptions importOptions );

    ImportSummary saveDataValueSet( InputStream in, ImportOptions importOptions, TaskId taskId );

    ImportSummary saveDataValueSetJson( InputStream in, ImportOptions importOptions, TaskId taskId );

    ImportSummary saveDataValueSetCsv( InputStream in, ImportOptions importOptions, TaskId id );

    ImportSummary saveDataValueSetPdf( InputStream in, ImportOptions importOptions, TaskId id );
}

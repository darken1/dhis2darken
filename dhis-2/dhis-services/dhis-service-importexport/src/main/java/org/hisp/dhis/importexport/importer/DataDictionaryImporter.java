package org.hisp.dhis.importexport.importer;

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

import org.amplecode.quick.BatchHandler;
import org.hisp.dhis.datadictionary.DataDictionary;
import org.hisp.dhis.datadictionary.DataDictionaryService;
import org.hisp.dhis.importexport.GroupMemberType;
import org.hisp.dhis.importexport.ImportParams;
import org.hisp.dhis.importexport.Importer;
import org.hisp.dhis.importexport.mapping.NameMappingUtil;

import java.util.List;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
public class DataDictionaryImporter
    extends AbstractImporter<DataDictionary> implements Importer<DataDictionary>
{
    protected DataDictionaryService dataDictionaryService;

    public DataDictionaryImporter()
    {
    }

    public DataDictionaryImporter( BatchHandler<DataDictionary> batchHandler, DataDictionaryService dataDictionaryService )
    {
        this.batchHandler = batchHandler;
        this.dataDictionaryService = dataDictionaryService;
    }

    @Override
    public void importObject( DataDictionary object, ImportParams params )
    {
        NameMappingUtil.addDataDictionaryMapping( object.getId(), object.getName() );

        read( object, GroupMemberType.NONE, params );
    }

    @Override
    protected void importUnique( DataDictionary object )
    {
        batchHandler.addObject( object );
    }

    @Override
    protected void importMatching( DataDictionary object, DataDictionary match )
    {
        match.setName( object.getName() );
        match.setDescription( object.getDescription() );
        match.setRegion( object.getRegion() );

        dataDictionaryService.saveDataDictionary( match );
    }

    @Override
    protected DataDictionary getMatching( DataDictionary object )
    {
        List<DataDictionary> dataDictionaryByName = dataDictionaryService.getDataDictionaryByName( object.getName() );
        return dataDictionaryByName.isEmpty() ? null : dataDictionaryByName.get( 0 );
    }

    @Override
    protected boolean isIdentical( DataDictionary object, DataDictionary existing )
    {
        if ( !object.getName().equals( existing.getName() ) )
        {
            return false;
        }
        if ( !isSimiliar( object.getDescription(), existing.getDescription() ) || (isNotNull( object.getDescription(), existing.getDescription() ) && !object.getDescription().equals( existing.getDescription() )) )
        {
            return false;
        }
        if ( !isSimiliar( object.getRegion(), existing.getRegion() ) || (isNotNull( object.getRegion(), existing.getRegion() ) && !object.getRegion().equals( existing.getRegion() )) )
        {
            return false;
        }

        return true;
    }
}

package org.hisp.dhis.jdbc.batchhandler;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.amplecode.quick.BatchHandler;
import org.amplecode.quick.BatchHandlerFactory;
import org.hisp.dhis.DhisTest;
import org.hisp.dhis.dataelement.DataElementGroup;
import org.hisp.dhis.dataelement.DataElementService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Lars Helge Overland
 * @version $Id: DataElementGroupBatchHandlerTest.java 4949 2008-04-21 07:59:54Z larshelg $
 */
public class DataElementGroupBatchHandlerTest
    extends DhisTest
{
    @Autowired
    private BatchHandlerFactory batchHandlerFactory;
    
    private DataElementService dataElementService;
    
    private BatchHandler<DataElementGroup> batchHandler;
    
    private DataElementGroup groupA;
    private DataElementGroup groupB;
    private DataElementGroup groupC;

    // -------------------------------------------------------------------------
    // Fixture
    // -------------------------------------------------------------------------

    @Override
    public void setUpTest()
    {
        dataElementService = (DataElementService) getBean( DataElementService.ID );
        
        batchHandler = batchHandlerFactory.createBatchHandler( DataElementGroupBatchHandler.class );

        batchHandler.init();
        
        groupA = createDataElementGroup( 'A' );
        groupB = createDataElementGroup( 'B' );
        groupC = createDataElementGroup( 'C' );
    }

    @Override
    public void tearDownTest()
    {
        batchHandler.flush();
    }
    
    @Override
    public boolean emptyDatabaseAfterTest()
    {
        return true;
    }
    
    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    public void testAddObject()
    {
        batchHandler.addObject( groupA );
        batchHandler.addObject( groupB );
        batchHandler.addObject( groupC );

        batchHandler.flush();
        
        Collection<DataElementGroup> groups = dataElementService.getAllDataElementGroups();
        
        assertTrue( groups.contains( groupA ) );
        assertTrue( groups.contains( groupB ) );
        assertTrue( groups.contains( groupC ) );
    }

    @Test
    public void testInsertObject()
    {
        int idA = batchHandler.insertObject( groupA, true );
        int idB = batchHandler.insertObject( groupB, true );
        int idC = batchHandler.insertObject( groupC, true );
        
        assertNotNull( dataElementService.getDataElementGroup( idA ) );
        assertNotNull( dataElementService.getDataElementGroup( idB ) );
        assertNotNull( dataElementService.getDataElementGroup( idC ) );
    }

    @Test
    public void testUpdateObject()
    {
        int id = batchHandler.insertObject( groupA, true );
        
        groupA.setId( id );
        groupA.setName( "UpdatedName" );
        
        batchHandler.updateObject( groupA );
        
        assertEquals( "UpdatedName", dataElementService.getDataElementGroup( id ).getName() );
    }

    @Test
    public void testGetObjectIdentifier()
    {
        int referenceId = dataElementService.addDataElementGroup( groupA );

        int retrievedId = batchHandler.getObjectIdentifier( "DataElementGroupA" );
        
        assertEquals( referenceId, retrievedId );
    }

    @Test
    public void testObjectExists()
    {
        dataElementService.addDataElementGroup( groupA );
        
        assertTrue( batchHandler.objectExists( groupA ) );
        
        assertFalse( batchHandler.objectExists( groupB ) );
    }
}

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
import org.hisp.dhis.indicator.IndicatorGroupSet;
import org.hisp.dhis.indicator.IndicatorService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
public class IndicatorGroupSetBatchHandlerTest
    extends DhisTest
{
    @Autowired
    private BatchHandlerFactory batchHandlerFactory;
    
    private BatchHandler<IndicatorGroupSet> batchHandler;
    
    private IndicatorGroupSet groupSetA;
    private IndicatorGroupSet groupSetB;
    private IndicatorGroupSet groupSetC;
    
    // -------------------------------------------------------------------------
    // Fixture
    // -------------------------------------------------------------------------

    @Override
    public void setUpTest()
    {
        indicatorService = (IndicatorService) getBean( IndicatorService.ID );
        
        batchHandler = batchHandlerFactory.createBatchHandler( IndicatorGroupSetBatchHandler.class );

        batchHandler.init();
        
        groupSetA = createIndicatorGroupSet( 'A' );
        groupSetB = createIndicatorGroupSet( 'B' );
        groupSetC = createIndicatorGroupSet( 'C' );
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
        batchHandler.addObject( groupSetA );
        batchHandler.addObject( groupSetB );
        batchHandler.addObject( groupSetC );

        batchHandler.flush();
        
        Collection<IndicatorGroupSet> groupSets = indicatorService.getAllIndicatorGroupSets();
        
        assertTrue( groupSets.contains( groupSetA ) );
        assertTrue( groupSets.contains( groupSetB ) );
        assertTrue( groupSets.contains( groupSetC ) );
    }

    @Test
    public void testInsertObject()
    {
        int idA = batchHandler.insertObject( groupSetA, true );
        int idB = batchHandler.insertObject( groupSetB, true );
        int idC = batchHandler.insertObject( groupSetC, true );
        
        assertNotNull( indicatorService.getIndicatorGroupSet( idA ) );
        assertNotNull( indicatorService.getIndicatorGroupSet( idB ) );
        assertNotNull( indicatorService.getIndicatorGroupSet( idC ) );
    }

    @Test
    public void testUpdateObject()
    {
        int id = batchHandler.insertObject( groupSetA, true );
        
        groupSetA.setId( id );
        groupSetA.setName( "UpdatedName" );
        
        batchHandler.updateObject( groupSetA );
        
        assertEquals( "UpdatedName", indicatorService.getIndicatorGroupSet( id ).getName() );
    }

    @Test
    public void testGetObjectIdentifier()
    {
        int referenceId = indicatorService.addIndicatorGroupSet( groupSetA );
        
        int retrievedId = batchHandler.getObjectIdentifier( "IndicatorGroupSetA" );
        
        assertEquals( referenceId, retrievedId );
    }

    @Test
    public void testObjectExists()
    {
        indicatorService.addIndicatorGroupSet( groupSetA );
        
        assertTrue( batchHandler.objectExists( groupSetA ) );
        
        assertFalse( batchHandler.objectExists( groupSetB ) );
    }
}

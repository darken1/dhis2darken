package org.hisp.dhis.dataelement;

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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.hisp.dhis.DhisSpringTest;
import org.junit.Test;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
public class DataElementCategoryOptionServiceTest
    extends DhisSpringTest
{
    private DataElementCategoryOption categoryOptionA;
    private DataElementCategoryOption categoryOptionB;
    private DataElementCategoryOption categoryOptionC;

    // -------------------------------------------------------------------------
    // Fixture
    // -------------------------------------------------------------------------

    @Override
    public void setUpTest()
    {
        categoryService = (DataElementCategoryService) getBean( DataElementCategoryService.ID );    
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    public void testAddGet()
    {
        categoryOptionA = new DataElementCategoryOption( "CategoryOptionA" );
        categoryOptionB = new DataElementCategoryOption( "CategoryOptionB" );
        categoryOptionC = new DataElementCategoryOption( "CategoryOptionC" );
        
        int idA = categoryService.addDataElementCategoryOption( categoryOptionA );
        int idB = categoryService.addDataElementCategoryOption( categoryOptionB );
        int idC = categoryService.addDataElementCategoryOption( categoryOptionC );
        
        assertEquals( categoryOptionA, categoryService.getDataElementCategoryOption( idA ) );
        assertEquals( categoryOptionB, categoryService.getDataElementCategoryOption( idB ) );
        assertEquals( categoryOptionC, categoryService.getDataElementCategoryOption( idC ) );
    }

    @Test
    public void testDelete()
    {
        categoryOptionA = new DataElementCategoryOption( "CategoryOptionA" );
        categoryOptionB = new DataElementCategoryOption( "CategoryOptionB" );
        categoryOptionC = new DataElementCategoryOption( "CategoryOptionC" );

        int idA = categoryService.addDataElementCategoryOption( categoryOptionA );
        int idB = categoryService.addDataElementCategoryOption( categoryOptionB );
        int idC = categoryService.addDataElementCategoryOption( categoryOptionC );
        
        assertNotNull( categoryService.getDataElementCategoryOption( idA ) );
        assertNotNull( categoryService.getDataElementCategoryOption( idB ) );
        assertNotNull( categoryService.getDataElementCategoryOption( idC ) );
        
        categoryService.deleteDataElementCategoryOption( categoryOptionA );

        assertNull( categoryService.getDataElementCategoryOption( idA ) );
        assertNotNull( categoryService.getDataElementCategoryOption( idB ) );
        assertNotNull( categoryService.getDataElementCategoryOption( idC ) );

        categoryService.deleteDataElementCategoryOption( categoryOptionB );

        assertNull( categoryService.getDataElementCategoryOption( idA ) );
        assertNull( categoryService.getDataElementCategoryOption( idB ) );
        assertNotNull( categoryService.getDataElementCategoryOption( idC ) );
    }

    @Test
    public void testGetAll()
    {
        categoryOptionA = new DataElementCategoryOption( "CategoryOptionA" );
        categoryOptionB = new DataElementCategoryOption( "CategoryOptionB" );
        categoryOptionC = new DataElementCategoryOption( "CategoryOptionC" );

        categoryService.addDataElementCategoryOption( categoryOptionA );
        categoryService.addDataElementCategoryOption( categoryOptionB );
        categoryService.addDataElementCategoryOption( categoryOptionC );
        
        Collection<DataElementCategoryOption> categoryOptions = categoryService.getAllDataElementCategoryOptions();
        
        assertEquals( 4, categoryOptions.size() ); // Including default
        assertTrue( categoryOptions.contains( categoryOptionA ) );
        assertTrue( categoryOptions.contains( categoryOptionB ) );
        assertTrue( categoryOptions.contains( categoryOptionC ) );        
    }
}

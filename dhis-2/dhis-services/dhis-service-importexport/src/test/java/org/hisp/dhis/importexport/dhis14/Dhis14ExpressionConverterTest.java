package org.hisp.dhis.importexport.dhis14;

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
import static org.hisp.dhis.dataelement.DataElement.AGGREGATION_OPERATOR_AVERAGE;
import static org.hisp.dhis.dataelement.DataElement.AGGREGATION_OPERATOR_SUM;
import static org.hisp.dhis.importexport.dhis14.util.Dhis14ExpressionConverter.convertExpressionFromDhis14;
import static org.hisp.dhis.importexport.dhis14.util.Dhis14ExpressionConverter.convertExpressionToDhis14;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
public class Dhis14ExpressionConverterTest
{
    private static final String dhis2Expression = "(([43.4]*1)+([53.4]*1)+([63.4]*1))";

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    public void testConvertFromDhis14()
    {
        Map<Object, Integer> mapping = new HashMap<Object, Integer>();
        mapping.put( 243, 43 );
        mapping.put( 253, 53 );
        mapping.put( 263, 63 );        
        
        String expression = "((Sum([R243])*1)+(Sum([R253])*1)+(Sum([S263])*1))";
        
        String actual = convertExpressionFromDhis14( expression, mapping, 4, "Indicator" );
        
        assertEquals( dhis2Expression, actual );
    }

    @Test
    public void testConvertToDhis14()
    {
        Map<Object, String> mapping = new HashMap<Object, String>();
        mapping.put( 43, AGGREGATION_OPERATOR_SUM );
        mapping.put( 53, AGGREGATION_OPERATOR_SUM );
        mapping.put( 63, AGGREGATION_OPERATOR_AVERAGE ); 

        String expected = "((Sum([R43])*1)+(Sum([R53])*1)+(Sum([S63])*1))";
        
        String actual = convertExpressionToDhis14( dhis2Expression, mapping );
        
        assertEquals( expected, actual );
    }
}

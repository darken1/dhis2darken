package org.hisp.dhis.importexport.mapping;

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

import java.util.Map;

import org.hisp.dhis.period.Period;

/**
 * This component is responsible for generating a mapping between the identifier
 * of an object prior to import and the identifier being auto-generated by the 
 * persistence medium during import. Setting the preview argument to true implies 
 * that no mapping takes place; simply returning the identifier prior to import.
 * 
 * @author Lars Helge Overland
 * @version $Id: ObjectMappingGenerator.java 6425 2008-11-22 00:08:57Z larshelg $
 */
public interface ObjectMappingGenerator
{
    String ID = ObjectMappingGenerator.class.getName();
    
    Map<Object, Integer> getConceptMapping( boolean skipMapping );
    
    Map<Object, Integer> getConstantMapping( boolean skipMapping );
    
    Map<Object, Integer> getCategoryMapping( boolean skipMapping );
    
    Map<Object, Integer> getCategoryComboMapping( boolean skipMapping );
    
    Map<Object, Integer> getCategoryOptionMapping( boolean skipMapping );

    Map<Object, Integer> getCategoryOptionComboMapping( boolean skipMapping );
        
    Map<Object, Integer> getDataElementMapping( boolean skipMapping );
    
    Map<Object, Integer> getDataElementGroupMapping( boolean skipMapping );
    
    Map<Object, Integer> getDataElementGroupSetMapping( boolean skipMapping );
    
    Map<Object, Integer> getIndicatorMapping( boolean skipMapping );

    Map<Object, Integer> getIndicatorGroupMapping( boolean skipMapping );
    
    Map<Object, Integer> getIndicatorGroupSetMapping( boolean skipMapping );
    
    Map<Object, Integer> getIndicatorTypeMapping( boolean skipMapping );
    
    Map<Object, Integer> getDataDictionaryMapping( boolean skipMapping );
    
    Map<Object, Integer> getDataSetMapping( boolean skipMapping );
    
    Map<Object, Integer> getOrganisationUnitMapping( boolean skipMapping );

    Map<Object, Integer> getOrganisationUnitGroupMapping( boolean skipMapping );
    
    Map<Object, Integer> getOrganisationUnitGroupSetMapping( boolean skipMapping );
    
    Map<Object, Integer> getReportTableMapping( boolean skipMapping );

    Map<Object, Integer> getPeriodMapping( boolean skipMapping );
    
    Map<Period, Integer> getPeriodObjectMapping( boolean skipMapping );
    
    Map<String, Integer> getPeriodTypeMapping();
}

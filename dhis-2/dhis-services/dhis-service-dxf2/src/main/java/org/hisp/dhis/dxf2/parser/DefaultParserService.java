package org.hisp.dhis.dxf2.parser;

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

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.hisp.dhis.dxf2.fieldfilter.FieldMap;
import org.hisp.dhis.dxf2.objectfilter.Filters;

import java.util.List;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class DefaultParserService implements ParserService
{
    @Override
    public Filters parseObjectFilter( List<String> filters )
    {
        Filters parsed = new Filters();

        for ( String filter : filters )
        {
            String[] split = filter.split( ":" );

            if ( !(split.length >= 2) )
            {
                continue;
            }

            if ( split.length >= 3 )
            {
                parsed.addFilter( split[0], split[1], split[2] );
            }
            else
            {
                parsed.addFilter( split[0], split[1], null );
            }
        }

        return parsed;
    }

    @Override
    public FieldMap parseFieldFilter( String fields )
    {
        List<String> prefixList = Lists.newArrayList();
        FieldMap fieldMap = new FieldMap();

        StringBuilder builder = new StringBuilder();

        for ( String c : fields.split( "" ) )
        {
            if ( c.equals( "," ) )
            {
                putInMap( fieldMap, joinedWithPrefix( builder, prefixList ) );
                builder = new StringBuilder();
                continue;
            }

            if ( c.equals( "[" ) )
            {
                prefixList.add( builder.toString() );
                builder = new StringBuilder();
                continue;
            }

            if ( c.equals( "]" ) )
            {
                if ( !builder.toString().isEmpty() )
                {
                    putInMap( fieldMap, joinedWithPrefix( builder, prefixList ) );
                }

                prefixList.remove( prefixList.size() - 1 );
                builder = new StringBuilder();
                continue;
            }

            if ( StringUtils.isAlpha( c ) || c.equals( "*" ) || c.equals( ":" ) || c.equals( "!" )
                || c.equals( "|" ) || c.equals( "{" ) || c.equals( "}" ) || c.equals( "(" ) || c.equals( ")" ) )
            {
                builder.append( c );
            }
        }

        if ( !builder.toString().isEmpty() )
        {
            putInMap( fieldMap, joinedWithPrefix( builder, prefixList ) );
        }

        return fieldMap;
    }

    private String joinedWithPrefix( StringBuilder builder, List<String> prefixList )
    {
        String prefixes = StringUtils.join( prefixList, "." );
        prefixes = prefixes.isEmpty() ? builder.toString() : (prefixes + "." + builder.toString());
        return prefixes;
    }

    @SuppressWarnings("unchecked")
    private void putInMap( FieldMap fieldMap, String path )
    {
        if ( StringUtils.isEmpty( path ) )
        {
            return;
        }

        for ( String p : path.split( "\\." ) )
        {
            if ( fieldMap.get( p ) == null )
            {
                fieldMap.put( p, new FieldMap() );
            }

            fieldMap = fieldMap.get( p );
        }
    }
}

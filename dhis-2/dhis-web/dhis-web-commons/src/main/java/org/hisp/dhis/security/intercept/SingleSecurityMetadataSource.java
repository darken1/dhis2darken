package org.hisp.dhis.security.intercept;

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

import java.util.Collection;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityMetadataSource;

/**
 * Generic SecurityMetadataSource for one single object.
 * 
 * @author Torgeir Lorange Ostby
 */
public class SingleSecurityMetadataSource
    implements SecurityMetadataSource
{
    private Object object;

    private Collection<ConfigAttribute> attributes;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public SingleSecurityMetadataSource( Object object )
    {
        this.object = object;
    }

    public SingleSecurityMetadataSource( Object object, Collection<ConfigAttribute> attributes )
    {
        this.object = object;   
        this.attributes = attributes;        
    }

    // -------------------------------------------------------------------------
    // SecurityMetadataSource implementation
    // -------------------------------------------------------------------------

    public Collection<ConfigAttribute> getAttributes( Object object )
        throws IllegalArgumentException
    {
        if ( !supports( object.getClass() ) )
        {
            throw new IllegalArgumentException( "Illegal type of object: " + object.getClass() );
        }

        if ( object.equals( this.object ) )
        {
            return attributes;
        }

        return null;
    }

    @Override
    public boolean supports( Class<?> clazz )
    {
        return clazz.isAssignableFrom( object.getClass() );
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes()
    {
        return this.attributes;
    }

}

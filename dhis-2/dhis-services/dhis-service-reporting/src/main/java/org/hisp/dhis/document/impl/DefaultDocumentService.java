package org.hisp.dhis.document.impl;

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

import org.hisp.dhis.common.GenericIdentifiableObjectStore;
import org.hisp.dhis.document.Document;
import org.hisp.dhis.document.DocumentService;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Lars Helge Overland
 * @version $Id$
 */
@Transactional
public class DefaultDocumentService
    implements DocumentService
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private GenericIdentifiableObjectStore<Document> documentStore;

    public void setDocumentStore( GenericIdentifiableObjectStore<Document> documentStore )
    {
        this.documentStore = documentStore;
    }

    // -------------------------------------------------------------------------
    // DocumentService implementation
    // -------------------------------------------------------------------------

    public int saveDocument( Document document )
    {
        return documentStore.save( document );
    }

    public Document getDocument( int id )
    {
        return documentStore.get( id );
    }

    public Document getDocument( String uid )
    {
        return documentStore.getByUid( uid );
    }

    public void deleteDocument( Document document )
    {
        documentStore.delete( document );
    }

    public List<Document> getAllDocuments()
    {
        return documentStore.getAll();
    }

    public List<Document> getDocumentByName( String name )
    {
        return documentStore.getAllEqName( name );
    }

    public int getDocumentCount()
    {
        return documentStore.getCount();
    }

    public int getDocumentCountByName( String name )
    {
        return documentStore.getCountLikeName( name );
    }

    public List<Document> getDocumentsBetween( int first, int max )
    {
        return documentStore.getAllOrderedName( first, max );
    }

    public List<Document> getDocumentsBetweenByName( String name, int first, int max )
    {
        return documentStore.getAllLikeNameOrderedName( name, first, max );
    }
    
    public List<Document> getDocumentsByUid( List<String> uids )
    {
        return documentStore.getByUid( uids );
    }
}

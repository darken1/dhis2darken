package org.hisp.dhis.validation;

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

import static org.hisp.dhis.i18n.I18nUtils.i18n;

import java.util.Collection;

import org.hisp.dhis.common.GenericNameableObjectStore;
import org.hisp.dhis.i18n.I18nService;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Chau Thu Tran
 * @version $Id$
 */
@Transactional
public class DefaultValidationCriteriaService
    implements ValidationCriteriaService
{
    // -------------------------------------------------------------------------
    // Dependency
    // -------------------------------------------------------------------------

    private GenericNameableObjectStore<ValidationCriteria> validationCriteriaStore;

    public void setValidationCriteriaStore( GenericNameableObjectStore<ValidationCriteria> validationCriteriaStore )
    {
        this.validationCriteriaStore = validationCriteriaStore;
    }

    private I18nService i18nService;

    public void setI18nService( I18nService service )
    {
        i18nService = service;
    }

    // -------------------------------------------------------------------------
    // ValidationCriteria implementation
    // -------------------------------------------------------------------------

    public int saveValidationCriteria( ValidationCriteria validationCriteria )
    {
        return validationCriteriaStore.save( validationCriteria );
    }

    public void updateValidationCriteria( ValidationCriteria validationCriteria )
    {
        validationCriteriaStore.update( validationCriteria );
    }

    public void deleteValidationCriteria( ValidationCriteria validationCriteria )
    {
        validationCriteriaStore.delete( validationCriteria );
    }

    public ValidationCriteria getValidationCriteria( int id )
    {
        return i18n( i18nService, validationCriteriaStore.get( id ) );
    }

    public Collection<ValidationCriteria> getAllValidationCriterias()
    {
        return i18n( i18nService, validationCriteriaStore.getAll() );
    }

    public ValidationCriteria getValidationCriteria( String name )
    {
        return i18n( i18nService, validationCriteriaStore.getByName( name ) );
    }

}

package org.hisp.dhis.trackedentity.action.dataentryform;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.dataentryform.DataEntryForm;
import org.hisp.dhis.dataentryform.DataEntryFormService;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.trackedentity.TrackedEntityForm;
import org.hisp.dhis.trackedentity.TrackedEntityFormService;

import com.opensymphony.xwork2.Action;

/**
 * @author Chau Thu Tran
 * 
 * @version SaveTrackedEntityFormAction.java 10:26:09 AM Jan 31, 2013 $
 */
public class SaveTrackedEntityFormAction
    implements Action
{
    Log logger = LogFactory.getLog( getClass() );

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private DataEntryFormService dataEntryFormService;

    public void setDataEntryFormService( DataEntryFormService dataEntryFormService )
    {
        this.dataEntryFormService = dataEntryFormService;
    }

    private ProgramService programService;

    public void setProgramService( ProgramService programService )
    {
        this.programService = programService;
    }

    private TrackedEntityFormService formService;

    public void setFormService( TrackedEntityFormService formService )
    {
        this.formService = formService;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    private String name;

    public void setName( String name )
    {
        this.name = name;
    }

    private String designTextarea;

    public void setDesignTextarea( String designTextarea )
    {
        this.designTextarea = designTextarea;
    }

    private Integer programId;

    public void setProgramId( Integer programId )
    {
        this.programId = programId;
    }

    private String message;

    public String getMessage()
    {
        return message;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
        TrackedEntityForm registrationForm = null;

        Program program = null;

        if ( programId == null )
        {
            registrationForm = formService.getFormsWithoutProgram();
        }
        else
        {
            program = programService.getProgram( programId );
            registrationForm = formService.getFormsWithProgram( program );
        }

        // ---------------------------------------------------------------------
        // Save data-entry-form
        // ---------------------------------------------------------------------

        if ( registrationForm == null )
        {
            registrationForm = new TrackedEntityForm();
            DataEntryForm dataEntryForm = new DataEntryForm( name, designTextarea );
            registrationForm.setDataEntryForm( dataEntryForm );
            if ( programId != null )
            {
                registrationForm.setProgram( program );
            }
            formService.saveTrackedEntityForm( registrationForm );
        }
        else
        {
            DataEntryForm dataEntryForm = registrationForm.getDataEntryForm();
            if ( dataEntryForm == null )
            {
                dataEntryForm = new DataEntryForm( name, designTextarea );
                dataEntryFormService.addDataEntryForm( dataEntryForm );
            }
            else
            {
                dataEntryForm.setName( name );
                dataEntryForm.setHtmlCode( designTextarea );
                dataEntryFormService.updateDataEntryForm( dataEntryForm );
            }
            registrationForm.setDataEntryForm( dataEntryForm );
            formService.updateTrackedEntityForm( registrationForm );
        }

        Integer dataEntryFormId = dataEntryFormService.getDataEntryFormByName( name ).getId();

        message = dataEntryFormId + "";

        return SUCCESS;
    }

}

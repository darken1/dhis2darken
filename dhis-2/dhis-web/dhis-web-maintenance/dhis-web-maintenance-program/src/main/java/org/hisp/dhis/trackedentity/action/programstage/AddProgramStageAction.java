package org.hisp.dhis.trackedentity.action.programstage;

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramService;
import org.hisp.dhis.program.ProgramStage;
import org.hisp.dhis.program.ProgramStageDataElement;
import org.hisp.dhis.program.ProgramStageDataElementService;
import org.hisp.dhis.program.ProgramStageService;
import org.hisp.dhis.trackedentity.TrackedEntityInstanceReminder;
import org.hisp.dhis.user.UserGroup;
import org.hisp.dhis.user.UserGroupService;

import com.opensymphony.xwork2.Action;

/**
 * @author Abyot Asalefew Gizaw
 * @modified Tran Thanh Tri
 * @version $Id$
 */
public class AddProgramStageAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private ProgramStageService programStageService;

    public void setProgramStageService( ProgramStageService programStageService )
    {
        this.programStageService = programStageService;
    }

    private ProgramService programService;

    public void setProgramService( ProgramService programService )
    {
        this.programService = programService;
    }

    private DataElementService dataElementService;

    public void setDataElementService( DataElementService dataElementService )
    {
        this.dataElementService = dataElementService;
    }

    private ProgramStageDataElementService programStageDataElementService;

    public void setProgramStageDataElementService( ProgramStageDataElementService programStageDataElementService )
    {
        this.programStageDataElementService = programStageDataElementService;
    }

    private UserGroupService userGroupService;

    public void setUserGroupService( UserGroupService userGroupService )
    {
        this.userGroupService = userGroupService;
    }

    // -------------------------------------------------------------------------
    // Input/Output
    // -------------------------------------------------------------------------

    private int id;

    public void setId( int id )
    {
        this.id = id;
    }

    public int getId()
    {
        return id;
    }

    private String name;

    public void setName( String name )
    {
        this.name = name;
    }

    private String description;

    public void setDescription( String description )
    {
        this.description = description;
    }

    private Integer minDaysFromStart;

    public void setMinDaysFromStart( Integer minDaysFromStart )
    {
        this.minDaysFromStart = minDaysFromStart;
    }

    private List<Integer> selectedDataElementsValidator = new ArrayList<Integer>();

    public void setSelectedDataElementsValidator( List<Integer> selectedDataElementsValidator )
    {
        this.selectedDataElementsValidator = selectedDataElementsValidator;
    }

    private List<Boolean> compulsories = new ArrayList<Boolean>();

    public void setCompulsories( List<Boolean> compulsories )
    {
        this.compulsories = compulsories;
    }

    private List<Boolean> allowProvidedElsewhere = new ArrayList<Boolean>();

    public void setAllowProvidedElsewhere( List<Boolean> allowProvidedElsewhere )
    {
        this.allowProvidedElsewhere = allowProvidedElsewhere;
    }

    private Boolean irregular;

    public void setIrregular( Boolean irregular )
    {
        this.irregular = irregular;
    }

    private Integer standardInterval;

    public void setStandardInterval( Integer standardInterval )
    {
        this.standardInterval = standardInterval;
    }

    private String reportDateDescription;

    public void setReportDateDescription( String reportDateDescription )
    {
        this.reportDateDescription = reportDateDescription;
    }

    private List<Integer> daysAllowedSendMessages = new ArrayList<Integer>();

    public void setDaysAllowedSendMessages( List<Integer> daysAllowedSendMessages )
    {
        this.daysAllowedSendMessages = daysAllowedSendMessages;
    }

    private List<String> templateMessages = new ArrayList<String>();

    public void setTemplateMessages( List<String> templateMessages )
    {
        this.templateMessages = templateMessages;
    }

    private List<Integer> sendTo = new ArrayList<Integer>();

    public void setSendTo( List<Integer> sendTo )
    {
        this.sendTo = sendTo;
    }

    private List<Integer> whenToSend = new ArrayList<Integer>();

    public void setWhenToSend( List<Integer> whenToSend )
    {
        this.whenToSend = whenToSend;
    }

    private List<Integer> messageType = new ArrayList<Integer>();

    public void setMessageType( List<Integer> messageType )
    {
        this.messageType = messageType;
    }

    private Boolean autoGenerateEvent;

    public void setAutoGenerateEvent( Boolean autoGenerateEvent )
    {
        this.autoGenerateEvent = autoGenerateEvent;
    }

    private List<Boolean> displayInReports = new ArrayList<Boolean>();

    public void setDisplayInReports( List<Boolean> displayInReports )
    {
        this.displayInReports = displayInReports;
    }

    private Boolean validCompleteOnly;

    public void setValidCompleteOnly( Boolean validCompleteOnly )
    {
        this.validCompleteOnly = validCompleteOnly;
    }

    private Boolean displayGenerateEventBox;

    public void setDisplayGenerateEventBox( Boolean displayGenerateEventBox )
    {
        this.displayGenerateEventBox = displayGenerateEventBox;
    }

    private Boolean captureCoordinates;

    public void setCaptureCoordinates( Boolean captureCoordinates )
    {
        this.captureCoordinates = captureCoordinates;
    }

    private List<Boolean> allowFutureDates;

    public void setAllowFutureDates( List<Boolean> allowFutureDates )
    {
        this.allowFutureDates = allowFutureDates;
    }

    private List<Integer> userGroup = new ArrayList<Integer>();

    public void setUserGroup( List<Integer> userGroup )
    {
        this.userGroup = userGroup;
    }

    private Boolean relatedEntityInstance;

    public void setRelatedEntityInstance( Boolean relatedEntityInstance )
    {
        this.relatedEntityInstance = relatedEntityInstance;
    }

    private Boolean generatedByEnrollmentDate;

    public void setGeneratedByEnrollmentDate( Boolean generatedByEnrollmentDate )
    {
        this.generatedByEnrollmentDate = generatedByEnrollmentDate;
    }

    private Boolean blockEntryForm;

    public void setBlockEntryForm( Boolean blockEntryForm )
    {
        this.blockEntryForm = blockEntryForm;
    }

    private Boolean remindCompleted = false;

    public void setRemindCompleted( Boolean remindCompleted )
    {
        this.remindCompleted = remindCompleted;
    }

    private Boolean allowGenerateNextVisit;

    public void setAllowGenerateNextVisit( Boolean allowGenerateNextVisit )
    {
        this.allowGenerateNextVisit = allowGenerateNextVisit;
    }

    private Boolean openAfterEnrollment;

    public void setOpenAfterEnrollment( Boolean openAfterEnrollment )
    {
        this.openAfterEnrollment = openAfterEnrollment;
    }

    private String reportDateToUse;

    public void setReportDateToUse( String reportDateToUse )
    {
        this.reportDateToUse = reportDateToUse;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
        minDaysFromStart = (minDaysFromStart == null) ? 0 : minDaysFromStart;
        irregular = (irregular == null) ? false : irregular;
        autoGenerateEvent = (autoGenerateEvent == null) ? false : autoGenerateEvent;
        validCompleteOnly = (validCompleteOnly == null) ? false : validCompleteOnly;
        displayGenerateEventBox = (displayGenerateEventBox == null) ? false : displayGenerateEventBox;
        captureCoordinates = (captureCoordinates == null) ? false : captureCoordinates;
        relatedEntityInstance = (relatedEntityInstance == null) ? false : relatedEntityInstance;
        generatedByEnrollmentDate = (generatedByEnrollmentDate == null) ? false : generatedByEnrollmentDate;
        blockEntryForm = (blockEntryForm == null) ? false : blockEntryForm;
        remindCompleted = (remindCompleted == null) ? false : remindCompleted;
        allowGenerateNextVisit = (allowGenerateNextVisit == null) ? false : allowGenerateNextVisit;
        openAfterEnrollment = (openAfterEnrollment == null) ? false : openAfterEnrollment;

        ProgramStage programStage = new ProgramStage();
        Program program = programService.getProgram( id );

        programStage.setName( name );
        programStage.setDescription( description );
        programStage.setProgram( program );
        programStage.setStandardInterval( standardInterval );
        programStage.setReportDateDescription( reportDateDescription );
        programStage.setIrregular( irregular );
        programStage.setMinDaysFromStart( minDaysFromStart );
        programStage.setDisplayGenerateEventBox( displayGenerateEventBox );
        programStage.setValidCompleteOnly( validCompleteOnly );
        if ( program.isSingleEvent() )
        {
            programStage.setAutoGenerateEvent( true );
        }
        else
        {
            programStage.setAutoGenerateEvent( autoGenerateEvent );
        }
        programStage.setCaptureCoordinates( captureCoordinates );
        programStage.setBlockEntryForm( blockEntryForm );
        programStage.setRemindCompleted( remindCompleted );
        programStage.setGeneratedByEnrollmentDate( generatedByEnrollmentDate );
        programStage.setAllowGenerateNextVisit( allowGenerateNextVisit );
        programStage.setOpenAfterEnrollment( openAfterEnrollment );
        programStage.setReportDateToUse( reportDateToUse );

        Set<TrackedEntityInstanceReminder> reminders = new HashSet<TrackedEntityInstanceReminder>();
        for ( int i = 0; i < daysAllowedSendMessages.size(); i++ )
        {
            TrackedEntityInstanceReminder reminder = new TrackedEntityInstanceReminder( "", daysAllowedSendMessages.get( i ),
                templateMessages.get( i ) );
            reminder.setDateToCompare( TrackedEntityInstanceReminder.DUE_DATE_TO_COMPARE );
            reminder.setName(program.getName() + "-" + name + "-" + i);
            reminder.setSendTo( sendTo.get( i ) );
            reminder.setWhenToSend( whenToSend.get( i ) );
            reminder.setMessageType( messageType.get( i ) );
            if ( sendTo.get( i ) == TrackedEntityInstanceReminder.SEND_TO_USER_GROUP )
            {
                UserGroup selectedUserGroup = userGroupService.getUserGroup( userGroup.get( i ) );
                reminder.setUserGroup( selectedUserGroup );
            }
            else
            {
                reminder.setUserGroup( null );
            }
            reminders.add( reminder );
        }
        programStage.setReminders( reminders );

        programStageService.saveProgramStage( programStage );

        for ( int i = 0; i < this.selectedDataElementsValidator.size(); i++ )
        {
            DataElement dataElement = dataElementService.getDataElement( selectedDataElementsValidator.get( i ) );
            Boolean allowed = allowProvidedElsewhere.get( i ) == null ? false : allowProvidedElsewhere.get( i );
            Boolean displayInReport = displayInReports.get( i ) == null ? false : displayInReports.get( i );
            Boolean allowDate = allowFutureDates.get( i ) == null ? false : allowFutureDates.get( i );

            ProgramStageDataElement programStageDataElement = new ProgramStageDataElement( programStage, dataElement,
                this.compulsories.get( i ), new Integer( i ) );
            programStageDataElement.setAllowProvidedElsewhere( allowed );
            programStageDataElement.setDisplayInReports( displayInReport );
            programStageDataElement.setAllowFutureDate( allowDate );
            programStageDataElementService.addProgramStageDataElement( programStageDataElement );
        }

        return SUCCESS;
    }
}
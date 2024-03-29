package org.hisp.dhis.trackedentity;

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
import java.util.Date;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;
import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramInstance;
import org.hisp.dhis.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValue;
import org.hisp.dhis.trackedentityattributevalue.TrackedEntityAttributeValueService;
import org.hisp.dhis.user.User;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Chau Thu Tran
 * 
 * @version DefaultTrackedEntityFormService.java 9:44:29 AM Jan 31, 2013 $
 */
@Transactional
public class DefaultTrackedEntityFormService
    implements TrackedEntityFormService
{
    private static final String TAG_OPEN = "<";

    private static final String TAG_CLOSE = "/>";

    private static final String PROGRAM_INCIDENT_DATE = "dateOfIncident";

    private static final String PROGRAM_ENROLLMENT_DATE = "enrollmentDate";

    private static final String DOB_FIELD = "@DOB_FIELD";

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private TrackedEntityFormStore formStore;

    public void setFormStore( TrackedEntityFormStore formStore )
    {
        this.formStore = formStore;
    }

    private TrackedEntityAttributeService attributeService;

    public void setAttributeService( TrackedEntityAttributeService attributeService )
    {
        this.attributeService = attributeService;
    }

    private TrackedEntityAttributeValueService attributeValueService;

    public void setAttributeValueService( TrackedEntityAttributeValueService attributeValueService )
    {
        this.attributeValueService = attributeValueService;
    }

    // -------------------------------------------------------------------------
    // TrackedEntityForm implementation
    // -------------------------------------------------------------------------

    @Override
    public int saveTrackedEntityForm( TrackedEntityForm registrationForm )
    {
        return formStore.save( registrationForm );
    }

    @Override
    public void deleteTrackedEntityForm( TrackedEntityForm registrationForm )
    {
        formStore.delete( registrationForm );
    }

    @Override
    public void updateTrackedEntityForm( TrackedEntityForm registrationForm )
    {
        formStore.update( registrationForm );
    }

    @Override
    public TrackedEntityForm getTrackedEntityForm( int id )
    {
        return formStore.get( id );
    }

    @Override
    public Collection<TrackedEntityForm> getAllTrackedEntityForms()
    {
        return formStore.getAll();
    }

    @Override
    public TrackedEntityForm getFormsWithProgram( Program program )
    {
        return formStore.get( program );
    }

    @Override
    public TrackedEntityForm getFormsWithoutProgram()
    {
        return formStore.getFormsWithoutProgram();
    }

    @Override
    public String prepareDataEntryFormForAdd( String htmlCode, Program program, Collection<User> healthWorkers,
        TrackedEntityInstance instance, ProgramInstance programInstance, I18n i18n, I18nFormat format )
    {
        int index = 1;

        StringBuffer sb = new StringBuffer();

        Matcher inputMatcher = INPUT_PATTERN.matcher( htmlCode );

        boolean hasBirthdate = false;
        boolean hasAge = false;

        while ( inputMatcher.find() )
        {
            // -----------------------------------------------------------------
            // Get HTML input field code
            // -----------------------------------------------------------------

            String inputHtml = inputMatcher.group();
            Matcher dynamicAttrMatcher = DYNAMIC_ATTRIBUTE_PATTERN.matcher( inputHtml );
            Matcher programMatcher = PROGRAM_PATTERN.matcher( inputHtml );

            index++;

            String hidden = "";
            String style = "";
            Matcher classMarcher = CLASS_PATTERN.matcher( inputHtml );
            if ( classMarcher.find() )
            {
                hidden = classMarcher.group( 2 );
            }

            Matcher styleMarcher = STYLE_PATTERN.matcher( inputHtml );
            if ( styleMarcher.find() )
            {
                style = styleMarcher.group( 2 );
            }

            if ( dynamicAttrMatcher.find() && dynamicAttrMatcher.groupCount() > 0 )
            {
                String uid = dynamicAttrMatcher.group( 1 );
                TrackedEntityAttribute attribute = attributeService.getTrackedEntityAttribute( uid );

                if ( attribute == null )
                {
                    inputHtml = "<input value='[" + i18n.getString( "missing_instance_attribute" ) + " " + uid
                        + "]' title='[" + i18n.getString( "missing_instance_attribute" ) + " " + uid + "]'>/";
                }
                else
                {
                    // Get value
                    String value = "";
                    if ( instance != null )
                    {
                        TrackedEntityAttributeValue attributeValue = attributeValueService
                            .getTrackedEntityAttributeValue( instance, attribute );
                        if ( attributeValue != null )
                        {
                            value = attributeValue.getValue();
                        }
                    }

                    inputHtml = getAttributeField( inputHtml, attribute, program, value, i18n, index, hidden, style );

                }

            }
            else if ( programMatcher.find() && programMatcher.groupCount() > 0 )
            {
                String property = programMatcher.group( 1 );

                // Get value
                String value = "";
                if ( programInstance != null )
                {
                    value = format.formatDate( ((Date) getValueFromProgram( StringUtils.capitalize( property ),
                        programInstance )) );
                }

                inputHtml = "<input id=\"" + property + "\" name=\"" + property + "\" tabindex=\"" + index
                    + "\" value=\"" + value + "\" " + TAG_CLOSE;
                if ( property.equals( PROGRAM_ENROLLMENT_DATE ) )
                {
                    if ( program != null && program.getSelectEnrollmentDatesInFuture() )
                    {
                        inputHtml += "<script>datePicker(\"" + property + "\", true);</script>";
                    }
                    else
                    {
                        inputHtml += "<script>datePickerValid(\"" + property + "\", true);</script>";
                    }
                }
                else if ( property.equals( PROGRAM_INCIDENT_DATE ) )
                {
                    if ( program != null && program.getSelectIncidentDatesInFuture() )
                    {
                        inputHtml += "<script>datePicker(\"" + property + "\", true);</script>";
                    }
                    else
                    {
                        inputHtml += "<script>datePickerValid(\"" + property + "\", true);</script>";
                    }
                }
            }

            inputMatcher.appendReplacement( sb, inputHtml );
        }

        inputMatcher.appendTail( sb );

        String entryForm = sb.toString();
        String dobType = "";
        if ( hasBirthdate && hasAge )
        {
            dobType = "<select id=\'dobType\' name=\"dobType\" style=\'width:120px\' onchange=\'dobTypeOnChange(\"instanceForm\")\' >";
            dobType += "<option value=\"V\" >" + i18n.getString( "verified" ) + "</option>";
            dobType += "<option value=\"D\" >" + i18n.getString( "declared" ) + "</option>";
            dobType += "<option value=\"A\" >" + i18n.getString( "approximated" ) + "</option>";
            dobType += "</select>";
        }
        else if ( hasBirthdate )
        {
            dobType = "<input type=\'hidden\' id=\'dobType\' name=\"dobType\" value=\'V\'>";
        }
        else if ( hasAge )
        {
            dobType = "<input type=\'hidden\' id=\'dobType\' name=\"dobType\" value=\'A\'>";
        }

        entryForm = entryForm.replaceFirst( DOB_FIELD, dobType );
        entryForm = entryForm.replaceAll( DOB_FIELD, "" );

        return entryForm;
    }

    // -------------------------------------------------------------------------
    // Supportive methods
    // -------------------------------------------------------------------------

    private String getAttributeField( String inputHtml, TrackedEntityAttribute attribute, Program program,
        String value, I18n i18n, int index, String hidden, String style )
    {
        boolean mandatory = false;
        boolean allowDateInFuture = false;

        if ( program != null && program.getAttribute( attribute ) != null )
        {
            ProgramTrackedEntityAttribute programAttribute = program.getAttribute( attribute );
            mandatory = programAttribute.isMandatory();
            allowDateInFuture = programAttribute.getAllowFutureDate();
        }

        inputHtml = TAG_OPEN + "input id=\"attr" + attribute.getId() + "\" name=\"attr" + attribute.getId()
            + "\" tabindex=\"" + index + "\" style=\"" + style + "\"";

        inputHtml += "\" class=\"" + hidden + " {validate:{required:" + mandatory;
        if ( TrackedEntityAttribute.TYPE_NUMBER.equals( attribute.getValueType() ) )
        {
            inputHtml += ",number:true";
        }
        else   if ( TrackedEntityAttribute.TYPE_PHONE_NUMBER.equals( attribute.getValueType() ) )
        {
            inputHtml += ",phone:true";
        }
        inputHtml += "}}\" ";


        if ( attribute.getValueType().equals( TrackedEntityAttribute.TYPE_PHONE_NUMBER ) )
        {
            inputHtml += " phoneNumber ";
        }
        else if ( attribute.getValueType().equals( TrackedEntityAttribute.TYPE_TRUE_ONLY ) )
        {
            inputHtml += " type='checkbox' value='true' ";
            if ( value.equals( "true" ) )
            {
                inputHtml += " checked ";
            }
        }
        else if ( attribute.getValueType().equals( TrackedEntityAttribute.TYPE_BOOL ) )
        {
            inputHtml = inputHtml.replaceFirst( "input", "select" ) + ">";

            if ( value.equals( "" ) )
            {
                inputHtml += "<option value=\"\" selected>" + i18n.getString( "no_value" ) + "</option>";
                inputHtml += "<option value=\"true\">" + i18n.getString( "yes" ) + "</option>";
                inputHtml += "<option value=\"false\">" + i18n.getString( "no" ) + "</option>";
            }
            else if ( value.equals( "true" ) )
            {
                inputHtml += "<option value=\"\">" + i18n.getString( "no_value" ) + "</option>";
                inputHtml += "<option value=\"true\" selected >" + i18n.getString( "yes" ) + "</option>";
                inputHtml += "<option value=\"false\">" + i18n.getString( "no" ) + "</option>";
            }
            else if ( value.equals( "false" ) )
            {
                inputHtml += "<option value=\"\">" + i18n.getString( "no_value" ) + "</option>";
                inputHtml += "<option value=\"true\">" + i18n.getString( "yes" ) + "</option>";
                inputHtml += "<option value=\"false\" selected >" + i18n.getString( "no" ) + "</option>";
            }

            inputHtml += "</select>";
        }
        else if ( attribute.getValueType().equals( TrackedEntityAttribute.TYPE_OPTION_SET ) )
        {
            inputHtml = inputHtml.replaceFirst( "input", "select" ) + ">";
            inputHtml += "<option value=\"\" selected>" + i18n.getString( "no_value" ) + "</option>";
            for ( String option : attribute.getOptionSet().getOptions() )
            {
                inputHtml += "<option value=\"" + option + "\" ";
                if ( option.equals( value ) )
                {
                    inputHtml += " selected ";
                }
                inputHtml += ">" + option + "</option>";
            }
            inputHtml += "</select>";
        }
        else if ( attribute.getValueType().equals( TrackedEntityAttribute.TYPE_DATE ) )
        {
            String jQueryCalendar = "<script>";
            if( allowDateInFuture ){
                jQueryCalendar += "datePicker";
            }
            else{
                jQueryCalendar += "datePickerValid";
            }
            jQueryCalendar += "(\"attr" + attribute.getId() + "\", false, false);</script>";
            
           inputHtml += " value=\"" + value + "\"" + TAG_CLOSE;
            inputHtml += jQueryCalendar;
        }
        else
        {
            inputHtml += " value=\"" + value + "\"" + TAG_CLOSE;
        }

        return inputHtml;
    }

    private Object getValueFromProgram( String property, ProgramInstance programInstance )
    {
        try
        {
            return ProgramInstance.class.getMethod( "get" + property ).invoke( programInstance );
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
        return null;
    }

}

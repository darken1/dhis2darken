package org.hisp.dhis.trackedentity.action.trackedentityattribute;

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
import java.util.Collection;
import java.util.List;

import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityAttributeService;

import com.opensymphony.xwork2.Action;

/**
 * @author Chau Thu Tran
 */
public class SaveAttributeInListNoProgramAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private TrackedEntityAttributeService attributeService;

    public void setAttributeService( TrackedEntityAttributeService attributeService )
    {
        this.attributeService = attributeService;
    }

    // -------------------------------------------------------------------------
    // Input/Output
    // -------------------------------------------------------------------------

    private List<Integer> selectedAttributeIds = new ArrayList<Integer>();

    public void setSelectedAttributeIds( List<Integer> selectedAttributeIds )
    {
        this.selectedAttributeIds = selectedAttributeIds;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
        Collection<TrackedEntityAttribute> attributes = attributeService.getAllTrackedEntityAttributes();

        int index = 1;
        
        if ( selectedAttributeIds != null )
        {
            for ( Integer objectId : selectedAttributeIds )
            {
                TrackedEntityAttribute attribute = attributeService.getTrackedEntityAttribute( objectId );
                attribute.setDisplayInListNoProgram( true );
                attribute.setSortOrderInListNoProgram( index );
                attributeService.updateTrackedEntityAttribute( attribute );
                index++;
                attributes.remove( attribute );
            }
        }

        // Set DisplayInListNoProgram=false for other attribute type
        for ( TrackedEntityAttribute attribute : attributes )
        {
            attribute.setDisplayInListNoProgram( false );
            attribute.setSortOrderInListNoProgram( 0 );
            attributeService.updateTrackedEntityAttribute( attribute );
        }

        return SUCCESS;
    }
}

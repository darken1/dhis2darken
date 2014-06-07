/*
 * Copyright (c) 2004-2013, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * * Neither the name of the HISP project nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
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

package org.hisp.dhis.caseentry.action.trackedentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.trackedentity.TrackedEntityAttributeService;
import org.hisp.dhis.trackedentity.comparator.TrackedEntityAttributeSortOrderComparator;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author Chau Thu Tran
 * 
 * @version $ GetVisitScheduleAttributesAction.java Apr 25, 2014 12:12:43 AM $
 */
public class GetVisitScheduleAttributesAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependency
    // -------------------------------------------------------------------------
    
    @Autowired
    private TrackedEntityAttributeService attributeService;
    
    // -------------------------------------------------------------------------
    // Getter && Setter
    // -------------------------------------------------------------------------
    
    private List<TrackedEntityAttribute> attributes = new ArrayList<TrackedEntityAttribute>();
    
    public List<TrackedEntityAttribute> getAttributes()
    {
        return attributes;
    }
    
    // -------------------------------------------------------------------------
    // Implementation Action
    // -------------------------------------------------------------------------
    
    @Override
    public String execute()
    {
        attributes = new ArrayList<TrackedEntityAttribute>(
            attributeService.getTrackedEntityAttributesByDisplayOnVisitSchedule( true ) );
        Collections.sort( attributes, new TrackedEntityAttributeSortOrderComparator() );
        
        return SUCCESS;
    }
}

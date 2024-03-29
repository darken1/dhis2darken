package org.hisp.dhis.dataapproval;

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

/**
 * "Base", or simplified, state of data approval for a given data selection.
 *
 * @author Jim Grace
 */
public enum DataApprovalBaseState
{
    /**
     * Data approval does not apply to this selection. (Data is neither
     * "approved" nor "unapproved".)
     */
    UNAPPROVABLE,

    /**
     * Data is unapproved, and is not ready to be approved for this selection.
     */
    UNAPPROVED_NOT_READY,

    /**
     * Data is unapproved, and is ready to be approved for this selection.
     */
    UNAPPROVED_READY,

    /**
     * Data is approved for some but not all periods inside this longer period
     * and is ready for approval in all periods inside this containing period.
     */
    PARTIALLY_APPROVED,

    /**
     * Data is approved (either here or elsewhere).
     */
    APPROVED,

    /**
     * Data is accepted for some but not all periods inside this longer period
     * and is ready for accepting in all periods inside this containing period.
     */
    PARTIALLY_ACCEPTED,

    /**
     * Data is approved and accepted (either here or elsewhere).
     */
    ACCEPTED;
}

package org.hisp.dhis.mobile.action.incoming;

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
import java.util.Iterator;
import java.util.List;

import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.paging.ActionPagingSupport;
import org.hisp.dhis.sms.config.ModemGatewayConfig;
import org.hisp.dhis.sms.config.SmsConfigurationManager;
import org.hisp.dhis.sms.incoming.IncomingSms;
import org.hisp.dhis.sms.incoming.IncomingSmsService;
import org.hisp.dhis.sms.incoming.SmsMessageStatus;
import org.hisp.dhis.user.CurrentUserService;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Nguyen Kim Lai
 */
public class ReceivingSMSAction
    extends ActionPagingSupport<IncomingSms>
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private IncomingSmsService incomingSmsService;

    public void setIncomingSmsService( IncomingSmsService incomingSmsService )
    {
        this.incomingSmsService = incomingSmsService;
    }

    private I18n i18n;

    public void setI18n( I18n i18n )
    {
        this.i18n = i18n;
    }
    
    private UserService userService;

    public void setUserService( UserService userService )
    {
        this.userService = userService;
    }
    
    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    @Autowired
    private SmsConfigurationManager smsConfigurationManager;

    // -------------------------------------------------------------------------
    // Input & Output
    // -------------------------------------------------------------------------

    private List<IncomingSms> listIncomingSms = new ArrayList<IncomingSms>();

    public List<IncomingSms> getListIncomingSms()
    {
        return listIncomingSms;
    }

    private String message;

    public String getMessage()
    {
        return message;
    }

    private Integer pollingInterval;

    public Integer getPollingInterval()
    {
        return pollingInterval;
    }

    private String smsStatus;

    public String getSmsStatus()
    {
        return smsStatus;
    }

    public void setSmsStatus( String smsStatus )
    {
        this.smsStatus = smsStatus;
    }

    private String keyword;

    public String getKeyword()
    {
        return keyword;
    }

    public void setKeyword( String keyword )
    {
        this.keyword = keyword;
    }
    
    private List<String> senderNames;

    public List<String> getSenderNames()
    {
        return senderNames;
    }

    private Integer total;

    public Integer getTotal()
    {
        return total;
    }
    
    private User user;
    
    public User getUser()
    {
        return user;
    }

    // -------------------------------------------------------------------------
    // Action Implementation
    // -------------------------------------------------------------------------
    @Override
    public String execute()
        throws Exception
    {
        this.user = currentUserService.getCurrentUser();
        
        ModemGatewayConfig gatewayConfig = (ModemGatewayConfig) smsConfigurationManager
            .checkInstanceOfGateway( ModemGatewayConfig.class );

        if ( gatewayConfig != null )
        {
            pollingInterval = gatewayConfig.getPollingInterval() * 1000;
        }
        else
        {
            pollingInterval = 4000;
        }
        listIncomingSms = incomingSmsService.listAllMessageFromModem();

        if ( listIncomingSms.size() > 0 )
        {
            for ( IncomingSms each : listIncomingSms )
            {
                incomingSmsService.save( each );
            }

            message = i18n.getString( "new_message" );

            incomingSmsService.deleteAllFromModem();
        }

        if ( keyword == null )
        {
            keyword = "";
        }

        if ( smsStatus == null || smsStatus.trim().equals( "" ) )
        {
            total = incomingSmsService.getSmsByStatus( null, keyword.trim() ).size();

            this.paging = createPaging( total );

            listIncomingSms = new ArrayList<IncomingSms>( incomingSmsService.getSmsByStatus( null, keyword,
                this.paging.getStartPos(), this.paging.getPageSize() ) );
        }
        else
        {
            SmsMessageStatus[] statusArray = SmsMessageStatus.values();

            for ( int i = 0; i < statusArray.length; i++ )
            {
                if ( statusArray[i].toString().equalsIgnoreCase( smsStatus ) )
                {
                    total = incomingSmsService.getSmsByStatus( statusArray[i], keyword ).size();

                    this.paging = createPaging( total );

                    listIncomingSms = new ArrayList<IncomingSms>( incomingSmsService.getSmsByStatus( statusArray[i],
                        keyword.trim(), this.paging.getStartPos(), this.paging.getPageSize() ) );

                    break;
                }
            }
        }
        
        // Get the name of senders
        senderNames = new ArrayList<String>();
        senderNames.add( "" );
        String tempString;
        for ( IncomingSms incomingSms : listIncomingSms )
        {
            tempString = "";
            String phoneNumber = incomingSms.getOriginator();
            if ( !phoneNumber.isEmpty() )
            {
                Collection<User> users = userService.getUsersByPhoneNumber( phoneNumber );
                if ( users == null || users.size() == 0 )
                {
                    tempString += "[unknown]";
                }
                else if ( users.size() > 0 )
                {

                    Iterator<User> usersIterator = users.iterator();
                    while ( usersIterator.hasNext() )
                    {
                        User user = usersIterator.next();
                        tempString += "[" + user.getUsername() + "]";
                    }
                }
            }
            else
            {
                tempString += "[unknown]";
            }
            senderNames.add( tempString );
        }
        
        return SUCCESS;
    }
}

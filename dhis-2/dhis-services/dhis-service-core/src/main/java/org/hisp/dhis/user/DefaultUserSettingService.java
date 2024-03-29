package org.hisp.dhis.user;

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

import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Torgeir Lorange Ostby
 * @version $Id: DefaultUserSettingService.java 5724 2008-09-18 14:37:01Z larshelg $
 */
@Transactional
public class DefaultUserSettingService
    implements UserSettingService
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    private UserService userService;

    public void setUserService( UserService userService )
    {
        this.userService = userService;
    }

    // -------------------------------------------------------------------------
    // UserSettingService implementation
    // -------------------------------------------------------------------------

    public void saveUserSetting( String name, Serializable value )
    {
        User currentUser = currentUserService.getCurrentUser();
        
        save( name, value, currentUser );
    }

    public void saveUserSetting( String name, Serializable value, String username )
    {
        UserCredentials credentials = userService.getUserCredentialsByUsername( username );
        
        if ( credentials != null )
        {        
            save( name, value, credentials.getUser() );
        }
    }

    private void save( String name, Serializable value, User user )
    {
        if ( user == null )
        {
            return;
        }

        UserSetting userSetting = userService.getUserSetting( user, name );

        if ( userSetting == null )
        {
            userSetting = new UserSetting();
            userSetting.setUser( user );
            userSetting.setName( name );
            userSetting.setValue( value );

            userService.addUserSetting( userSetting );
        }
        else
        {
            userSetting.setValue( value );

            userService.updateUserSetting( userSetting );
        }
    }

    public Serializable getUserSetting( String name )
    {
        User currentUser = currentUserService.getCurrentUser();
        return getUserSetting(name, currentUser);
    }


    public Serializable getUserSetting( String name, String username )
    {
        UserCredentials credentials = userService.getUserCredentialsByUsername( username );
        
        return getUserSetting( name, credentials == null ? null : credentials.getUser() );
    }

    private Serializable getUserSetting( String name, User currentUser ) 
    {
        if ( currentUser == null )
        {
            return null;
        }

        UserSetting userSetting = userService.getUserSetting( currentUser, name );

        if ( userSetting != null )
        {
            return userSetting.getValue();
        }

        return null;
    }

    public Serializable getUserSetting( String name, Serializable defaultValue )
    {
        User currentUser = currentUserService.getCurrentUser();

        if ( currentUser == null )
        {
            return defaultValue;
        }

        UserSetting userSetting = userService.getUserSetting( currentUser, name );

        if ( userSetting != null )
        {
            return userSetting.getValue();
        }

        return defaultValue;
    }

    public Collection<UserSetting> getAllUserSettings()
    {
        User currentUser = currentUserService.getCurrentUser();

        if ( currentUser == null )
        {
            return Collections.emptySet();
        }

        return userService.getAllUserSettings( currentUser );
    }

    public void deleteUserSetting( String name )
    {
        User currentUser = currentUserService.getCurrentUser();

        if ( currentUser != null )
        {
            userService.deleteUserSetting( userService.getUserSetting( currentUser, name ) );
        }
    }
}

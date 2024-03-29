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

import org.hisp.dhis.DhisSpringTest;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;

import static org.junit.Assert.assertEquals;

/**
 * @author Kiran Prakash
 */
@Ignore
public class UserSettingServiceTest
    extends DhisSpringTest
{
    @Autowired
    private UserSettingService userSettingService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserCredentialsStore userCredentialStore;

    private User testUser;

    @Override
    protected void setUpTest()
        throws Exception
    {
        testUser = createUser( 'D' );
        userService.addUser( testUser );
        UserCredentials userCredentials = testUser.getUserCredentials();
        userCredentials.setUser( testUser );
        userCredentialStore.addUserCredentials( userCredentials );
    }

    @Test
    public void testSaveUserPreferences()
        throws Exception
    {
        userSettingService.saveUserSetting( "mykey", "myvalue", "username" );
        UserSetting setting = userCredentialStore.getUserSetting( testUser, "mykey" );
        assertEquals( "myvalue", setting.getValue() );
        assertEquals( "mykey", setting.getName() );
    }

   @Test
   public void testShouldGetUserSettings() {
       UserCredentials userCredentials = testUser.getUserCredentials();
       userCredentials.setUser( testUser );
       userCredentialStore.addUserCredentials( userCredentials );
       userSettingService.saveUserSetting("mykey", "value", "username");
       Serializable preference = userSettingService.getUserSetting("mykey", "username");
       assertEquals(preference, "value");
   }
}

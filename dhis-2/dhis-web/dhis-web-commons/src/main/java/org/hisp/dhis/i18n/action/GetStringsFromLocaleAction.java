package org.hisp.dhis.i18n.action;

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

import com.opensymphony.xwork2.Action;
import org.hisp.dhis.i18n.I18n;
import org.hisp.dhis.i18n.I18nManager;
import org.hisp.dhis.setting.TranslateSystemSettingManager;
import org.hisp.dhis.system.util.LocaleUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

/**
 * @author Lars Helge Overland
 */
public class GetStringsFromLocaleAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependency
    // -------------------------------------------------------------------------
    @Autowired
    private I18nManager manager;

    @Autowired
    private TranslateSystemSettingManager translateSystemSettingManager;

    // -------------------------------------------------------------------------
    // Input
    // -------------------------------------------------------------------------
    private String loc;

    public void setLoc( String loc )
    {
        this.loc = loc;
    }
    
    // -------------------------------------------------------------------------
    // Output
    // -------------------------------------------------------------------------

    private Map<String, String> translations = new Hashtable<String, String>();

    public Map<String, String> getTranslations()
    {
        return translations;
    }

    private I18n i18nObject;

    public I18n getI18nObject()
    {
        return i18nObject;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
        if ( loc != null )
        {
            Locale locale = LocaleUtils.getLocale( loc );
    
            i18nObject = manager.getI18n( this.getClass(), locale );
        
            translations = translateSystemSettingManager.getTranslationSystemAppearanceSettings( loc );
        }
        
        return SUCCESS;
    }
}

package org.hisp.dhis.settings.user.action;

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

import static org.hisp.dhis.user.UserSettingService.DEFAULT_ANALYSIS_DISPLAY_PROPERTY;
import static org.hisp.dhis.user.UserSettingService.KEY_ANALYSIS_DISPLAY_PROPERTY;
import static org.hisp.dhis.user.UserSettingService.KEY_MESSAGE_EMAIL_NOTIFICATION;
import static org.hisp.dhis.user.UserSettingService.KEY_MESSAGE_SMS_NOTIFICATION;

import java.util.List;
import java.util.Locale;
import java.util.SortedMap;

import org.hisp.dhis.i18n.I18nService;
import org.hisp.dhis.i18n.locale.LocaleManager;
import org.hisp.dhis.setting.StyleManager;
import org.hisp.dhis.user.UserSettingService;

import com.opensymphony.xwork2.Action;

/**
 * @author Chau Thu Tran
 */
public class GetGeneralSettingsAction
    implements Action
{
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private I18nService i18nService;

    public void setI18nService( I18nService i18nService )
    {
        this.i18nService = i18nService;
    }

    private LocaleManager localeManager;

    public void setLocaleManager( LocaleManager localeManager )
    {
        this.localeManager = localeManager;
    }

    private UserSettingService userSettingService;

    public void setUserSettingService( UserSettingService userSettingService )
    {
        this.userSettingService = userSettingService;
    }

    private StyleManager styleManager;

    public void setStyleManager( StyleManager styleManager )
    {
        this.styleManager = styleManager;
    }

    // -------------------------------------------------------------------------
    // Output
    // -------------------------------------------------------------------------

    private List<Locale> availableLocales;

    public List<Locale> getAvailableLocales()
    {
        return availableLocales;
    }

    private Locale currentLocale;

    public Locale getCurrentLocale()
    {
        return currentLocale;
    }

    private List<Locale> availableLocalesDb;

    public List<Locale> getAvailableLocalesDb()
    {
        return availableLocalesDb;
    }

    private Locale currentLocaleDb;

    public Locale getCurrentLocaleDb()
    {
        return currentLocaleDb;
    }

    private String currentStyle;

    public String getCurrentStyle()
    {
        return currentStyle;
    }

    private SortedMap<String, String> styles;

    public SortedMap<String, String> getStyles()
    {
        return styles;
    }

    private String analysisDisplayProperty;

    public String getAnalysisDisplayProperty()
    {
        return analysisDisplayProperty;
    }

    private Boolean messageEmailNotification;

    public Boolean getMessageEmailNotification()
    {
        return messageEmailNotification;
    }

    private Boolean messageSmsNotification;

    public Boolean getMessageSmsNotification()
    {
        return messageSmsNotification;
    }

    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute()
        throws Exception
    {
        // ---------------------------------------------------------------------
        // Get available UI locales
        // ---------------------------------------------------------------------

        availableLocales = localeManager.getAvailableLocales();

        currentLocale = localeManager.getCurrentLocale();
        
        // ---------------------------------------------------------------------
        // Get available DB locales
        // ---------------------------------------------------------------------

        availableLocalesDb = i18nService.getAvailableLocales();

        currentLocaleDb = i18nService.getCurrentLocale();

        // ---------------------------------------------------------------------
        // Get styles
        // ---------------------------------------------------------------------

        styles = styleManager.getStyles();

        currentStyle = styleManager.getCurrentStyle();

        analysisDisplayProperty = (String) userSettingService.getUserSetting( KEY_ANALYSIS_DISPLAY_PROPERTY,
            DEFAULT_ANALYSIS_DISPLAY_PROPERTY );

        messageEmailNotification = (Boolean) userSettingService.getUserSetting( KEY_MESSAGE_EMAIL_NOTIFICATION, false );

        messageSmsNotification = (Boolean) userSettingService.getUserSetting( KEY_MESSAGE_SMS_NOTIFICATION, false );

        return SUCCESS;
    }
}

package org.hisp.dhis.webapi.controller;

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

import org.hisp.dhis.analytics.partition.PartitionManager;
import org.hisp.dhis.cache.HibernateCacheManager;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.maintenance.MaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Lars Helge Overland
 */
@Controller
@RequestMapping( value = MaintenanceController.RESOURCE_PATH )
public class MaintenanceController
{
    public static final String RESOURCE_PATH = "/maintenance";

    @Autowired
    private MaintenanceService maintenanceService;
    
    @Autowired
    private DataElementCategoryService categoryService;

    @Autowired
    private HibernateCacheManager cacheManager;

    @Autowired
    private PartitionManager partitionManager;    
    
    @RequestMapping( value = "/periodPruning", method = { RequestMethod.PUT, RequestMethod.POST } )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_PERFORM_MAINTENANCE')" )
    public void prunePeriods()
    {
        maintenanceService.prunePeriods();
    }

    @RequestMapping( value = "/zeroDataValueRemoval", method = { RequestMethod.PUT, RequestMethod.POST } )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_PERFORM_MAINTENANCE')" )
    public void deleteZeroDataValues()
    {
        maintenanceService.deleteZeroDataValues();
    }

    @RequestMapping( value = "/categoryOptionComboUpdate", method = { RequestMethod.PUT, RequestMethod.POST } )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_PERFORM_MAINTENANCE')" )
    public void updateCategoryOptionCombos()
    {
        categoryService.updateAllOptionCombos();
    }

    @RequestMapping( value = "/cacheClearing", method = { RequestMethod.PUT, RequestMethod.POST } )
    @PreAuthorize( "hasRole('ALL') or hasRole('F_PERFORM_MAINTENANCE')" )
    public void clearCache()
    {
        cacheManager.clearCache();
        partitionManager.clearCaches();
    }    
}

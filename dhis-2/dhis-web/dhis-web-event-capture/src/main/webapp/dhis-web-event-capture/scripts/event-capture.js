
dhis2.util.namespace('dhis2.ec');

// whether current user has any organisation units
dhis2.ec.emptyOrganisationUnits = false;

// Instance of the StorageManager
dhis2.ec.storageManager = new StorageManager();

var EC_STORE_NAME = "dhis2ec";
var i18n_no_orgunits = 'No organisation unit attached to current user, no data entry possible';
var i18n_offline_notification = 'You are offline, data will be stored locally';
var i18n_online_notification = 'You are online';
var i18n_need_to_sync_notification = 'There is data stored locally, please upload to server';
var i18n_sync_now = 'Upload';
var i18n_sync_success = 'Upload to server was successful';
var i18n_sync_failed = 'Upload to server failed, please try again later';
var i18n_uploading_data_notification = 'Uploading locally stored data to the server';

var PROGRAMS_METADATA = 'EVENT_PROGRAMS';

var EVENT_VALUES = 'EVENT_VALUES';

dhis2.ec.store = new dhis2.storage.Store({
    name: EC_STORE_NAME,
    adapters: [dhis2.storage.IndexedDBAdapter, dhis2.storage.DomSessionStorageAdapter, dhis2.storage.InMemoryAdapter],
    objectStores: ['eventCapturePrograms', 'programStages', 'optionSets']
});

(function($) {
    $.safeEach = function(arr, fn)
    {
        if (arr)
        {
            $.each(arr, fn);
        }
    };
})(jQuery);


/**
 * Page init. The order of events is:
 *
 * 1. Load ouwt 2. Load meta-data (and notify ouwt) 3. Check and potentially
 * download updated forms from server
 */
$(document).ready(function()
{
    $.ajaxSetup({
        type: 'POST',
        cache: false
    });

    $('#loaderSpan').show();

    $('#orgUnitTree').one('ouwtLoaded', function()
    {        
        var def = $.Deferred();
        var promise = def.promise();
        
        promise = promise.then( dhis2.ec.store.open );
        promise = promise.then( getUserProfile );     
        promise = promise.then( getMetaPrograms );     
        promise = promise.then( getPrograms );     
        promise = promise.then( getProgramStages );    
        promise = promise.then( getOptionSets );    
        promise.done( function() {           
            selection.responseReceived();            
        });           
        
        def.resolve();
        
    });

    $(document).bind('dhis2.online', function(event, loggedIn)
    {        
        if (loggedIn)
        {
            if (dhis2.ec.storageManager.hasLocalData())
            {
                var message = i18n_need_to_sync_notification
                        + ' <button id="sync_button" type="button">' + i18n_sync_now + '</button>';

                setHeaderMessage(message);

                $('#sync_button').bind('click', uploadLocalData);
            }
            else
            {
                if (dhis2.ec.emptyOrganisationUnits) {
                    setHeaderMessage(i18n_no_orgunits);
                }
                else {
                    setHeaderDelayMessage(i18n_online_notification);
                }
            }
        }
        else
        {
            var form = [
                '<form style="display:inline;">',
                '<label for="username">Username</label>',
                '<input name="username" id="username" type="text" style="width: 70px; margin-left: 10px; margin-right: 10px" size="10"/>',
                '<label for="password">Password</label>',
                '<input name="password" id="password" type="password" style="width: 70px; margin-left: 10px; margin-right: 10px" size="10"/>',
                '<button id="login_button" type="button">Login</button>',
                '</form>'
            ].join('');

            setHeaderMessage(form);
            ajax_login();
        }
    });

    $(document).bind('dhis2.offline', function()
    {
        if (dhis2.ec.emptyOrganisationUnits) {
            setHeaderMessage(i18n_no_orgunits);
        }
        else {
            setHeaderMessage(i18n_offline_notification);
            selection.responseReceived(); //notify angular 
        }
    });
   
    dhis2.availability.startAvailabilityCheck();
    
});

function ajax_login()
{
    $('#login_button').bind('click', function()
    {
        var username = $('#username').val();
        var password = $('#password').val();

        $.post('../dhis-web-commons-security/login.action', {
            'j_username': username,
            'j_password': password
        }).success(function()
        {
            var ret = dhis2.availability.syncCheckAvailability();

            if (!ret)
            {
                alert(i18n_ajax_login_failed);
            }
        });
    });
}


function getUserProfile()
{
    var def = $.Deferred();

    $.ajax({
        url: '../api/me/profile',
        type: 'GET'
    }).done( function(response) {            
        localStorage['USER_PROFILE'] = JSON.stringify(response);           
        def.resolve();
    });
    
    return def.promise(); 
}

function getMetaPrograms()
{
    var def = $.Deferred();

    $.ajax({
        url: '../api/programs.json',
        type: 'GET',
        data:'type=3&paging=false&fields=id,name,version,programStages[id,version,programStageDataElements[dataElement[id,optionSet[id,version]]]]'
    }).done( function(response) {          
        var programs = [];
        _.each( _.values( response.programs ), function ( program ) { 
            if( program.programStages &&
                program.programStages.length &&
                program.programStages[0].programStageDataElements &&
                program.programStages[0].programStageDataElements.length ) {
            
                programs.push(program);
            }  
            
        });
        
        def.resolve( programs );
    });
    
    return def.promise(); 
}

function getPrograms( programs )
{
    if( !programs ){
        return;
    }
    
    var mainDef = $.Deferred();
    var mainPromise = mainDef.promise();

    var def = $.Deferred();
    var promise = def.promise();

    var builder = $.Deferred();
    var build = builder.promise();

    _.each( _.values( programs ), function ( program ) {
        build = build.then(function() {
            var d = $.Deferred();
            var p = d.promise();
            dhis2.ec.store.get('eventCapturePrograms', program.id).done(function(obj) {
                if(!obj || obj.version !== program.version) {
                    promise = promise.then( getProgram( program.id ) );
                }

                d.resolve();
            });

            return p;
        });
    });

    build.done(function() {
        def.resolve();

        promise = promise.done( function () {
            mainDef.resolve( programs );
        } );
    });

    builder.resolve();

    return mainPromise;
}

function getProgram( id )
{
    return function() {
        return $.ajax( {
            url: '../api/programs.json?filter=id:eq:' + id +'&fields=id,name,version,dateOfEnrollmentDescription,dateOfIncidentDescription,displayIncidentDate,ignoreOverdueEvents,organisationUnits[id,name],programStages[id,name,version]',
            type: 'GET'
        }).done( function( response ){
            
            _.each( _.values( response.programs ), function ( program ) { 
                
                var ou = {};
                _.each(_.values( program.organisationUnits), function(o){
                    ou[o.id] = o.name;
                });

                program.organisationUnits = ou;

                var ur = {};
                _.each(_.values( program.userRoles), function(u){
                    ur[u.id] = u.name;
                });

                program.userRoles = ur;

                dhis2.ec.store.set( 'eventCapturePrograms', program );

            });         
        });
    };
}

function getProgramStages( programs )
{
    if( !programs ){
        return;
    }
    
    var mainDef = $.Deferred();
    var mainPromise = mainDef.promise();

    var def = $.Deferred();
    var promise = def.promise();

    var builder = $.Deferred();
    var build = builder.promise();

    _.each( _.values( programs ), function ( program ) {

        build = build.then(function() {
            var d = $.Deferred();
            var p = d.promise();
            dhis2.ec.store.get('programStages', program.programStages[0].id).done(function(obj) {
                if(!obj || obj.version !== program.programStages[0].version) {
                    promise = promise.then( getProgramStage( program.programStages[0].id ) );
                }

                d.resolve();
            });

            return p;
        });
              
    });

    build.done(function() {
        def.resolve();

        promise = promise.done( function () {
            mainDef.resolve( programs );
        } );
    });

    builder.resolve();

    return mainPromise;    
}

function getProgramStage( id )
{
    return function() {
        return $.ajax( {
            url: '../api/programStages.json?filter=id:eq:' + id +'&fields=id,name,version,description,reportDateDescription,captureCoordinates,dataEntryForm,minDaysFromStart,repeatable,programStageDataElements[displayInReports,allowProvidedElsewhere,allowDateInFuture,compulsory,dataElement[id,name,type,optionSet[id]]]',
            type: 'GET'
        }).done( function( response ){            
            _.each( _.values( response.programStages ), function( programStage ) {                
                dhis2.ec.store.set( 'programStages', programStage );
            });
        });
    };
}

function getOptionSets( programs )
{
    if( !programs ){
        return;
    }
    
    var mainDef = $.Deferred();
    var mainPromise = mainDef.promise();

    var def = $.Deferred();
    var promise = def.promise();

    var builder = $.Deferred();
    var build = builder.promise();    

    _.each( _.values( programs ), function ( program ) {
        _.each(_.values( program.programStages[0].programStageDataElements), function(prStDe){
            if( prStDe.dataElement.optionSet && prStDe.dataElement.optionSet.id ){
                build = build.then(function() {
                    var d = $.Deferred();
                    var p = d.promise();
                    dhis2.ec.store.get('optionSets', prStDe.dataElement.optionSet.id).done(function(obj) {                    
                        if(!obj || obj.version !== prStDe.dataElement.optionSet.version) {
                            promise = promise.then( getOptionSet( prStDe.dataElement.optionSet.id ) );
                        }
                        d.resolve();
                    });

                    return p;
                });
            }            
        });                      
    });

    build.done(function() {
        def.resolve();

        promise = promise.done( function () {
            mainDef.resolve( programs );
        } );
    });

    builder.resolve();

    return mainPromise;    
}

function getOptionSet( id )
{
    return function() {
        return $.ajax( {
            url: '../api/optionSets.json?filter=id:eq:' + id +'&fields=id,name,version,options',
            type: 'GET'
        }).done( function( response ){            
            _.each( _.values( response.optionSets ), function( optionSet ) {                
                dhis2.ec.store.set( 'optionSets', optionSet );
            });
        });
    };
}

function uploadLocalData()
{
    if ( !dhis2.ec.storageManager.hasLocalData() )
    {
        return;
    }

    setHeaderWaitMessage( i18n_uploading_data_notification );
    
    var events = dhis2.ec.storageManager.getEventsAsArray();   
    
    _.each( _.values( events ), function( event ) {
        
        if( event.hasOwnProperty('src')){ 
            delete event.src;            
        }       
        delete event.event;
    });    
    
    events = {events: events};
    
    //jackson insists for valid json, where properties are bounded with ""    
    events = JSON.stringify(events);  
    
    $.ajax( {
        url: '../api/events',
        type: 'POST',
        data: events,
        contentType: 'application/json',              
        success: function()
        {
            dhis2.ec.storageManager.clear();
            log( 'Successfully uploaded local events' );      
            setHeaderDelayMessage( i18n_sync_success );
            selection.responseReceived(); //notify angular 
        },
        error: function( xhr )
        {
            if ( 409 == xhr.status ) // Invalid event
            {
                // there is something wrong with the data - ignore for now.

                dhis2.ec.storageManager.clear();
            }
            else // Connection lost during upload
            {
                var message = i18n_sync_failed
                    + ' <button id="sync_button" type="button">' + i18n_sync_now + '</button>';

                setHeaderMessage( message );
                $( '#sync_button' ).bind( 'click', uploadLocalData );
            }
        }
    } );
}

// -----------------------------------------------------------------------------
// StorageManager
// -----------------------------------------------------------------------------

/**
 * This object provides utility methods for localStorage and manages data entry
 * forms and data values.
 */
function StorageManager()
{
    var MAX_SIZE = new Number(2600000);

    /**
     * Returns the total number of characters currently in the local storage.
     *
     * @return number of characters.
     */
    this.totalSize = function()
    {
        var totalSize = new Number();

        for (var i = 0; i < localStorage.length; i++)
        {
            var value = localStorage.key(i);

            if (value)
            {
                totalSize += value.length;
            }
        }

        return totalSize;
    };

    /**
     * Return the remaining capacity of the local storage in characters, ie. the
     * maximum size minus the current size.
     */
    this.remainingStorage = function()
    {
        return MAX_SIZE - this.totalSize();
    };
    
    /**
     * Clears stored events. 
     */
    this.clear = function ()
    {
        localStorage.removeItem(EVENT_VALUES);        
    };    
    
    /**
     * Saves an event
     *
     * @param event The event in json format.
     */
    this.saveEvent = function(event)
    {
        //var newEvent = event;
        
        if( !event.hasOwnProperty('src') )
        {
            if( !event.event){
                event.event = this.generatePseudoUid();
                event.src = 'local';
            }            
        }        

        var events = {};

        if (localStorage[EVENT_VALUES] != null)
        {
            events = JSON.parse(localStorage[EVENT_VALUES]);
        }

        events[event.event] = event;

        try
        {
            localStorage[EVENT_VALUES] = JSON.stringify(events);

            log('Successfully stored event - locally');
        }
        catch (e)
        {
            log('Max local storage quota reached, not storing data value locally');
        }
    };
    
    /**
     * Gets the value for the event with the given arguments, or null if it
     * does not exist.
     *
     * @param id the event identifier.
     *
     */
    this.getEvent = function(id)
    {
        if (localStorage[EVENT_VALUES] != null)
        {
            var events = JSON.parse(localStorage[EVENT_VALUES]);

            return events[id];
        }

        return null;
    };

    /**
     * Removes the given event from localStorage.
     *
     * @param event and identifiers in json format.
     */
    this.clearEvent = function(event)
    {
        var events = this.getAllEvents();

        if (events != null && events[event.event] != null)
        {
            delete events[event.event];
            localStorage[EVENT_VALUES] = JSON.stringify(events);
        }
    };
    
    /**
     * Returns events matching the arguments provided
     * 
     * @param orgUnit 
     * @param programStage
     * 
     * @return a JSON associative array.
     */
    this.getEvents = function(orgUnit, programStage)
    {
        var events = this.getEventsAsArray();
        var match = [];
        for( var i=0; i<events.length; i++){
            if(events[i].orgUnit == orgUnit && events[i].programStage == programStage ){
                match.push(events[i]);
            }
        }
        
        var pager = {pageSize: 50, page: 1, toolBarDisplay: 5, pageCount: 1};  
        return {events: match, pager: pager};
    };
    
    /**
     *
     * @return a JSON associative array.
     */
    this.getAllEvents = function()
    {
        return localStorage[EVENT_VALUES] != null ? JSON.parse( localStorage[EVENT_VALUES] ) : null;
    };    

    /**
     * Returns all event objects in an array. Returns an empty array if no
     * event exist. Items in array are guaranteed not to be undefined.
     */
    this.getEventsAsArray = function()
    {
        var values = new Array();
        var events = this.getAllEvents();

        if (undefined == events)
        {
            return values;
        }

        for (i in events)
        {
            if (events.hasOwnProperty(i) && undefined !== events[i])
            {
                values.push(events[i]);
            }
        }

        return values;
    };
    
    /**
     * Indicates whether there exists data values or complete data set
     * registrations in the local storage.
     *
     * @return true if local data exists, false otherwise.
     */
    this.hasLocalData = function()
    {
        var events = this.getAllEvents();        

        if (events == null)
        {
            return false;
        }
        if (Object.keys(events).length < 1)
        {
            return false;
        }    

        return true;
    };
    
    this.generatePseudoUid = function () 
    {
        return Math.random().toString(36).substr(2, 11);
    };
}

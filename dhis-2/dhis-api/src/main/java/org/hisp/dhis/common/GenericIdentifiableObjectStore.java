package org.hisp.dhis.common;

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

import org.hisp.dhis.user.User;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Lars Helge Overland
 */
public interface GenericIdentifiableObjectStore<T>
    extends GenericStore<T>
{
    /**
     * Retrieves the object with the given uid.
     *
     * @param uid the uid.
     * @return the object with the given uid.
     */
    T getByUid( String uid );

    /**
     * Retrieves the object with the given uid. Bypasses the ACL system.
     *
     * @param uid the uid.
     * @return the object with the given uid.
     */
    T getByUidNoAcl( String uid );

    /**
     * Update object. Bypasses the ACL system.
     *
     * @param object Object update
     * @return the object with the given uid.
     */
    void updateNoAcl( T object );

    /**
     * Retrieves the object with the given name.
     *
     * @param name the name.
     * @return the object with the given name.
     */
    T getByName( String name );

    /**
     * Retrieves the object with the given code.
     *
     * @param code the code.
     * @return the object with the given code.
     */
    T getByCode( String code );

    /**
     * Retrieves a List of all objects (sorted on name).
     *
     * @return a List of all objects.
     */
    List<T> getAllOrderedName();

    /**
     * Retrieves the objects determined by the given first result and max result.
     *
     * @param first the first result object to return.
     * @param max   the max number of result objects to return.
     * @return list of objects.
     */
    List<T> getAllOrderedName( int first, int max );

    /**
     * Retrieves a List of objects where the name is equal the given name.
     *
     * @param name the name.
     * @return a List of objects.
     */
    List<T> getAllEqName( String name );

    /**
     * Retrieves a List of objects where the name is equal the given name (ignore case).
     *
     * @param name the name.
     * @return a List of objects.
     */
    List<T> getAllEqNameIgnoreCase( String name );

    /**
     * Return the number of objects where the name is equal the given name.
     * <p/>
     * This count is _unfiltered_ (no ACL!), so this is not the same as
     * getAllEqName().size().
     *
     * @param name the name.
     * @return Count of objects.
     */
    int getCountEqNameNoAcl( String name );

    /**
     * Retrieves a List of objects where the name is like the given name.
     *
     * @param name the name.
     * @return a List of objects.
     */
    List<T> getAllLikeName( String name );

    /**
     * Retrieves a List of objects where the shortName is like the given shortName.
     *
     * @param shortName the shortName.
     * @return a List of objects.
     */
    List<T> getAllLikeShortName( String shortName );

    /**
     * Retrieves the objects determined by the given first result and max result.
     * The returned list is ordered by the last updated property descending.
     *
     * @param first the first result object to return.
     * @param max   the max number of result objects to return.
     * @return List of objects.
     */
    List<T> getAllOrderedLastUpdated( int first, int max );

    /**
     * Retrieves the objects determined by the given first result and max result
     * which name is like the given name.
     *
     * @param name  the name which result object names must be like.
     * @param first the first result object to return.
     * @param max   the max number of result objects to return.
     * @return List of objects.
     */
    List<T> getAllLikeNameOrderedName( String name, int first, int max );

    /**
     * Gets the count of objects which name is like the given name.
     *
     * @param name the name which result object names must be like.
     * @return the count of objects.
     */
    int getCountLikeName( String name );

    /**
     * Retrieves a list of objects referenced by the given List of uids.
     *
     * @param uids a List of uids.
     * @return a list of objects.
     */
    List<T> getByUid( Collection<String> uids );

    /**
     * Retrieves a list of objects referenced by the given List of uids. 
     * Bypasses the ACL system.
     *
     * @param uids a List of uids.
     * @return a list of objects.
     */
    List<T> getByUidNoAcl( Collection<String> uids );

    /**
     * Returns all objects that are equal to or newer than given date.
     *
     * @param created Date to compare with.
     * @return All objects equal or newer than given date.
     */
    List<T> getAllGeCreated( Date created );

    /**
     * Returns all objects that are equal to or newer than given date.
     *
     * @param lastUpdated Date to compare with.
     * @return All objects equal or newer than given date.
     */
    List<T> getAllGeLastUpdated( Date lastUpdated );

    /**
     * Returns all objects that are equal to or newer than given date.
     * (ordered by name)
     *
     * @param lastUpdated Date to compare to.
     * @return All objects equal or newer than given date.
     */
    List<T> getAllGeLastUpdatedOrderedName( Date lastUpdated );

    /**
     * Returns the number of objects that are equal to or newer than given date.
     *
     * @param lastUpdated Date to compare to.
     * @return the number of objects equal or newer than given date.
     */
    long getCountGeLastUpdated( Date lastUpdated );

    /**
     * Retrieves objects associated with the given user.
     *
     * @param user the user.
     * @return list of objects.
     */
    List<T> getByUser( User user );
}

package org.hisp.dhis.dxf2.metadata.importers;

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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import org.hisp.dhis.acl.AclService;
import org.hisp.dhis.attribute.Attribute;
import org.hisp.dhis.attribute.AttributeService;
import org.hisp.dhis.attribute.AttributeValue;
import org.hisp.dhis.common.BaseIdentifiableObject;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.NameableObject;
import org.hisp.dhis.dataelement.DataElementOperand;
import org.hisp.dhis.dataelement.DataElementOperandService;
import org.hisp.dhis.dataentryform.DataEntryForm;
import org.hisp.dhis.dataentryform.DataEntryFormService;
import org.hisp.dhis.dxf2.importsummary.ImportConflict;
import org.hisp.dhis.dxf2.metadata.ImportOptions;
import org.hisp.dhis.dxf2.metadata.ImportTypeSummary;
import org.hisp.dhis.dxf2.metadata.ImportUtils;
import org.hisp.dhis.dxf2.metadata.Importer;
import org.hisp.dhis.dxf2.metadata.ObjectBridge;
import org.hisp.dhis.dxf2.metadata.handlers.ObjectHandler;
import org.hisp.dhis.dxf2.metadata.handlers.ObjectHandlerUtils;
import org.hisp.dhis.eventreport.EventReport;
import org.hisp.dhis.expression.Expression;
import org.hisp.dhis.expression.ExpressionService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodService;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.program.ProgramStage;
import org.hisp.dhis.program.ProgramStageDataElement;
import org.hisp.dhis.program.ProgramStageDataElementService;
import org.hisp.dhis.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.program.ProgramValidation;
import org.hisp.dhis.schema.SchemaService;
import org.hisp.dhis.system.util.ReflectionUtils;
import org.hisp.dhis.trackedentity.TrackedEntity;
import org.hisp.dhis.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserCredentials;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hisp.dhis.system.util.PredicateUtils.idObjectCollectionsWithScanned;
import static org.hisp.dhis.system.util.PredicateUtils.idObjects;

/**
 * Importer that can handle IdentifiableObject and NameableObject.
 *
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class DefaultIdentifiableObjectImporter<T extends BaseIdentifiableObject>
    implements Importer<T>
{
    private static final Log log = LogFactory.getLog( DefaultIdentifiableObjectImporter.class );

    //-------------------------------------------------------------------------------------------------------
    // Dependencies
    //-------------------------------------------------------------------------------------------------------

    @Autowired
    private PeriodService periodService;

    @Autowired
    private AttributeService attributeService;

    @Autowired
    private ExpressionService expressionService;

    @Autowired
    private DataEntryFormService dataEntryFormService;

    @Autowired
    private DataElementOperandService dataElementOperandService;

    @Autowired
    private ProgramStageDataElementService programStageDataElementService;

    @Autowired
    private ObjectBridge objectBridge;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private AclService aclService;

    @Autowired
    private SchemaService schemaService;

    @Autowired( required = false )
    private List<ObjectHandler<T>> objectHandlers;

    //-------------------------------------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------------------------------------

    public DefaultIdentifiableObjectImporter( Class<T> importerClass )
    {
        this.importerClass = importerClass;
    }

    private final Class<T> importerClass;

    //-------------------------------------------------------------------------------------------------------
    // Internal state
    //-------------------------------------------------------------------------------------------------------

    protected ImportTypeSummary summaryType;

    protected ImportOptions options;

    // keeping this internal for now, might be split into several classes
    private class NonIdentifiableObjects
    {
        private Set<AttributeValue> attributeValues = Sets.newHashSet();

        private Expression leftSide;
        private Expression rightSide;

        private DataEntryForm dataEntryForm;

        private Set<DataElementOperand> compulsoryDataElementOperands = Sets.newHashSet();
        private Set<DataElementOperand> greyedFields = Sets.newHashSet();

        private Collection<ProgramStageDataElement> programStageDataElements = Lists.newArrayList();
        private Set<ProgramTrackedEntityAttribute> programTrackedEntityAttributes = Sets.newHashSet();

        public void extract( T object )
        {
            attributeValues = extractAttributeValues( object );
            leftSide = extractExpression( object, "leftSide" );
            rightSide = extractExpression( object, "rightSide" );
            dataEntryForm = extractDataEntryForm( object, "dataEntryForm" );
            compulsoryDataElementOperands = extractDataElementOperands( object, "compulsoryDataElementOperands" );
            greyedFields = extractDataElementOperands( object, "greyedFields" );
            programStageDataElements = extractProgramStageDataElements( object );
            programTrackedEntityAttributes = extractProgramTrackedEntityAttributes( object );
        }

        public void delete( T object )
        {
            if ( !options.isDryRun() )
            {
                deleteAttributeValues( object );
                deleteExpression( object, "leftSide" );
                deleteExpression( object, "rightSide" );
                deleteDataEntryForm( object, "dataEntryForm" );

                if ( options.getImportStrategy().isDelete() )
                {
                    deleteDataElementOperands( object, "compulsoryDataElementOperands" );
                }

                deleteDataElementOperands( object, "greyedFields" );
                deleteProgramStageDataElements( object );
                deleteProgramTrackedEntityAttributes( object );
            }
        }

        public void save( T object )
        {
            saveAttributeValues( object, attributeValues );
            saveExpression( object, "leftSide", leftSide );
            saveExpression( object, "rightSide", rightSide );
            saveDataEntryForm( object, "dataEntryForm", dataEntryForm );
            saveDataElementOperands( object, "compulsoryDataElementOperands", compulsoryDataElementOperands );
            saveDataElementOperands( object, "greyedFields", greyedFields );
            saveProgramStageDataElements( object, programStageDataElements );
            saveProgramTrackedEntityAttributes( object, programTrackedEntityAttributes );
        }

        private void saveDataEntryForm( T object, String fieldName, DataEntryForm dataEntryForm )
        {
            if ( dataEntryForm != null )
            {
                Map<Field, Collection<Object>> identifiableObjectCollections = detachCollectionFields( dataEntryForm );
                reattachCollectionFields( dataEntryForm, identifiableObjectCollections );

                dataEntryForm.setId( 0 );
                dataEntryFormService.addDataEntryForm( dataEntryForm );

                ReflectionUtils.invokeSetterMethod( fieldName, object, dataEntryForm );
            }
        }

        private DataEntryForm extractDataEntryForm( T object, String fieldName )
        {
            DataEntryForm dataEntryForm = null;

            if ( ReflectionUtils.findGetterMethod( fieldName, object ) != null )
            {
                dataEntryForm = ReflectionUtils.invokeGetterMethod( fieldName, object );

                if ( dataEntryForm != null )
                {
                    ReflectionUtils.invokeSetterMethod( fieldName, object, new Object[]{ null } );
                }
            }

            return dataEntryForm;
        }

        private void deleteDataEntryForm( T object, String fieldName )
        {
            DataEntryForm dataEntryForm = extractDataEntryForm( object, fieldName );

            if ( dataEntryForm != null )
            {
                dataEntryFormService.deleteDataEntryForm( dataEntryForm );
                sessionFactory.getCurrentSession().flush();
            }
        }

        private Expression extractExpression( T object, String fieldName )
        {
            if ( ReflectionUtils.findGetterMethod( fieldName, object ) != null )
            {
                Object expression = ReflectionUtils.invokeGetterMethod( fieldName, object );

                if ( expression != null && Expression.class.isAssignableFrom( expression.getClass() ) )
                {
                    ReflectionUtils.invokeSetterMethod( fieldName, object, new Object[]{ null } );
                }
            }

            return null;
        }

        private Set<DataElementOperand> extractDataElementOperands( T object, String fieldName )
        {
            Set<DataElementOperand> dataElementOperands = Sets.newHashSet();

            if ( ReflectionUtils.findGetterMethod( fieldName, object ) != null )
            {
                Set<DataElementOperand> detachedDataElementOperands = ReflectionUtils.invokeGetterMethod( fieldName, object );
                dataElementOperands = Sets.newHashSet( detachedDataElementOperands );

                if ( detachedDataElementOperands.size() > 0 )
                {
                    detachedDataElementOperands.clear();
                    ReflectionUtils.invokeSetterMethod( fieldName, object, Sets.newHashSet() );
                }
            }

            return dataElementOperands;
        }

        private Set<AttributeValue> extractAttributeValues( T object )
        {
            Set<AttributeValue> attributeValues = Sets.newHashSet();

            if ( ReflectionUtils.findGetterMethod( "attributeValues", object ) != null )
            {
                attributeValues = ReflectionUtils.invokeGetterMethod( "attributeValues", object );

                if ( attributeValues.size() > 0 )
                {
                    ReflectionUtils.invokeSetterMethod( "attributeValues", object, Sets.newHashSet() );
                }
            }

            return attributeValues;
        }

        private void saveExpression( T object, String fieldName, Expression expression )
        {
            if ( expression != null )
            {
                Map<Field, Collection<Object>> identifiableObjectCollections = detachCollectionFields( expression );
                reattachCollectionFields( expression, identifiableObjectCollections );

                expression.setId( 0 );
                expressionService.addExpression( expression );

                ReflectionUtils.invokeSetterMethod( fieldName, object, expression );
            }
        }

        private void saveDataElementOperands( T object, String fieldName, Set<DataElementOperand> dataElementOperands )
        {
            if ( dataElementOperands.size() > 0 )
            {
                // need special handling for compulsoryDataElementOperands since they cascade with all-delete-orphan
                if ( "compulsoryDataElementOperands".equals( fieldName ) )
                {
                    for ( DataElementOperand dataElementOperand : dataElementOperands )
                    {
                        Map<Field, Object> identifiableObjects = detachFields( dataElementOperand );
                        reattachFields( dataElementOperand, identifiableObjects );
                    }

                    Set<DataElementOperand> detachedDataElementOperands = ReflectionUtils.invokeGetterMethod( fieldName, object );
                    detachedDataElementOperands.clear();
                    detachedDataElementOperands.addAll( dataElementOperands );
                    sessionFactory.getCurrentSession().flush();
                }
                else
                {
                    for ( DataElementOperand dataElementOperand : dataElementOperands )
                    {
                        Map<Field, Object> identifiableObjects = detachFields( dataElementOperand );
                        reattachFields( dataElementOperand, identifiableObjects );

                        dataElementOperand.setId( 0 );
                        dataElementOperandService.addDataElementOperand( dataElementOperand );
                    }

                    ReflectionUtils.invokeSetterMethod( fieldName, object, dataElementOperands );
                }
            }
        }

        private void deleteAttributeValues( T object )
        {
            if ( !Attribute.class.isAssignableFrom( object.getClass() ) )
            {
                Set<AttributeValue> attributeValues = extractAttributeValues( object );

                for ( AttributeValue attributeValue : attributeValues )
                {
                    attributeService.deleteAttributeValue( attributeValue );
                }
            }
        }

        private void saveAttributeValues( T object, Set<AttributeValue> attributeValues )
        {
            if ( attributeValues.size() > 0 )
            {
                for ( AttributeValue attributeValue : attributeValues )
                {
                    Attribute attribute = objectBridge.getObject( attributeValue.getAttribute() );

                    if ( attribute == null )
                    {
                        log.debug( "Unknown reference to " + attributeValue.getAttribute() + " on object " + attributeValue );
                        return;
                    }

                    attributeValue.setId( 0 );
                    attributeValue.setAttribute( attribute );
                }

                for ( AttributeValue attributeValue : attributeValues )
                {
                    attributeService.addAttributeValue( attributeValue );
                }

                ReflectionUtils.invokeSetterMethod( "attributeValues", object, attributeValues );
            }
        }

        private void deleteExpression( T object, String fieldName )
        {
            Expression expression = extractExpression( object, fieldName );

            if ( expression != null )
            {
                expressionService.deleteExpression( expression );
            }
        }

        private void deleteDataElementOperands( T object, String fieldName )
        {
            Set<DataElementOperand> dataElementOperands = extractDataElementOperands( object, fieldName );

            for ( DataElementOperand dataElementOperand : dataElementOperands )
            {
                dataElementOperandService.deleteDataElementOperand( dataElementOperand );
            }
        }

        private Set<ProgramTrackedEntityAttribute> extractProgramTrackedEntityAttributes( T object )
        {
            Set<ProgramTrackedEntityAttribute> programTrackedEntityAttributeSet = Sets.newHashSet();

            if ( ReflectionUtils.isCollection( "attributes", object, ProgramTrackedEntityAttribute.class ) )
            {
                Set<ProgramTrackedEntityAttribute> programTrackedEntityAttributes = ReflectionUtils.invokeGetterMethod( "attributes", object );

                for ( ProgramTrackedEntityAttribute trackedEntityAttribute : programTrackedEntityAttributes )
                {
                    if ( sessionFactory.getCurrentSession().contains( trackedEntityAttribute ) )
                    {
                        sessionFactory.getCurrentSession().delete( trackedEntityAttribute );
                    }

                    programTrackedEntityAttributeSet.add( trackedEntityAttribute );
                }

                sessionFactory.getCurrentSession().flush();
                programTrackedEntityAttributes.clear();
                sessionFactory.getCurrentSession().flush();
            }

            return programTrackedEntityAttributeSet;
        }

        private void deleteProgramTrackedEntityAttributes( T object )
        {
            extractProgramTrackedEntityAttributes( object );
        }

        private void saveProgramTrackedEntityAttributes( T object, Set<ProgramTrackedEntityAttribute> programTrackedEntityAttributes )
        {
            for ( ProgramTrackedEntityAttribute programTrackedEntityAttribute : programTrackedEntityAttributes )
            {
                Map<Field, Object> identifiableObjects = detachFields( programTrackedEntityAttribute );
                reattachFields( programTrackedEntityAttribute, identifiableObjects );
                sessionFactory.getCurrentSession().persist( programTrackedEntityAttribute );
            }

            ReflectionUtils.invokeSetterMethod( "programTrackedEntityAttributes", object, programTrackedEntityAttributes );
        }

        private Collection<ProgramStageDataElement> extractProgramStageDataElements( T object )
        {
            Method method = ReflectionUtils.findGetterMethod( "programStageDataElements", object );

            if ( method != null )
            {
                Collection<ProgramStageDataElement> programStageDataElements = ReflectionUtils.invokeGetterMethod( "programStageDataElements", object );

                for ( ProgramStageDataElement programStageDataElement : programStageDataElements )
                {
                    if ( sessionFactory.getCurrentSession().contains( programStageDataElement ) )
                    {
                        programStageDataElementService.deleteProgramStageDataElement( programStageDataElement );
                    }
                }

                sessionFactory.getCurrentSession().flush();
                ReflectionUtils.invokeSetterMethod( "programStageDataElements", object,
                    ReflectionUtils.newCollectionInstance( method.getReturnType() ) );
                sessionFactory.getCurrentSession().flush();

                return programStageDataElements;
            }

            return null;
        }

        private void saveProgramStageDataElements( T object, Collection<ProgramStageDataElement> programStageDataElements )
        {
            if ( programStageDataElements == null )
            {
                return;
            }

            Collection<ProgramStageDataElement> programStageDataElementCollection =
                ReflectionUtils.newCollectionInstance( programStageDataElements.getClass() );

            for ( ProgramStageDataElement programStageDataElement : programStageDataElements )
            {
                Map<Field, Object> identifiableObjects = detachFields( programStageDataElement );
                reattachFields( programStageDataElement, identifiableObjects );

                if ( ProgramStage.class.isAssignableFrom( object.getClass() ) )
                {
                    programStageDataElement.setProgramStage( (ProgramStage) object );
                }

                ProgramStageDataElement persisted = programStageDataElementService.get( programStageDataElement.getProgramStage(), programStageDataElement.getDataElement() );

                if ( persisted == null )
                {
                    programStageDataElementService.addProgramStageDataElement( programStageDataElement );
                }
                else
                {
                    programStageDataElementCollection.add( persisted );
                }
            }

            ReflectionUtils.invokeSetterMethod( "programStageDataElements", object, programStageDataElementCollection );
        }

        private void deleteProgramStageDataElements( T object )
        {
            // deletion will be done in extractProgramStageDataElements
            extractProgramStageDataElements( object );
        }
    }

    //-------------------------------------------------------------------------------------------------------
    // Generic implementations of deleteObject, newObject, updatedObject
    //-------------------------------------------------------------------------------------------------------

    /**
     * Called every time a idObject is to be deleted.
     *
     * @param user            User to check
     * @param persistedObject The current version of the idObject
     * @return An ImportConflict instance if there was a conflict, otherwise null
     */
    protected boolean deleteObject( User user, T persistedObject )
    {
        if ( !aclService.canDelete( user, persistedObject ) )
        {
            summaryType.getImportConflicts().add(
                new ImportConflict( ImportUtils.getDisplayName( persistedObject ), "You do not have delete access to class type." ) );

            log.debug( "You do not have delete access to class type." );

            return false;
        }

        log.debug( "Trying to delete object => " + ImportUtils.getDisplayName( persistedObject ) + " (" + persistedObject.getClass().getSimpleName() + ")" );

        try
        {
            objectBridge.deleteObject( persistedObject );
        }
        catch ( Exception ex )
        {
            summaryType.getImportConflicts().add(
                new ImportConflict( ImportUtils.getDisplayName( persistedObject ), ex.getMessage() ) );
            return false;
        }

        log.debug( "Delete successful." );

        return true;
    }


    /**
     * Called every time a new idObject is to be imported.
     *
     * @param user   User to check
     * @param object Object to import
     * @return An ImportConflict instance if there was a conflict, otherwise null
     */
    protected boolean newObject( User user, T object )
    {
        if ( !aclService.canCreate( user, object.getClass() ) )
        {
            summaryType.getImportConflicts().add(
                new ImportConflict( ImportUtils.getDisplayName( object ), "You do not have create access to class type." ) );

            log.debug( "You do not have create access to class type." );

            return false;
        }

        // make sure that the internalId is 0, so that the system will generate a ID
        object.setId( 0 );
        // object.setUser( user );
        object.setUser( null );

        NonIdentifiableObjects nonIdentifiableObjects = new NonIdentifiableObjects();
        nonIdentifiableObjects.extract( object );

        UserCredentials userCredentials = null;

        if ( object instanceof User )
        {
            userCredentials = ((User) object).getUserCredentials();

            if ( userCredentials == null )
            {
                summaryType.getImportConflicts().add(
                    new ImportConflict( ImportUtils.getDisplayName( object ), "User is missing userCredentials part." ) );

                return false;
            }
        }

        Map<Field, Object> fields = detachFields( object );
        Map<Field, Collection<Object>> collectionFields = detachCollectionFields( object );

        reattachFields( object, fields );

        log.debug( "Trying to save new object => " + ImportUtils.getDisplayName( object ) + " (" + object.getClass().getSimpleName() + ")" );
        objectBridge.saveObject( object );

        updatePeriodTypes( object );
        reattachCollectionFields( object, collectionFields );

        objectBridge.updateObject( object );

        if ( object instanceof User && !options.isDryRun() )
        {
            userCredentials.setUser( (User) object );
            userCredentials.setId( object.getId() );

            Map<Field, Collection<Object>> collectionFieldsUserCredentials = detachCollectionFields( userCredentials );

            sessionFactory.getCurrentSession().save( userCredentials );

            reattachCollectionFields( userCredentials, collectionFieldsUserCredentials );

            sessionFactory.getCurrentSession().saveOrUpdate( userCredentials );

            ((User) object).setUserCredentials( userCredentials );

            objectBridge.updateObject( object );
        }

        if ( !options.isDryRun() )
        {
            nonIdentifiableObjects.save( object );
        }

        log.debug( "Save successful." );

        return true;
    }

    /**
     * Update idObject from old => new.
     *
     * @param user            User to check for access.
     * @param object          Object to import
     * @param persistedObject The current version of the idObject
     * @return An ImportConflict instance if there was a conflict, otherwise null
     */
    protected boolean updateObject( User user, T object, T persistedObject )
    {
        if ( !aclService.canUpdate( user, persistedObject ) )
        {
            summaryType.getImportConflicts().add(
                new ImportConflict( ImportUtils.getDisplayName( object ), "You do not have update access to object." ) );

            return false;
        }

        // for now, don't support ProgramStage, ProgramValidation and EventReport for dryRun
        if ( (ProgramStage.class.isAssignableFrom( persistedObject.getClass() )
            || ProgramValidation.class.isAssignableFrom( persistedObject.getClass() )
            || EventReport.class.isAssignableFrom( persistedObject.getClass() )) && options.isDryRun() )
        {
            return true;
        }

        NonIdentifiableObjects nonIdentifiableObjects = new NonIdentifiableObjects();
        nonIdentifiableObjects.extract( object );
        nonIdentifiableObjects.delete( persistedObject );

        UserCredentials userCredentials = null;

        if ( object instanceof User )
        {
            userCredentials = ((User) object).getUserCredentials();

            if ( userCredentials == null )
            {
                summaryType.getImportConflicts().add(
                    new ImportConflict( ImportUtils.getDisplayName( object ), "User is missing userCredentials part." ) );

                return false;
            }
        }

        Map<Field, Object> fields = detachFields( object );
        Map<Field, Collection<Object>> collectionFields = detachCollectionFields( object );

        reattachFields( object, fields );

        persistedObject.mergeWith( object );

        updatePeriodTypes( persistedObject );

        reattachCollectionFields( persistedObject, collectionFields );

        log.debug( "Starting update of object " + ImportUtils.getDisplayName( persistedObject ) + " (" + persistedObject.getClass()
            .getSimpleName() + ")" );

        objectBridge.updateObject( persistedObject );

        if ( !options.isDryRun() )
        {
            if ( object instanceof User )
            {
                Map<Field, Collection<Object>> collectionFieldsUserCredentials = detachCollectionFields( userCredentials );

                ((User) persistedObject).getUserCredentials().mergeWith( userCredentials );
                reattachCollectionFields( ((User) persistedObject).getUserCredentials(), collectionFieldsUserCredentials );

                sessionFactory.getCurrentSession().saveOrUpdate( ((User) persistedObject).getUserCredentials() );
            }

            nonIdentifiableObjects.save( persistedObject );
        }

        log.debug( "Update successful." );

        return true;
    }

    private void updatePeriodTypes( T object )
    {
        for ( Field field : object.getClass().getDeclaredFields() )
        {
            if ( PeriodType.class.isAssignableFrom( field.getType() ) )
            {
                PeriodType periodType = ReflectionUtils.invokeGetterMethod( field.getName(), object );
                periodType = objectBridge.getObject( periodType );
                ReflectionUtils.invokeSetterMethod( field.getName(), object, periodType );
            }
        }
    }

    //-------------------------------------------------------------------------------------------------------
    // Importer<T> Implementation
    //-------------------------------------------------------------------------------------------------------

    @Override
    public ImportTypeSummary importObjects( User user, List<T> objects, ImportOptions options )
    {
        this.options = options;
        this.summaryType = new ImportTypeSummary( importerClass.getSimpleName() );
        this.summaryType.setDataValueCount( null );

        if ( objects.isEmpty() )
        {
            return summaryType;
        }

        ObjectHandlerUtils.preObjectsHandlers( objects, objectHandlers );

        for ( T object : objects )
        {
            ObjectHandlerUtils.preObjectHandlers( object, objectHandlers );
            importObjectLocal( user, object );
            ObjectHandlerUtils.postObjectHandlers( object, objectHandlers );
        }

        ObjectHandlerUtils.postObjectsHandlers( objects, objectHandlers );

        return summaryType;
    }

    @Override
    public ImportTypeSummary importObject( User user, T object, ImportOptions options )
    {
        this.options = options;
        this.summaryType = new ImportTypeSummary( importerClass.getSimpleName() );
        this.summaryType.setDataValueCount( null );

        if ( object == null )
        {
            summaryType.getImportCount().incrementIgnored();
            return summaryType;
        }

        ObjectHandlerUtils.preObjectHandlers( object, objectHandlers );
        importObjectLocal( user, object );
        ObjectHandlerUtils.postObjectHandlers( object, objectHandlers );

        return summaryType;
    }

    @Override
    public boolean canHandle( Class<?> clazz )
    {
        return importerClass.equals( clazz );
    }

    //-------------------------------------------------------------------------------------------------------
    // Helpers
    //-------------------------------------------------------------------------------------------------------
    private void importObjectLocal( User user, T object )
    {
        if ( validateIdentifiableObject( object ) )
        {
            startImport( user, object );
        }
        else
        {
            summaryType.incrementIgnored();
        }
    }

    private void startImport( User user, T object )
    {
        T persistedObject = objectBridge.getObject( object );

        if ( options.getImportStrategy().isCreate() )
        {
            if ( newObject( user, object ) )
            {
                summaryType.incrementImported();
            }
        }
        else if ( options.getImportStrategy().isUpdate() )
        {
            if ( updateObject( user, object, persistedObject ) )
            {
                summaryType.incrementUpdated();
            }
            else
            {
                summaryType.incrementIgnored();
            }
        }
        else if ( options.getImportStrategy().isCreateAndUpdate() )
        {
            if ( persistedObject != null )
            {
                if ( updateObject( user, object, persistedObject ) )
                {
                    summaryType.incrementUpdated();
                }
                else
                {
                    summaryType.incrementIgnored();
                }
            }
            else
            {
                if ( newObject( user, object ) )
                {
                    summaryType.incrementImported();
                }
                else
                {
                    summaryType.incrementIgnored();
                }
            }
        }
        else if ( options.getImportStrategy().isDelete() )
        {
            if ( deleteObject( user, persistedObject ) )
            {
                summaryType.incrementDeleted();
            }
            else
            {
                summaryType.incrementIgnored();
            }
        }
    }

    private boolean validateIdentifiableObject( T object )
    {
        ImportConflict conflict = null;
        boolean success = true;

        if ( options.getImportStrategy().isDelete() )
        {
            success = validateForDeleteStrategy( object );
            return success;
        }

        if ( object.getName() == null || object.getName().length() == 0 )
        {
            conflict = new ImportConflict( ImportUtils.getDisplayName( object ), "Empty name for object " + object );
        }

        if ( NameableObject.class.isInstance( object ) )
        {
            NameableObject nameableObject = (NameableObject) object;

            if ( (nameableObject.getShortName() == null || nameableObject.getShortName().length() == 0)
                // this is nasty, but we have types in the system which have shortName, but which do -not- require not-null )
                && !TrackedEntityAttribute.class.isAssignableFrom( object.getClass() )
                && !TrackedEntity.class.isAssignableFrom( object.getClass() ) )
            {
                conflict = new ImportConflict( ImportUtils.getDisplayName( object ), "Empty shortName for object " + object );
            }
        }

        if ( conflict != null )
        {
            summaryType.getImportConflicts().add( conflict );
        }

        if ( options.getImportStrategy().isCreate() )
        {
            success = validateForNewStrategy( object );
        }
        else if ( options.getImportStrategy().isUpdate() )
        {
            success = validateForUpdatesStrategy( object );
        }
        else if ( options.getImportStrategy().isCreateAndUpdate() )
        {
            // if we have a match on at least one of the objects, then assume update
            if ( objectBridge.getObjects( object ).size() > 0 )
            {
                success = validateForUpdatesStrategy( object );
            }
            else
            {
                success = validateForNewStrategy( object );
            }
        }

        return success;
    }

    private boolean validateForUpdatesStrategy( T object )
    {
        ImportConflict conflict = null;
        Collection<T> objects = objectBridge.getObjects( object );

        if ( objects.isEmpty() )
        {
            conflict = reportLookupConflict( object );
        }
        else if ( objects.size() > 1 )
        {
            conflict = reportMoreThanOneConflict( object );
        }

        if ( conflict != null )
        {
            summaryType.getImportConflicts().add( conflict );

            return false;
        }

        return true;
    }

    private boolean validateForNewStrategy( T object )
    {
        ImportConflict conflict;
        Collection<T> objects = objectBridge.getObjects( object );

        if ( objects.size() > 0 )
        {
            conflict = reportConflict( object );
            summaryType.getImportConflicts().add( conflict );

            return false;
        }

        return true;
    }

    private boolean validateForDeleteStrategy( T object )
    {
        ImportConflict conflict = null;
        Collection<T> objects = objectBridge.getObjects( object );

        if ( objects.isEmpty() )
        {
            conflict = reportLookupConflict( object );
        }
        else if ( objects.size() > 1 )
        {
            conflict = reportMoreThanOneConflict( object );
        }

        if ( conflict != null )
        {
            summaryType.getImportConflicts().add( conflict );

            return false;
        }

        return true;
    }

    private IdentifiableObject findObjectByReference( IdentifiableObject identifiableObject )
    {
        if ( identifiableObject == null )
        {
            return null;
        }
        else if ( Period.class.isAssignableFrom( identifiableObject.getClass() ) )
        {
            Period period = (Period) identifiableObject;

            if ( !options.isDryRun() )
            {
                period = periodService.reloadPeriod( period );
                sessionFactory.getCurrentSession().flush();
            }

            return period;
        }

        return objectBridge.getObject( identifiableObject );
    }

    private Map<Field, Object> detachFields( final Object object )
    {
        final Map<Field, Object> fieldMap = Maps.newHashMap();
        final Collection<Field> fieldCollection = ReflectionUtils.collectFields( object.getClass(), idObjects );

        for ( Field field : fieldCollection )
        {
            Object ref = ReflectionUtils.invokeGetterMethod( field.getName(), object );

            if ( ref != null )
            {
                fieldMap.put( field, ref );
                ReflectionUtils.invokeSetterMethod( field.getName(), object, new Object[]{ null } );
            }
        }

        return fieldMap;
    }

    private void reattachFields( Object object, Map<Field, Object> fields )
    {
        for ( Field field : fields.keySet() )
        {
            IdentifiableObject idObject = (IdentifiableObject) fields.get( field );
            IdentifiableObject reference = findObjectByReference( idObject );

            if ( reference == null )
            {
                if ( schemaService.getSchema( idObject.getClass() ) != null )
                {
                    reportReferenceError( object, idObject );
                }
            }

            if ( !options.isDryRun() )
            {
                ReflectionUtils.invokeSetterMethod( field.getName(), object, reference );
            }
        }
    }

    private Map<Field, Collection<Object>> detachCollectionFields( final Object object )
    {
        final Map<Field, Collection<Object>> collectionFields = Maps.newHashMap();
        final Collection<Field> fieldCollection = ReflectionUtils.collectFields( object.getClass(), idObjectCollectionsWithScanned );

        for ( Field field : fieldCollection )
        {
            Collection<Object> objects = ReflectionUtils.invokeGetterMethod( field.getName(), object );

            if ( objects != null && !objects.isEmpty() )
            {
                collectionFields.put( field, objects );
                Collection<Object> emptyCollection = ReflectionUtils.newCollectionInstance( field.getType() );
                ReflectionUtils.invokeSetterMethod( field.getName(), object, emptyCollection );
            }
        }

        return collectionFields;
    }

    private void reattachCollectionFields( final Object idObject, Map<Field, Collection<Object>> collectionFields )
    {
        for ( Field field : collectionFields.keySet() )
        {
            Collection<Object> collection = collectionFields.get( field );
            final Collection<Object> objects = ReflectionUtils.newCollectionInstance( field.getType() );

            for ( Object object : collection )
            {
                IdentifiableObject ref = findObjectByReference( (IdentifiableObject) object );

                if ( ref != null )
                {
                    objects.add( ref );
                }
                else
                {
                    if ( schemaService.getSchema( idObject.getClass() ) != null || UserCredentials.class.isAssignableFrom( idObject.getClass() ) )
                    {
                        reportReferenceError( idObject, object );
                    }
                }
            }

            if ( !options.isDryRun() )
            {
                ReflectionUtils.invokeSetterMethod( field.getName(), idObject, objects );
            }
        }
    }

    private ImportConflict reportLookupConflict( IdentifiableObject object )
    {
        return new ImportConflict( ImportUtils.getDisplayName( object ), "Object does not exist." );
    }

    private ImportConflict reportMoreThanOneConflict( IdentifiableObject object )
    {
        return new ImportConflict( ImportUtils.getDisplayName( object ), "More than one object matches identifiers." );
    }

    private ImportConflict reportConflict( IdentifiableObject object )
    {
        return new ImportConflict( ImportUtils.getDisplayName( object ), "Object already exists." );
    }

    public String identifiableObjectToString( Object object )
    {
        if ( IdentifiableObject.class.isInstance( object ) )
        {
            IdentifiableObject identifiableObject = (IdentifiableObject) object;

            return "IdentifiableObject{" +
                "id=" + identifiableObject.getId() +
                ", uid='" + identifiableObject.getUid() + '\'' +
                ", code='" + identifiableObject.getCode() + '\'' +
                ", name='" + identifiableObject.getName() + '\'' +
                ", created=" + identifiableObject.getCreated() +
                ", lastUpdated=" + identifiableObject.getLastUpdated() +
                '}';
        }

        return object.toString();
    }

    private void reportReferenceError( Object object, Object reference )
    {
        String objectName = object != null ? object.getClass().getSimpleName() : "null";
        String referenceName = reference != null ? reference.getClass().getSimpleName() : "null";

        String logMsg = "Unknown reference to " + identifiableObjectToString( reference ) + " (" + referenceName + ")" +
            " on object " + identifiableObjectToString( object ) + " (" + objectName + ").";

        log.debug( logMsg );

        ImportConflict importConflict = new ImportConflict( ImportUtils.getDisplayName( object ), logMsg );
        summaryType.getImportConflicts().add( importConflict );
    }
}

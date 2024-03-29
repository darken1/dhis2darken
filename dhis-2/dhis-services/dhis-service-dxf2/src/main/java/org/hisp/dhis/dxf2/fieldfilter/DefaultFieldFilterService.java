package org.hisp.dhis.dxf2.fieldfilter;

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
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
 */

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.PresetProvider;
import org.hisp.dhis.dxf2.parser.ParserService;
import org.hisp.dhis.node.AbstractNode;
import org.hisp.dhis.node.Node;
import org.hisp.dhis.node.NodePropertyConverter;
import org.hisp.dhis.node.NodeTransformer;
import org.hisp.dhis.node.types.CollectionNode;
import org.hisp.dhis.node.types.ComplexNode;
import org.hisp.dhis.node.types.SimpleNode;
import org.hisp.dhis.schema.Property;
import org.hisp.dhis.schema.Schema;
import org.hisp.dhis.schema.SchemaService;
import org.hisp.dhis.system.util.ReflectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public class DefaultFieldFilterService implements FieldFilterService
{
    private static final Log log = LogFactory.getLog( DefaultFieldFilterService.class );

    @Autowired
    private ParserService parserService;

    @Autowired
    private SchemaService schemaService;

    @Autowired(required = false)
    private Set<PresetProvider> presetProviders = Sets.newHashSet();

    @Autowired(required = false)
    private Set<NodePropertyConverter> nodePropertyConverters = Sets.newHashSet();

    @Autowired(required = false)
    private Set<NodeTransformer> nodeTransformers = Sets.newHashSet();

    private ImmutableMap<String, PresetProvider> presets = ImmutableMap.of();

    private ImmutableMap<String, NodePropertyConverter> converters = ImmutableMap.of();

    private ImmutableMap<String, NodeTransformer> transformers = ImmutableMap.of();

    @PostConstruct
    public void init()
    {
        ImmutableMap.Builder<String, PresetProvider> presetBuilder = ImmutableMap.builder();

        for ( PresetProvider presetProvider : presetProviders )
        {
            presetBuilder.put( presetProvider.name(), presetProvider );
        }

        presets = presetBuilder.build();

        ImmutableMap.Builder<String, NodePropertyConverter> converterBuilder = ImmutableMap.builder();

        for ( NodePropertyConverter converter : nodePropertyConverters )
        {
            converterBuilder.put( converter.name(), converter );
        }

        converters = converterBuilder.build();

        ImmutableMap.Builder<String, NodeTransformer> transformerBuilder = ImmutableMap.builder();

        for ( NodeTransformer transformer : nodeTransformers )
        {
            transformerBuilder.put( transformer.name(), transformer );
        }

        transformers = transformerBuilder.build();
    }

    @Override
    public <T extends IdentifiableObject> CollectionNode filter( Class<?> klass, List<T> objects, List<String> fieldList )
    {
        String fields = fieldList == null ? "" : Joiner.on( "," ).join( fieldList );

        Schema rootSchema = schemaService.getDynamicSchema( klass );

        CollectionNode collectionNode = new CollectionNode( rootSchema.getCollectionName() );
        collectionNode.setNamespace( rootSchema.getNamespace() );

        if ( objects == null || objects.isEmpty() )
        {
            return collectionNode;
        }

        FieldMap fieldMap = new FieldMap();
        Schema schema = schemaService.getDynamicSchema( objects.get( 0 ).getClass() );

        if ( fields == null )
        {
            for ( Property property : schema.getProperties() )
            {
                fieldMap.put( property.getName(), new FieldMap() );
            }
        }
        else
        {
            fieldMap = parserService.parseFieldFilter( fields );
        }

        for ( Object object : objects )
        {
            collectionNode.addChild( buildNode( fieldMap, klass, object ) );
        }

        return collectionNode;
    }

    @SuppressWarnings("unchecked")
    private AbstractNode buildNode( FieldMap fieldMap, Class<?> klass, Object object )
    {
        Schema schema = schemaService.getDynamicSchema( klass );

        ComplexNode complexNode = new ComplexNode( schema.getName() );
        complexNode.setNamespace( schema.getNamespace() );

        if ( object == null )
        {
            return new SimpleNode( schema.getName(), null );
        }

        updateFields( fieldMap, schema.getKlass() );

        for ( String fieldKey : fieldMap.keySet() )
        {
            AbstractNode child = null;

            if ( !schema.getPropertyMap().containsKey( fieldKey ) )
            {
                continue;
            }

            Property property = schema.getPropertyMap().get( fieldKey );

            Object returnValue = ReflectionUtils.invokeMethod( object, property.getGetterMethod() );
            Schema propertySchema = schemaService.getDynamicSchema( property.getKlass() );

            FieldMap fieldValue = fieldMap.get( fieldKey );

            if ( property.isCollection() )
            {
                updateFields( fieldValue, property.getItemKlass() );
            }
            else
            {
                updateFields( fieldValue, property.getKlass() );
            }

            if ( fieldValue.haveNodePropertyConverter() )
            {
                NodePropertyConverter converter = fieldValue.getNodePropertyConverter();

                if ( converter.canConvertTo( property, returnValue ) )
                {
                    child = (AbstractNode) converter.convertTo( property, returnValue );
                }
            }
            else if ( fieldValue.isEmpty() )
            {
                List<String> fields = presets.get( "identifiable" ).provide();

                if ( property.isCollection() )
                {
                    Collection<?> collection = (Collection<?>) returnValue;

                    child = complexNode.addChild( new CollectionNode( property.getCollectionName() ) );
                    child.setNamespace( property.getNamespace() );

                    if ( property.isIdentifiableObject() )
                    {
                        for ( Object collectionObject : collection )
                        {
                            child.addChild( getProperties( property, collectionObject, fields ) );
                        }
                    }
                    else if ( !property.isSimple() )
                    {
                        FieldMap map = getFullFieldMap( schemaService.getDynamicSchema( property.getItemKlass() ) );

                        for ( Object collectionObject : collection )
                        {
                            Node node = buildNode( map, property.getItemKlass(), collectionObject );

                            if ( !node.getChildren().isEmpty() )
                            {
                                child.addChild( node );
                            }
                        }
                    }
                    else
                    {
                        for ( Object collectionObject : collection )
                        {
                            child.addChild( new SimpleNode( property.getName(), collectionObject ) );
                        }
                    }
                }
                else if ( property.isIdentifiableObject() )
                {
                    child = getProperties( property, returnValue, fields );
                }
                else
                {
                    if ( propertySchema.getProperties().isEmpty() )
                    {
                        SimpleNode simpleNode = new SimpleNode( fieldKey, returnValue );
                        simpleNode.setAttribute( property.isAttribute() );
                        simpleNode.setNamespace( property.getNamespace() );

                        child = simpleNode;
                    }
                    else
                    {
                        child = buildNode( getFullFieldMap( propertySchema ), property.getKlass(), returnValue );
                    }
                }
            }
            else
            {
                if ( property.isCollection() )
                {
                    child = complexNode.addChild( new CollectionNode( property.getCollectionName() ) );
                    child.setNamespace( property.getNamespace() );

                    for ( Object collectionObject : (Collection<?>) returnValue )
                    {
                        Node node = buildNode( fieldValue, property.getItemKlass(), collectionObject );

                        if ( !node.getChildren().isEmpty() )
                        {
                            child.addChild( node );
                        }
                    }
                }
                else
                {
                    child = buildNode( fieldValue, property.getKlass(), returnValue );
                }
            }

            if ( child != null )
            {
                complexNode.addChild( fieldValue.getPipeline().process( child ) );
            }
        }

        return complexNode;
    }

    private void updateFields( FieldMap fieldMap, Class<?> klass )
    {
        // we need two run this (at least) two times, since some of the presets might contain other presets
        _updateFields( fieldMap, klass, true );
        _updateFields( fieldMap, klass, false );
    }

    private void _updateFields( FieldMap fieldMap, Class<?> klass, boolean expandOnly )
    {
        Schema schema = schemaService.getDynamicSchema( klass );
        List<String> cleanupFields = Lists.newArrayList();

        Pattern pattern = Pattern.compile( "(\\w+)(?:::(\\w+))?(?:\\|rename\\((\\w+)\\))?" );

        for ( String fieldKey : Sets.newHashSet( fieldMap.keySet() ) )
        {
            if ( "*".equals( fieldKey ) )
            {
                for ( String mapKey : schema.getPropertyMap().keySet() )
                {
                    if ( !fieldMap.containsKey( mapKey ) )
                    {
                        fieldMap.put( mapKey, new FieldMap() );
                    }
                }

                cleanupFields.add( fieldKey );
            }
            else if ( fieldKey.startsWith( ":" ) )
            {
                PresetProvider presetProvider = presets.get( fieldKey.substring( 1 ) );

                if ( presetProvider == null )
                {
                    continue;
                }

                List<String> fields = presetProvider.provide();

                for ( String field : fields )
                {
                    if ( !fieldMap.containsKey( field ) )
                    {
                        fieldMap.put( field, new FieldMap() );
                    }
                }

                cleanupFields.add( fieldKey );
            }
            else if ( fieldKey.startsWith( "!" ) && !expandOnly )
            {
                cleanupFields.add( fieldKey );
            }
            else if ( fieldKey.contains( "::" ) || fieldKey.contains( "|rename(" ) )
            {
                Matcher matcher = pattern.matcher( fieldKey );

                if ( !matcher.find() )
                {
                    continue;
                }

                FieldMap value = new FieldMap();

                if ( matcher.group( 2 ) != null )
                {
                    if ( converters.containsKey( matcher.group( 2 ) ) )
                    {
                        value.setNodePropertyConverter( converters.get( matcher.group( 2 ) ) );
                    }
                }

                if ( matcher.group( 3 ) != null )
                {
                    NodeTransformer transformer = transformers.get( "rename" );
                    value.getPipeline().addTransformer( transformer, Lists.newArrayList( matcher.group( 3 ) ) );
                }

                fieldMap.put( matcher.group( 1 ), value );

                cleanupFields.add( fieldKey );
            }
        }

        for ( String field : cleanupFields )
        {
            fieldMap.remove( field );

            if ( !expandOnly )
            {
                fieldMap.remove( field.substring( 1 ) );
            }
        }
    }

    private FieldMap getFullFieldMap( Schema schema )
    {
        FieldMap fieldMap = new FieldMap();

        for ( String mapKey : schema.getPropertyMap().keySet() )
        {
            fieldMap.put( mapKey, new FieldMap() );
        }

        return fieldMap;
    }

    private ComplexNode getProperties( Property currentProperty, Object object, List<String> fields )
    {
        if ( object == null )
        {
            return null;
        }

        ComplexNode complexNode = new ComplexNode( currentProperty.getName() );
        complexNode.setNamespace( currentProperty.getNamespace() );

        Schema schema;

        if ( currentProperty.isCollection() )
        {
            schema = schemaService.getDynamicSchema( currentProperty.getItemKlass() );

        }
        else
        {
            schema = schemaService.getDynamicSchema( currentProperty.getKlass() );
        }

        for ( String field : fields )
        {
            Property property = schema.getPropertyMap().get( field );

            if ( property == null )
            {
                continue;
            }

            Object returnValue = ReflectionUtils.invokeMethod( object, property.getGetterMethod() );

            SimpleNode simpleNode = new SimpleNode( field, returnValue );
            simpleNode.setAttribute( property.isAttribute() );
            simpleNode.setNamespace( property.getNamespace() );

            complexNode.addChild( simpleNode );
        }

        return complexNode;
    }
}

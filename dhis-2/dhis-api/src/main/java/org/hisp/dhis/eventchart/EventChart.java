package org.hisp.dhis.eventchart;

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
import java.util.Date;
import java.util.List;

import org.hisp.dhis.common.BaseAnalyticalObject;
import org.hisp.dhis.common.BaseIdentifiableObject;
import org.hisp.dhis.common.DxfNamespaces;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.view.DetailedView;
import org.hisp.dhis.common.view.DimensionalView;
import org.hisp.dhis.common.view.ExportView;
import org.hisp.dhis.i18n.I18nFormat;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramStage;
import org.hisp.dhis.user.User;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * @author Jan Henrik Overland
 */

public class EventChart
    extends BaseAnalyticalObject
{

    public static final String COUNT_TYPE_EVENTS = "events";

    public static final String COUNT_TYPE_TRACKED_ENTITY_INSTANCES = "tracked_entity_instances";

    public static final String TYPE_COLUMN = "column";

    public static final String TYPE_STACKED_COLUMN = "stackedcolumn";

    public static final String TYPE_BAR = "bar";

    public static final String TYPE_STACKED_BAR = "stackedbar";

    public static final String TYPE_LINE = "line";

    public static final String TYPE_AREA = "area";

    public static final String TYPE_PIE = "pie";

    public static final String TYPE_RADAR = "radar"; // Spider web

    private Program program;

    private ProgramStage programStage;

    private Date startDate;

    private Date endDate;

    private String type;

    private List<String> columnDimensions = new ArrayList<String>();

    private List<String> rowDimensions = new ArrayList<String>();

    private List<String> filterDimensions = new ArrayList<String>();

    private boolean hideEmptyRows;

    private String countType;

    private String domainAxisLabel;

    private String rangeAxisLabel;

    private boolean hideLegend;

    private boolean regression;

    private boolean hideTitle;

    private boolean hideSubtitle;

    private String title;

    private Double targetLineValue;

    private String targetLineLabel;

    private Double baseLineValue;

    private String baseLineLabel;

    private boolean showData;

    private Double rangeAxisMaxValue;

    private Double rangeAxisMinValue;

    private Integer rangeAxisSteps; // min 1

    private Integer rangeAxisDecimals;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public EventChart()
    {
    }

    public EventChart( String name )
    {
        this.name = name;
    }

    // -------------------------------------------------------------------------
    // AnalyticalObject
    // -------------------------------------------------------------------------

    @Override
    public void init( User user, Date date, OrganisationUnit organisationUnit,
        List<OrganisationUnit> organisationUnitsAtLevel, List<OrganisationUnit> organisationUnitsInGroups,
        I18nFormat format )
    {
    }

    @Override
    public void populateAnalyticalProperties()
    {
        for ( String column : columnDimensions )
        {
            columns.addAll( getDimensionalObjectList( column ) );
        }

        for ( String row : rowDimensions )
        {
            rows.addAll( getDimensionalObjectList( row ) );
        }

        for ( String filter : filterDimensions )
        {
            filters.addAll( getDimensionalObjectList( filter ) );
        }
    }

    // -------------------------------------------------------------------------
    // Getters and setters properties
    // -------------------------------------------------------------------------

    @JsonProperty
    @JsonSerialize( as = BaseIdentifiableObject.class )
    @JsonView( { DetailedView.class, ExportView.class, DimensionalView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public Program getProgram()
    {
        return program;
    }

    public void setProgram( Program program )
    {
        this.program = program;
    }

    @JsonProperty
    @JsonSerialize( as = BaseIdentifiableObject.class )
    @JsonView( { DetailedView.class, ExportView.class, DimensionalView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public ProgramStage getProgramStage()
    {
        return programStage;
    }

    public void setProgramStage( ProgramStage programStage )
    {
        this.programStage = programStage;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class, DimensionalView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate( Date startDate )
    {
        this.startDate = startDate;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class, DimensionalView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public Date getEndDate()
    {
        return endDate;
    }

    public void setEndDate( Date endDate )
    {
        this.endDate = endDate;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class, DimensionalView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public String getType()
    {
        return type;
    }

    public void setType( String type )
    {
        this.type = type;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlElementWrapper( localName = "columnDimensions", namespace = DxfNamespaces.DXF_2_0 )
    @JacksonXmlProperty( localName = "column", namespace = DxfNamespaces.DXF_2_0 )
    public List<String> getColumnDimensions()
    {
        return columnDimensions;
    }

    public void setColumnDimensions( List<String> columnDimensions )
    {
        this.columnDimensions = columnDimensions;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlElementWrapper( localName = "rowDimensions", namespace = DxfNamespaces.DXF_2_0 )
    @JacksonXmlProperty( localName = "row", namespace = DxfNamespaces.DXF_2_0 )
    public List<String> getRowDimensions()
    {
        return rowDimensions;
    }

    public void setRowDimensions( List<String> rowDimensions )
    {
        this.rowDimensions = rowDimensions;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class } )
    @JacksonXmlElementWrapper( localName = "filterDimensions", namespace = DxfNamespaces.DXF_2_0 )
    @JacksonXmlProperty( localName = "filter", namespace = DxfNamespaces.DXF_2_0 )
    public List<String> getFilterDimensions()
    {
        return filterDimensions;
    }

    public void setFilterDimensions( List<String> filterDimensions )
    {
        this.filterDimensions = filterDimensions;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class, DimensionalView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public boolean isHideEmptyRows()
    {
        return hideEmptyRows;
    }

    public void setHideEmptyRows( boolean hideEmptyRows )
    {
        this.hideEmptyRows = hideEmptyRows;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class, DimensionalView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public String getCountType()
    {
        return countType;
    }

    public void setCountType( String countType )
    {
        this.countType = countType;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class, DimensionalView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public String getDomainAxisLabel()
    {
        return domainAxisLabel;
    }

    public void setDomainAxisLabel( String domainAxisLabel )
    {
        this.domainAxisLabel = domainAxisLabel;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class, DimensionalView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public String getRangeAxisLabel()
    {
        return rangeAxisLabel;
    }

    public void setRangeAxisLabel( String rangeAxisLabel )
    {
        this.rangeAxisLabel = rangeAxisLabel;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class, DimensionalView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public boolean isHideLegend()
    {
        return hideLegend;
    }

    public void setHideLegend( boolean hideLegend )
    {
        this.hideLegend = hideLegend;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class, DimensionalView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public boolean isRegression()
    {
        return regression;
    }

    public void setRegression( boolean regression )
    {
        this.regression = regression;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class, DimensionalView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public boolean isHideTitle()
    {
        return hideTitle;
    }

    public void setHideTitle( boolean hideTitle )
    {
        this.hideTitle = hideTitle;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class, DimensionalView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public boolean isHideSubtitle()
    {
        return hideSubtitle;
    }

    public void setHideSubtitle( Boolean hideSubtitle )
    {
        this.hideSubtitle = hideSubtitle;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class, DimensionalView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public String getTitle()
    {
        return this.title;
    }

    public void setTitle( String title )
    {
        this.title = title;
    }    

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class, DimensionalView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public Double getTargetLineValue()
    {
        return targetLineValue;
    }

    public void setTargetLineValue( Double targetLineValue )
    {
        this.targetLineValue = targetLineValue;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class, DimensionalView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public String getTargetLineLabel()
    {
        return targetLineLabel;
    }

    public void setTargetLineLabel( String targetLineLabel )
    {
        this.targetLineLabel = targetLineLabel;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class, DimensionalView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public Double getBaseLineValue()
    {
        return baseLineValue;
    }

    public void setBaseLineValue( Double baseLineValue )
    {
        this.baseLineValue = baseLineValue;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class, DimensionalView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public String getBaseLineLabel()
    {
        return baseLineLabel;
    }

    public void setBaseLineLabel( String baseLineLabel )
    {
        this.baseLineLabel = baseLineLabel;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class, DimensionalView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public boolean isShowData()
    {
        return showData;
    }

    public void setShowData( boolean showData )
    {
        this.showData = showData;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class, DimensionalView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public Double getRangeAxisMaxValue()
    {
        return rangeAxisMaxValue;
    }

    public void setRangeAxisMaxValue( Double rangeAxisMaxValue )
    {
        this.rangeAxisMaxValue = rangeAxisMaxValue;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class, DimensionalView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public Double getRangeAxisMinValue()
    {
        return rangeAxisMinValue;
    }

    public void setRangeAxisMinValue( Double rangeAxisMinValue )
    {
        this.rangeAxisMinValue = rangeAxisMinValue;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class, DimensionalView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public Integer getRangeAxisSteps()
    {
        return rangeAxisSteps;
    }

    public void setRangeAxisSteps( Integer rangeAxisSteps )
    {
        this.rangeAxisSteps = rangeAxisSteps;
    }

    @JsonProperty
    @JsonView( { DetailedView.class, ExportView.class, DimensionalView.class } )
    @JacksonXmlProperty( namespace = DxfNamespaces.DXF_2_0 )
    public Integer getRangeAxisDecimals()
    {
        return rangeAxisDecimals;
    }

    public void setRangeAxisDecimals( Integer rangeAxisDecimals )
    {
        this.rangeAxisDecimals = rangeAxisDecimals;
    }

    // -------------------------------------------------------------------------
    // Merge with
    // -------------------------------------------------------------------------

    @Override
    public void mergeWith( IdentifiableObject other )
    {
        super.mergeWith( other );

        if ( other.getClass().isInstance( this ) )
        {
            EventChart eventChart = (EventChart) other;

            program = eventChart.getProgram();
            programStage = eventChart.getProgramStage();
            startDate = eventChart.getStartDate();
            endDate = eventChart.getEndDate();
            type = eventChart.getType();

            columnDimensions.clear();
            columnDimensions.addAll( eventChart.getColumnDimensions() );

            rowDimensions.clear();
            rowDimensions.addAll( eventChart.getRowDimensions() );

            filterDimensions.clear();
            filterDimensions.addAll( eventChart.getFilterDimensions() );

            hideEmptyRows = eventChart.isHideEmptyRows();
            countType = eventChart.getCountType();
            domainAxisLabel = eventChart.getDomainAxisLabel();
            rangeAxisLabel = eventChart.getRangeAxisLabel();            
            hideLegend = eventChart.isHideLegend();
            regression = eventChart.isRegression();            
            hideTitle = eventChart.isHideTitle();
            hideSubtitle = eventChart.isHideSubtitle();
            title = eventChart.getTitle();            
            targetLineValue = eventChart.getTargetLineValue();
            targetLineLabel = eventChart.getTargetLineLabel();
            baseLineValue = eventChart.getBaseLineValue();
            baseLineLabel = eventChart.getBaseLineLabel();            
            showData = eventChart.isShowData();
            rangeAxisMaxValue = eventChart.getRangeAxisMaxValue();
            rangeAxisMinValue = eventChart.getRangeAxisMinValue();
            rangeAxisSteps = eventChart.getRangeAxisSteps();
            rangeAxisDecimals = eventChart.getRangeAxisDecimals();
        }
    }
}

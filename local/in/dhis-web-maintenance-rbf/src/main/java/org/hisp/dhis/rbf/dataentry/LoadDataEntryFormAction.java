package org.hisp.dhis.rbf.dataentry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.velocity.tools.generic.NumberTool;
import org.hisp.dhis.attribute.AttributeValue;
import org.hisp.dhis.constant.Constant;
import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataelement.DataElementCategoryOptionCombo;
import org.hisp.dhis.dataelement.DataElementCategoryService;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.datavalue.DataValue;
import org.hisp.dhis.datavalue.DataValueService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupService;
import org.hisp.dhis.organisationunit.OrganisationUnitGroupSet;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.period.Period;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.rbf.api.LookupService;
import org.hisp.dhis.rbf.api.PBFDataValue;
import org.hisp.dhis.rbf.api.PBFDataValueService;
import org.hisp.dhis.rbf.api.TariffDataValueService;
import org.hisp.dhis.rbf.api.UtilizationRateService;
import org.hisp.dhis.user.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 */
public class LoadDataEntryFormAction implements Action
{
    private final static String TARIFF_SETTING_AUTHORITY = "TARIFF_SETTING_AUTHORITY";
    private final static String UTILIZATION_RULE_DATAELEMENT_ATTRIBUTE = "UTILIZATION_RULE_DATAELEMENT_ATTRIBUTE";
    private final static String UTILIZATION_RATE_DATAELEMENT_ID = "UTILIZATION_RATE_DATAELEMENT_ID";
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private PBFDataValueService pbfDataValueService;

    public void setPbfDataValueService( PBFDataValueService pbfDataValueService )
    {
        this.pbfDataValueService = pbfDataValueService;
    }

    private OrganisationUnitService organisationUnitService;

    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }

    private DataSetService dataSetService;

    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }

    private DataValueService dataValueService;

    public void setDataValueService( DataValueService dataValueService )
    {
        this.dataValueService = dataValueService;
    }

    private LookupService lookupService;

    public void setLookupService( LookupService lookupService )
    {
        this.lookupService = lookupService;
    }

    private DataElementCategoryService dataElementCategoryService;

    public void setDataElementCategoryService( DataElementCategoryService dataElementCategoryService )
    {
        this.dataElementCategoryService = dataElementCategoryService;
    }

    private TariffDataValueService tariffDataValueService;

    public void setTariffDataValueService( TariffDataValueService tariffDataValueService )
    {
        this.tariffDataValueService = tariffDataValueService;
    }

    private CurrentUserService currentUserService;

    public void setCurrentUserService( CurrentUserService currentUserService )
    {
        this.currentUserService = currentUserService;
    }

    private ConstantService constantService;

    public void setConstantService( ConstantService constantService )
    {
        this.constantService = constantService;
    }
   
    @Autowired
    private OrganisationUnitGroupService orgUnitGroupService;
    
    private UtilizationRateService utilizationRateService;
    
    public void setUtilizationRateService( UtilizationRateService utilizationRateService )
    {
        this.utilizationRateService = utilizationRateService;
    }
    
    @Autowired
    private DataElementService dataElementService;
    
    @Autowired
    private DataElementCategoryService categoryService;
    // -------------------------------------------------------------------------
    // Comparator
    // -------------------------------------------------------------------------
    /*
     * private Comparator<DataElement> dataElementComparator;
     * 
     * public void setDataElementComparator( Comparator<DataElement>
     * dataElementComparator ) { this.dataElementComparator =
     * dataElementComparator; }
     */
    // -------------------------------------------------------------------------
    // Input/Output
    // -------------------------------------------------------------------------

    private Map<DataElement, PBFDataValue> pbfDataValueMap;

    public Map<DataElement, PBFDataValue> getPbfDataValueMap()
    {
        return pbfDataValueMap;
    }

    private String orgUnitId;

    public void setOrgUnitId( String orgUnitId )
    {
        this.orgUnitId = orgUnitId;
    }

    private int dataSetId;

    public void setDataSetId( int dataSetId )
    {
        this.dataSetId = dataSetId;
    }

    private String selectedPeriodId;

    public void setSelectedPeriodId( String selectedPeriodId )
    {
        this.selectedPeriodId = selectedPeriodId;
    }

    private List<DataElement> dataElements = new ArrayList<DataElement>();

    public List<DataElement> getDataElements()
    {
        return dataElements;
    }

    private OrganisationUnit organisationUnit;

    public OrganisationUnit getOrganisationUnit()
    {
        return organisationUnit;
    }

    public Map<String, String> dataValueMap;

    public Map<String, String> getDataValueMap()
    {
        return dataValueMap;
    }

    private DataSet dataSet;

    public DataSet getDataSet()
    {
        return dataSet;
    }

    private Period period;

    public Period getPeriod()
    {
        return period;
    }

    private List<DataElementCategoryOptionCombo> optionCombos = new ArrayList<DataElementCategoryOptionCombo>();

    public List<DataElementCategoryOptionCombo> getOptionCombos()
    {
        return optionCombos;
    }

    private DataElementCategoryOptionCombo tariffOptCombo;

    public DataElementCategoryOptionCombo getTariffOptCombo()
    {
        return tariffOptCombo;
    }

    private DataElementCategoryOptionCombo qValOptCombo;

    public DataElementCategoryOptionCombo getqValOptCombo()
    {
        return qValOptCombo;
    }

    private NumberTool nullTool;
    
    public NumberTool getNullTool()
    {
        return nullTool;
    }
    
    public Map<Integer, String> utilizationRatesMap = new HashMap<Integer, String>();
    
    public Map<Integer, String> getUtilizationRatesMap()
    {
        return utilizationRatesMap;
    }
    
    private List<DataElement> utilizationRateDataElements = new ArrayList<DataElement>();
    
    public List<DataElement> getUtilizationRateDataElements()
    {
        return utilizationRateDataElements;
    }
    
    private String utilizationRate = "";

    public String getUtilizationRate()
    {
        return utilizationRate;
    }
    
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------


    public String execute()
    {
        dataValueMap = new HashMap<String, String>();

        //Lookup lookup = lookupService.getLookupByName( Lookup.OC_TARIFF );

        //Lookup lookup2 = lookupService.getLookupByName( Lookup.QV_TARIFF );

        //tariffOptCombo = dataElementCategoryService.getDataElementCategoryOptionCombo( Integer.parseInt( lookup.getValue() ) );

        //qValOptCombo = dataElementCategoryService.getDataElementCategoryOptionCombo( Integer.parseInt( lookup.getValue() ) );

        organisationUnit = organisationUnitService.getOrganisationUnit( orgUnitId );

        dataSet = dataSetService.getDataSet( dataSetId );

        period = PeriodType.getPeriodFromIsoString( selectedPeriodId );

        dataElements = new ArrayList<DataElement>( dataSet.getDataElements() );

        Collections.sort( dataElements );

        optionCombos = new ArrayList<DataElementCategoryOptionCombo>();

        Map<Integer, Double> tariffDataValueMap = new HashMap<Integer, Double>();

        // find parent
        Constant tariff_authority = constantService.getConstantByName( TARIFF_SETTING_AUTHORITY );
        int tariff_setting_authority = 0;
        if ( tariff_authority == null )
        {
            tariff_setting_authority = 3;

        }
        else
        {
            tariff_setting_authority = (int) tariff_authority.getValue();
        }

        OrganisationUnitGroup orgUnitGroup = findPBFOrgUnitGroupforTariff( organisationUnit );
        
        List<OrganisationUnit> orgUnitBranch = organisationUnitService.getOrganisationUnitBranch( organisationUnit.getId() );
        String orgUnitBranchIds = "-1";
        for( OrganisationUnit orgUnit : orgUnitBranch )
        {
        	orgUnitBranchIds += "," + orgUnit.getId();
        }
        
        if( orgUnitGroup != null )
        {
            tariffDataValueMap.putAll( tariffDataValueService.getTariffDataValues( orgUnitGroup, orgUnitBranchIds, dataSet, period ) );
        }
        
        /*
        OrganisationUnit parentOrgunit = findParentOrgunitforTariff( organisationUnit, tariff_setting_authority );

        if ( parentOrgunit != null )
        {
            tariffDataValueMap.putAll( tariffDataValueService.getTariffDataValues( parentOrgunit, dataSet, period ) );
        }
        */
        

        pbfDataValueMap = new HashMap<DataElement, PBFDataValue>();

        Set<PBFDataValue> pbfDataValues = new HashSet<PBFDataValue>( pbfDataValueService.getPBFDataValues( organisationUnit, dataSet, period ) );
        for ( PBFDataValue pbfDataValue : pbfDataValues )
        {
            DataElement de = pbfDataValue.getDataElement();
            if ( pbfDataValue.getTariffAmount() == null )
            {
                Double tariffAmount = tariffDataValueMap.get( de.getId() );
                if ( tariffAmount != null )
                {
                    pbfDataValue.setStoredBy( currentUserService.getCurrentUsername() );
                    pbfDataValue.setTariffAmount( tariffAmount );
                    pbfDataValue.setTimestamp( new Date() );
                    pbfDataValueService.updatePBFDataValue( pbfDataValue );
                }
            }
            pbfDataValueMap.put( de, pbfDataValue );
        }

        Set<DataElement> tempDes = new HashSet<DataElement>();
        tempDes.addAll( dataElements );

        tempDes.removeAll( pbfDataValueMap.keySet() );

        for ( DataElement de : tempDes )
        {
            Double tariffAmount = tariffDataValueMap.get( de.getId() );
            if ( tariffAmount != null )
            {
                PBFDataValue pbfDataValue = new PBFDataValue();

                pbfDataValue.setDataSet( dataSet );
                pbfDataValue.setDataElement( de );
                pbfDataValue.setPeriod( period );
                pbfDataValue.setOrganisationUnit( organisationUnit );
                pbfDataValue.setTariffAmount( tariffAmount );
                pbfDataValue.setStoredBy( currentUserService.getCurrentUsername() );
                pbfDataValue.setTimestamp( new Date() );

                //pbfDataValueService.addPBFDataValue( pbfDataValue );
                pbfDataValueMap.put( de, pbfDataValue );
            }
            else
            {
                PBFDataValue pbfDataValue = new PBFDataValue();

                pbfDataValue.setDataSet( dataSet );
                pbfDataValue.setDataElement( de );
                pbfDataValue.setPeriod( period );
                pbfDataValue.setOrganisationUnit( organisationUnit );
                pbfDataValue.setTariffAmount( null );

                pbfDataValueMap.put( de, pbfDataValue );
            }
        }
        

        /*
         * for( DataElement dataElement : dataElements ) {
         * //DataElementCategoryOptionCombo decoc =
         * dataElementCategoryService.getDefaultDataElementCategoryOptionCombo
         * ();
         * 
         * DataElementCategoryCombo dataElementCategoryCombo =
         * dataElement.getCategoryCombo();
         * 
         * optionCombos = new ArrayList<DataElementCategoryOptionCombo>(
         * dataElementCategoryCombo.getOptionCombos() );
         * 
         * for( DataElementCategoryOptionCombo decombo : optionCombos ) {
         * DataValue dataValue = new DataValue();
         * 
         * dataValue = dataValueService.getDataValue( dataElement, period,
         * organisationUnit, decombo );
         * 
         * String value = "";
         * 
         * if ( dataValue != null ) { value = dataValue.getValue(); } else { if(
         * decombo.getId() == tariffOptCombo.getId() ) { Double tariffValue =
         * tariffDataValueMap.get( dataElement.getId() );
         * 
         * if( tariffValue != null ) { value = tariffValue+"";
         * 
         * dataValue = new DataValue( ); dataValue.setDataElement(dataElement);
         * dataValue.setPeriod(period); dataValue.setSource(organisationUnit);
         * dataValue.setValue( value ); dataValue.setStoredBy(
         * currentUserService.getCurrentUsername() ); dataValue.setTimestamp(
         * new Date() ); dataValue.setCategoryOptionCombo( decombo );
         * 
         * dataValueService.addDataValue( dataValue ); } } }
         * 
         * String key = dataElement.getId()+ ":" + decombo.getId();
         * 
         * dataValueMap.put( key, value ); }
         * 
         * }
         */

        /*
         * for( DataElementCategoryOptionCombo decombo : optionCombos ) {
         * System.out.println(" decombo ---" + decombo.getId() +" -- " +
         * decombo.getName() ); }
         */
        
        
        Constant utilizationRuleAttribute = constantService.getConstantByName( UTILIZATION_RULE_DATAELEMENT_ATTRIBUTE );
        
        utilizationRateDataElements = new ArrayList<DataElement>(); 
        
        for ( DataElement de : dataElements )
        {
            Set<AttributeValue> attrValueSet = new HashSet<AttributeValue>( de.getAttributeValues() );
            for ( AttributeValue attValue : attrValueSet )
            {
                if ( attValue.getAttribute().getId() == utilizationRuleAttribute.getValue() && attValue.getValue().equalsIgnoreCase( "true" ) )
                {
                    utilizationRateDataElements.add( de );
                }
            }
        }
        
        utilizationRatesMap = new HashMap<Integer, String>( utilizationRateService.getUtilizationRates() );
        
        /*
        for( Integer key : utilizationRatesMap.keySet() )
        {
            System.out.println( " Key is  ---" + key + " -- Value " + utilizationRatesMap.get( key ) );
        }
        */
        
        //-------------------------------------------------------------
        // Availbale Amount
        //-------------------------------------------------------------
        Constant paymentAmount = constantService.getConstantByName( UTILIZATION_RATE_DATAELEMENT_ID );
        DataElement dataElement = dataElementService.getDataElement( (int) paymentAmount.getValue() );
        DataElementCategoryOptionCombo optionCombo = categoryService.getDefaultDataElementCategoryOptionCombo();
        DataValue dataValue = dataValueService.getDataValue( dataElement, period, organisationUnit, optionCombo );
        if ( dataValue != null )
        {
            utilizationRate = dataValue.getValue();
        }        
        
      
        return SUCCESS;
    }

    public OrganisationUnitGroup findPBFOrgUnitGroupforTariff( OrganisationUnit organisationUnit )
    {
    	Constant tariff_authority = constantService.getConstantByName( TARIFF_SETTING_AUTHORITY );
    	
    	OrganisationUnitGroupSet orgUnitGroupSet = orgUnitGroupService.getOrganisationUnitGroupSet( (int) tariff_authority.getValue() );
    	
    	OrganisationUnitGroup orgUnitGroup = organisationUnit.getGroupInGroupSet( orgUnitGroupSet );
    	
    	return orgUnitGroup;
    }
    
    public OrganisationUnit findParentOrgunitforTariff( OrganisationUnit organisationUnit, Integer tariffOULevel )
    {
        Integer ouLevel = organisationUnitService.getLevelOfOrganisationUnit( organisationUnit.getId() );
        if ( tariffOULevel == ouLevel )
        {
            return organisationUnit;
        }
        else
        {
            return findParentOrgunitforTariff( organisationUnit.getParent(), tariffOULevel );
        }
    }

}

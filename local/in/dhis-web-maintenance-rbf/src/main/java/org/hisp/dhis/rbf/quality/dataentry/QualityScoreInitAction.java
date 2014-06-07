package org.hisp.dhis.rbf.quality.dataentry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hisp.dhis.constant.ConstantService;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.dataset.DataSetService;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.ouwt.manager.OrganisationUnitSelectionManager;
import org.hisp.dhis.rbf.api.Lookup;
import org.hisp.dhis.rbf.api.LookupService;

import com.opensymphony.xwork2.Action;

/**
 * @author Mithilesh Kumar Thakur
 */
public class QualityScoreInitAction implements Action
{
	private final static String TARIFF_SETTING_AUTHORITY = "MAXSCORE_SETTING_AUTHORITY";
    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private OrganisationUnitSelectionManager selectionManager;

    public void setSelectionManager( OrganisationUnitSelectionManager selectionManager )
    {
        this.selectionManager = selectionManager;
    }
    
    private OrganisationUnitService organisationUnitService;
    
    public void setOrganisationUnitService( OrganisationUnitService organisationUnitService )
    {
        this.organisationUnitService = organisationUnitService;
    }
    
    private LookupService lookupService;
    
    public void setLookupService( LookupService lookupService )
    {
        this.lookupService = lookupService;
    }

    private DataSetService dataSetService;
    
    public void setDataSetService( DataSetService dataSetService )
    {
        this.dataSetService = dataSetService;
    }
    
    private ConstantService constantService;

    public void setConstantService( ConstantService constantService )
    {
        this.constantService = constantService;
    }
    // -------------------------------------------------------------------------
    // Input/output
    // -------------------------------------------------------------------------

    private OrganisationUnit organisationUnit;

    public OrganisationUnit getOrganisationUnit()
    {
        return organisationUnit;
    }    
    
    private String orgUnitId;
    
    public void setOrgUnitId( String orgUnitId )
    {
        this.orgUnitId = orgUnitId;
    }
    
    private List<DataSet> dataSets = new ArrayList<DataSet>();
    
    public List<DataSet> getDataSets()
    {
        return dataSets;
    }
    private String tariff_setting_authority;

    public String getTariff_setting_authority()
    {
        return tariff_setting_authority;
    }

    private List<String> levelOrgUnitIds = new ArrayList<String>();

    public List<String> getLevelOrgUnitIds()
    {
        return levelOrgUnitIds;
    }
    // -------------------------------------------------------------------------
    // Action implementation
    // -------------------------------------------------------------------------

    public String execute() throws Exception
    {
        //selectionManager.clearSelectedOrganisationUnits();
    	 
        organisationUnit = selectionManager.getSelectedOrganisationUnit();
        
        if( organisationUnit == null )
        {
            System.out.println("Organisationunit is null");
        }
        else
        {
            System.out.println("Organisationunit is not null ---" + organisationUnit.getId() );
        }
        
        if( organisationUnit == null && orgUnitId != null )
        {
            organisationUnit = organisationUnitService.getOrganisationUnit( orgUnitId );
           
        }
       // List<OrganisationUnit> organisationUnitList = new ArrayList<OrganisationUnit>( organisationUnitService.getLeafOrganisationUnits(organisationUnit.getId()) ) ;
        
        dataSets = new ArrayList<DataSet>( organisationUnit.getDataSets() );
        
        List<Lookup> lookups = new ArrayList<Lookup>( lookupService.getAllLookupsByType( Lookup.DS_QUALITY_TYPE ) );
        
        List<DataSet> pbfDataSets = new ArrayList<DataSet>();
        
        for( Lookup lookup : lookups )
        {
            Integer dataSetId = Integer.parseInt( lookup.getValue() );
            
            DataSet dataSet = dataSetService.getDataSet( dataSetId );
            if( dataSet != null )
            {
                pbfDataSets.add(dataSet);
           
            }
        }
        
        dataSets.retainAll( pbfDataSets );
        Collections.sort(dataSets);
        return SUCCESS;
    }
}


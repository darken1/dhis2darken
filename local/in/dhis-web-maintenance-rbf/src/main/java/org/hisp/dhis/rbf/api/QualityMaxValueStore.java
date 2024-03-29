package org.hisp.dhis.rbf.api;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.hisp.dhis.dataelement.DataElement;
import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.organisationunit.OrganisationUnit;
import org.hisp.dhis.organisationunit.OrganisationUnitGroup;
import org.hisp.dhis.period.Period;


public interface QualityMaxValueStore
{
	String ID = QualityMaxValueStore.class.getName();
	
	void addQuantityMaxValue( QualityMaxValue qualityMaxValue );
    
    void updateQuantityMaxValue( QualityMaxValue qualityMaxValue );
    
    void deleteQuantityMaxValue( QualityMaxValue qualityMaxValue );
    
    Collection<QualityMaxValue> getAllQuanlityMaxValues();
    
    Collection<QualityMaxValue> getQuanlityMaxValues( OrganisationUnit organisationUnit, DataSet dataSet );
    
    Collection<QualityMaxValue> getQuanlityMaxValues( OrganisationUnit organisationUnit, DataElement dataElement );
    
    QualityMaxValue getQualityMaxValue( OrganisationUnit organisationUnit, DataElement dataElement, DataSet dataSet, Date startDate ,Date endDate);
    
    QualityMaxValue getQualityMaxValue( OrganisationUnitGroup orgUnitGroup, OrganisationUnit organisationUnit, DataElement dataElement, DataSet dataSet,Date startDate ,Date endDate);
    
    Collection<QualityMaxValue> getQuanlityMaxValues( OrganisationUnitGroup orgUnitGroup, OrganisationUnit organisationUnit, DataElement dataElement);
    
    Collection<QualityMaxValue> getQuanlityMaxValues( OrganisationUnitGroup orgUnitGroup, OrganisationUnit organisationUnit, DataSet dataSet);
    
    Map<Integer, Double> getQualityMaxValues( OrganisationUnitGroup orgUnitGroup, String orgUnitBranchIds, DataSet dataSet, Period period );
    
}

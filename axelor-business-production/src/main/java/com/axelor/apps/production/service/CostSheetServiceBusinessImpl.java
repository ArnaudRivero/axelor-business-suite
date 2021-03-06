/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2015 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.production.service;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.production.db.CostSheetLine;
import com.axelor.apps.production.db.ProdHumanResource;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;

public class CostSheetServiceBusinessImpl extends CostSheetServiceImpl  {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject
	public CostSheetServiceBusinessImpl(GeneralService generalService, UnitConversionService unitConversionService, CostSheetLineService costSheetLineService, BillOfMaterialRepository billOfMaterialRepo) {
		
		super(generalService, unitConversionService, costSheetLineService, billOfMaterialRepo);
		
	}


	@Override
	protected void _computeHumanResourceCost(ProdHumanResource prodHumanResource, int priority, int bomLevel, CostSheetLine parentCostSheetLine) throws AxelorException  {

		Employee employee = prodHumanResource.getEmployee();
		
		if(employee != null)  {

			BigDecimal durationHours = new BigDecimal(prodHumanResource.getDuration()/3600);
			
			costSheet.addCostSheetLineListItem(
					costSheetLineService.createWorkCenterCostSheetLine(prodHumanResource.getWorkCenter(), priority, bomLevel, parentCostSheetLine, 
							durationHours, employee.getHourlyRate().multiply(durationHours), hourUnit));
			
		}
		else  {

			super._computeHumanResourceCost(prodHumanResource, priority, bomLevel, parentCostSheetLine);

		}
		
		

	}
	
}

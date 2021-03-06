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

import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.administration.SequenceService;
//import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.db.repo.ProductionOrderRepository;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ProductionOrderServiceImpl implements ProductionOrderService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Inject
	private ManufOrderService manufOrderService;
	
	@Inject
	private SequenceService sequenceService;
	
	@Inject
	protected ProductionOrderRepository productionOrderRepo;
	
	
	public ProductionOrder createProductionOrder() throws AxelorException  {
		
		return new ProductionOrder(this.getProductionOrderSeq());
		
		
	}
	
	
	
	public String getProductionOrderSeq() throws AxelorException  {
		
		String seq = sequenceService.getSequenceNumber(IAdministration.PRODUCTION_ORDER);
		
		if(seq == null)  {
			throw new AxelorException(I18n.get(IExceptionMessage.PRODUCTION_ORDER_SEQ), IException.CONFIGURATION_ERROR);
		}
		
		return seq;
	}
	
	
	/**
	 * Generate a Production Order
	 * @param product
	 * 		Product must be passed in param because product can be different of bill of material product (Product variant)
	 * @param billOfMaterial
	 * @param qtyRequested
	 * @param businessProject
	 * @return
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public ProductionOrder generateProductionOrder(Product product, BillOfMaterial billOfMaterial, BigDecimal qtyRequested) throws AxelorException  {
		
		ProductionOrder productionOrder = this.createProductionOrder();
		
		this.addManufOrder(productionOrder, product, billOfMaterial, qtyRequested);
		
		return productionOrderRepo.save(productionOrder);
		
	}
	
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public ProductionOrder addManufOrder(ProductionOrder productionOrder, Product product, BillOfMaterial billOfMaterial, BigDecimal qtyRequested) throws AxelorException  {
		
		ManufOrder manufOrder = manufOrderService.generateManufOrder(
				product, 
				qtyRequested, 
				ManufOrderService.DEFAULT_PRIORITY, 
				ManufOrderService.IS_TO_INVOICE, 
				billOfMaterial, 
				new LocalDateTime());
		
		productionOrder.addManufOrderListItem(manufOrder);
		
		return productionOrderRepo.save(productionOrder);
		
	}
	
}

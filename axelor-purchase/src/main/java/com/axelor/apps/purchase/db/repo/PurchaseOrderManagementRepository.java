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
package com.axelor.apps.purchase.db.repo;

import javax.persistence.PersistenceException;

import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.service.PurchaseOrderService;
import com.axelor.inject.Beans;

public class PurchaseOrderManagementRepository extends PurchaseOrderRepository {

	@Override
	public PurchaseOrder copy(PurchaseOrder entity, boolean deep) {
		entity.setStatusSelect(1);
		entity.setPurchaseOrderSeq(null);
		return super.copy(entity, deep);
 	}

	@Override
	public PurchaseOrder save(PurchaseOrder purchaseOrder) {

		try{
			Beans.get(PurchaseOrderService.class).setDraftSequence(purchaseOrder);
			return super.save(purchaseOrder);
		} catch(Exception e){
			throw new PersistenceException(e.getLocalizedMessage());
		}
	}

}

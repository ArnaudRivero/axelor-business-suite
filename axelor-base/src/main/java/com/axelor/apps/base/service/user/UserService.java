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
package com.axelor.apps.base.service.user;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Team;
import com.axelor.auth.db.User;
import com.axelor.meta.db.MetaFile;

/**
 * UserService is a class that implement all methods for user informations
 * 
 */
public interface UserService {

	/**
	 * Method that return the current connected user
	 * 
	 * @return user
	 * 		the current connected user
	 */
	public User getUser();
	
	/**
	 * Method that return the id of the current connected user
	 * 
	 * @return user
	 * 		the id of current connected user
	 */
	public Long getUserId();
	
	/**
	 * Method that return the active company of the current connected user
	 * 
	 * @return Company
	 * 		the active company
	 */
	public Company getUserActiveCompany();

	/**
	 * Method that return the active company id of the current connected user
	 * 
	 * @return Company
	 * 		the active company id
	 */
	public Long getUserActiveCompanyId(); 
	
	/**
	 * Method that return the active team of the current connected user
	 * 
	 * @return Team
	 * 		the active team
	 */
	public MetaFile getUserActiveCompanyLogo();
	
	/**
	 * Method that return the active team of the current connected user
	 * 
	 * @return Team
	 * 		the active team
	 */
	public Team getUserActiveTeam();
	
	/**
	 * Method that return the active team of the current connected user
	 * 
	 * @return Team
	 * 		the active team id
	 */
	public Long getUserActiveTeamId();
	
	/**
	 * Method that return the partner of the current connected user
	 * 
	 * @return Partner
	 * 		the user partner
	 */
	public Partner getUserPartner();
}

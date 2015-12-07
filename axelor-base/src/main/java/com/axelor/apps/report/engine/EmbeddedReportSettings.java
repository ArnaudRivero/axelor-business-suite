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
package com.axelor.apps.report.engine;

import java.io.IOException;

import org.eclipse.birt.core.exception.BirtException;

import com.axelor.app.internal.AppFilter;
import com.axelor.inject.Beans;
import com.axelor.report.ReportGenerator;

public class EmbeddedReportSettings  extends ReportSettings  {
	
	
	public EmbeddedReportSettings(String rptdesign, String outputName)  {
		
		super(rptdesign, outputName);
		
	}
	
	
	@Override
	public EmbeddedReportSettings generate() throws IOException, BirtException  {
		
		super.generate();
		
		final ReportGenerator generator = Beans.get(ReportGenerator.class);

		this.output = generator.generate(rptdesign, format, params, AppFilter.getLocale());

		this.attach();
        
		return this;
        
	}
	
	
}

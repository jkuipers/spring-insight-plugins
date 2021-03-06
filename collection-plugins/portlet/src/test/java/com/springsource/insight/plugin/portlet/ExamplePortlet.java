/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.springsource.insight.plugin.portlet;

import java.io.IOException;
import java.util.Random;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.springsource.insight.util.StringUtil;

public class ExamplePortlet extends GenericPortlet {
	public static final String NAME="example-portlet";
	
	protected static final String VIEW_PREFIX = "/";
	protected static final String VIEW_SUFFIX = ".jsp";
	protected static final String OPERATOR_PREF = "example.operator";
	
	protected final Random random = new Random();
	public ExamplePortlet () {
		super();
	}

	@Override
	public void render(RenderRequest request, RenderResponse response)
			throws PortletException, IOException {
		
		// Set title
		response.setTitle("Example Portlet");
		
		// Check the operator being used in equations ('+' or '-')
		String operator = getOperator(request);
		PortletMode	mode=request.getPortletMode();
		
		// Choose view
		String view;
		if (PortletMode.VIEW.equals(mode)) {
			view = "ask";
			request.setAttribute("term1", new Integer(random.nextInt(10)));
			request.setAttribute("term2", new Integer(random.nextInt(10)));
			request.setAttribute("operator", operator);
			if (request.getParameter("answerCorrect") != null) {
				request.setAttribute("answerCorrect", Boolean.valueOf(request.getParameter("answerCorrect")));
			}
		}
		else if (PortletMode.EDIT.equals(mode)) {
			view = "edit";
			request.setAttribute("operator", operator);
		}
		else if (PortletMode.HELP.equals(mode)) {
			view = "help";
		} else {
			throw new PortletException("Unsupported portlet mode: " + mode);
		}
		
		// Render view as a JSP page
		PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher(VIEW_PREFIX + view + VIEW_SUFFIX);
		dispatcher.include(request, response);
	}

	@Override
	public void processAction(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {
		
		// Check, based on mode, if this is an equation answer or configuration request
		PortletMode	mode=request.getPortletMode();
		if (PortletMode.VIEW.equals(mode)) {
			processEquationAnswer(request, response);
		}
		else if (PortletMode.EDIT.equals(mode)) {
			processConfigurationRequest(request, response);
		}
	}

	protected void processEquationAnswer(ActionRequest request, ActionResponse response) {
		String	answer=request.getParameter("answer");
		if (StringUtil.isEmpty(answer)) {
			return;
		}

		int term1 = Integer.parseInt(request.getParameter("term1"));
		int term2 = Integer.parseInt(request.getParameter("term2"));
		int result;
		String operator = getOperator(request);
		if ("+".equals(operator)) {
			result = term1 + term2;
		} else {
			result = term1 - term2;
		}

		if (result == Integer.parseInt(answer)) {
			response.setRenderParameter("answerCorrect", "true");
		} else {
			response.setRenderParameter("answerCorrect", "false");
		}
	}

	protected void processConfigurationRequest(ActionRequest request, ActionResponse response) 
		throws PortletException {
		String operator = request.getParameter("operator");
		PortletPreferences	preferences=request.getPreferences();
		preferences.setValue(OPERATOR_PREF, operator);
	}
	
	protected String getOperator(PortletRequest request) {
		PortletPreferences	preferences=request.getPreferences();
		return preferences.getValue(OPERATOR_PREF, "+");		
	}
}

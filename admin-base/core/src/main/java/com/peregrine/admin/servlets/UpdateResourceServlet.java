package com.peregrine.admin.servlets;

/*-
 * #%L
 * admin base - Core
 * %%
 * Copyright (C) 2017 headwire inc.
 * %%
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * #L%
 */

import com.peregrine.admin.resource.ResourceManagement;
import com.peregrine.admin.resource.ResourceManagement.ManagementException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;

import static com.peregrine.util.PerUtil.EQUALS;
import static com.peregrine.util.PerUtil.PER_PREFIX;
import static com.peregrine.util.PerUtil.PER_VENDOR;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Update Resource servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + "POST",
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + "api/admin/updateResource"
    }
)
@SuppressWarnings("serial")
public class UpdateResourceServlet extends AbstractBaseServlet {

    @Reference
    ModelFactory modelFactory;

    @Reference
    ResourceManagement resourceManagement;

    @Override
    Response handleRequest(Request request) throws IOException {
        String path = request.getParameter("path");
        String content = request.getParameter("content");
        try {
            Resource updatePage = resourceManagement.updateResource(request.getResourceResolver(), path, content);
            return new JsonResponse()
                .writeAttribute("type", "page").writeAttribute("status", "created")
                .writeAttribute("name", updatePage.getName()).writeAttribute("path", updatePage.getPath());
        } catch (ManagementException e) {
            return new ErrorResponse().setHttpErrorCode(SC_BAD_REQUEST).setErrorMessage("Failed to Update Page").setRequestPath(path).setException(e);
        }
    }

}

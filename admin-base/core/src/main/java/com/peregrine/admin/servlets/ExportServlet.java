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

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_EXPORT;
import static com.peregrine.commons.util.PerConstants.DATA;
import static com.peregrine.commons.util.PerConstants.DATA_JSON_EXTENSION;
import static com.peregrine.commons.util.PerConstants.JACKSON;
import static com.peregrine.commons.util.PerConstants.JCR_CONTENT;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static com.peregrine.commons.util.PerUtil.GET;
import static com.peregrine.commons.util.PerUtil.PER_PREFIX;
import static com.peregrine.commons.util.PerUtil.PER_VENDOR;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

import com.peregrine.commons.servlets.AbstractBaseServlet;
import java.io.IOException;
import javax.servlet.Servlet;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;
import org.apache.sling.models.factory.ExportException;
import org.apache.sling.models.factory.MissingExporterException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
/**
 * Forwards the Request to .data.json page rendering and replacing any
 * selectors for 'data'
 *
 * The API Definition can be found in the Swagger Editor configuration:
 *    ui.apps/src/main/content/jcr_root/perapi/definitions/admin.yaml
 */
@Component(
    service = Servlet.class,
    property = {
        SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Content Servlet",
        SERVICE_VENDOR + EQUALS + PER_VENDOR,
        SLING_SERVLET_METHODS + EQUALS + GET,
        SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_EXPORT
    }
)
@SuppressWarnings("serial")
public class ExportServlet extends AbstractBaseServlet {

    private final Logger log = LoggerFactory.getLogger(ExportServlet.class);

    @Reference
    ModelFactory modelFactory;

    @Override
    protected Response handleRequest(Request request) throws IOException {

        String suffix = request.getSuffix();
        log.info("===== 76 suffix " + suffix);
        if(suffix.endsWith(DATA_JSON_EXTENSION)) {
            suffix = suffix.substring(0, suffix.indexOf(DATA_JSON_EXTENSION));
        }
        log.info("===== 83 suffix " + suffix);
        Resource res = request.getResourceByPath(suffix);
        log.info("===== 85 res " + res);

        Resource content = res.getChild(JCR_CONTENT);
        log.info("===== 88 content " + content);

        try {
            // if(content == null) return Collections.<String, String> emptyMap();
            Map page = modelFactory
                    .exportModelForResource(content, JACKSON, Map.class, Collections.emptyMap());

            log.info("===== 95 page " + page);
        } catch (ExportException e) {
            log.error("not able to export model", e);
        } catch (MissingExporterException e) {
            log.error("not able to find exporter for model", e);
        }

        RequestDispatcherOptions rdOptions = new RequestDispatcherOptions();
        rdOptions.setReplaceSelectors(DATA);


        return new ForwardResponse(res, rdOptions);
    }
}


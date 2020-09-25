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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import com.peregrine.adaption.PerPage;
import com.peregrine.adaption.PerPageManager;

import javax.script.Bindings;
import org.apache.sling.api.SlingHttpServletRequest;
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

        log.info("====== 99 request.getClass().getName() " + request.getClass().getName());
        SlingHttpServletRequest slingHttpServletRequest = request.getRequest();

        String language = request.getParameter("language");
        log.info("====== 95 language " + language);

        Locale locale = new Locale(language);
        log.info("===== 99 locale " + locale);

        ResourceBundle bundle = null;
        bundle = slingHttpServletRequest.getResourceBundle(locale);


        JsonResponse jsonResponse = new JsonResponse();
        ArrayList<String> excludedProperties =
                new ArrayList<String>(Arrays.asList("name", "path", "component",
                        "link", "linkType", "image", "imageLinkType", "url",
                        "jcr:lastModified", "jcr:primaryType", "slot", "jcr:lastModifiedBy",
                        "aligntext"));

        String suffix = request.getSuffix();
        log.info("===== 76 suffix " + suffix);
        if(suffix.endsWith(DATA_JSON_EXTENSION)) {
            suffix = suffix.substring(0, suffix.indexOf(DATA_JSON_EXTENSION));
        }
        log.info("===== 83 suffix " + suffix);

        // here we can put our logic of child nodes from tree
        PerPageManager ppm = request.getResource().getResourceResolver().adaptTo(PerPageManager.class);
        // PerPage perPage = ppm.getPage("/content/themecapybara/pages");
        PerPage perPage = ppm.getPage(suffix);
        if (perPage != null) {
            for (PerPage child : perPage.listChildren()) {
                if (!child.getPath().equals(perPage.getPath())) {
                    log.info("===== 103 child.getTitle() " + child.getTitle());
                    log.info("===== 104 child.getPath() " + child.getPath());
                    // childPages.add(new Page(child, levels));
                    log.info("========= start ==============");
                    Resource res = request.getResourceByPath(child.getPath());
                    log.info("===== 106 child res " + res);

                    Resource content = res.getChild(JCR_CONTENT);
                    log.info("===== 111 child content " + content);


                    try {
                        // if(content == null) return Collections.<String, String> emptyMap();
                        Map childPage = modelFactory
                                .exportModelForResource(content, JACKSON, Map.class, Collections.emptyMap());

                        log.info("===== 119 childPage " + childPage);
                        log.info("===== 120 childPage.getClass().getName() " + childPage.getClass().getName());
                        log.info("===== 121 childPage.get('children').getClass().getName() " + childPage.get("children").getClass().getName());

                        ArrayList childrens = (ArrayList) childPage.get("children");
                        for (int index = 0; index < childrens.size(); index++) {
                            log.info("===== 125 childrens.get(index).getClass().getName() " + childrens.get(index).getClass().getName());
                            LinkedHashMap childContentMap = (LinkedHashMap) childrens.get(index);
                            log.info("====== 126 childContentMap " + childContentMap);

                            Set childContentSet = childContentMap.entrySet();
                            Iterator childContentIterator = childContentSet.iterator();

                            while(childContentIterator.hasNext()) {
                                Map.Entry componentProperty = (Map.Entry) childContentIterator.next();
                                if (componentProperty.getValue() != null && "java.lang.String".equals(componentProperty.getValue().getClass().getName())) {
                                    log.info("==== 137 it is plan key and value");
                                    log.info("====== 138 componentProperty " + componentProperty);
                                    log.info("====== 141 componentProperty.getClass().getName() " + componentProperty.getClass().getName());

                                    String componentPropertyValue = (String) componentProperty.getValue();
                                    if (bundle != null) {
                                        componentPropertyValue = bundle.getString(componentPropertyValue);
                                    }
                                    if (!excludedProperties.contains(componentProperty.getKey()) && ! componentPropertyValue.isEmpty() ) {
                                        jsonResponse.writeAttribute((String) componentProperty.getValue(), componentPropertyValue);
                                    }
                                } else if (componentProperty.getValue() != null && "java.util.ArrayList".equals(componentProperty.getValue().getClass().getName())) {
                                    log.info("===== 144 it is like carousel or cards");

                                    ArrayList childValues = (ArrayList) componentProperty.getValue();
                                    for (int i = 0; i < childValues.size(); i++) {
                                        LinkedHashMap subChildContentMap = (LinkedHashMap) childValues.get(i);

                                        Set subChildContentSet = subChildContentMap.entrySet();
                                        Iterator subChildContentIterator = subChildContentSet.iterator();

                                        while(subChildContentIterator.hasNext()) {
                                            Map.Entry childContent = (Map.Entry)subChildContentIterator.next();
                                            log.info("====== 151 childContent " + childContent);
                                            log.info("====== 152 childContent.getClass().getName() " + childContent.getClass().getName());

                                            log.info("====== 157 childContent.getKey() " + childContent.getKey());
                                            log.info("====== 158 childContent.getValue() " + childContent.getValue());

                                            if (!excludedProperties.contains(childContent.getKey())) {
                                                log.info("===== 173");

                                                String childContentValue = (String) childContent.getValue();
                                                if (bundle != null) {
                                                    childContentValue = bundle.getString(childContentValue);
                                                }

                                                if (!childContentValue.isEmpty()) {
                                                    log.info("===== 176");
                                                    jsonResponse.writeAttribute((String) childContent.getValue(), childContentValue);
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }

                    } catch (ExportException e) {
                        log.error("not able to export model", e);
                    } catch (MissingExporterException e) {
                        log.error("not able to find exporter for model", e);
                    }

                    log.info("=========== end ============");

                }
            }
        }

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



        return jsonResponse;


        /*
        RequestDispatcherOptions rdOptions = new RequestDispatcherOptions();
        rdOptions.setReplaceSelectors(DATA);



        return new ForwardResponse(res, rdOptions);

         */
    }
}


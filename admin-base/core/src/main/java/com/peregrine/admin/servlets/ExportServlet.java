package com.peregrine.admin.servlets;

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_EXPORT;
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

import com.peregrine.adaption.PerPage;
import com.peregrine.adaption.PerPageManager;
import com.peregrine.commons.servlets.AbstractBaseServlet;

import org.apache.sling.api.SlingHttpServletRequest;
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
import java.io.IOException;
import javax.servlet.Servlet;

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
        String language = request.getParameter("language");
        ResourceBundle bundle = null;
        if (language != null) {
            Locale locale = new Locale(language);
            SlingHttpServletRequest slingHttpServletRequest = request.getRequest();
            bundle = slingHttpServletRequest.getResourceBundle(locale);
        }

        JsonResponse jsonResponse = new JsonResponse();
        ArrayList<String> excludedProperties =
                new ArrayList<String>(Arrays.asList("name", "path", "component",
                        "link", "linkType", "image", "imageLinkType", "url",
                        "jcr:lastModified", "jcr:primaryType", "slot", "jcr:lastModifiedBy",
                        "aligntext"));

        String suffix = request.getSuffix();
        PerPageManager ppm = request.getResource().getResourceResolver().adaptTo(PerPageManager.class);
        PerPage perPage = ppm.getPage(suffix);
        if (perPage != null) {
            for (PerPage child : perPage.listChildren()) {
                if (!child.getPath().equals(perPage.getPath())) {
                    log.info("===== 103 child.getTitle() " + child.getTitle());
                    // about-us
                    log.info("===== 104 child.getPath() " + child.getPath());
                    // /content/themecapybara/pages/about-us

                    log.info("========= start ==============");
                    // getResourceByPath is reference from ContentServlet
                    Resource res = request.getResourceByPath(child.getPath());
                    log.info("===== 106 child res " + res);
                    // JcrNodeResource, type=per:Page, superType=null, path=/content/themecapybara/pages/about-us

                    // Reference from PageMerge, getMerged method 102
                    Resource content = res.getChild(JCR_CONTENT);
                    log.info("===== 111 child content " + content);
                    // JcrNodeResource, type=themecapybara/components/page, superType=null, path=/content/themecapybara/pages/about-us/jcr:content
                    try {
                        if (content != null) {
                            // Reference from PageMerge, getMerged method 102
                            Map childPage = modelFactory
                                    .exportModelForResource(content, JACKSON, Map.class, Collections.emptyMap());

                            log.info("===== 119 childPage " + childPage);
                            /*

                            {
                               "experiences=null",
                               "children="[
                                  {
                                     "experiences=null",
                                     "title=This is a""miracle",
                                     "text=lets go and <b>write</b> some content!",
                                     "path=/jcr":content/nc396a373-b22d-426b-8842-5132cfa6d418,
                                     "component=themecapybara-components-richtext"
                                  },
                                  {
                                     "experiences=null",
                                     "slides="[
                                        {
                                           name=slides0,
                                           "path=/jcr":content/naed9ea61-5e55-4738-b443-187ea75e1127/slides/slides0,
                                           "component=nt":"unstructured",
                                           "jcr":"primaryType=nt":"unstructured",
                                           subtitle=SUMMER COLLECTION 2020,
                                           "imageLinkType=internalLink",
                                           image=/content/themecapybara/assets/images/Carousel1.png,
                                           "title=Colorful summer dresses are already in store",
                                           "aligntext=left"
                                        },
                                        {
                                           name=slides1,
                                           "path=/jcr":content/naed9ea61-5e55-4738-b443-187ea75e1127/slides/slides1,
                                           "component=nt":"unstructured",
                                           "jcr":"primaryType=nt":"unstructured",
                                           subtitle=SUMMER COLLECTION 2020,
                                           "imageLinkType=internalLink",
                                           image=/content/themecapybara/assets/images/Carousel2.png,
                                           "title=Find clothing that expresses your individuality",
                                           "aligntext=right"
                                        }
                                     ],
                                     "path=/jcr":content/naed9ea61-5e55-4738-b443-187ea75e1127,
                                     "component=themecapybara-components-carousel"
                                  }
                             */
                            log.info("===== 120 childPage.getClass().getName() " + childPage.getClass().getName());
                            // java.util.LinkedHashMap

                            log.info("===== 121 childPage.get('children').getClass().getName() " + childPage.get("children").getClass().getName());
                            // java.util.ArrayList
                            ArrayList children = (ArrayList) childPage.get("children");
                            for (int index = 0; index < children.size(); index++) {
                                log.info("===== 125 children.get(index).getClass().getName() " + children.get(index).getClass().getName());
                                // java.util.LinkedHashMap
                                LinkedHashMap childContentMap = (LinkedHashMap) children.get(index);
                                log.info("====== 126 childContentMap " + childContentMap);
                                /*
                                {
                                   "experiences=null",
                                   "title=This is a""miracle",
                                   "text=lets go and <b>write</b> some content!",
                                   "path=/jcr":content/nc396a373-b22d-426b-8842-5132cfa6d418,
                                   "component=themecapybara-components-richtext"
                                }

                                OR

                                {
                                   "experiences=null",
                                   "slides="[
                                      {
                                         name=slides0,
                                         "path=/jcr":content/naed9ea61-5e55-4738-b443-187ea75e1127/slides/slides0,
                                         "component=nt":"unstructured",
                                         "jcr":"primaryType=nt":"unstructured",
                                         subtitle=SUMMER COLLECTION 2020,
                                         "imageLinkType=internalLink",
                                         image=/content/themecapybara/assets/images/Carousel1.png,
                                         "title=Colorful summer dresses are already in store",
                                         "aligntext=left"
                                      },
                                      {
                                         name=slides1,
                                         "path=/jcr":content/naed9ea61-5e55-4738-b443-187ea75e1127/slides/slides1,
                                         "component=nt":"unstructured",
                                         "jcr":"primaryType=nt":"unstructured",
                                         subtitle=SUMMER COLLECTION 2020,
                                         "imageLinkType=internalLink",
                                         image=/content/themecapybara/assets/images/Carousel2.png,
                                         "title=Find clothing that expresses your individuality",
                                         "aligntext=right"
                                      }
                                   ],
                                   "path=/jcr":content/naed9ea61-5e55-4738-b443-187ea75e1127,
                                   "component=themecapybara-components-carousel"
                                }

                                 */

                                Set childContentSet = childContentMap.entrySet();
                                Iterator childContentIterator = childContentSet.iterator();

                                while (childContentIterator.hasNext()) {
                                    Map.Entry componentProperty = (Map.Entry) childContentIterator.next();
                                    if (componentProperty.getValue() != null) {
                                        if ("java.lang.String".equals(componentProperty.getValue().getClass().getName())) {
                                            log.info("==== 137 it is plan key and value");
                                            log.info("====== 138 componentProperty " + componentProperty);
                                            // path=/jcr:content/nc396a373-b22d-426b-8842-5132cfa6d418

                                            log.info("====== 141 componentProperty.getClass().getName() " + componentProperty.getClass().getName());
                                            // java.util.LinkedHashMap$Entry

                                            String componentPropertyValue = (String) componentProperty.getValue();
                                            if (bundle != null) {
                                                componentPropertyValue = bundle.getString(componentPropertyValue);
                                            }
                                            if (!excludedProperties.contains(componentProperty.getKey()) && !componentPropertyValue.isEmpty()) {
                                                jsonResponse.writeAttribute((String) componentProperty.getValue(), componentPropertyValue);
                                            }
                                        } else if ("java.util.ArrayList".equals(componentProperty.getValue().getClass().getName())) {
                                            log.info("===== 144 it is like carousel or cards");

                                            ArrayList containerComponent = (ArrayList) componentProperty.getValue();
                                            for (int i = 0; i < containerComponent.size(); i++) {
                                                LinkedHashMap subComponent = (LinkedHashMap) containerComponent.get(i);

                                                Set subComponentSet = subComponent.entrySet();
                                                Iterator subComponentIterator = subComponentSet.iterator();

                                                while (subComponentIterator.hasNext()) {
                                                    Map.Entry childContent = (Map.Entry) subComponentIterator.next();
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

        return jsonResponse;
    }
}


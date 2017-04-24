package com.peregrine.admin.servlets;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.resource.Resource;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
        service = Servlet.class,
        property = {
                Constants.SERVICE_DESCRIPTION + "=search servlet",
                Constants.SERVICE_VENDOR + "=headwire.com, Inc",
                "sling.servlet.paths=/bin/search"
        }
)
@SuppressWarnings("serial")
public class SearchServlet extends SlingSafeMethodsServlet {

    private static final long ROWS_PER_PAGE = 100;
    private final Logger log = LoggerFactory.getLogger(SearchServlet.class);

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response) throws ServletException,
            IOException {

        String query = request.getParameter("q");
        if(query == null) query = "";

        Writer w = response.getWriter();
        if(query.length() == 0) {
            noInput(response, query, w);
        } else {
            Session session = request.getResourceResolver().adaptTo(Session.class);
            try {
                response.setContentType("application/json");

                if (query != null && query.trim().length() > 0) {
                    QueryManager qm = session.getWorkspace().getQueryManager();
                    Query q = qm.createQuery(query, Query.SQL);
                    q.setLimit(ROWS_PER_PAGE+1);
                    String pageParam = request.getParameter("page");
                    int page = 0;
                    if(pageParam != null) {
                        page = Integer.parseInt(pageParam);
                    }
                    q.setOffset(page*ROWS_PER_PAGE);

                    QueryResult res = q.execute();
                    NodeIterator nodes = res.getNodes();
                    w.write("{");
                    w.write("\"current\": "+"1"+",");
                    w.write("\"more\": "+(nodes.getSize() > ROWS_PER_PAGE)+",");
                    w.write("\"data\": [");
                    while(nodes.hasNext()) {
                        Node node = nodes.nextNode();
                        w.write("{ \"name\": \""+node.getName()+"\",");
                        w.write("\"path\": \""+node.getPath()+"\"}");
                        if(nodes.hasNext()) {
                            w.write(',');
                        }
                    }
                    w.write("]");
                    w.write("}");
                }
            } catch(Exception e) {
                log.error("not able to get query manager",e);
            }
        }
    }

    private void noInput(SlingHttpServletResponse response, String query, Writer w) throws IOException {
        response.setContentType("text/html");
        w.write("<html>");
        w.write("<head>");
        w.write("<title>jcr query tool</title>");
        w.write("</head>");
        w.write("<body>");
        w.write("<form><input size='100' type='text' name='q' value=\""+query+"\"></form>");
        w.write("</body>");
        w.write("</html>");
    }

}


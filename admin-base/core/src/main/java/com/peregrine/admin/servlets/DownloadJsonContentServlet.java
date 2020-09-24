package com.peregrine.admin.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.peregrine.admin.servlets.AdminPaths.RESOURCE_TYPE_DOWNLOAD_JSON_CONTENT;
import static com.peregrine.commons.util.PerUtil.*;
import static com.peregrine.commons.util.PerUtil.EQUALS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

@Component(
        service = Servlet.class,
        property = {
                SERVICE_DESCRIPTION + EQUALS + PER_PREFIX + "Content Servlet",
                SERVICE_VENDOR + EQUALS + PER_VENDOR,
                SLING_SERVLET_METHODS + EQUALS + GET,
                SLING_SERVLET_RESOURCE_TYPES + EQUALS + RESOURCE_TYPE_DOWNLOAD_JSON_CONTENT
        }
)

public class DownloadJsonContentServlet extends SlingAllMethodsServlet {

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        String filePath = "https://dev-cms.hotwax.io/content/sites/themecleanflex/index.data.json";
        URL url = new URL(filePath);
        HttpURLConnection httpConn = (HttpURLConnection)
                url.openConnection();

        InputStream inStream = httpConn.getInputStream();

        // if you want to use a relative path to context root:
        String relativePath = getServletContext().getRealPath("");

        // obtains ServletContext
        ServletContext context = getServletContext();

        // gets MIME type of the file
        String mimeType = context.getMimeType(filePath);
        if (mimeType == null) {
            // set to
            mimeType = "application/json";
        }

        // modifies response
        response.setContentType(mimeType);
        response.setContentLength((int) httpConn.getContentLength());

        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1,
                filePath.length());

        // forces download
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"",
                fileName);
        response.setHeader(headerKey, headerValue);

        // obtains response's output stream
        OutputStream outStream = response.getOutputStream();

        byte[] buffer = new byte[4096];
        int bytesRead = -1;

        while ((bytesRead = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }

        inStream.close();
        outStream.close();
    }
    
}

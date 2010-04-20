/*
   Copyright 2010 Kristian Andersen

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.kriand.warpdrive.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * A Servlet filter providing far-future expires headers for all responses going through it.
 * If the requested resources is gzipped, Content-Encoding is also set to gzip.
 * Needless to say, it is important to get the filter-mapping in web.xml correct for this
 * filter or else chaos and havoc will ensue.
 * <p/>
 * Created by IntelliJ IDEA.
 *
 * @author kriand <a href="http://mailhide.recaptcha.net/d?k=01r9lbYEAtg9V5s1Ru_jtZ1g==&c=-aIoeZ0yU0yPn2kdog349bCmN-h1pe5Ed0LsyuWMbEc=">Show email</a>
 * Date: Mar 3, 2010
 * Time: 9:27:22 PM
 */
public class WarpDriveFilter implements Filter {

    /**
     * For simple formatting of dates.
     */
    private static final String EXPIRES_HEADER_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";

    /**
     * Resources will expire one year from time of access.
     */
    private static final long ONE_YEAR_IN_SECONDS = 31536000L;

    /**
     * No init is performed by this filter.
     *
     * @param filterConfig Provided filter configuration
     * @throws ServletException Not likely...
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(final FilterConfig filterConfig) throws ServletException {

    }

    /**
     *
     * Adds expires headers to the response with expiry one year from time of access.
     * Also sets the Content-Type to gzip if required.
     *
     * @param request The request
     * @param response The response
     * @param chain The filter chain
     * @throws IOException If error
     * @throws ServletException If error
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public final void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String oneYearFromNow = getOneYearFromNow();
        if (isGzippedResource(req)) {
            resp.setHeader("Content-Encoding", "gzip");
        }
        resp.setHeader("Expires", oneYearFromNow);
        resp.setHeader("Cache-Control", "max-age=" + ONE_YEAR_IN_SECONDS + ";public;must-revalidate;");
        chain.doFilter(req, resp);
    }

    /**
     *
     * Gets the date one year, readly formatted for use in a Http header field.
     *
     * @return The date one year, readly formatted for use in a Http header field
     */
    private String getOneYearFromNow() {
        SimpleDateFormat sdf = new SimpleDateFormat(EXPIRES_HEADER_FORMAT);
        Calendar cal = GregorianCalendar.getInstance();
        cal.add(1, Calendar.YEAR);
        return sdf.format(cal.getTime());
    }

    /**
     *
     * Checks if a resource is gzipped, based on extension.
     * The WarpDrive plugin names gzipped resources &quot;.gz.css&quot;
     * instead of &quot;.css.gz&quot; to avoid mime mapping issues.
     *
     * @param request A request for a resource
     * @return True if the resource looks like it was gzipped by the
     *         WarpDrive plugin, false otherwise.
     */
    private boolean isGzippedResource(final HttpServletRequest request) {
        return request.getRequestURI().endsWith(".gz.css")
            || request.getRequestURI().endsWith(".gz.js");
    }

    /**
     * Nothing to destroy..
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {

    }

}

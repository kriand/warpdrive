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
package org.kriand.warpdrive.processors.js;

import org.kriand.warpdrive.mojo.WarpDriveMojo;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

/**
 *
 * An error reporter that logs errors to the Maven log.
 *
 *
 * @author kriand <a href="http://mailhide.recaptcha.net/d?k=01r9lbYEAtg9V5s1Ru_jtZ1g==&c=-aIoeZ0yU0yPn2kdog349bCmN-h1pe5Ed0LsyuWMbEc=">Show email</a>
 * Date: Mar 6, 2010
 * Time: 6:57:28 PM
 */
class JsErrorReporter implements ErrorReporter {

    /**
     * Reference to the Maven plugin, need to access log.
     */
    private final WarpDriveMojo mojo;

    /**
     * Constructor.
     * @param inMojo The WarpDrive Mojo, needed for access to log.
     */
    public JsErrorReporter(final WarpDriveMojo inMojo) {
        this.mojo = inMojo;
    }

    /**
     *
     * Logs a warning message to the Maven log.
     *
     * @param message The message to log
     * @param sourceName The sourceName to log
     * @param line The line to log
     * @param lineSource The lineSource to log
     * @param lineOffset The lineOffset to log
     */
    public void warning(final String message, final String sourceName, final int line, final String lineSource, final int lineOffset) {
        if (line < 0) {
            mojo.getLog().warn(message);
        } else {
            mojo.getLog().warn(line + ':' + lineOffset + ':' + message);
        }
    }

    /**
     *
     * Logs a error message to the Maven log.
     *
     * @param message The message to log
     * @param sourceName The sourceName to log
     * @param line The line to log
     * @param lineSource The lineSource to log
     * @param lineOffset The lineOffset to log
     */
    public void error(final String message, final String sourceName, final int line, final String lineSource, final int lineOffset) {
        if (line < 0) {
            mojo.getLog().error(message);
        } else {
            mojo.getLog().error(line + ':' + lineOffset + ':' + message);
        }
    }

    /**
     *
     * Logs a runtime error to the Maven log.
     *
     * @param message The message to log
     * @param sourceName The sourceName to log
     * @param line The line to log
     * @param lineSource The lineSource to log
     * @param lineOffset The lineOffset to log
     * @return EvaluatorException containing the errormessage.
     */
    public EvaluatorException runtimeError(final String message, final String sourceName, final int line, final String lineSource, final int lineOffset) {
        error(message, sourceName, line, lineSource, lineOffset);
        return new EvaluatorException(message);
    }
}

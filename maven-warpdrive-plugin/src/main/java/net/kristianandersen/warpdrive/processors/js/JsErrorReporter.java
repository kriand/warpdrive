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
package net.kristianandersen.warpdrive.processors.js;

import net.kristianandersen.warpdrive.mojo.WarpDriveMojo;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

/**
 * Created by IntelliJ IDEA.
 * User: kriand
 * Date: Mar 6, 2010
 * Time: 6:57:28 PM
 * To change this template use File | Settings | File Templates.
 */
class JsErrorReporter implements ErrorReporter {

    private final WarpDriveMojo mojo;

    public JsErrorReporter(final WarpDriveMojo inMojo) {
        this.mojo = inMojo;
    }

    public void warning(final String message, final String sourceName, final int line, final String lineSource, final int lineOffset) {
        if (line < 0) {
            mojo.getLog().warn(message);
        } else {
            mojo.getLog().warn(line + ':' + lineOffset + ':' + message);
        }
    }

    public void error(final String message, final String sourceName, final int line, final String lineSource, final int lineOffset) {
        if (line < 0) {
            mojo.getLog().error(message);
        } else {
            mojo.getLog().error(line + ':' + lineOffset + ':' + message);
        }
    }

    public EvaluatorException runtimeError(final String message, final String sourceName, final int line, final String lineSource, final int lineOffset) {
        error(message, sourceName, line, lineSource, lineOffset);
        return new EvaluatorException(message);
    }
}

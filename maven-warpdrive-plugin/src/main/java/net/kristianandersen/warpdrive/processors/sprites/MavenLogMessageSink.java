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
package net.kristianandersen.warpdrive.processors.sprites;

import org.carrot2.labs.smartsprites.message.MessageSink;
import org.carrot2.labs.smartsprites.message.Message;
import org.apache.maven.plugin.logging.Log;

/**
 * Created by IntelliJ IDEA.
 * User: kriand
 * Date: Mar 7, 2010
 * Time: 6:48:40 PM
 * To change this template use File | Settings | File Templates.
 */
class MavenLogMessageSink implements MessageSink {

    private final Log log;

    public MavenLogMessageSink (Log log) {
        this.log = log;
    }

    public void add(Message message) {
        String str = message.getFormattedMessage();
        switch(message.level) {
            case ERROR:
                log.error(str);
                break;
            case WARN :
                log.warn(str);
                break;
            case INFO:
                log.info(str);
                break;
            default:
                log.debug(str);
                break;
        }
    }
}

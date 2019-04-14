/*
###############################################################################
#                                                                             #
#    Copyright 2016, AdeptJ (http://www.adeptj.com)                           #
#                                                                             #
#    Licensed under the Apache License, Version 2.0 (the "License");          #
#    you may not use this file except in compliance with the License.         #
#    You may obtain a copy of the License at                                  #
#                                                                             #
#        http://www.apache.org/licenses/LICENSE-2.0                           #
#                                                                             #
#    Unless required by applicable law or agreed to in writing, software      #
#    distributed under the License is distributed on an "AS IS" BASIS,        #
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. #
#    See the License for the specific language governing permissions and      #
#    limitations under the License.                                           #
#                                                                             #
###############################################################################
*/

package com.adeptj.runtime.extensions.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;

import java.util.List;

/**
 * LogbackManager
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public interface LogbackManager {

    LoggerContext getLoggerContext();

    LogbackManager addAppender(Appender<ILoggingEvent> appender);

    List<Appender<ILoggingEvent>> getAppenders();

    Appender<ILoggingEvent> getAppender(String name);

    void addLogger(LogbackConfig logbackConfig);

    boolean detachAppender(String loggerName, String appenderName);

    PatternLayoutEncoder newLayoutEncoder(String logPattern);

    ConsoleAppender<ILoggingEvent> newConsoleAppender(String name, String logPattern);

    RollingFileAppender<ILoggingEvent> newRollingFileAppender(LogbackConfig logbackConfig);

    void newAsyncAppender(LogbackConfig logbackConfig);
}

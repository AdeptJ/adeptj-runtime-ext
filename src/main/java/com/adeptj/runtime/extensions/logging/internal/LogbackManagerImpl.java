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

package com.adeptj.runtime.extensions.logging.internal;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import com.adeptj.runtime.extensions.logging.LogbackManager;
import com.adeptj.runtime.extensions.logging.core.LogLevelHighlightingCustomizer;
import com.adeptj.runtime.extensions.logging.core.LogThreadNameCustomizer;
import com.adeptj.runtime.extensions.logging.core.LogbackConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Default implementation of {@link LogbackManager}.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class LogbackManagerImpl implements LogbackManager {

    private static final String SYS_PROP_LOG_IMMEDIATE_FLUSH = "log.immediate.flush";

    private final List<Appender<ILoggingEvent>> appenders;

    private final LoggerContext loggerContext;

    public LogbackManagerImpl() {
        this.appenders = new CopyOnWriteArrayList<>();
        this.loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    }

    @Override
    public LoggerContext getLoggerContext() {
        return this.loggerContext;
    }

    @Override
    public LogbackManager addAppender(Appender<ILoggingEvent> appender) {
        this.appenders.add(appender);
        return this;
    }

    @Override
    public List<Appender<ILoggingEvent>> getAppenders() {
        return this.appenders;
    }

    @Override
    public Appender<ILoggingEvent> getAppender(String name) {
        return this.appenders.stream()
                .filter(appender -> StringUtils.equals(appender.getName(), name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void addLogger(LogbackConfig logbackConfig) {
        logbackConfig.getLoggerNames()
                .forEach(name -> {
                    Logger logger = this.loggerContext.getLogger(name);
                    logger.setLevel(Level.toLevel(logbackConfig.getLevel()));
                    logger.setAdditive(logbackConfig.isAdditivity());
                    if (logbackConfig.getAppenders().isEmpty()) {
                        this.appenders.forEach(logger::addAppender);
                    } else {
                        logbackConfig.getAppenders().forEach(logger::addAppender);
                    }
                });
    }

    @Override
    public boolean detachAppender(String loggerName, String appenderName) {
        return this.loggerContext.getLogger(loggerName).detachAppender(appenderName);
    }

    @Override
    public void detachAppenders(String loggerName) {
        this.appenders.forEach(appender -> this.loggerContext.getLogger(loggerName).detachAppender(appender));
    }

    @Override
    public PatternLayoutEncoder newLayoutEncoder(String logPattern) {
        PatternLayoutEncoder layoutEncoder = new PatternLayoutEncoder();
        layoutEncoder.setContext(this.loggerContext);
        layoutEncoder.setPattern(logPattern);
        layoutEncoder.setCharset(UTF_8);
        layoutEncoder.start();
        PatternLayout layout = (PatternLayout) layoutEncoder.getLayout();
        layout.getDefaultConverterMap().put("highlight", LogLevelHighlightingCustomizer.class.getName());
        layout.getDefaultConverterMap().put("thread", LogThreadNameCustomizer.class.getName());
        return layoutEncoder;
    }

    @Override
    public ConsoleAppender<ILoggingEvent> newConsoleAppender(String name, String logPattern) {
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setContext(this.loggerContext);
        consoleAppender.setName(name);
        consoleAppender.setEncoder(this.newLayoutEncoder(logPattern));
        consoleAppender.setWithJansi(true);
        consoleAppender.start();
        return consoleAppender;
    }

    @Override
    public RollingFileAppender<ILoggingEvent> newRollingFileAppender(LogbackConfig logbackConfig) {
        RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<>();
        fileAppender.setContext(this.loggerContext);
        fileAppender.setName(logbackConfig.getAppenderName());
        fileAppender.setFile(logbackConfig.getLogFile());
        fileAppender.setAppend(true);
        fileAppender.setEncoder(this.newLayoutEncoder(logbackConfig.getPattern()));
        fileAppender.setImmediateFlush(Boolean.getBoolean(SYS_PROP_LOG_IMMEDIATE_FLUSH));
        if (!fileAppender.isImmediateFlush()) {
            fileAppender.setImmediateFlush(logbackConfig.isImmediateFlush());
        }
        SizeAndTimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new SizeAndTimeBasedRollingPolicy<>();
        rollingPolicy.setContext(this.loggerContext);
        rollingPolicy.setParent(fileAppender);
        rollingPolicy.setMaxFileSize(FileSize.valueOf(logbackConfig.getLogMaxSize()));
        rollingPolicy.setFileNamePattern(logbackConfig.getRolloverFile());
        rollingPolicy.setMaxHistory(logbackConfig.getLogMaxHistory());
        rollingPolicy.start();
        fileAppender.setRollingPolicy(rollingPolicy);
        fileAppender.setTriggeringPolicy(rollingPolicy);
        fileAppender.start();
        return fileAppender;
    }

    @Override
    public void addAsyncAppender(LogbackConfig logbackConfig) {
        AsyncAppender asyncAppender = new AsyncAppender();
        asyncAppender.setContext(this.loggerContext);
        asyncAppender.setName(logbackConfig.getAsyncAppenderName());
        asyncAppender.setQueueSize(logbackConfig.getAsyncLogQueueSize());
        asyncAppender.setDiscardingThreshold(logbackConfig.getAsyncLogDiscardingThreshold());
        asyncAppender.addAppender(logbackConfig.getAsyncAppender());
        asyncAppender.start();
    }
}

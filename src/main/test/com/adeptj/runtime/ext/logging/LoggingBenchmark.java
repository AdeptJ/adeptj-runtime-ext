package com.adeptj.runtime.extensions.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static ch.qos.logback.classic.Level.toLevel;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

public class LoggingBenchmark {

    @Test
    public void testLog4j2() {
//        long start = System.nanoTime();
//        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
//        AppenderComponentBuilder console = builder.newAppender("stdout", "Console");
//        builder.add(console);
//        LayoutComponentBuilder standard = builder.newLayout("PatternLayout");
//        standard.addAttribute("pattern", "%d [%t] %-5level: %msg%n%throwable");
//        console.add(standard);
//        RootLoggerComponentBuilder rootLogger = builder.newRootLogger(Level.ERROR);
//        rootLogger.add(builder.newAppenderRef("stdout"));
//        builder.add(rootLogger);
//        LoggerComponentBuilder logger = builder.newLogger("com.adeptj", Level.DEBUG);
//        logger.add(builder.newAppenderRef("log"));
//        logger.addAttribute("additivity", false);
//        builder.add(logger);
//        Configurator.initialize(builder.build());
//        System.out.println(NANOSECONDS.toMillis(System.nanoTime() - start));
//        Logger log = LoggerFactory.getLogger("com.adeptj");
//        log.info("Hello World!!");
    }

    @Test
    public void testLogback() {
        long start = System.nanoTime();
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        PatternLayoutEncoder layoutEncoder = new PatternLayoutEncoder();
        layoutEncoder.setContext(loggerContext);
        layoutEncoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} %green([%.-23thread]) %highlight(%-5level) %cyan(%logger{100}) - %msg%n");
        layoutEncoder.start();
        PatternLayout layout = (PatternLayout) layoutEncoder.getLayout();
        layout.getDefaultConverterMap().put("highlight", ExtHighlightingCompositeConverter.class.getName());
        layout.getDefaultConverterMap().put("thread", ExtThreadConverter.class.getName());
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setName("CONSOLE");
        consoleAppender.setContext(loggerContext);
        consoleAppender.setEncoder(layoutEncoder);
        consoleAppender.setWithJansi(true);
        consoleAppender.start();
        Logger root = loggerContext.getLogger(ROOT_LOGGER_NAME);
        root.setLevel(toLevel("DEBUG"));
        root.addAppender(consoleAppender);
        loggerContext.start();
        System.out.println(NANOSECONDS.toMillis(System.nanoTime() - start));
        org.slf4j.Logger log = LoggerFactory.getLogger("com.adeptj");
        log.info("Hello World!!");
        log.info("{}", OffsetDateTime.now().plus(Duration.ofDays(31)).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        log.info("{}", new Date());
    }
}

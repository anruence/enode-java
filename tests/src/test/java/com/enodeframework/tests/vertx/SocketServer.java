package com.enodeframework.tests.vertx;

import com.enodeframework.common.SysProperties;
import io.vertx.core.Vertx;
import io.vertx.core.parsetools.RecordParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SocketServer {

    public static Logger logger = LoggerFactory.getLogger(SocketServer.class);

    public static void main(String[] args) throws IOException {
        Vertx.vertx().createNetServer().connectHandler(s -> {
            RecordParser parser = RecordParser.newDelimited(SysProperties.DELIMITED, s);
            parser.handler(b -> logger.info("receive.:{}", b.toString()));
        }).listen(6008);
        System.in.read();
    }
}

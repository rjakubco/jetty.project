//
//  ========================================================================
//  Copyright (c) 1995-2012 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//


package org.eclipse.jetty.spdy.http;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jetty.server.AbstractConnectionFactory;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.HttpChannelConfig;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SelectChannelConnector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.spdy.NPNServerConnectionFactory;
import org.eclipse.jetty.spdy.SPDYServerConnector;
import org.eclipse.jetty.spdy.api.SPDY;
import org.eclipse.jetty.spdy.http.PushStrategy.None;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class HTTPSPDYServerConnector extends SelectChannelConnector
{
    public HTTPSPDYServerConnector(Server server)
    {
        this(server, Collections.<Short, PushStrategy>emptyMap());
    }

    public HTTPSPDYServerConnector(Server server, SslContextFactory sslContextFactory)
    {
        this(server, sslContextFactory, Collections.<Short, PushStrategy>emptyMap());
    }

    public HTTPSPDYServerConnector(Server server, Map<Short, PushStrategy> pushStrategies)
    {
        this(server, null, pushStrategies);
    }

    public HTTPSPDYServerConnector(Server server, SslContextFactory sslContextFactory, Map<Short, PushStrategy> pushStrategies)
    {
        this(server,new HttpChannelConfig(),sslContextFactory,pushStrategies);
    }

    public HTTPSPDYServerConnector(Server server, short version, HttpChannelConfig httpChannelConfig, PushStrategy push)
    {
        super(server,new HTTPSPDYServerConnectionFactory(version,httpChannelConfig,push));
       
    }
    
    public HTTPSPDYServerConnector(Server server, HttpChannelConfig config, SslContextFactory sslContextFactory, Map<Short, PushStrategy> pushStrategies)
    {
        super(server,AbstractConnectionFactory.getFactories(sslContextFactory,
            sslContextFactory==null
            ?new ConnectionFactory[] {new HttpConnectionFactory(config)}
            :new ConnectionFactory[] {new NPNServerConnectionFactory("spdy/3","spdy/2","http/1.1"),
                new HttpConnectionFactory(config),
                new HTTPSPDYServerConnectionFactory(SPDY.V3,new HttpChannelConfig(),getPushStrategy(SPDY.V3, pushStrategies)),
                new HTTPSPDYServerConnectionFactory(SPDY.V2,new HttpChannelConfig(),getPushStrategy(SPDY.V2, pushStrategies))}));
        if (getConnectionFactory(NPNServerConnectionFactory.class)!=null)
            getConnectionFactory(NPNServerConnectionFactory.class).setDefaultProtocol("http/1.1");
    }


    private static PushStrategy getPushStrategy(short version, Map<Short, PushStrategy> pushStrategies)
    {
        PushStrategy pushStrategy = pushStrategies.get(version);
        if (pushStrategy == null)
            pushStrategy = new PushStrategy.None();
        return pushStrategy;
    }
}

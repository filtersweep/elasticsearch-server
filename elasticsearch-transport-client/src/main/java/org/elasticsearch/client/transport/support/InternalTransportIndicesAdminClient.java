/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.client.transport.support;

import org.elasticsearch.common.collect.ImmutableMap;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.*;
import org.elasticsearch.action.admin.indices.IndicesAction;
import org.elasticsearch.client.ServerIndicesAdminClient;
import org.elasticsearch.client.support.AbstractIndicesAdminClient;
import org.elasticsearch.client.transport.TransportClientNodesService;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.collect.MapBuilder;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.transport.TransportService;

import java.util.Map;
import org.elasticsearch.threadpool.ThreadPool;

/**
 *
 */
public class InternalTransportIndicesAdminClient extends AbstractIndicesAdminClient implements ServerIndicesAdminClient {

    private final Settings settings;
    
    private final TransportClientNodesService nodesService;

    private final ThreadPool threadPool;

    private final ImmutableMap<IndicesAction, TransportActionNodeProxy> actions;

    @Inject
    public InternalTransportIndicesAdminClient(Settings settings, TransportClientNodesService nodesService, ThreadPool threadPool, TransportService transportService,
                                               Map<String, GenericAction> actions) {
        this.settings = settings;
        this.threadPool = threadPool;
        this.nodesService = nodesService;
        MapBuilder<IndicesAction, TransportActionNodeProxy> actionsBuilder = new MapBuilder<IndicesAction, TransportActionNodeProxy>();
        for (GenericAction action : actions.values()) {
            if (action instanceof IndicesAction) {
                actionsBuilder.put((IndicesAction) action, new TransportActionNodeProxy(settings, action, transportService));
            }
        }
        this.actions = actionsBuilder.immutableMap();
    }

    @Override
    public ThreadPool threadPool() {
        return this.threadPool;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> ActionFuture<Response> execute(final IndicesAction<Request, Response, RequestBuilder> action, final Request request) {
        final TransportActionNodeProxy<Request, Response> proxy = actions.get(action);
        return nodesService.execute(new TransportClientNodesService.NodeCallback<ActionFuture<Response>>() {
            @Override
            public ActionFuture<Response> doWithNode(DiscoveryNode node) throws ElasticSearchException {
                return proxy.execute(node, request);
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> void execute(final IndicesAction<Request, Response, RequestBuilder> action, final Request request, ActionListener<Response> listener) {
        final TransportActionNodeProxy<Request, Response> proxy = actions.get(action);
        nodesService.execute(new TransportClientNodesService.NodeListenerCallback<Response>() {
            @Override
            public void doWithNode(DiscoveryNode node, ActionListener<Response> listener) throws ElasticSearchException {
                proxy.execute(node, request, listener);
            }
        }, listener);
    }
    
    @Override
    public Settings settings() {
        return this.settings;
    }
    
    @Override
    public void close() {
        // nothing really to do
    }    
}

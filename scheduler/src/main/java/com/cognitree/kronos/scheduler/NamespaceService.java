/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cognitree.kronos.scheduler;

import com.cognitree.kronos.Service;
import com.cognitree.kronos.ServiceProvider;
import com.cognitree.kronos.model.Namespace;
import com.cognitree.kronos.model.NamespaceId;
import com.cognitree.kronos.scheduler.store.NamespaceStore;
import com.cognitree.kronos.scheduler.store.StoreConfig;
import com.cognitree.kronos.scheduler.store.StoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NamespaceService implements Service {
    private static final Logger logger = LoggerFactory.getLogger(NamespaceService.class);

    private final StoreConfig storeConfig;
    private NamespaceStore namespaceStore;

    public NamespaceService(StoreConfig storeConfig) {
        this.storeConfig = storeConfig;
    }

    public static NamespaceService getService() {
        return (NamespaceService) ServiceProvider.getService(NamespaceService.class.getSimpleName());
    }

    @Override
    public void init() throws Exception {
        logger.info("Initializing namespace service");
        namespaceStore = (NamespaceStore) Class.forName(storeConfig.getStoreClass())
                .getConstructor().newInstance();
        namespaceStore.init(storeConfig.getConfig());
    }

    @Override
    public void start() {
        logger.info("Starting namespace service");

    }

    public List<Namespace> get() throws ServiceException {
        logger.debug("Received request to get all namespaces");
        try {
            return namespaceStore.load();
        } catch (StoreException e) {
            logger.error("unable to get all namespaces", e);
            throw new ServiceException(e.getMessage());
        }
    }

    public Namespace get(NamespaceId namespaceId) throws ServiceException {
        logger.debug("Received request to get namespace with id {}", namespaceId);
        try {
            return namespaceStore.load(namespaceId);
        } catch (StoreException e) {
            logger.error("unable to get namespace with id {}", namespaceId, e);
            throw new ServiceException(e.getMessage());
        }
    }

    public void add(Namespace namespace) throws ServiceException {
        logger.debug("Received request to add namespace {}", namespace);
        try {
            namespaceStore.store(namespace);
        } catch (StoreException e) {
            logger.error("unable to add namespace {}", namespace, e);
            throw new ServiceException(e.getMessage());
        }
    }

    public void update(Namespace namespace) throws ServiceException {
        logger.debug("Received request to update namespace to {}", namespace);
        try {
            namespaceStore.store(namespace);
        } catch (StoreException e) {
            logger.error("unable to update namespace to {}", namespace, e);
            throw new ServiceException(e.getMessage());
        }
    }

    @Override
    public void stop() {
        logger.info("Stopping namespace service");
        namespaceStore.stop();
    }

}

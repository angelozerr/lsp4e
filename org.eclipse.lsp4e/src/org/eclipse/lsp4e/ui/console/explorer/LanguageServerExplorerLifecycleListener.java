/*******************************************************************************
 * Copyright (c) 2023 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.lsp4e.ui.console.explorer;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.lsp4e.LanguageServerWrapper;
import org.eclipse.lsp4e.ServerStatus;
import org.eclipse.lsp4e.lifecycle.LanguageServerLifecycleListener;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.messages.Message;

/**
 * Language server listener to refresh the language server explorer according to the server state and fill the LSP console.
 *
 * @author Angelo ZERR
 */
public class LanguageServerExplorerLifecycleListener implements LanguageServerLifecycleListener {

    private final Map<LanguageServerWrapper, TracingMessageConsumer> tracingPerServer = new HashMap<>(10);

    private boolean disposed;

    private final LanguageServerExplorer explorer;

    public LanguageServerExplorerLifecycleListener(LanguageServerExplorer explorer) {
        this.explorer = explorer;
    }

    @Override
    public void handleStatusChanged(LanguageServerWrapper languageServer) {
        ServerStatus serverStatus = languageServer.getServerStatus();
        boolean selectProcess = serverStatus == ServerStatus.starting;
        updateServerStatus(languageServer, serverStatus, selectProcess);
    }

    @Override
    public void handleLSPMessage(Message message, MessageConsumer messageConsumer, LanguageServerWrapper languageServer) {
        if (explorer.isDisposed()) {
            return;
        }
        LanguageServerModel processTreeNode = updateServerStatus(languageServer, null, false);
        ServerTrace serverTrace = getServerTrace(languageServer.serverDefinition.id);
        if (serverTrace == ServerTrace.off) {
            return;
        }

        TracingMessageConsumer tracing = getLSPRequestCacheFor(languageServer);
        String log = tracing.log(message, messageConsumer, serverTrace);
        invokeLater(() -> showMessage(processTreeNode, log));
    }

    @Override
    public void handleError(LanguageServerWrapper languageServer, Throwable exception) {
        LanguageServerModel processTreeNode = updateServerStatus(languageServer, null, false);
        if (exception == null) {
            return;
        }

        invokeLater(() -> showError(processTreeNode, exception));
    }

    private TracingMessageConsumer getLSPRequestCacheFor(LanguageServerWrapper languageServer) {
        TracingMessageConsumer cache = tracingPerServer.get(languageServer);
        if (cache != null) {
            return cache;
        }
        synchronized (tracingPerServer) {
            cache = tracingPerServer.get(languageServer);
            if (cache != null) {
                return cache;
            }
            cache = new TracingMessageConsumer();
            tracingPerServer.put(languageServer, cache);
            return cache;
        }
    }


    private static ServerTrace getServerTrace(String languageServerId) {
        ServerTrace serverTrace = ServerTrace.verbose;
        return serverTrace != null ? serverTrace : ServerTrace.off;
    }

    private LanguageServerModel updateServerStatus(LanguageServerWrapper languageServer, ServerStatus serverStatus, boolean selectProcess) {
        final var processTreeNode = explorer.findLanguageServerItem(languageServer);
        boolean serverStatusChanged = serverStatus != null && serverStatus != processTreeNode.getServerStatus();
        boolean updateUI = serverStatusChanged || selectProcess;
        if (updateUI) {
           final var status = serverStatus;
            final var select = selectProcess;
            invokeLater(() -> {
                if (explorer.isDisposed()) {
                    return;
                }
                if (serverStatusChanged) {
                	processTreeNode.setServerStatus(status);
                }
                if (select) {
                    explorer.selectAndExpand(processTreeNode);
                }
            });
        }
        return processTreeNode;
    }

    private void showMessage(LanguageServerModel processTreeNode, String message) {
        if (explorer.isDisposed()) {
            return;
        }
        explorer.showMessage(processTreeNode, message);
    }

    private void showError(LanguageServerModel processTreeNode, Throwable exception) {
        if (explorer.isDisposed()) {
            return;
        }
        explorer.showError(processTreeNode, exception);
    }

    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void dispose() {
        disposed = true;
        tracingPerServer.clear();
    }

    private static void invokeLater(Runnable runnable) {
    	runnable.run();
    }

}

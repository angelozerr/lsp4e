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
package org.eclipse.lsp4e.lifecycle;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.lsp4e.LanguageServerPlugin;
import org.eclipse.lsp4e.LanguageServerWrapper;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.messages.Message;

/**
 * Language server lifecycle manager
 */
public class LanguageServerLifecycleManager {

    private static final LanguageServerLifecycleManager INSTANCE = new LanguageServerLifecycleManager();

	private final Collection<LanguageServerLifecycleListener> listeners;

    private boolean disposed;

    public LanguageServerLifecycleManager() {
        this(new ConcurrentLinkedQueue<>());
    }

    public LanguageServerLifecycleManager(Collection<LanguageServerLifecycleListener> listeners) {
        this.listeners = listeners;
    }

    public void addLanguageServerLifecycleListener(LanguageServerLifecycleListener listener) {
        this.listeners.add(listener);
    }

    public void removeLanguageServerLifecycleListener(LanguageServerLifecycleListener listener) {
        this.listeners.remove(listener);
    }

    public void onStatusChanged(LanguageServerWrapper languageServer) {
        if (isDisposed()) {
            return;
        }
        for (LanguageServerLifecycleListener listener : this.listeners) {
            try {
                listener.handleStatusChanged(languageServer);
            } catch (Exception e) {
            	LanguageServerPlugin.logError("Error while status changed of the language server '" + languageServer.serverDefinition.id + "'", e); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    public void logLSPMessage(Message message, MessageConsumer consumer, LanguageServerWrapper languageServer) {
        if (isDisposed()) {
            return;
        }
        for (LanguageServerLifecycleListener listener : this.listeners) {
            try {
                listener.handleLSPMessage(message, consumer, languageServer);
            } catch (Exception e) {
            	LanguageServerPlugin.logError("Error while handling LSP message of the language server '" + languageServer.serverDefinition.id + "'", e); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    public void onError(LanguageServerWrapper languageServer, Throwable exception) {
        if (isDisposed()) {
            return;
        }
        for (LanguageServerLifecycleListener listener : this.listeners) {
            try {
                listener.handleError(languageServer, exception);
            } catch (Exception e) {
            	LanguageServerPlugin.logError("Error while handling error of the language server '" + languageServer.serverDefinition.id + "'", e); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }
    public boolean isDisposed() {
        return disposed;
    }

    public void dispose() {
        disposed = true;
        listeners.stream().forEach(LanguageServerLifecycleListener::dispose);
        listeners.clear();
    }

	public static LanguageServerLifecycleManager getInstance() {
		return INSTANCE ;
	}


}

package org.eclipse.lsp4e.ui.console.explorer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.lsp4e.LanguageServerWrapper;
import org.eclipse.lsp4e.ServerStatus;

public class LanguageServerModel {

	private final @NonNull LanguageServerWrapper languageServer;

	private final @NonNull LanguageServerExplorer explorer;

	private ServerStatus serverStatus;

	public LanguageServerModel(@NonNull LanguageServerWrapper languageServer, @NonNull LanguageServerExplorer explorer) {
		this.languageServer = languageServer;
		this.explorer = explorer;
		this.serverStatus = languageServer.getServerStatus();
	}

	public LanguageServerWrapper getLanguageServer() {
		return languageServer;
	}

	public ServerStatus getServerStatus() {
		return serverStatus;
	}

	public void setServerStatus(ServerStatus serverStatus) {
		boolean refresh = this.serverStatus != serverStatus;
		this.serverStatus = serverStatus;
		if (refresh) {
			explorer.refresh(this);
		}
	}

	public String getText() {
        if (!languageServer.isEnabled()) {
            return "disabled"; //$NON-NLS-1$
        }
        Throwable serverError = languageServer.getServerError();
        StringBuilder name = new StringBuilder();
        if (serverError == null) {
            name.append(serverStatus.name());
        } else {
            name.append(serverStatus == ServerStatus.stopped ? "crashed" : serverStatus.name()); //$NON-NLS-1$
            int nbTryRestart = languageServer.getNumberOfRestartAttempts();
            int nbTryRestartMax = languageServer.getMaxNumberOfRestartAttempts();
            name.append(" ["); //$NON-NLS-1$
            name.append(nbTryRestart);
            name.append("/"); //$NON-NLS-1$
            name.append(nbTryRestartMax);
            name.append("]"); //$NON-NLS-1$
        }
        Long pid = languageServer.getCurrentProcessId();
        if (pid != null) {
            name.append(" pid:"); //$NON-NLS-1$
            name.append(pid);
        }
        return name.toString();
    }
}

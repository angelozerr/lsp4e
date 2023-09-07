package org.eclipse.lsp4e.ui.console.explorer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.lsp4e.LanguageServerWrapper;
import org.eclipse.lsp4e.LanguageServersRegistry;
import org.eclipse.lsp4e.LanguageServersRegistry.LanguageServerDefinition;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4e.lifecycle.LanguageServerLifecycleManager;
import org.eclipse.lsp4e.ui.console.LSPConsolesPanel;
import org.eclipse.lsp4e.ui.console.LSPConsolesView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;

public class LanguageServerExplorer {

	private final Map<LanguageServerDefinition, List<LanguageServerModel>> cache;

	private final LanguageServerExplorerLifecycleListener listener;

	private @NonNull final TreeViewer viewer;

	private LSPConsolesPanel consolesPanel;

	private boolean loading;
	private final LSPConsolesView provider;

	public LanguageServerExplorer(Composite parent, LSPConsolesView provider) {
		this.provider = provider;
		viewer = new TreeViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

		listener = new LanguageServerExplorerLifecycleListener(this);
		LanguageServerLifecycleManager.getInstance().addLanguageServerLifecycleListener(listener);
		cache = new HashMap<>();

		viewer.getTree().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

			}
		});
		viewer.setLabelProvider(new LanguageServerExplorerLabelProvider());
		viewer.setContentProvider(new LanguageServerExplorerContentProvider());
	}

	public void setConsolesPanel(LSPConsolesPanel consolesPanel) {
		this.consolesPanel = consolesPanel;
	}

	private static final class LanguageServerExplorerLabelProvider extends LabelProvider {

		@Override
		public String getText(@Nullable final Object element) {
			if (element instanceof LanguageServerDefinition serverDefinition) {
				return serverDefinition.label;
			} else if (element instanceof LanguageServerModel model) {
				return model.getText();
			}
			return ""; //$NON-NLS-1$
		}
	}

	private final class LanguageServerExplorerContentProvider implements ITreeContentProvider {

		@Override
		public Object[] getChildren(Object element) {
			if (element instanceof LanguageServerDefinition serverDefinition) {
				List<LanguageServerModel> servers = cache.get(serverDefinition);
				if (servers != null) {
					return servers.toArray();
				}
			}
			return new Object[0];
		}

		@Override
		public Object[] getElements(Object arg0) {
			return LanguageServersRegistry.getInstance().getLanguageServerDefinitions().toArray();
		}

		@Override
		public Object getParent(Object arg0) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

	}

	/**
	 * Initialize language server process with the started language servers.
	 */
	public void load() {
		try {
			loading = true;
			LanguageServiceAccessor.getStartedWrappers(null, null, true) //
					.forEach(ls -> {
						Throwable serverError = ls.getServerError();
						listener.handleStatusChanged(ls);
						if (serverError != null) {
							listener.handleError(ls, serverError);
						}
					});
			viewer.setInput(LanguageServersRegistry.getInstance().getLanguageServerDefinitions());
			viewer.expandAll();
		} finally {
			loading = false;
		}
	}

	public boolean isDisposed() {
		// TODO Auto-generated method stub
		return false;
	}

	public void showMessage(LanguageServerModel processTreeNode, String message) {
		if (provider != null) {
			provider.getConsoleStream().println(message);
		}
	}

	public void showError(LanguageServerModel processTreeNode, Throwable exception) {
		if (consolesPanel != null) {
			consolesPanel.showError(processTreeNode, exception);
		}
	}

	public LanguageServerModel findLanguageServerItem(LanguageServerWrapper languageServer) {
		List<LanguageServerModel> servers = cache.get(languageServer.serverDefinition);
		if (servers == null) {
			servers = new ArrayList<>();
			cache.put(languageServer.serverDefinition, servers);
		}
		Optional<LanguageServerModel> serverModel = servers.stream()
				.filter(m -> m.getLanguageServer().equals(languageServer)).findFirst();
		if (!serverModel.isEmpty()) {
			return serverModel.get();
		}
		LanguageServerModel server = new LanguageServerModel(languageServer, this);
		servers.add(server);
		if (!loading) {
			refresh(languageServer.serverDefinition);
		}
		return server;
	}

	void refresh(Object element) {
		viewer.getTree().getDisplay().asyncExec(() -> viewer.refresh(element));
	}

	public void selectAndExpand(LanguageServerModel node) {
		viewer.getTree().getDisplay().asyncExec(() -> {
			viewer.expandAll();
			viewer.setSelection(new StructuredSelection(node));
		});
	}

	public void dispose() {
		LanguageServerLifecycleManager.getInstance().removeLanguageServerLifecycleListener(listener);
	}

}

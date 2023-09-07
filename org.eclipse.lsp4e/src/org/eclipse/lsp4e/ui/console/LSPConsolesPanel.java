package org.eclipse.lsp4e.ui.console;

import java.util.Map;

import org.eclipse.lsp4e.ui.console.explorer.LanguageServerModel;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.part.PageBook;

public class LSPConsolesPanel extends PageBook {

	private Map<LanguageServerModel, IConsole> consoles;

	public LSPConsolesPanel(Composite parent, int style) {
		super(parent, style);
	}

	public void showMessage(LanguageServerModel processTreeNode, String message) {
		if (isDisposed()) {
            return;
        }
        var consoleOrErrorPanel = consoles.get(processTreeNode);
        if (consoleOrErrorPanel != null) {
            //consoleOrErrorPanel.showMessage(message);
        }

	}

	public void showError(LanguageServerModel processTreeNode, Throwable exception) {
		// TODO Auto-generated method stub

	}

}

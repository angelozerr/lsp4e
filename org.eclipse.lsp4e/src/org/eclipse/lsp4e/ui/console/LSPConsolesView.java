package org.eclipse.lsp4e.ui.console;

import org.eclipse.lsp4e.ui.console.explorer.LanguageServerExplorer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.console.TextConsoleViewer;
import org.eclipse.ui.part.ViewPart;

public class LSPConsolesView extends ViewPart {

    private SashForm form;
	private LanguageServerExplorer explorer;
	private LSPConsolesPanel consolesPanel;
	private MessageConsoleStream consoleStream;

    public LSPConsolesView() {
    }

    @Override
    public void createPartControl(Composite parent) {
        // Créez un SashForm
        form = new SashForm(parent, SWT.HORIZONTAL);
        form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        explorer = new LanguageServerExplorer(form, this);
        //consolesPanel = new LSPConsolesPanel(form, SWT.BORDER);
        //explorer.setConsolesPanel(consolesPanel);

     // Créez un Composite pour afficher les consoles à droite
        Composite consoleComposite = new Composite(form, SWT.NONE);
        consoleComposite.setLayout(new GridLayout(1, false));

        form.setWeights(new int[] {1, 2});

        explorer.load();


        String consoleName = "XXX"; //$NON-NLS-1$


        // Créez ou récupérez la console associée
        MessageConsole myConsole = new MessageConsole(consoleName, null); //findOrCreateConsole(consoleName);

        // Créez un TextConsoleViewer pour afficher la console
       /* if (consoleViewer != null) {
            consoleViewer.getControl().dispose(); // Supprimez le viewer existant
        }*/
        TextConsoleViewer consoleViewer = new TextConsoleViewer(consoleComposite, myConsole);
        consoleViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        consoleStream = myConsole.newMessageStream();
        consoleStream.println("Ceci est un message dans la console Eclipse."); //$NON-NLS-1$

        // Rafraîchissez le Composite
        consoleComposite.layout();


     // Ajoutez un écouteur de sélection pour le Tree
                // Créez un Tree à gauche
        /*tree = new Tree(sashForm, SWT.BORDER);
        TreeItem rootItem = new TreeItem(tree, SWT.NONE);
        rootItem.setText("Root");

        TreeItem childItem = new TreeItem(rootItem, SWT.NONE);
        childItem.setText("Child");

        // Écoutez les événements de clic sur les éléments de l'arbre
        tree.addListener(SWT.Selection, event -> {
            TreeItem selectedItem = tree.getSelection()[0];
            if (selectedItem != null) {
                // Créez une instance de console

                MessageConsole myConsole = new MessageConsole("Ma Console", null);
                ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { myConsole });


                IConsoleView consoleView = ConsolePlugin.getDefault().getConsoleView();

                // Ouvrez la console
                ConsolePlugin.getDefault().getConsoleManager().showConsoleView(myConsole);

                // Écrivez dans la console
                MessageConsoleStream out = myConsole.newMessageStream();
                out.println("Bonjour, ceci est un exemple de console Eclipse.");

                // Affichez la console dans la vue de la console
               // IConsoleView consoleView = ConsolePlugin.getDefault().getConsoleView();
               // consoleView.display(console);
            }
        });*/
    }


    public MessageConsoleStream getConsoleStream() {
		return consoleStream;
	}

    @Override
    public void setFocus() {
        // Définissez le focus sur un composant au besoin
    }

    @Override
    public void dispose() {
        // Disposez correctement des ressources lorsque la vue est fermée
        if (form != null && !form.isDisposed()) {
            form.dispose();
        }
        if (explorer != null) {
        	explorer.dispose();
        }
        super.dispose();
    }
}

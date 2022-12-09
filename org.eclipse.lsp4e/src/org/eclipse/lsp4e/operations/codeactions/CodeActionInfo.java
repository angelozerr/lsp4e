package org.eclipse.lsp4e.operations.codeactions;

import java.util.List;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;

public class CodeActionInfo {

	private final TextDocumentService textDocumentService;

	private final List<Either<Command, CodeAction>> commands;

	public CodeActionInfo(List<Either<Command, CodeAction>> commands, TextDocumentService textDocumentService) {
		super();
		this.textDocumentService = textDocumentService;
		this.commands = commands;
	}

	public TextDocumentService getTextDocumentService() {
		return textDocumentService;
	}

	public List<Either<Command, CodeAction>> getCommands() {
		return commands;
	}
}

package me.coley.jremapper.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;


import me.coley.jremapper.Program;

public class ActionDragFile  implements ActionListener {
	private final Program callback;
	private final File file;

	public ActionDragFile(Program callback,File file) {
		this.callback = callback;
		this.file = file;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		callback.onFileSelect(this.file);
	}
}

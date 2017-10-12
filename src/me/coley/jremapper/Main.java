package me.coley.jremapper;

import javax.swing.UIManager;

public class Main {
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		Program program = new Program();
		program.showGui();
	}

}

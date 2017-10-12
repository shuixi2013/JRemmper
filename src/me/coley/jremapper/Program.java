package me.coley.jremapper;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang3.StringEscapeUtils;
import org.benf.cfr.reader.PluginRunner;

import me.coley.bmf.JarReader;
import me.coley.bmf.mapping.ClassMapping;
import me.coley.jremapper.gui.MainWindow;
import me.coley.jremapper.search.Search;

public class Program {
	private JarReader jar;
	/**
	 * GUI
	 */
	private final MainWindow window = new MainWindow(this);
	/**
	 * File chooser for selecting jars.
	 */
	private JFileChooser fileChooser;
	/**
	 * Map for handling renamed references to the proper bytecode.
	 */
	private CFRResourceLookup lookupHelper;
	/**
	 * Current class in text area.
	 */
	private ClassMapping currentClass;
	/**
	 * Options not pertaining to CFR.
	 */
	private Options options = new Options();
	/**
	 * Manager for keeping track of past actions.
	 */
	private History history = new History();
	/**
	 * Search handler
	 */
	private Search searcher = new Search(this);

	/**
	 * Displays the GUI.
	 */
	public void showGui() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					window.initialize();
					window.display();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Called when a file is loaded.
	 * 
	 * @param file
	 *            Jar loaded.
	 */
	public void onFileSelect(File file) {
		try {
		// Load jar file into BMF
		// Set up mappings
		jar = new JarReader(file, false, false);
		jar.read();
		jar.genMappings();
		// update file tree
		window.getFileTree().setup();
		// refresh lookup helper
		lookupHelper = new CFRResourceLookup(this);
		}catch (Exception e) {
			window.showDialog("Warn", "this file is not a jar file,please make sure you have select a correct jar file.");
		}
	}

	/**
	 * Called when the a mappings file is selected to be applied to the current
	 * jar.
	 * 
	 * @param selectedFile
	 */
	public void onLoadMapping(File selectedFile) {
		try {
			jar.loadMappingsFrom(selectedFile);
			refreshTree();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Called when the current progress is saved as a mappings file.
	 * 
	 * @param selectedFile
	 */
	public void onSaveMappings(File selectedFile) {
		try {
			jar.saveMappingsTo(selectedFile, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Called when the current progress is saved as a jar file.
	 * 
	 * @param selectedFile
	 */
	public void onSaveJar(File selectedFile) {
		jar.saveJarTo(selectedFile);
	}

	/**
	 * Updates a tree path given the initial/after values of a class rename.
	 * 
	 * @param original
	 * @param renamed
	 */
	public void updateTreePath(String original, String renamed) {
		window.getFileTree().update(jar, original, renamed);
	}

	/**
	 * Regenerates the tree.
	 */
	public void refreshTree() {
		window.getFileTree().refresh();
	}

	/**
	 * Called when a class in the file tree is selected.
	 * 
	 * @param mapping
	 */
	public void onClassSelect(ClassMapping clazz) {
		String originalName = clazz.name.original;
		// Update current class
		this.currentClass = clazz;
		// window.getHistory().addHistory(clazz);
		// Decompile using CFR, send text to the text-area.
		try {
			PluginRunner pluginRunner = new PluginRunner(CFRSetting.toStringMap(), new CFRSourceImpl(lookupHelper));
			String decomp = pluginRunner.getDecompilationFor(originalName);
			String regex = "/*\n * Decompiled with CFR.\n */\n";
			if(decomp.startsWith(regex)) {
				decomp = decomp.substring(regex.length());
			}
			if(options.get(Options.SUPPORT_UNICODES)) {				
				decomp = StringEscapeUtils.unescapeJava(decomp);
			}
			window.openSourceTab(clazz.name.getValue(), decomp);
		} catch (Exception e) {
			window.openTab("Error", "Error: CFR failed to decompile this class:\n" + e.getMessage());
		}
	}

	/**
	 * Returns the file chooser. If it is null it is instantiated and set to the
	 * working directory with a filter for jar files.
	 * 
	 * @return
	 */
	public JFileChooser getFileChooser() {
		return getFileChooser("Java Archives", "jar");
	}

	/**
	 * Returns the file chooser. If it is null it is instantiated and set to the
	 * working directory with a filter for the given file type. To allow any
	 * type, have the parameters be null.
	 * 
	 * @param fileType
	 *            Name of the type of file
	 * @param extension
	 *            Actual file extension.
	 * @return
	 */
	public JFileChooser getFileChooser(String fileType, String extension) {
		if (fileChooser == null) {
			fileChooser = new JFileChooser();
			String dir = System.getProperty("user.dir");
			File fileDir = new File(dir);
			fileChooser.setDialogTitle("Open File");
			fileChooser.setCurrentDirectory(fileDir);
		}
		if (fileType == null || extension == null) {
			fileChooser.setFileFilter(null);
		} else {
			FileNameExtensionFilter filter = new FileNameExtensionFilter(fileType, extension);
			fileChooser.setFileFilter(filter);
		}
		return fileChooser;
	}

	/**
	 * Creates and returns a file chooser set in the working directory.
	 * 
	 * @return
	 */
	public JFileChooser createFileSaver() {
		JFileChooser fileSaver = new JFileChooser();
		String dir = System.getProperty("user.dir");
		File fileDir = new File(dir);
		fileSaver.setCurrentDirectory(fileDir);
		fileSaver.setDialogTitle("Save to File");
		return fileSaver;
	}

	/**
	 * Returns the GUI.
	 * 
	 * @return
	 */
	public final MainWindow getWindow() {
		return window;
	}

	/**
	 * Returns the current ClassMapping of the class in the text area.
	 * 
	 * @return
	 */
	public ClassMapping getCurrentClass() {
		return currentClass;
	}

	/**
	 * Returns the JarReader.
	 * 
	 * @return
	 */
	public JarReader getJarReader() {
		return jar;
	}

	/**
	 * Returns program options.
	 * 
	 * @return
	 */
	public Options getOptions() {
		return options;
	}

	/**
	 * Returns history manager.
	 * 
	 * @return
	 */
	public History getHistory() {
		return history;
	}

	/**
	 * Returns search handler.
	 * 
	 * @return
	 */
	public Search getSearcher() {
		return searcher;
	}
}

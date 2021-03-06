package de.frosner.datagenerator.gui.main;

import static de.frosner.datagenerator.gui.verifiers.InputVerifier.isDouble;
import static de.frosner.datagenerator.gui.verifiers.InputVerifier.isInteger;
import static de.frosner.datagenerator.gui.verifiers.InputVerifier.isName;
import static de.frosner.datagenerator.gui.verifiers.InputVerifier.verifyComponent;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpringLayout;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;

import net.sf.qualitycheck.Check;

import org.jgraph.JGraph;
import org.jgraph.graph.DefaultGraphCell;

import com.google.common.collect.Lists;

import de.frosner.datagenerator.distributions.BernoulliDistribution;
import de.frosner.datagenerator.distributions.CategorialDistribution;
import de.frosner.datagenerator.distributions.ContinuousVariableParameter;
import de.frosner.datagenerator.distributions.FixedParameter;
import de.frosner.datagenerator.distributions.GaussianDistribution;
import de.frosner.datagenerator.distributions.Parameter;
import de.frosner.datagenerator.distributions.VariableParameter;
import de.frosner.datagenerator.exceptions.UnknownActionEventSourceException;
import de.frosner.datagenerator.exceptions.UnsupportedSelectionException;
import de.frosner.datagenerator.export.CsvFileExportConfiguration;
import de.frosner.datagenerator.export.ExportFeatureNames;
import de.frosner.datagenerator.export.ExportInstanceIds;
import de.frosner.datagenerator.features.FeatureDefinition;
import de.frosner.datagenerator.gui.main.GaussianFeatureEntry.MeanIsDependent;
import de.frosner.datagenerator.gui.services.DataGeneratorService;
import de.frosner.datagenerator.gui.services.FeatureDefinitionGraphVisualizationManager;
import de.frosner.datagenerator.gui.services.FeatureParameterDependencySelectorManager;
import de.frosner.datagenerator.gui.services.GenerationButtonsToggleManager;
import de.frosner.datagenerator.gui.services.PreviewTableManager;
import de.frosner.datagenerator.gui.services.ProgressBarManager;
import de.frosner.datagenerator.gui.services.TextAreaLogManager;
import de.frosner.datagenerator.util.ApplicationMetaData;
import de.frosner.datagenerator.util.VisibleForTesting;

public final class SwingMenu extends JFrame implements ActionListener {

	private static final long serialVersionUID = ApplicationMetaData.SERIAL_VERSION_UID;

	private static final int LINE_HEIGHT = 30;
	private static final int LINE_WIDTH = 180;
	private static final int PADDING = 5;

	@VisibleForTesting
	final JButton _addFeatureButton;
	@VisibleForTesting
	final JButton _editFeatureButton;
	@VisibleForTesting
	final JButton _removeFeatureButton;
	@VisibleForTesting
	final JButton _generateDataButton;
	@VisibleForTesting
	final JButton _abortDataGenerationButton;

	private final JLabel _distributionSelectorLabel;
	@VisibleForTesting
	final JComboBox _distributionSelector;
	private final JLabel _featureNameLabel;
	@VisibleForTesting
	final JTextField _featureNameField;
	private final JLabel _gaussianMeanLabel;
	@VisibleForTesting
	final JComboBox _gaussianMeanParameterTypeSelector;
	private final JPanel _gaussianMeanValuePanel;
	@VisibleForTesting
	final JComboBox _gaussianMeanSelector;
	@VisibleForTesting
	final JTextField _gaussianMeanField;
	private final JLabel _gaussianSigmaLabel;
	@VisibleForTesting
	final JTextField _gaussianSigmaField;
	private final JLabel _bernoulliProbabilityLabel;
	@VisibleForTesting
	final JTextField _bernoulliProbabilityField;
	private final JLabel _uniformCategorialNumberOfStatesLabel;
	@VisibleForTesting
	final JTextField _uniformCategorialNumberOfStatesField;

	private final JLabel _numberOfInstancesLabel;
	@VisibleForTesting
	final JTextField _numberOfInstancesField;
	private final JLabel _exportFileLabel;
	@VisibleForTesting
	final JFileChooser _exportFileDialog;
	@VisibleForTesting
	final JButton _exportFileButton;
	@VisibleForTesting
	final JTextField _exportFileField;
	@VisibleForTesting
	final JCheckBox _exportInstanceIdsBox;
	@VisibleForTesting
	final JCheckBox _exportFeatureNamesBox;

	@VisibleForTesting
	static final FileFilter CSV_FILE_FILTER = new ExtensionFileFilter("Comma Separated Values (.csv)", "csv");
	@VisibleForTesting
	static final FileFilter ALL_FILE_FILTER = new AllFileFilter();

	@VisibleForTesting
	final JGraph _featureGraph;
	private final JScrollPane _featureGraphScroller;

	@VisibleForTesting
	final VariableColumnCountTableModel _previewTableModel;
	@VisibleForTesting
	final JTable _previewTable;
	@VisibleForTesting
	final JOptionPane _featureDefinitionPane;
	@VisibleForTesting
	final FeatureDefinitionDialog _featureDefinitionDialog;
	private final JPanel _distributionParametersPanel;
	private final JPanel _featureDefinitionDialogPanel;

	@VisibleForTesting
	final JProgressBar _progressBar;

	@VisibleForTesting
	final JEditorPane _logArea;
	private final JScrollPane _logAreaScroller;

	@VisibleForTesting
	final JMenuBar _menuBar;
	private final JMenu _fileMenu;
	@VisibleForTesting
	final JMenuItem _generateDataMenuItem;
	@VisibleForTesting
	final JMenuItem _closeMenuItem;
	private final JMenu _featuresMenu;
	@VisibleForTesting
	final JMenuItem _addFeatureMenuItem;
	private final JMenu _helpMenu;
	private final JMenuItem _aboutMenuItem;

	private GenerateDataButtonWorker _generateDataButtonWorker;

	private JPanel _gaussianPanel;

	public SwingMenu() {
		// BEGIN frame initialization
		setTitle(ApplicationMetaData.getName());
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(true);
		setIconImage(new ImageIcon(getClass().getClassLoader().getResource("frame_icon.png")).getImage());
		// END frame initialization

		// BEGIN menu bar initialization
		_menuBar = new JMenuBar();
		setJMenuBar(_menuBar);

		_fileMenu = new JMenu("File");
		_fileMenu.setMnemonic(KeyEvent.VK_F);
		_generateDataMenuItem = new JMenuItem("Generate Data");
		_generateDataMenuItem.addActionListener(this);
		_closeMenuItem = new JMenuItem("Exit");
		_closeMenuItem.addActionListener(this);
		_fileMenu.add(_generateDataMenuItem);
		_fileMenu.add(_closeMenuItem);

		_featuresMenu = new JMenu("Features");
		_featuresMenu.setMnemonic(KeyEvent.VK_A);
		_addFeatureMenuItem = new JMenuItem("Add Feature");
		_addFeatureMenuItem.addActionListener(this);
		_featuresMenu.add(_addFeatureMenuItem);

		_helpMenu = new JMenu("Help");
		_helpMenu.setMnemonic(KeyEvent.VK_O);
		_aboutMenuItem = new JMenuItem("About");
		_aboutMenuItem.addActionListener(this);
		_helpMenu.add(_aboutMenuItem);

		_menuBar.add(_fileMenu);
		_menuBar.add(_featuresMenu);
		_menuBar.add(_helpMenu);
		// END menu bar initialization

		// BEGIN component definition
		_distributionSelectorLabel = new JLabel("Distribution", JLabel.RIGHT);
		_distributionSelector = new JComboBox(new Object[] { BernoulliFeatureEntry.KEY,
				UniformCategorialFeatureEntry.KEY, GaussianFeatureEntry.KEY });
		_distributionSelector.addActionListener(this);
		_featureNameLabel = new JLabel("Name", JLabel.RIGHT);
		_featureNameField = new JTextField();
		_featureNameField.setMinimumSize(new Dimension(LINE_WIDTH, LINE_HEIGHT));
		_gaussianMeanLabel = new JLabel("Mean", JLabel.RIGHT);
		_gaussianMeanParameterTypeSelector = new JComboBox(new Object[] { FixedParameter.KEY, VariableParameter.KEY });
		_gaussianMeanParameterTypeSelector.addActionListener(this);
		_gaussianMeanSelector = new JComboBox();
		FeatureParameterDependencySelectorManager.manageGaussianMeanSelector(_gaussianMeanSelector);
		_gaussianMeanField = new JTextField();
		_gaussianMeanField.setMinimumSize(new Dimension(LINE_WIDTH, LINE_HEIGHT));
		_gaussianSigmaLabel = new JLabel("Sigma", JLabel.RIGHT);
		_gaussianSigmaField = new JTextField();
		_gaussianSigmaField.setMinimumSize(new Dimension(LINE_WIDTH, LINE_HEIGHT));
		_bernoulliProbabilityLabel = new JLabel("P", JLabel.RIGHT);
		_bernoulliProbabilityField = new JTextField();
		_bernoulliProbabilityField.setMinimumSize(new Dimension(LINE_WIDTH, LINE_HEIGHT));
		_uniformCategorialNumberOfStatesLabel = new JLabel("#States", JLabel.RIGHT);
		_uniformCategorialNumberOfStatesField = new JTextField();
		_uniformCategorialNumberOfStatesField.setMinimumSize(new Dimension(LINE_WIDTH, LINE_HEIGHT));
		_addFeatureButton = new JButton("Add Feature");
		_addFeatureButton.addActionListener(this);
		_editFeatureButton = new JButton("Edit Feature");
		_editFeatureButton.addActionListener(this);
		_featureDefinitionDialog = new FeatureDefinitionDialog(this, "Add Feature");
		_featureGraph = FeatureDefinitionGraphVisualizationManager.createNewManagedJGraph();
		_featureGraph.setCloneable(true);
		_featureGraph.setDragEnabled(false);
		_featureGraph.setDropEnabled(false);
		_featureGraph.setEdgeLabelsMovable(false);
		_featureGraph.setEditable(false);
		_featureGraph.setMoveable(false);
		_featureGraph.setSizeable(false);
		_featureGraph.setXorEnabled(false);
		_featureGraph.setJumpToDefaultPort(true);
		_featureGraphScroller = new JScrollPane(_featureGraph);
		_removeFeatureButton = new JButton("Remove Feature");
		_removeFeatureButton.addActionListener(this);
		_numberOfInstancesLabel = new JLabel("#Instances", JLabel.RIGHT);
		_numberOfInstancesField = new JTextField();
		_numberOfInstancesField.setPreferredSize(new Dimension(LINE_WIDTH, LINE_HEIGHT));
		_exportFileLabel = new JLabel("Export File", JLabel.RIGHT);
		_exportFileDialog = new ExportFileChooser(ALL_FILE_FILTER);
		_exportFileDialog.addChoosableFileFilter(CSV_FILE_FILTER);
		_exportFileDialog.setFileFilter(CSV_FILE_FILTER);
		_exportFileButton = new JButton("...");
		_exportFileButton.addActionListener(this);
		_exportFileField = new JTextField();
		_exportFileField.setEditable(false);
		_exportFileField.setPreferredSize(new Dimension(LINE_WIDTH - 25, LINE_HEIGHT));
		_exportInstanceIdsBox = new JCheckBox("Instance IDs");
		_exportFeatureNamesBox = new JCheckBox("Feature Names");
		_progressBar = new JProgressBar(0, 100);
		_progressBar.setPreferredSize(new Dimension(100, LINE_HEIGHT + 5));
		ProgressBarManager.manageProgressBar(_progressBar);
		_generateDataButton = new JButton("Generate Data");
		_generateDataButton.addActionListener(this);
		_abortDataGenerationButton = new JButton("Abort Generation");
		_abortDataGenerationButton.addActionListener(this);
		_abortDataGenerationButton.setEnabled(false);
		GenerationButtonsToggleManager.manageButtons(Lists.newArrayList((AbstractButton) _generateDataButton,
				(AbstractButton) _generateDataMenuItem), Lists
				.newArrayList((AbstractButton) _abortDataGenerationButton));
		_previewTableModel = new VariableColumnCountTableModel(10, 4);
		_previewTable = new JTable(_previewTableModel);
		_previewTable.setEnabled(false);
		_previewTable.setPreferredSize(new Dimension(700, 50));
		PreviewTableManager.managePreviewTable(_previewTableModel);
		_logArea = new JEditorPane("text/html", null);
		_logArea.setPreferredSize(new Dimension(LINE_WIDTH, 70));
		_logAreaScroller = new JScrollPane(_logArea);
		TextAreaLogManager.manageLogArea(_logArea);
		_logArea.setBorder(new LineBorder(Color.gray, 1));
		_logArea.setEditable(false);
		_logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		_logArea.setAutoscrolls(true);
		_logAreaScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		// END component definition

		// BEGIN main layout
		Container contentPane = getContentPane();
		contentPane.setLayout(new SpringLayout());

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new SpringLayout());
		topPanel.setMaximumSize(new Dimension(0, 0)); // avoid resizing
		contentPane.add(topPanel);

		JPanel featureButtonPanel = new JPanel();
		featureButtonPanel.setLayout(new SpringLayout());
		topPanel.add(featureButtonPanel);
		featureButtonPanel.add(_addFeatureButton);
		featureButtonPanel.add(_editFeatureButton);
		featureButtonPanel.add(_removeFeatureButton);
		JLabel whiteSpaceLabel = new JLabel(" "); // just for adding whitespace at bottom
		whiteSpaceLabel.setPreferredSize(new Dimension(50, 250));
		featureButtonPanel.add(whiteSpaceLabel);
		SpringUtilities.makeCompactGrid(featureButtonPanel, 4, 1, 0, 0, 5, 5);

		JPanel featureGraphPanel = new JPanel();
		featureGraphPanel.setLayout(new SpringLayout());
		topPanel.add(featureGraphPanel);
		featureGraphPanel.setLayout(new BorderLayout());
		featureGraphPanel.add(_featureGraphScroller, BorderLayout.CENTER);
		featureGraphPanel.setPreferredSize(new Dimension(LINE_WIDTH, 5));

		SpringUtilities.makeCompactGrid(topPanel, 1, 2, 0, 0, 0, 0);

		JPanel midPanel = new JPanel();
		midPanel.setLayout(new SpringLayout());
		contentPane.add(midPanel);
		JPanel midLeftPanel = new JPanel();
		midPanel.add(midLeftPanel);
		midLeftPanel.setLayout(new SpringLayout());
		JPanel generateDataPanel = new JPanel();
		generateDataPanel.setLayout(new SpringLayout());
		generateDataPanel.add(_numberOfInstancesLabel);
		generateDataPanel.add(_numberOfInstancesField);
		generateDataPanel.add(_exportFileLabel);
		JPanel exportFileSubPanel = new JPanel();
		generateDataPanel.add(exportFileSubPanel);
		exportFileSubPanel.setLayout(new SpringLayout());
		exportFileSubPanel.add(_exportFileField);
		exportFileSubPanel.add(_exportFileButton);
		JPanel exportCheckBoxPanel = new JPanel();
		generateDataPanel.add(new JLabel());
		generateDataPanel.add(exportCheckBoxPanel);
		exportCheckBoxPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		exportCheckBoxPanel.add(_exportInstanceIdsBox);
		exportCheckBoxPanel.add(_exportFeatureNamesBox);
		SpringUtilities.makeCompactGrid(exportFileSubPanel, 1, 2, 0, 0, 0, 0);
		SpringUtilities.makeCompactGrid(generateDataPanel, 3, 2, 0, 0, 0, 0);
		JPanel midLeftButtonPanel = new JPanel();
		midLeftButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		midLeftButtonPanel.add(_abortDataGenerationButton);
		midLeftButtonPanel.add(_generateDataButton);
		midLeftPanel.add(generateDataPanel);
		midLeftPanel.add(midLeftButtonPanel);
		midLeftPanel.add(_progressBar);
		SpringUtilities.makeCompactGrid(midLeftPanel, 3, 1, 0, 0, 0, 0);
		midPanel.add(midLeftPanel);
		midPanel.add(_previewTable);
		SpringUtilities.makeCompactGrid(midPanel, 1, 2, 0, 0, PADDING, PADDING);

		contentPane.add(_logAreaScroller);

		SpringUtilities.makeCompactGrid(contentPane, 3, 1, 15, 15, 15, 15);
		// END main layout

		// BEGIN dialogs layout
		_distributionParametersPanel = new JPanel();
		_distributionParametersPanel.setLayout(new MinimalCardLayout());

		JPanel bernoulliPanel = new JPanel();
		_distributionParametersPanel.add(bernoulliPanel, BernoulliFeatureEntry.KEY);
		bernoulliPanel.setLayout(new SpringLayout());
		bernoulliPanel.add(_bernoulliProbabilityLabel);
		bernoulliPanel.add(_bernoulliProbabilityField);
		SpringUtilities.makeCompactGrid(bernoulliPanel, 1, 2, 0, 0, PADDING, PADDING);

		JPanel uniformCategorialPanel = new JPanel();
		_distributionParametersPanel.add(uniformCategorialPanel, UniformCategorialFeatureEntry.KEY);
		uniformCategorialPanel.setLayout(new SpringLayout());
		uniformCategorialPanel.add(_uniformCategorialNumberOfStatesLabel);
		uniformCategorialPanel.add(_uniformCategorialNumberOfStatesField);
		SpringUtilities.makeCompactGrid(uniformCategorialPanel, 1, 2, 0, 0, PADDING, PADDING);

		_gaussianPanel = new JPanel();
		_distributionParametersPanel.add(_gaussianPanel, GaussianFeatureEntry.KEY);
		_gaussianPanel.setLayout(new SpringLayout());
		_gaussianPanel.add(_gaussianMeanLabel);
		_gaussianPanel.add(_gaussianMeanParameterTypeSelector);
		_gaussianPanel.add(new JLabel());
		_gaussianMeanValuePanel = new JPanel(new MinimalCardLayout());
		_gaussianMeanValuePanel.add(FixedParameter.KEY, _gaussianMeanField);
		_gaussianMeanValuePanel.add(VariableParameter.KEY, _gaussianMeanSelector);
		_gaussianPanel.add(_gaussianMeanValuePanel);
		_gaussianPanel.add(_gaussianSigmaLabel);
		_gaussianPanel.add(_gaussianSigmaField);
		SpringUtilities.makeCompactGrid(_gaussianPanel, FeatureDefinitionDialog.GAUSSIAN_PANEL_ROWS,
				FeatureDefinitionDialog.GAUSSIAN_PANEL_COLUMNS, 0, 0, PADDING, PADDING);

		_featureDefinitionDialogPanel = new JPanel();
		_featureDefinitionDialogPanel.setLayout(new BoxLayout(_featureDefinitionDialogPanel, BoxLayout.Y_AXIS));
		JPanel distributionSelectionAndFeatureNamePanel = new JPanel();
		distributionSelectionAndFeatureNamePanel.setLayout(new SpringLayout());
		_featureDefinitionDialogPanel.add(distributionSelectionAndFeatureNamePanel);
		distributionSelectionAndFeatureNamePanel.add(_distributionSelectorLabel);
		distributionSelectionAndFeatureNamePanel.add(_distributionSelector);
		distributionSelectionAndFeatureNamePanel.add(_featureNameLabel);
		distributionSelectionAndFeatureNamePanel.add(_featureNameField);
		SpringUtilities.makeCompactGrid(distributionSelectionAndFeatureNamePanel, 2, 2, 0, 0, PADDING, PADDING);

		_featureDefinitionDialogPanel.add(Box.createRigidArea(new Dimension(0, 12)));
		_featureDefinitionDialogPanel.add(new JSeparator());
		_featureDefinitionDialogPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		JPanel distributionParametersLabelPanel = new JPanel();
		distributionParametersLabelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		distributionParametersLabelPanel.add(new JLabel("Distribution Parameters"));
		_featureDefinitionDialogPanel.add(distributionParametersLabelPanel);
		_featureDefinitionDialogPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		_featureDefinitionDialogPanel.add(_distributionParametersPanel);

		_featureDefinitionPane = new JOptionPane(_featureDefinitionDialogPanel, JOptionPane.PLAIN_MESSAGE,
				JOptionPane.OK_CANCEL_OPTION);
		_featureDefinitionDialog.setContentPane(_featureDefinitionPane);
		_featureDefinitionDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		_featureDefinitionPane.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				String property = e.getPropertyName();

				if (_featureDefinitionDialog.isVisible() && (e.getSource() == _featureDefinitionPane)
						&& (property.equals(JOptionPane.VALUE_PROPERTY))) {
					if (_featureDefinitionPane.getValue().equals(JOptionPane.OK_OPTION)) {
						if (verifyInputs()) {
							addFeatureDefinition();
							_featureDefinitionDialog.setVisible(false);
							_featureDefinitionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
						} else {
							_featureDefinitionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
						}
					} else if (_featureDefinitionPane.getValue().equals(JOptionPane.UNINITIALIZED_VALUE)) {
						// Do nothing as this happens when OK was clicked but inputs could not be verified
					} else {
						verifyInputs();
						_featureDefinitionDialog.setVisible(false);
						_featureDefinitionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
					}
				}
			}
		});

		_featureDefinitionDialog.setResizable(false);
		_featureDefinitionDialog.setLocation(getCenteredLocationOf(_featureDefinitionDialog));
		_featureDefinitionDialog.pack();
		// END dialog layouts

		// BEGIN define custom focus traversal
		List<Component> focusOrder = Lists.newArrayList();
		focusOrder.add(_addFeatureButton);
		focusOrder.add(_featureGraph);
		focusOrder.add(_editFeatureButton);
		focusOrder.add(_removeFeatureButton);
		focusOrder.add(_numberOfInstancesField);
		focusOrder.add(_exportFileButton);
		focusOrder.add(_exportInstanceIdsBox);
		focusOrder.add(_exportFeatureNamesBox);
		focusOrder.add(_generateDataButton);
		focusOrder.add(_abortDataGenerationButton);
		focusOrder.add(_logArea);
		setFocusTraversalPolicy(new OrderedFocusTraversalPolicy(focusOrder));
		// END define custom focus traversal

		// BEGIN finalize
		pack();
		setLocation(getCenteredLocationOf(this));
		setMinimumSize(getSize());
		setVisible(true);
		// END finalize
	}

	public void detachGenerateDataButtonWorker() {
		_generateDataButtonWorker = null;
	}

	/**
	 * @throws UnknownActionEventSourceException
	 *             if the performed {@linkplain ActionEvent} cannot be handled by the {@linkplain SwingMenu}
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if (source.equals(_addFeatureButton) || source.equals(_addFeatureMenuItem)) {
			_featureDefinitionDialog.setVisible(true);

		} else if (source.equals(_distributionSelector)) {
			updateCardLayoutedPanelBySelector(_distributionParametersPanel, _distributionSelector);
			_featureDefinitionDialog.pack();

		} else if (source.equals(_gaussianMeanParameterTypeSelector)) {
			updateCardLayoutedPanelBySelector(_gaussianMeanValuePanel, _gaussianMeanParameterTypeSelector);
			SpringUtilities.makeCompactGrid(_gaussianPanel, FeatureDefinitionDialog.GAUSSIAN_PANEL_ROWS,
					FeatureDefinitionDialog.GAUSSIAN_PANEL_COLUMNS, 0, 0, PADDING, PADDING);
			_featureDefinitionDialog.pack();

		} else if (source.equals(_editFeatureButton)) {
			final Object selectedCell = _featureGraph.getSelectionCell();
			if (selectedCell != null) {
				FeatureDefinitionEntry selectedEntry = FeatureDefinitionGraphVisualizationManager
						.getFeatureDefinitionEntryByCell(selectedCell);
				_featureDefinitionDialog.setFeatureToEdit(selectedEntry);
				_featureNameField.setText(selectedEntry.getFeatureName());
				if (selectedEntry instanceof BernoulliFeatureEntry) {
					_distributionSelector.setSelectedItem(BernoulliFeatureEntry.KEY);
					_bernoulliProbabilityField.setText(((BernoulliFeatureEntry) selectedEntry).getP());
				} else if (selectedEntry instanceof UniformCategorialFeatureEntry) {
					_distributionSelector.setSelectedItem(UniformCategorialFeatureEntry.KEY);
					_uniformCategorialNumberOfStatesField.setText(((UniformCategorialFeatureEntry) selectedEntry)
							.getNumberOfStates());
				} else if (selectedEntry instanceof GaussianFeatureEntry) {
					_distributionSelector.setSelectedItem(GaussianFeatureEntry.KEY);
					_gaussianMeanField.setText(((GaussianFeatureEntry) selectedEntry).getMeanAsString());
					_gaussianSigmaField.setText(((GaussianFeatureEntry) selectedEntry).getSigma());
				}
				_featureDefinitionDialog.setVisible(true);
				_featureDefinitionDialog.leaveEditMode();
			}

		} else if (source.equals(_removeFeatureButton)) {
			DefaultGraphCell selectedCell = (DefaultGraphCell) _featureGraph.getSelectionCell();
			if (selectedCell != null) {
				final FeatureDefinitionEntry selectedFeatureEntry = (FeatureDefinitionEntry) selectedCell
						.getUserObject();
				new Thread(new Runnable() {
					@Override
					public void run() {
						DataGeneratorService.INSTANCE.removeFeatureDefinition(selectedFeatureEntry);
					}
				}).start();
			}

		} else if (source.equals(_exportFileButton)) {
			if (_exportFileDialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				_exportFileField.setText(_exportFileDialog.getSelectedFile().getPath());
				verifyComponent(_exportFileField, isName(_exportFileField.getText()).isFileName().verify());
			}

		} else if (source.equals(_generateDataButton) || source.equals(_generateDataMenuItem)) {
			if (verifyComponent(_numberOfInstancesField, isInteger(_numberOfInstancesField.getText()).isPositive()
					.verify())
					& verifyComponent(_featureGraph, _featureGraph.getModel().getRootCount() > 0)
					& verifyComponent(_exportFileField, isName(_exportFileField.getText()).isFileName().verify())) {
				final int numberOfInstances = Integer.parseInt(_numberOfInstancesField.getText());
				final File exportFile = _exportFileDialog.getSelectedFile();
				final ExportInstanceIds exportInstanceIds = ExportInstanceIds.when(_exportInstanceIdsBox.isSelected());
				final ExportFeatureNames exportFeatureNames = ExportFeatureNames.when(_exportFeatureNamesBox
						.isSelected());
				_generateDataButtonWorker = new GenerateDataButtonWorker(numberOfInstances,
						new CsvFileExportConfiguration(exportFile, exportInstanceIds, exportFeatureNames));
				_generateDataButtonWorker.execute();
			}

		} else if (source.equals(_abortDataGenerationButton)) {
			if (_generateDataButtonWorker != null) {
				_generateDataButtonWorker.cancel(true);
			}

		} else if (source.equals(_aboutMenuItem)) {
			JTextArea applicationMetaData = new JTextArea(ApplicationMetaData.getName() + "\nVersion: "
					+ ApplicationMetaData.getVersion() + "\nRevision: " + ApplicationMetaData.getRevision()
					+ "\nBuilt on: " + ApplicationMetaData.getTimestamp());
			applicationMetaData.setEditable(false);
			JOptionPane dialog = new JOptionPane(applicationMetaData, JOptionPane.INFORMATION_MESSAGE,
					JOptionPane.DEFAULT_OPTION);
			dialog.createDialog("About").setVisible(true);

		} else if (source.equals(_closeMenuItem)) {
			this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));

		} else {
			throw new UnknownActionEventSourceException(source);
		}
	}

	private static void updateCardLayoutedPanelBySelector(JPanel cardLayoutedPanel, JComboBox selector) {
		Check.instanceOf(CardLayout.class, cardLayoutedPanel.getLayout());
		((CardLayout) cardLayoutedPanel.getLayout()).show(cardLayoutedPanel, (String) selector.getSelectedItem());
	}

	private void addFeatureDefinition() {
		String name = _featureNameField.getText();
		Object selectedItem = _distributionSelector.getSelectedItem();
		final FeatureDefinition featureDefinition;
		final FeatureDefinitionEntry featureDefinitionEntry;

		if (selectedItem.equals(BernoulliFeatureEntry.KEY)) {
			double p = Double.parseDouble(_bernoulliProbabilityField.getText());
			featureDefinition = new FeatureDefinition(name, new BernoulliDistribution(new FixedParameter<Double>(p)));
			featureDefinitionEntry = new BernoulliFeatureEntry(featureDefinition, _bernoulliProbabilityField.getText());

		} else if (selectedItem.equals(UniformCategorialFeatureEntry.KEY)) {
			int numberOfStates = Integer.parseInt(_uniformCategorialNumberOfStatesField.getText());
			List<Double> probabilities = Lists.newArrayList();
			for (int i = 0; i < numberOfStates; i++) {
				probabilities.add(1D / numberOfStates);
			}
			featureDefinition = new FeatureDefinition(name, new CategorialDistribution(
					new FixedParameter<List<Double>>(probabilities)));
			featureDefinitionEntry = new UniformCategorialFeatureEntry(featureDefinition,
					_uniformCategorialNumberOfStatesField.getText());

		} else if (selectedItem.equals(GaussianFeatureEntry.KEY)) {
			Parameter<Double> meanParameter;
			MeanIsDependent meanIsDependent;
			Object meanEntry;

			if (_gaussianMeanParameterTypeSelector.getSelectedItem().equals(FixedParameter.KEY)) {
				double mean = Double.parseDouble(_gaussianMeanField.getText());
				meanParameter = new FixedParameter<Double>(mean);
				meanIsDependent = MeanIsDependent.FALSE;
				meanEntry = _gaussianMeanField.getText();
			} else {
				meanIsDependent = MeanIsDependent.TRUE;
				meanEntry = _gaussianMeanSelector.getSelectedItem();
				meanParameter = new ContinuousVariableParameter(((FeatureDefinitionEntry) meanEntry)
						.getFeatureDefinition());
			}

			double sigma = Double.parseDouble(_gaussianSigmaField.getText());

			featureDefinition = new FeatureDefinition(name, new GaussianDistribution(meanParameter,
					new FixedParameter<Double>(sigma)));
			featureDefinitionEntry = new GaussianFeatureEntry(featureDefinition, meanEntry, meanIsDependent,
					_gaussianSigmaField.getText());

		} else {
			throw new UnsupportedSelectionException(selectedItem);
		}

		if (_featureDefinitionDialog.isInEditMode()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					DataGeneratorService.INSTANCE.replaceFeatureDefinition(_featureDefinitionDialog.getFeatureToEdit(),
							featureDefinitionEntry);
				}
			}).start();
		} else {
			new Thread(new Runnable() {
				@Override
				public void run() {
					DataGeneratorService.INSTANCE.addFeatureDefinition(featureDefinitionEntry);
				}
			}).start();
		}
		verifyComponent(_featureGraph, true);
	}

	private boolean verifyInputs() {
		String name = _featureNameField.getText();
		if (!verifyComponent(_featureNameField, isName(name).isNotLongerThan(30))) {
			return false;
		}
		Object selectedDistribution = _distributionSelector.getSelectedItem();

		if (selectedDistribution.equals(BernoulliFeatureEntry.KEY)) {
			return verifyComponent(_bernoulliProbabilityField, isDouble(_bernoulliProbabilityField.getText())
					.isProbability());

		} else if (selectedDistribution.equals(UniformCategorialFeatureEntry.KEY)) {
			return verifyComponent(_uniformCategorialNumberOfStatesField, isInteger(
					_uniformCategorialNumberOfStatesField.getText()).isPositive().isInInterval(1, 1000));

		} else if (selectedDistribution.equals(GaussianFeatureEntry.KEY)) {
			boolean meanIsVerified;
			Object selectedMeanParameterType = _gaussianMeanParameterTypeSelector.getSelectedItem();
			if (selectedMeanParameterType.equals(FixedParameter.KEY)) {
				meanIsVerified = verifyComponent(_gaussianMeanField, isDouble(_gaussianMeanField.getText()).verify());
			} else if (selectedMeanParameterType.equals(VariableParameter.KEY)) {
				meanIsVerified = _gaussianMeanSelector.getSelectedIndex() != -1;
			} else {
				throw new UnsupportedSelectionException(selectedMeanParameterType);
			}
			boolean sigmaIsVerified = verifyComponent(_gaussianSigmaField, isDouble(_gaussianSigmaField.getText())
					.isPositive());
			return meanIsVerified && sigmaIsVerified;

		} else {
			throw new UnsupportedSelectionException(selectedDistribution);
		}
	}

	private static Point getCenteredLocationOf(Component component) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Point middle = new Point(screenSize.width / 2, screenSize.height / 2);
		Point newLocation = new Point(middle.x - (component.getWidth() / 2), middle.y - (component.getHeight() / 2));
		return newLocation;
	}

}

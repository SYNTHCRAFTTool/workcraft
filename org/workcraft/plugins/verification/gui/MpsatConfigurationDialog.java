package org.workcraft.plugins.verification.gui;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.workcraft.gui.SimpleFlowLayout;
import org.workcraft.plugins.verification.MpsatMode;
import org.workcraft.plugins.verification.MpsatPresetManager;
import org.workcraft.plugins.verification.MpsatSettings;
import org.workcraft.plugins.verification.MpsatSettings.SolutionMode;
import org.workcraft.util.GUI;

@SuppressWarnings("serial")
public class MpsatConfigurationDialog extends JDialog {

	private JPanel content, presetPanel, reachPanel, buttonsPanel;
	private JLabel numberOfSolutionsLabel;
	private JScrollPane optionsPanel;
	private JComboBox presetCombo, modeCombo, satCombo, verbosityCombo;
	private JButton manageButton, saveButton, runButton, updateButton, cancelButton;
	private JTextField solutionLimitText;
	private JTextArea reachText;
	private JRadioButton allSolutionsButton, firstSolutionButton, cheapestSolutionButton;
	private MpsatPresetManager presetManager;

	private TableLayout layout;
	private int modalResult = 0;

	class IntMode {
		public int value;
		public String description;

		public IntMode(int value, String description) {
			this.value = value;
			this.description = description;
		}

		public String toString() {
			return description;
		}
	}

	private void createPresetPanel() {
		presetPanel = new JPanel(new SimpleFlowLayout(15, 3));

		presetCombo = new JComboBox();
		for (MpsatSettings p : presetManager.getPresets())
			presetCombo.addItem(p);

		presetCombo.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applySettingsToControls();
			}
		});


		manageButton = new JButton ("Manage presets...");
		updateButton = new JButton ("Save settings");

		saveButton = new JButton ("Save settings as new preset...");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createPreset();
			}
		});

		presetPanel.add(GUI.createLabeledComponent(presetCombo, "Preset:"));
		presetPanel.add(new SimpleFlowLayout.LineBreak(3));
		presetPanel.add(updateButton);
		presetPanel.add(saveButton);
		presetPanel.add(manageButton);

	}

	private void createOptionsPanel() {
		JPanel optionsPanelContent = new JPanel(new SimpleFlowLayout());

		optionsPanel = new JScrollPane(optionsPanelContent);
		optionsPanel.setBorder(BorderFactory.createTitledBorder("MPSat options"));
		optionsPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		optionsPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		modeCombo = new JComboBox();
		modeCombo.setEditable(false);

		for (MpsatMode mode : MpsatMode.modes)
			modeCombo.addItem(mode);

		modeCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MpsatMode selectedMode = (MpsatMode)modeCombo.getSelectedItem();

				if (selectedMode.isReach()) {
					reachPanel.setVisible(true);
					layout.setRow(2, TableLayout.FILL);
				}
				else {
					reachPanel.setVisible(false);
					layout.setRow(2, 0);
				}
			}
		});

		optionsPanelContent.add(new JLabel("Mode:"));
		optionsPanelContent.add(new SimpleFlowLayout.LineBreak());
		optionsPanelContent.add(modeCombo);
		optionsPanelContent.add(new SimpleFlowLayout.LineBreak(8));

		optionsPanelContent.add(new JLabel("Solution mode:"));
		optionsPanelContent.add(new SimpleFlowLayout.LineBreak(-2));

		createSolutionModeButtons();

		optionsPanelContent.add(firstSolutionButton);
		optionsPanelContent.add(cheapestSolutionButton);
		optionsPanelContent.add(allSolutionsButton);

		optionsPanelContent.add(new SimpleFlowLayout.LineBreak());

		solutionLimitText = new JTextField();
		solutionLimitText.setText("WWWW");
		Dimension preferredSize = solutionLimitText.getPreferredSize();
		solutionLimitText.setText("");
		solutionLimitText.setPreferredSize(preferredSize);

		JPanel numberOfSolutionsPanel = new JPanel (new FlowLayout(FlowLayout.LEFT, 3, 0));
		numberOfSolutionsLabel = new JLabel("Maximum number of solutions (leave blank for no limit):");
		numberOfSolutionsPanel.add(numberOfSolutionsLabel);
		numberOfSolutionsPanel.add(solutionLimitText);

		disableNumberOfSolutionControls();

		optionsPanelContent.add(numberOfSolutionsPanel);
		optionsPanelContent.add(new SimpleFlowLayout.LineBreak(8));

		satCombo = new JComboBox();
		satCombo.addItem(new IntMode(0, "ZChaff"));
		satCombo.addItem(new IntMode(1, "MiniSat"));

		optionsPanelContent.add(GUI.createLabeledComponent(satCombo, "SAT solver:"));

		verbosityCombo = new JComboBox();
		for (int i=0; i<=9; i++)
			verbosityCombo.addItem(new IntMode(i, Integer.toString(i)));

		optionsPanelContent.add(GUI.createLabeledComponent(verbosityCombo, "Verbosity level:"));
	}

	private void createSolutionModeButtons() {
		firstSolutionButton = new JRadioButton ("Find any solution (default)");
		firstSolutionButton.setSelected(true);
		firstSolutionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				disableNumberOfSolutionControls();
			}
		});
		allSolutionsButton = new JRadioButton("Find all solutions");
		allSolutionsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				enableNumberOfSolutionControls();
			}
		});

		cheapestSolutionButton = new JRadioButton("Minimise cost function");
		cheapestSolutionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				disableNumberOfSolutionControls();
			}
		});


		ButtonGroup bg = new ButtonGroup();
		bg.add(firstSolutionButton); bg.add(allSolutionsButton); bg.add(cheapestSolutionButton);
	}

	private void disableNumberOfSolutionControls() {
		numberOfSolutionsLabel.setEnabled(false);
		solutionLimitText.setEnabled(false);
	}

	private void enableNumberOfSolutionControls() {
		numberOfSolutionsLabel.setEnabled(true);
		solutionLimitText.setEnabled(true);
	}

	private void createReachPanel() {
		reachPanel = new JPanel  (new BorderLayout());
		reachPanel.setBorder(BorderFactory.createTitledBorder("Property specification (Reach)"));

		reachText = new JTextArea();
		reachText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		reachText.setText("test");
		reachPanel.add(reachText);
	}

	private void applySettingsToControls() {
		MpsatSettings p = (MpsatSettings)presetCombo.getSelectedItem();

		modeCombo.setSelectedItem(p.getMode());
		satCombo.setSelectedIndex(p.getSatSolver());
		verbosityCombo.setSelectedIndex(p.getVerbosity());
		switch (p.getSolutionMode()) {
		case ALL:
			allSolutionsButton.setSelected(true);
			enableNumberOfSolutionControls();
			break;
		case MINIMUM_COST:
			cheapestSolutionButton.setSelected(true);
			disableNumberOfSolutionControls();
			break;
		case FIRST:
			firstSolutionButton.setSelected(true);
			disableNumberOfSolutionControls();
			break;
		}
		int n = p.getSolutionNumberLimit();

		if (n>0)
			solutionLimitText.setText(Integer.toString(n));
		else
			solutionLimitText.setText("");

		reachText.setText(p.getReach());
	}

	public MpsatConfigurationDialog(Window owner, MpsatPresetManager presetManager) {
		super(owner, "MPSat configuration", ModalityType.APPLICATION_MODAL);
		this.presetManager = presetManager;

		createPresetPanel();
		createOptionsPanel();
		createReachPanel();
		createButtonsPanel();

		double size[][] = new double[][] {
				{TableLayout.FILL},
				{TableLayout.PREFERRED, TableLayout.FILL, TableLayout.FILL, buttonsPanel.getPreferredSize().height}
		};

		layout = new TableLayout(size);
		layout.setHGap(3);
		layout.setVGap(3);

		content = new JPanel(layout);
		content.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

		content.add(presetPanel, "0 0");
		content.add(optionsPanel, "0 1");
		content.add(reachPanel, "0 2");
		content.add(buttonsPanel, "0 3");

		setContentPane(content);

		presetCombo.setSelectedIndex(0);
	}

	public MpsatSettings getSettings() {
		return getSettingsFromControls("Custom settings");
	}

	private void createButtonsPanel() {
		buttonsPanel = new JPanel (new FlowLayout(FlowLayout.RIGHT));

		runButton = new JButton ("Run");
		runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				modalResult = 1;
				setVisible(false);
			}
		});
		cancelButton = new JButton ("Cancel");
		runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				modalResult = 0;
				setVisible(false);
			}
		});

		buttonsPanel.add(cancelButton);
		buttonsPanel.add(runButton);
	}

	private void createPreset() {
		String desc = JOptionPane.showInputDialog(this, "Please enter the description of the new preset:");

		if (! (desc == null || desc.isEmpty())) {
			MpsatSettings preset = getSettingsFromControls(desc);
			presetManager.createPreset(preset);
			presetCombo.addItem(preset);
		}
	}

	private MpsatSettings getSettingsFromControls(String desc) {
		SolutionMode m;

		if (firstSolutionButton.isSelected())
			m = SolutionMode.FIRST;
		else if (cheapestSolutionButton.isSelected())
			m = SolutionMode.MINIMUM_COST;
		else
			m = SolutionMode.ALL;

		int n;

		try {
			n = Integer.parseInt(solutionLimitText.getText());
		} catch (NumberFormatException e) {
			n = 0;
		}

		if (n<0)
			n=0;

		MpsatSettings preset = new MpsatSettings((MpsatMode) modeCombo
				.getSelectedItem(), verbosityCombo.getSelectedIndex(), satCombo
				.getSelectedIndex(), m, n, reachText.getText(), desc, false);
		return preset;
	}

	public int getModalResult() {
		return modalResult;
	}
}

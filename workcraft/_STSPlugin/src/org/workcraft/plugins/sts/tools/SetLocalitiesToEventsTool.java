package org.workcraft.plugins.sts.tools;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.workcraft.dom.Node;
import org.workcraft.gui.tools.AbstractGraphEditorTool;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.gui.tools.Decorator;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.plugins.sts.EventWithLocality;
import org.workcraft.plugins.sts.STS;
import org.workcraft.plugins.sts.VisualSTS;
import org.workcraft.shared.IntDocument;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.GuiUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class SetLocalitiesToEventsTool extends AbstractGraphEditorTool {
    private JPanel panel;
    protected JPanel controlPanel;
    protected JPanel cycleCountPanel;
    protected JPanel eventNamePanel;
    protected JPanel localityPanel;
    protected JPanel localityColorPanel;
    protected JPanel submitPanel;

    private JLabel cycleCountLabel;
    private JLabel eventNameLabel;
    private JLabel localityLabel;
//    private JLabel LocalityColorLabel;
    private VisualSTS sts;
    private int cycleCount;
    private int locality = 1;
    private String eventName = "";
    public Set<EventWithLocality> eventsWitlLoca = new HashSet<>();
    public static Map<String, Integer> eventsWithLocalities = new HashMap<>();

//    public SetLocalitiesToEventsTool() {
//        STS.setClearLocalities();
//    }

    @Override
    public void activated(final GraphEditor editor) {
//        eventsWithLocalities.clear();
        super.activated(editor);
        sts = (VisualSTS) editor.getModel();
        cycleCount = sts.getStepTransitionSystemModel().createEvents().size();
    }

    private void updateState(final GraphEditor editor) {
        sts = (VisualSTS) editor.getModel();
        int noEvents = sts.getStepTransitionSystemModel().createEvents().size();
        if (cycleCount != noEvents) {
            cycleCount = noEvents;
            editor.repaint();
        }
    }

    @Override
    public JPanel getControlsPanel(final GraphEditor editor) {
        if (panel != null) {
            return panel;
        }
        sts = (VisualSTS) editor.getModel();
        cycleCount = sts.getStepTransitionSystemModel().createEvents().size();

        controlPanel = new JPanel(new GridLayout(0, 1));
        final JTextField cycleCountText = new JTextField();
        Dimension dimension = cycleCountText.getPreferredSize();
        dimension.width = 80;
        cycleCountText.setPreferredSize(dimension);
        cycleCountText.setDocument(new IntDocument(200));
        cycleCountText.setText(String.valueOf(cycleCount));
        cycleCountText.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent arg0) {
                if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        cycleCount = Integer.parseInt(cycleCountText.getText());
                    } catch (NumberFormatException e) {
                        cycleCountText.setText(String.valueOf(cycleCount));
                    }
                } else if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cycleCountText.setText(String.valueOf(cycleCount));
                }
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
            }

            @Override
            public void keyTyped(KeyEvent arg0) {
            }
        });

        cycleCountText.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent arg0) {
                cycleCountText.setText(String.valueOf(cycleCount));
            }

            @Override
            public void focusLost(FocusEvent arg0) {
//                cycleCount = Integer.parseInt(cycleCountText.getText());
            }
        });

        cycleCountText.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                cycleCountText.setText(String.valueOf(cycleCount));
            }

            @Override
            public void insertUpdate(DocumentEvent arg0) {
//                int noEvents = sts.getStepTransitionSystemModel().createEvents().size();
//                if (cycleCount != noEvents) {
//                    cycleCount = noEvents;
//                    cycleCountText.setText(String.valueOf(cycleCount));

//                    editor.repaint();                                
//            }
            }

            @Override
            public void removeUpdate(DocumentEvent arg0) {
//                int noEvents = sts.getStepTransitionSystemModel().createEvents().size();
//                if (cycleCount != noEvents) {
//                    cycleCount = noEvents;
//                    cycleCountText.setText(String.valueOf(cycleCount));
//                    editor.repaint();                                
//            }
            }
            // implement the methods
        });

        final JTextField eventNameText = new JTextField();
        Dimension dimension2 = eventNameText.getPreferredSize();
        dimension2.width = 80;
        eventNameText.setPreferredSize(dimension2);
//        eventNameText.setDocument(new IntDocument(20));
        eventNameText.setText(String.valueOf(eventName));

        final JTextField localityText = new JTextField();
        Dimension dimension3 = localityText.getPreferredSize();
        dimension3.width = 80;
        localityText.setPreferredSize(dimension3);
        localityText.setDocument(new IntDocument(20));
        localityText.setText(String.valueOf(locality));
        localityText.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent arg0) {
                if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        locality = Integer.parseInt(localityText.getText());
                        resetSelectedCycle(editor);
                    } catch (NumberFormatException e) {
                        localityText.setText(String.valueOf(locality));
                    }
                } else if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    localityText.setText(String.valueOf(locality));
                }
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
            }

            @Override
            public void keyTyped(KeyEvent arg0) {
            }
        });

        localityText.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent arg0) {
                localityText.setText(String.valueOf(locality));
            }

            @Override
            public void focusLost(FocusEvent arg0) {
                locality = Integer.parseInt(localityText.getText());
                resetSelectedCycle(editor);
            }
        });

        JButton submite = new JButton("<html><center>Submite</center></html>");
        JButton reset = new JButton("<html><center>Reset</center></html>");
        submitPanel = new JPanel();
        submitPanel.add(submite);
        submitPanel.add(reset);

        submite.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                String eName = String.valueOf(eventNameText.getText());
                if (sts.getStepTransitionSystemModel().createEvents().contains(eName)) {
                    int loc = Integer.parseInt(localityText.getText());
                    eventsWitlLoca.add(new EventWithLocality(eName, loc, null, null));
                    eventsWithLocalities.put(eName, loc);
                    eventNameText.setText(null);
                } else {
                    DialogUtils.showError("There is no an event with the name " + eName + " in the sts.", "Error");
                    eventNameText.setText(null);
                }
                STS.setEventsLocalities(eventsWithLocalities);
            }
        });
        reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                STS.setClearLocalities();
            }                        
        });
        cycleCountLabel = new JLabel();
        cycleCountLabel.setText("No. of events:");
        cycleCountLabel.setLabelFor(cycleCountText);

        cycleCountPanel = new JPanel(new GridLayout(0, 1));
        cycleCountPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        cycleCountPanel.add(cycleCountLabel);
        cycleCountPanel.add(cycleCountText);

        eventNameLabel = new JLabel();
        eventNameLabel.setText("Event name:  ");
        eventNameLabel.setLabelFor(eventNameText);

        eventNamePanel = new JPanel(new GridLayout(0, 1));
        eventNamePanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        eventNamePanel.add(eventNameLabel);
        eventNamePanel.add(eventNameText);

        localityLabel = new JLabel();
        localityLabel.setText("Locality:        ");
        localityLabel.setLabelFor(localityText);

        localityPanel = new JPanel(new GridLayout(0, 1));
        localityPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        localityPanel.add(localityLabel);
        localityPanel.add(localityText);

        controlPanel.add(cycleCountPanel);
        controlPanel.add(eventNamePanel);
        controlPanel.add(localityPanel);

        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(controlPanel, BorderLayout.PAGE_START);
        panel.add(submitPanel, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(0, 0));
        return panel;
    }

    private void resetSelectedCycle(final GraphEditor editor) {
        editor.repaint();
    }

    @Override
    public void deactivated(final GraphEditor editor) {
        super.deactivated(editor);
        sts = null;
        editor.repaint();
    }

    @Override
    public void setPermissions(final GraphEditor editor) {
        WorkspaceEntry we = editor.getWorkspaceEntry();
        we.setCanModify(false);
        we.setCanSelect(false);
        we.setCanCopy(false);
    }

    @Override
    public String getLabel() {
        return "Events Localities";
    }

    @Override
    public int getHotKeyCode() {
        return KeyEvent.VK_L;
    }

    @Override
    public Icon getIcon() {
        return GuiUtils.createIconFromSVG("images/sts-selection-locality.svg");
    }

    @Override
    public Decorator getDecorator(final GraphEditor editor) {
        return new Decorator() {
            @Override
            public Decoration getDecoration(Node node) {
                int bgIintencity = 150;
                final Color bgColor = new Color(bgIintencity, 0, 0);
                return new Decoration() {
                    @Override
                    public Color getColorisation() {
                        return null;
                    }

                    @Override
                    public Color getBackground() {
                        return bgColor;
                    }
                };
            }

        };
    }
}

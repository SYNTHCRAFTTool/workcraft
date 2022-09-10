package org.workcraft.plugins.sts.tools;

public class Test {
//    package org.workcraft.plugins.sts.tools;
//
//    import java.awt.BorderLayout;
//    import java.awt.Color;
//    import java.awt.Dimension;
//    import java.awt.FlowLayout;
//    import java.awt.GridLayout;
//    import java.awt.event.FocusEvent;
//    import java.awt.event.FocusListener;
//    import java.awt.event.KeyEvent;
//    import java.awt.event.KeyListener;
//    import java.util.ArrayList;
//
//    import javax.swing.Icon;
//    import javax.swing.JLabel;
//    import javax.swing.JPanel;
//    import javax.swing.JTextField;
//    import javax.swing.border.EmptyBorder;
//
//    import org.workcraft.dom.Node;
//    import org.workcraft.gui.layouts.WrapLayout;
//    import org.workcraft.gui.tools.AbstractGraphEditorTool;
//    import org.workcraft.gui.tools.Decoration;
//    import org.workcraft.gui.tools.Decorator;
//    import org.workcraft.gui.tools.GraphEditor;
//    import org.workcraft.plugins.builtin.settings.CommonVisualSettings;
//    import org.workcraft.plugins.sts.EventWithLocality;
//    import org.workcraft.plugins.sts.VisualSTS;
//    import org.workcraft.shared.IntDocument;
//    import org.workcraft.utils.GuiUtils;
//    import org.workcraft.workspace.WorkspaceEntry;
//
//    public class SetLocalitiesToEventsTool extends AbstractGraphEditorTool {
//        private JPanel panel;
//        protected JPanel controlPanel;
//        protected JPanel  cycleCountPanel;
//        protected JPanel eventNamePanel;
//        protected JPanel localityPanel;
//        protected JPanel localityColorPanel;
//        
//        private JLabel cycleCountLabel;
//        private JLabel eventNameLabel;
//        private JLabel localityLabel;
//        private JLabel LocalityColorLabel;
//        private VisualSTS sts;
//        private int cycleCount;
//        private int locality = 1;
//        private String eventName = "";
//        protected Color localityColor = CommonVisualSettings.getLabelColor();
//        protected Color fillLocalityColor = CommonVisualSettings.getFillColor();
//        public ArrayList<EventWithLocality> eventsWitlLoca = new ArrayList<>();
//
//        @Override
//        public void activated(final GraphEditor editor) {
//            super.activated(editor);
//            sts = (VisualSTS) editor.getModel();
////            cycleTable.clearSelection();
////            selectedCycle = null;
//            cycleCount = sts.getStepTransitionSystemModel().createEvents().size();
//            System.out.println(cycleCount);
////            cycles = findCycles();
////            if ((cycles != null) && (cycleCountLabel != null)) {
////                cycleCountLabel.setText("Number of events:");
////            }
//            for(int i=0; i < cycleCount; i++) {
//            if((eventName != "") & (locality != 0)) {
//                EventWithLocality event = new EventWithLocality(eventName, locality, null,null);
//                eventsWitlLoca.add(event);
//            }
//            }
//            
////            System.out.println(eventsWitlLoca.size());
////            for(int i=0; i < eventsWitlLoca.size(); i++) {
////                System.out.println(i + ":-" + eventsWitlLoca.get(i).getEventName());
//    //
////            }
//
//        }
//        @Override
//        public JPanel getControlsPanel(final GraphEditor editor) {
//            if (panel != null) {
//                return panel;
//            }
//            controlPanel = new JPanel();
//            final JTextField cycleCountText = new JTextField();
//            Dimension dimension = cycleCountText.getPreferredSize();
//            dimension.width = 80;
//            cycleCountText.setPreferredSize(dimension);
//            cycleCountText.setDocument(new IntDocument(200));
//            cycleCountText.setText(String.valueOf(cycleCount));
//            cycleCountText.addKeyListener(new KeyListener() {
//                @Override
//                public void keyPressed(KeyEvent arg0) {
//                    if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
//                        try {
//                            cycleCount = Integer.parseInt(cycleCountText.getText());
////                            resetSelectedCycle(editor);
//                        } catch (NumberFormatException e) {
//                            cycleCountText.setText(String.valueOf(cycleCount));
//                        }
//                    } else if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
//                        cycleCountText.setText(String.valueOf(cycleCount));
//                    }
//                }
//
//                @Override
//                public void keyReleased(KeyEvent arg0) {
//                }
//
//                @Override
//                public void keyTyped(KeyEvent arg0) {
//                }
//            });
//
//            cycleCountText.addFocusListener(new FocusListener() {
//                @Override
//                public void focusGained(FocusEvent arg0) {
//                    cycleCountText.setText(String.valueOf(cycleCount));
//                }
//
//                @Override
//                public void focusLost(FocusEvent arg0) {
//                    cycleCount = Integer.parseInt(cycleCountText.getText());
////                    resetSelectedCycle(editor);
//                }
//            });
//
//            final JTextField eventNameText = new JTextField();
//            Dimension dimension2 = eventNameText.getPreferredSize();
//            dimension2.width = 80;
//            eventNameText.setPreferredSize(dimension2);
////            eventNameText.setDocument(new IntDocument(20));
//            eventNameText.setText(String.valueOf(eventName));
//            eventNameText.addKeyListener(new KeyListener() {
//                @Override
//                public void keyPressed(KeyEvent arg0) {
//                    if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
//                        try {
//                            eventName = String.valueOf(eventNameText.getText());
////                            resetSelectedCycle(editor);
//                        } catch (NumberFormatException e) {
//                            eventNameText.setText(String.valueOf(eventName));
//                        }
//                    } else if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
////                        eventNameText.setText(String.valueOf(eventName));
//                    }
//                }
//
//                @Override
//                public void keyReleased(KeyEvent arg0) {
//                }
//
//                @Override
//                public void keyTyped(KeyEvent arg0) {
//                }
//            });
//
//            cycleCountText.addFocusListener(new FocusListener() {
//                @Override
//                public void focusGained(FocusEvent arg0) {
//                    eventNameText.setText(String.valueOf(eventName));
//                }
//
//                @Override
//                public void focusLost(FocusEvent arg0) {
//                    eventName = String.valueOf(eventNameText.getText());
//                    resetSelectedCycle(editor);
//                }
//            });
//
//            
//            final JTextField localityText = new JTextField();
//            Dimension dimension3 = localityText.getPreferredSize();
//            dimension3.width = 80;
//            localityText.setPreferredSize(dimension3);
//            localityText.setDocument(new IntDocument(20));
//            localityText.setText(String.valueOf(locality));
//            localityText.addKeyListener(new KeyListener() {
//                @Override
//                public void keyPressed(KeyEvent arg0) {
//                    if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
//                        try {
//                            locality = Integer.parseInt(localityText.getText());
//                            resetSelectedCycle(editor);
//                        } catch (NumberFormatException e) {
//                            localityText.setText(String.valueOf(locality));
//                        }
//                    } else if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
//                        localityText.setText(String.valueOf(locality));
//                    }
//                }
//
//                @Override
//                public void keyReleased(KeyEvent arg0) {
//                }
//
//                @Override
//                public void keyTyped(KeyEvent arg0) {
//                }
//            });
//
//            cycleCountText.addFocusListener(new FocusListener() {
//                @Override
//                public void focusGained(FocusEvent arg0) {
//                    localityText.setText(String.valueOf(locality));
//                }
//
//                @Override
//                public void focusLost(FocusEvent arg0) {
//                    locality = Integer.parseInt(localityText.getText());
//                    resetSelectedCycle(editor);
//                }
//            });
//            
//            final JTextField localityColorText = new JTextField();
//            Dimension dimension4 = localityColorText.getPreferredSize();
//            dimension4.width = 80;
//            localityColorText.setPreferredSize(dimension4);
////            localityColor.setDocument(new IntDocument(20));
//            localityColorText.setText(String.valueOf(String.format("#%x", localityColor.getRGB() & 0xffffff)));
//            localityColorText.addKeyListener(new KeyListener() {
//                @Override
//                public void keyPressed(KeyEvent arg0) {
//                    if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
//                        try {
//                            localityColor =  localityColor;
//                            resetSelectedCycle(editor);
//                        } catch (NumberFormatException e) {
//                            localityColorText.setText(String.format("#%x", localityColor.getRGB() & 0xffffff));
//                        }
//                    } else if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
//                        localityColorText.setText(String.format("#%x", localityColor.getRGB() & 0xffffff));
//                    }
//                }
//
//                @Override
//                public void keyReleased(KeyEvent arg0) {
//                }
//
//                @Override
//                public void keyTyped(KeyEvent arg0) {
//                }
//            });
//
//            cycleCountText.addFocusListener(new FocusListener() {
//                @Override
//                public void focusGained(FocusEvent arg0) {
//                    localityColorText.setText(String.format("#%x", localityColor.getRGB() & 0xffffff));
//                }
//
//                @Override
//                public void focusLost(FocusEvent arg0) {
////                    localityColor = Integer.parseInt(localityColorText.getText()); 
//                    resetSelectedCycle(editor);
//                }
//            });
//            
//            cycleCountLabel = new JLabel();
//            cycleCountLabel.setText("Number of events:");
//            cycleCountLabel.setLabelFor(cycleCountText);
//
////            cycleCountPanel = new JPanel(new GridLayout(0, 1));
////            cycleCountPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
////            cycleCountPanel.add(cycleCountLabel);
////            cycleCountPanel.add(cycleCountText);
//
//            eventNameLabel = new JLabel();
//            eventNameLabel.setText("Event name:");
//            eventNameLabel.setLabelFor(eventNameText);
//
////            eventNamePanel = new JPanel(new GridLayout(0, 1));
////            eventNamePanel.setLayout(new FlowLayout(FlowLayout.LEADING));
////            eventNamePanel.add(eventNameLabel);
////            eventNamePanel.add(eventNameText);
//            
//            localityLabel = new JLabel();
//            localityLabel.setText("Locality:");
//            localityLabel.setLabelFor(localityText);
//          
////            localityPanel = new JPanel(new GridLayout(0, 1));
////            localityPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
////            localityPanel.add(localityLabel);
////            localityPanel.add(localityText);
//            
//            LocalityColorLabel = new JLabel();
//            LocalityColorLabel.setText("Locality color:");
//            LocalityColorLabel.setLabelFor(localityColorText);
//      
//                
////            localityColorPanel = new JPanel(new GridLayout(0, 1));
////            localityColorPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
////            localityColorPanel.add(LocalityColorLabel);
////            localityColorPanel.add(localityColorText);
//            
//            JPanel labels = new JPanel(new GridLayout(0,1));
//            labels.add(cycleCountLabel);
//            labels.add(eventNameLabel);
//            labels.add(cycleCountLabel);
//            labels.add(LocalityColorLabel);
//
//            JPanel controls = new JPanel(new GridLayout(0,1));
//            controls.add(cycleCountText);
//            controls.add(eventNameText);
//            controls.add(localityText);
//            controls.add(localityColorText);
//
//
//            panel = new JPanel(new BorderLayout());
//            panel.setBorder(new EmptyBorder(1,1,1,1));
//
//            panel.add(labels, BorderLayout.WEST);
//            panel.add(controls, BorderLayout.CENTER);
////            panel.add(controlPanel);
////            panel.add(cycleCountPanel);
////            panel.add(eventNamePanel);
////            panel.add(localityPanel);
////            panel.add(localityColorPanel);
//
////            panel.setPreferredSize(new Dimension(0, 0));
//            return panel;
//        }
//
//        private void resetSelectedCycle(final GraphEditor editor) {
////            selectedCycle = null;
////            cycleTable.tableChanged(null);
//            editor.repaint();
//        }
//
//        
//
//        @Override
//        public void deactivated(final GraphEditor editor) {
//            super.deactivated(editor);
////            cycles = null;
////            selectedCycle = null;
//            sts = null;
////            cycleTable.clearSelection();
//        }
//
//        @Override
//        public void setPermissions(final GraphEditor editor) {
//            WorkspaceEntry we = editor.getWorkspaceEntry();
//            we.setCanModify(false);
//            we.setCanSelect(false);
//            we.setCanCopy(false);
//        }
//
//        @Override
//        public String getLabel() {
//            return "Events Localities";
//        }
//
//        @Override
//        public int getHotKeyCode() {
//            return KeyEvent.VK_L;
//        }
//
//        @Override
//        public Icon getIcon() {
//            return GuiUtils.createIconFromSVG("images/dfs-tool-cycle_analysis.svg");
//        }
//
//        @Override
//        public Decorator getDecorator(final GraphEditor editor) {
//            return new Decorator() {
//                @Override
//                public Decoration getDecoration(Node node) {
//                    int bgIintencity = 150;
//                    final Color bgColor = new Color(bgIintencity, 0, 0);
//                    return new Decoration() {
//                        @Override
//                        public Color getColorisation() {
//                            return null;
//                        }
//
//                        @Override
//                        public Color getBackground() {
//                            return bgColor;
//                        }
//                    };
//                }
//
//            };
//        }

//    }


}

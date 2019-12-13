package org.workcraft.gui.properties;

import org.workcraft.gui.actions.Action;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

@SuppressWarnings("serial")
public class ActionCellRenderer extends JButton implements TableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        if (value instanceof Action) {
            Action action = (Action) value;
            setText(action.getTitle());
        }
        return this;
    }

}

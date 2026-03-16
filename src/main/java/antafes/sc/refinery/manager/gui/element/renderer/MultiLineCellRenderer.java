/*
 * This file is part of SCRefineryManager.
 *
 * SCRefineryManager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SCRefineryManager is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SCRefineryManager. If not, see <http://www.gnu.org/licenses/>.
 *
 * @package SCRefineryManager
 * @author Marian Pollzien <map@wafriv.de>
 * @copyright (c) 2026, Marian Pollzien
 * @license https://www.gnu.org/licenses/lgpl.html LGPLv3
 */

/*
 * This file is part of SCRefineryManager.
 *
 * SCRefineryManager is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SCRefineryManager is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SCRefineryManager. If not, see <http://www.gnu.org/licenses/>.
 */

package antafes.sc.refinery.manager.gui.element.renderer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class MultiLineCellRenderer extends JTextArea implements TableCellRenderer
{
    private static final Border INNER_PADDING = BorderFactory.createEmptyBorder(6, 4, 6, 4);

    private final DefaultTableCellRenderer borderProbe = new DefaultTableCellRenderer();

    public MultiLineCellRenderer()
    {
        setLineWrap(true);
        setWrapStyleWord(true);
        setOpaque(true);
        setEditable(false);
        setFocusable(false);

        Border noFocus = UIManager.getBorder("Table.cellNoFocusBorder");
        if (noFocus != null) {
            setBorder(BorderFactory.createCompoundBorder(noFocus, INNER_PADDING));
        } else {
            setBorder(INNER_PADDING);
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        setFont(table.getFont());
        setText(value == null ? "" : value.toString());

        if (isSelected) {
            setBackground(table.getSelectionBackground());
            setForeground(table.getSelectionForeground());
        } else {
            setBackground(table.getBackground());
            setForeground(table.getForeground());
        }

        // Reuse the L&F-provided border and add inner padding so the text isn't flush to the edge.
        Component probeComponent = borderProbe.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
        if (probeComponent instanceof JComponent probe && probe.getBorder() != null) {
            setBorder(BorderFactory.createCompoundBorder(probe.getBorder(), INNER_PADDING));
        } else {
            Border fallback = UIManager.getBorder("Table.cellNoFocusBorder");
            setBorder(fallback != null ? BorderFactory.createCompoundBorder(fallback, INNER_PADDING) : INNER_PADDING);
        }

        return this;
    }
}

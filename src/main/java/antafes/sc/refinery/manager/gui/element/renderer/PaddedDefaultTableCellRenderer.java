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
import java.awt.*;

/**
 * Default table cell renderer with a small left/right padding.
 */
public class PaddedDefaultTableCellRenderer extends DefaultTableCellRenderer
{
    private Border innerPadding = BorderFactory.createEmptyBorder(0, 2, 0, 2);

    public PaddedDefaultTableCellRenderer(int horizontalAlignment)
    {
        setHorizontalAlignment(horizontalAlignment);
    }

    public PaddedDefaultTableCellRenderer(int horizontalAlignment, Insets padding)
    {
        setHorizontalAlignment(horizontalAlignment);
        setPadding(padding);
    }

    public PaddedDefaultTableCellRenderer(int horizontalAlignment, Border padding)
    {
        setHorizontalAlignment(horizontalAlignment);
        setPadding(padding);
    }

    public void setPadding(Insets padding)
    {
        if (padding == null) {
            this.innerPadding = null;
            return;
        }
        this.innerPadding = BorderFactory.createEmptyBorder(padding.top, padding.left, padding.bottom, padding.right);
    }

    public void setPadding(Border padding)
    {
        this.innerPadding = padding;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (this.innerPadding == null) {
            return this;
        }

        Border base = getBorder();
        if (base != null) {
            setBorder(BorderFactory.createCompoundBorder(base, this.innerPadding));
        } else {
            setBorder(this.innerPadding);
        }

        return this;
    }
}

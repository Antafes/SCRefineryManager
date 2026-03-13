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

package antafes.sc.refinery.manager.gui.icon;

import javax.swing.*;
import java.awt.*;

public class TrashIcon implements Icon
{
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y)
    {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(190, 53, 53));
        g2.fillRoundRect(x + 4, y + 5, 8, 8, 2, 2);
        g2.fillRoundRect(x + 3, y + 3, 10, 2, 2, 2);
        g2.fillRoundRect(x + 6, y + 1, 4, 2, 2, 2);
        g2.setColor(Color.WHITE);
        g2.fillRect(x + 6, y + 6, 1, 6);
        g2.fillRect(x + 8, y + 6, 1, 6);
        g2.fillRect(x + 10, y + 6, 1, 6);
        g2.dispose();
    }

    @Override
    public int getIconWidth()
    {
        return 16;
    }

    @Override
    public int getIconHeight()
    {
        return 16;
    }
}

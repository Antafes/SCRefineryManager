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

public class PenIcon implements Icon
{
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y)
    {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(57, 116, 214));
        g2.rotate(Math.toRadians(-35), x + 8, y + 8);
        g2.fillRoundRect(x + 5, y + 2, 5, 10, 2, 2);
        g2.setColor(new Color(255, 214, 153));
        g2.fillPolygon(new int[]{x + 5, x + 10, x + 7}, new int[]{y + 2, y + 2, y}, 3);
        g2.setColor(new Color(60, 60, 60));
        g2.fillPolygon(new int[]{x + 6, x + 8, x + 7}, new int[]{y, y, y - 2}, 3);
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

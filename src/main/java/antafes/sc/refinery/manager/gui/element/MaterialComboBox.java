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

package antafes.sc.refinery.manager.gui.element;

import antafes.sc.base.entity.Material;
import antafes.sc.base.repository.MaterialRepository;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class MaterialComboBox extends JComboBox<Material> {
    private final MaterialRepository materialRepository;

    public MaterialComboBox(MaterialRepository repository) {
        this.materialRepository = repository;
        populate();
        initRenderer();
    }

    private void initRenderer() {
        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Material) {
                    lbl.setText(((Material) value).getName().getEnglish());
                } else {
                    lbl.setText(value == null ? "" : value.toString());
                }
                return lbl;
            }
        });
    }

    public void populate() {
        removeAllItems();
        Map<String, Material> map = this.materialRepository.findAllOres();
        map.values().forEach(this::addItem);
    }

    public void refresh() {
        populate();
    }
}

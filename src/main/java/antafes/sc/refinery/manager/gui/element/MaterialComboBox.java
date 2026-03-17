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
import antafes.sc.refinery.manager.util.Name;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.Map;

public class MaterialComboBox extends JComboBox<Material> {
    private static final String INERT_MATERIAL_KEY = "inert-materials";

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
                    lbl.setText(Name.fetchTranslatedName((Material) value));
                } else {
                    lbl.setText(value == null ? "" : value.toString());
                }
                return lbl;
            }
        });
    }

    public void populate() {
        removeAllItems();

        Map<String, Material> materialsByKey = this.materialRepository.findAllOres();
        Material inertMaterial = this.materialRepository.findOne(INERT_MATERIAL_KEY);
        if (inertMaterial != null) {
            materialsByKey.putIfAbsent(inertMaterial.getKey(), inertMaterial);
        }

        materialsByKey.values().stream().sorted(
            Comparator
                .comparing((Material m) -> {
                    String name = Name.fetchTranslatedName(m);
                    return name == null ? "" : name;
                }, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Material::getKey, String.CASE_INSENSITIVE_ORDER)
        ).toList()
            .forEach(this::addItem);
    }

    public void refresh() {
        populate();
    }
}

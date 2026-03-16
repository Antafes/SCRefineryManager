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

package antafes.sc.refinery.manager.gui.editRefinement;

import antafes.sc.base.entity.Material;
import antafes.sc.base.repository.MaterialRepository;
import antafes.sc.refinery.manager.Configuration;
import antafes.sc.refinery.manager.SCRefineryManager;
import antafes.sc.refinery.manager.entity.RefinedMaterial;
import antafes.sc.refinery.manager.entity.Refinement;
import antafes.sc.refinery.manager.gui.element.MaterialComboBox;
import antafes.sc.refinery.manager.gui.event.SaveEditRefinementEvent;
import antafes.sc.refinery.manager.gui.event.SaveEditRefinementListener;
import antafes.sc.refinery.manager.gui.filter.IntegerDocumentFilter;
import antafes.sc.refinery.manager.repository.RefinementRepository;
import antafes.utilities.language.LanguageInterface;
import jakarta.annotation.PostConstruct;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.*;
import java.util.List;

@org.springframework.stereotype.Component
public class EditRefinementPanel extends JPanel
{
    @Autowired
    private Configuration configuration;
    @Autowired
    private MaterialRepository materialRepository;
    @Autowired
    private RefinementRepository refinementRepository;

    private LanguageInterface language;

    private JLabel costLabel;
    private JLabel costErrorLabel;
    private JLabel headerMaterialLabel;
    private JLabel headerAmountLabel;
    private JLabel headerQualityLabel;
    private JLabel headerRevenueLabel;

    private JTextField costField;
    private JPanel materialsContainer;
    private JButton addMaterialRowButton;
    private final List<MaterialRow> materialRows = new ArrayList<>();

    @PostConstruct
    private void initAfterInjection()
    {
        this.language = this.configuration.getLanguageObject();
        this.initComponents();
        this.setFieldTexts();
        this.addListeners();
    }

    private void initComponents()
    {
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(4, 4, 4, 4);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;

        this.costLabel = new JLabel();
        this.costField = new JTextField(15);
        if (this.costField.getDocument() instanceof AbstractDocument) {
            ((AbstractDocument) this.costField.getDocument()).setDocumentFilter(new IntegerDocumentFilter());
        }

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0;
        add(this.costLabel, constraints);

        constraints.gridx++;
        constraints.gridy = 0;
        constraints.weightx = 1.0;
        add(this.costField, constraints);

        this.costErrorLabel = createErrorLabel();
        constraints.gridx = 1;
        constraints.gridy = 1;
        add(this.costErrorLabel, constraints);

        this.materialsContainer = new JPanel();
        this.materialsContainer.setLayout(new BoxLayout(this.materialsContainer, BoxLayout.Y_AXIS));

        JPanel header = new JPanel(new GridBagLayout());
        GridBagConstraints headerConstraints = new GridBagConstraints();
        headerConstraints.insets = new Insets(2, 2, 2, 2);
        headerConstraints.fill = GridBagConstraints.HORIZONTAL;
        this.headerMaterialLabel = new JLabel();
        this.headerAmountLabel = new JLabel();
        this.headerQualityLabel = new JLabel();
        this.headerRevenueLabel = new JLabel();

        headerConstraints.gridx = 0;
        headerConstraints.gridy = 0;
        headerConstraints.weightx = 1.0;
        header.add(this.headerMaterialLabel, headerConstraints);

        headerConstraints.gridx++;
        headerConstraints.gridy = 0;
        headerConstraints.weightx = 0.5;
        header.add(this.headerAmountLabel, headerConstraints);

        headerConstraints.gridx++;
        headerConstraints.gridy = 0;
        headerConstraints.weightx = 0.5;
        header.add(this.headerQualityLabel, headerConstraints);

        headerConstraints.gridx++;
        headerConstraints.gridy = 0;
        headerConstraints.weightx = 0.5;
        header.add(this.headerRevenueLabel, headerConstraints);

        this.materialsContainer.add(header);

        JPanel materialsWrapper = new JPanel(new BorderLayout());
        materialsWrapper.add(this.materialsContainer, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(materialsWrapper);
        // Wider so all material-row fields (incl. long material names) are visible.
        scroll.setPreferredSize(new Dimension(700, 150));
        scroll.setMinimumSize(new Dimension(700, 150));

        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        add(scroll, constraints);
        constraints.gridwidth = 1;
        constraints.weighty = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;

        this.addMaterialRowButton = new JButton();
        this.addMaterialRowButton.addActionListener(e -> addMaterialRow());

        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 2;
        add(this.addMaterialRowButton, constraints);
        constraints.gridwidth = 1;
    }

    private void setFieldTexts()
    {
        this.costLabel.setText(this.language.translate("cost") + ":");

        this.headerMaterialLabel.setText(this.language.translate("material"));
        this.headerAmountLabel.setText(this.language.translate("amount"));
        this.headerQualityLabel.setText(this.language.translate("quality"));
        this.headerRevenueLabel.setText(this.language.translate("revenue"));

        this.addMaterialRowButton.setText(this.language.translate("addMaterialRow"));
    }

    private void addListeners()
    {
        SCRefineryManager.getDispatcher().addListener(
            SaveEditRefinementEvent.class,
            new SaveEditRefinementListener(event -> {
                if (!validateInputs()) {
                    return;
                }

                Refinement refinement = buildRefinementFromInputs();
                refinement.setKey(event.getKey());

                Refinement existing = this.refinementRepository.findOne(event.getKey());
                if (existing != null) {
                    refinement.setCreatedAt(existing.getCreatedAt());
                }

                refinementRepository.update(event.getKey(), refinement);

                event.getDialog().dispose();
            })
        );
    }

    public void loadRefinement(@NonNull Integer key)
    {
        Refinement refinement = this.refinementRepository.findOne(key);
        if (refinement == null) {
            throw new IllegalArgumentException("Unknown refinement key: " + key);
        }

        clearValidationErrors();
        this.costField.setText(String.valueOf(refinement.getCost()));

        for (int i = this.materialsContainer.getComponentCount() - 1; i >= 1; i--) {
            this.materialsContainer.remove(i);
        }
        this.materialRows.clear();

        if (refinement.getMaterials() == null || refinement.getMaterials().isEmpty()) {
            addMaterialRow();
        } else {
            refinement.getMaterials().values().forEach(this::addMaterialRow);
        }

        this.materialsContainer.revalidate();
        this.materialsContainer.repaint();
        revalidate();
        repaint();
    }

    private void addMaterialRow()
    {
        addMaterialRow(null);
    }

    private void addMaterialRow(RefinedMaterial initial)
    {
        JPanel row = new JPanel(new GridBagLayout());
        GridBagConstraints rowConstraints = new GridBagConstraints();
        rowConstraints.insets = new Insets(2, 2, 2, 2);
        rowConstraints.fill = GridBagConstraints.HORIZONTAL;
        MaterialComboBox materialCombo = getMaterialComboBox();

        rowConstraints.gridx = 0;
        rowConstraints.gridy = 0;
        rowConstraints.weightx = 1.0;
        row.add(materialCombo, rowConstraints);

        JTextField amountField = new JTextField(8);
        ((AbstractDocument) amountField.getDocument()).setDocumentFilter(new IntegerDocumentFilter());

        rowConstraints.gridx++;
        rowConstraints.weightx = 0.5;
        row.add(amountField, rowConstraints);

        JTextField qualityField = new JTextField(8);
        ((AbstractDocument) qualityField.getDocument()).setDocumentFilter(new IntegerDocumentFilter());

        rowConstraints.gridx++;
        rowConstraints.weightx = 0.5;
        row.add(qualityField, rowConstraints);

        JTextField revenueField = new JTextField(8);
        ((AbstractDocument) revenueField.getDocument()).setDocumentFilter(new IntegerDocumentFilter());
        revenueField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e)
            {
                SwingUtilities.invokeLater(revenueField::selectAll);
            }
        });

        rowConstraints.gridx++;
        rowConstraints.weightx = 0.5;
        row.add(revenueField, rowConstraints);

        JLabel materialErrorLabel = createErrorLabel();
        JLabel amountErrorLabel = createErrorLabel();
        JLabel revenueErrorLabel = createErrorLabel();
        MaterialRow materialRow = new MaterialRow(materialCombo, amountField, qualityField, revenueField, materialErrorLabel, amountErrorLabel, revenueErrorLabel);
        this.materialRows.add(materialRow);

        rowConstraints.gridy = 1;
        rowConstraints.gridx = 0;
        rowConstraints.weightx = 1.0;
        row.add(materialErrorLabel, rowConstraints);

        rowConstraints.gridx = 1;
        rowConstraints.weightx = 0.5;
        row.add(amountErrorLabel, rowConstraints);

        rowConstraints.gridx = 3;
        rowConstraints.weightx = 0.5;
        row.add(revenueErrorLabel, rowConstraints);

        JButton remove = createRemoveButton(row, materialRow);
        rowConstraints.gridy = 0;
        rowConstraints.gridx = 4;
        rowConstraints.weightx = 0;
        row.add(remove, rowConstraints);

        this.materialsContainer.add(row);

        materialCombo.refresh();

        if (initial != null) {
            selectMaterial(materialCombo, initial.getBaseMaterial());
            amountField.setText(String.valueOf(initial.getAmount()));
            qualityField.setText(String.valueOf(initial.getQuality()));
            revenueField.setText(String.valueOf(initial.getSellingPrice()));
        }

        updateRemoveButtonsState();
    }

    private void selectMaterial(MaterialComboBox comboBox, Material material)
    {
        if (material == null) return;

        for (int i = 0; i < comboBox.getItemCount(); i++) {
            Object item = comboBox.getItemAt(i);
            if (item instanceof Material m && Objects.equals(m.getKey(), material.getKey())) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }

        comboBox.setSelectedItem(material);
    }

    private JButton createRemoveButton(JPanel row, MaterialRow materialRow)
    {
        JButton remove = new JButton("-");
        remove.setActionCommand("remove");
        remove.addActionListener(e -> {
            if (this.materialRows.size() > 1) {
                this.materialRows.remove(materialRow);
                this.materialsContainer.remove(row);
                this.materialsContainer.revalidate();
                this.materialsContainer.repaint();
            } else {
                if (materialRow.materialField().getItemCount() > 0) {
                    materialRow.materialField().setSelectedIndex(0);
                }
                materialRow.amountField().setText("");
                materialRow.qualityField().setText("");
                materialRow.revenueField().setText("");
                hideError(materialRow.materialErrorLabel());
                hideError(materialRow.amountErrorLabel());
                hideError(materialRow.revenueErrorLabel());
            }
            updateRemoveButtonsState();
        });
        return remove;
    }

    private MaterialComboBox getMaterialComboBox()
    {
        return new MaterialComboBox(this.materialRepository);
    }

    private void updateRemoveButtonsState()
    {
        int count = this.materialRows.size();
        Arrays.stream(this.materialsContainer.getComponents())
            .filter(rowComp -> rowComp instanceof JPanel)
            .map(rowComp -> (JPanel) rowComp)
            .flatMap(row -> Arrays.stream(row.getComponents()))
            .filter(child -> child instanceof JButton)
            .map(child -> (JButton) child)
            .filter(btn -> "remove".equals(btn.getActionCommand()))
            .forEachOrdered(btn -> btn.setEnabled(count > 1));
    }

    private Refinement buildRefinementFromInputs()
    {
        Refinement refinement = new Refinement();
        Integer cost = parseInteger(this.costField.getText());
        if (cost != null) {
            refinement.setCost(cost);
        }

        Map<UUID, RefinedMaterial> materials = new HashMap<>();
        for (MaterialRow materialRow : this.materialRows) {
            Material material = getSelectedMaterial(materialRow);
            Integer amount = parseInteger(materialRow.amountField().getText());
            Integer quality = parseInteger(materialRow.qualityField().getText());
            Integer revenue = parseInteger(materialRow.revenueField().getText());

            if (material != null) {
                RefinedMaterial rm = new RefinedMaterial();
                rm.setKey(UUID.randomUUID());
                rm.setBaseMaterial(material);
                rm.setAmount(amount == null ? 0 : amount);
                rm.setQuality(quality == null ? 0 : quality);
                rm.setSellingPrice(revenue == null ? 0 : revenue);
                materials.put(rm.getKey(), rm);
            }
        }

        refinement.setMaterials(materials);
        return refinement;
    }

    private boolean validateInputs()
    {
        clearValidationErrors();

        boolean valid = true;
        Component firstInvalidComponent = null;

        if (parseInteger(this.costField.getText()) == null) {
            showError(this.costErrorLabel, "costRequired");
            valid = false;
            firstInvalidComponent = this.costField;
        }

        for (MaterialRow materialRow : this.materialRows) {
            if (getSelectedMaterial(materialRow) == null) {
                showError(materialRow.materialErrorLabel(), "materialRequired");
                valid = false;
                if (firstInvalidComponent == null) {
                    firstInvalidComponent = materialRow.materialField();
                }
            }

            if (parseInteger(materialRow.amountField().getText()) == null) {
                showError(materialRow.amountErrorLabel(), "amountRequired");
                valid = false;
                if (firstInvalidComponent == null) {
                    firstInvalidComponent = materialRow.amountField();
                }
            }

            if (parseInteger(materialRow.revenueField().getText()) == null) {
                showError(materialRow.revenueErrorLabel(), "revenueRequired");
                valid = false;
                if (firstInvalidComponent == null) {
                    firstInvalidComponent = materialRow.revenueField();
                }
            }
        }

        if (firstInvalidComponent != null) {
            firstInvalidComponent.requestFocusInWindow();
        }

        revalidate();
        repaint();
        return valid;
    }

    private void clearValidationErrors()
    {
        hideError(this.costErrorLabel);
        this.materialRows.forEach(materialRow -> {
            hideError(materialRow.materialErrorLabel());
            hideError(materialRow.amountErrorLabel());
            hideError(materialRow.revenueErrorLabel());
        });
    }

    private JLabel createErrorLabel()
    {
        JLabel label = new JLabel();
        label.setForeground(Color.RED);
        label.setVisible(false);
        return label;
    }

    private void showError(JLabel label, String messageKey)
    {
        label.setText(this.language.translate(messageKey));
        label.setVisible(true);
    }

    private void hideError(JLabel label)
    {
        label.setText("");
        label.setVisible(false);
    }

    private Integer parseInteger(String text)
    {
        if (text == null || text.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Material getSelectedMaterial(MaterialRow materialRow)
    {
        Object selectedItem = materialRow.materialField().getSelectedItem();
        return selectedItem instanceof Material material ? material : null;
    }

    private record MaterialRow(
        MaterialComboBox materialField,
        JTextField amountField,
        JTextField qualityField,
        JTextField revenueField,
        JLabel materialErrorLabel,
        JLabel amountErrorLabel,
        JLabel revenueErrorLabel
    ) {}
}

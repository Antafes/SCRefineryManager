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

package antafes.sc.refinery.manager.gui.newRefinement;

import antafes.sc.base.entity.Material;
import antafes.sc.base.repository.MaterialRepository;
import antafes.sc.refinery.manager.Configuration;
import antafes.sc.refinery.manager.SCRefineryManager;
import antafes.sc.refinery.manager.dto.*;
import antafes.sc.refinery.manager.gui.element.MaterialComboBox;
import antafes.sc.refinery.manager.gui.event.LanguageChangedEvent;
import antafes.sc.refinery.manager.gui.event.LanguageChangedListener;
import antafes.sc.refinery.manager.gui.event.ResetNewRefinementDialogEvent;
import antafes.sc.refinery.manager.gui.event.ResetNewRefinementDialogListener;
import antafes.sc.refinery.manager.gui.event.SaveRefinementEvent;
import antafes.sc.refinery.manager.gui.event.SaveRefinementListener;
import antafes.sc.refinery.manager.gui.filter.IntegerDocumentFilter;
import antafes.sc.refinery.manager.service.RefinementService;
import antafes.utilities.language.LanguageInterface;
import jakarta.annotation.PostConstruct;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@org.springframework.stereotype.Component
public class NewRefinementPanel extends JPanel
{
    @Autowired
    private Configuration configuration;
    @Autowired
    private MaterialRepository materialRepository;
    @Autowired
    private RefinementService refinementService;

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
        this.applyForm(this.refinementService.createNewForm());
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
        this.addMaterialRowButton.addActionListener(_ -> this.addMaterialRow());

        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 2;
        add(this.addMaterialRowButton, constraints);
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
            SaveRefinementEvent.class,
            new SaveRefinementListener(event -> {
                SaveRefinementResult saveResult = this.refinementService.add(this.collectForm());
                if (saveResult.successful()) {
                    event.getDialog().dispose();
                    return;
                }

                this.applyValidation(saveResult.validation());
            })
        );
        SCRefineryManager.getDispatcher().addListener(
            ResetNewRefinementDialogEvent.class,
            new ResetNewRefinementDialogListener(_ -> this.applyForm(this.refinementService.createNewForm()))
        );
        SCRefineryManager.getDispatcher().addListener(
            LanguageChangedEvent.class,
            new LanguageChangedListener(event -> SwingUtilities.invokeLater(() -> {
                this.language = event.getLanguage();
                this.setFieldTexts();
            }))
        );
    }

    private void addMaterialRow()
    {
        this.addMaterialRow(this.refinementService.createNewMaterialRow());
    }

    private void addMaterialRow(RefinementMaterialForm initial)
    {
        JPanel row = new JPanel(new GridBagLayout());
        GridBagConstraints rowConstraints = new GridBagConstraints();
        rowConstraints.insets = new Insets(2, 2, 2, 2);
        rowConstraints.fill = GridBagConstraints.HORIZONTAL;
        MaterialComboBox materialCombo = this.getMaterialComboBox();

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

        JButton remove = this.createRemoveButton(row, materialRow);
        rowConstraints.gridy = 0;
        rowConstraints.gridx = 4;
        rowConstraints.weightx = 0;
        row.add(remove, rowConstraints);

        this.materialsContainer.add(row);
        this.materialsContainer.revalidate();
        this.materialsContainer.repaint();

        materialCombo.refresh();
        if (initial != null) {
            this.selectMaterial(materialCombo, initial.material());
            amountField.setText(initial.amount() == null ? "" : initial.amount());
            qualityField.setText(initial.quality() == null ? "" : initial.quality());
            revenueField.setText(initial.revenue() == null ? "" : initial.revenue());
        }
        this.updateRemoveButtonsState();
    }

    private void selectMaterial(MaterialComboBox comboBox, Material material)
    {
        if (material == null) {
            return;
        }

        for (int i = 0; i < comboBox.getItemCount(); i++) {
            Object item = comboBox.getItemAt(i);
            if (item instanceof Material listedMaterial && Objects.equals(listedMaterial.getKey(), material.getKey())) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }

        comboBox.setSelectedItem(material);
    }

    private @NonNull JButton createRemoveButton(JPanel row, MaterialRow materialRow)
    {
        JButton remove = new JButton("-");
        remove.setActionCommand("remove");
        remove.addActionListener(_ -> {
            if (this.materialRows.size() > 1) {
                this.materialRows.remove(materialRow);
                this.materialsContainer.remove(row);
                this.materialsContainer.revalidate();
                this.materialsContainer.repaint();
            } else {
                this.applyMaterialRow(materialRow, this.refinementService.createNewMaterialRow());
                this.hideError(materialRow.materialErrorLabel());
                this.hideError(materialRow.amountErrorLabel());
                this.hideError(materialRow.revenueErrorLabel());
            }
            this.updateRemoveButtonsState();
        });
        return remove;
    }

    private void applyMaterialRow(MaterialRow materialRow, RefinementMaterialForm form)
    {
        if (materialRow.materialField().getItemCount() > 0) {
            materialRow.materialField().setSelectedIndex(0);
        }
        this.selectMaterial(materialRow.materialField(), form.material());
        materialRow.amountField().setText(form.amount() == null ? "" : form.amount());
        materialRow.qualityField().setText(form.quality() == null ? "" : form.quality());
        materialRow.revenueField().setText(form.revenue() == null ? "" : form.revenue());
    }

    private @NonNull MaterialComboBox getMaterialComboBox()
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

    private RefinementForm collectForm()
    {
        return new RefinementForm(
            this.costField.getText(),
            this.materialRows.stream()
                .map(materialRow -> new RefinementMaterialForm(
                    this.getSelectedMaterial(materialRow),
                    materialRow.amountField().getText(),
                    materialRow.qualityField().getText(),
                    materialRow.revenueField().getText()
                ))
                .toList()
        );
    }

    private void applyForm(RefinementForm form)
    {
        this.clearValidationErrors();
        this.costField.setText(form.cost() == null ? "" : form.cost());

        for (int i = this.materialsContainer.getComponentCount() - 1; i >= 1; i--) {
            this.materialsContainer.remove(i);
        }
        this.materialRows.clear();

        List<RefinementMaterialForm> materials = form.materials();
        if (materials == null || materials.isEmpty()) {
            this.addMaterialRow(this.refinementService.createNewMaterialRow());
        } else {
            materials.forEach(this::addMaterialRow);
        }

        this.materialsContainer.revalidate();
        this.materialsContainer.repaint();
        revalidate();
        repaint();
    }

    private void applyValidation(RefinementValidation validation)
    {
        this.clearValidationErrors();
        this.showErrorIfPresent(this.costErrorLabel, validation.costErrorKey());

        for (int rowIndex = 0; rowIndex < Math.min(this.materialRows.size(), validation.materialRows().size()); rowIndex++) {
            MaterialRow materialRow = this.materialRows.get(rowIndex);
            MaterialRowValidation rowValidation = validation.materialRows().get(rowIndex);
            this.showErrorIfPresent(materialRow.materialErrorLabel(), rowValidation.materialErrorKey());
            this.showErrorIfPresent(materialRow.amountErrorLabel(), rowValidation.amountErrorKey());
            this.showErrorIfPresent(materialRow.revenueErrorLabel(), rowValidation.revenueErrorKey());
        }

        this.focusInvalidField(validation.firstInvalidField());
        revalidate();
        repaint();
    }

    private void focusInvalidField(InvalidField invalidField)
    {
        if (invalidField == null) {
            return;
        }

        Component componentToFocus = switch (invalidField.type()) {
            case COST -> this.costField;
            case MATERIAL -> this.getMaterialRowComponent(invalidField.rowIndex(), MaterialRow::materialField);
            case AMOUNT -> this.getMaterialRowComponent(invalidField.rowIndex(), MaterialRow::amountField);
            case REVENUE -> this.getMaterialRowComponent(invalidField.rowIndex(), MaterialRow::revenueField);
        };

        if (componentToFocus != null) {
            componentToFocus.requestFocusInWindow();
        }
    }

    private Component getMaterialRowComponent(Integer rowIndex, MaterialRowComponentExtractor extractor)
    {
        if (rowIndex == null || rowIndex < 0 || rowIndex >= this.materialRows.size()) {
            return null;
        }

        return extractor.extract(this.materialRows.get(rowIndex));
    }

    private void clearValidationErrors()
    {
        this.hideError(this.costErrorLabel);
        this.materialRows.forEach(materialRow -> {
            this.hideError(materialRow.materialErrorLabel());
            this.hideError(materialRow.amountErrorLabel());
            this.hideError(materialRow.revenueErrorLabel());
        });
    }

    private JLabel createErrorLabel()
    {
        JLabel label = new JLabel();
        label.setForeground(Color.RED);
        label.setVisible(false);
        return label;
    }

    private void showErrorIfPresent(JLabel label, String messageKey)
    {
        if (messageKey != null) {
            this.showError(label, messageKey);
        }
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

    private Material getSelectedMaterial(MaterialRow materialRow)
    {
        Object selectedItem = materialRow.materialField().getSelectedItem();
        return selectedItem instanceof Material material ? material : null;
    }

    @FunctionalInterface
    private interface MaterialRowComponentExtractor
    {
        Component extract(MaterialRow materialRow);
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

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

package antafes.sc.refinery.manager.gui;

import antafes.sc.base.entity.Material;
import antafes.sc.refinery.manager.Configuration;
import antafes.sc.refinery.manager.entity.RefinedMaterial;
import antafes.sc.refinery.manager.entity.Refinement;
import antafes.sc.refinery.manager.gui.element.renderer.MultiLineCellRenderer;
import antafes.sc.refinery.manager.gui.element.renderer.PaddedDefaultTableCellRenderer;
import antafes.sc.refinery.manager.gui.icon.PenIcon;
import antafes.sc.refinery.manager.gui.icon.TrashIcon;
import antafes.sc.refinery.manager.repository.RefinementRepository;
import antafes.sc.refinery.manager.util.Cargo;
import antafes.sc.refinery.manager.util.Currency;
import antafes.sc.refinery.manager.util.Name;
import antafes.utilities.language.LanguageInterface;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RefinementTable extends JTable
{
    private static final int ACTIONS_COLUMN_INDEX = 5;
    private static final int MATERIALS_COLUMN_INDEX = 4;
    private static final int BASE_ROW_HEIGHT = 32;

    private static final DateTimeFormatter CREATED_AT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final RefinementRepository refinementRepository;
    private LanguageInterface language;
    private Configuration.Language appLanguage;
    private final RefinementTableModel refinementTableModel;

    public RefinementTable(
        @Autowired RefinementRepository refinementRepository,
        @Autowired Configuration configuration
    ) {
        this.refinementRepository = refinementRepository;
        this.language = configuration.getLanguageObject();
        this.appLanguage = (Configuration.Language) configuration.getLanguage();
        this.refinementTableModel = new RefinementTableModel();
        this.setModel(this.refinementTableModel);
        this.configureTable();
        this.addListener();
    }

    private void addListener()
    {
        antafes.sc.refinery.manager.SCRefineryManager.getDispatcher().addListener(
            antafes.sc.refinery.manager.gui.event.LanguageChangedEvent.class,
            new antafes.sc.refinery.manager.gui.event.LanguageChangedListener(
                event -> SwingUtilities.invokeLater(() -> this.updateLanguage(event.getLanguage(), event.getAppLanguage()))
            )
        );
    }

    private void updateLanguage(LanguageInterface language, Configuration.Language appLanguage)
    {
        if (language != null) {
            this.language = language;
        }
        if (appLanguage != null) {
            this.appLanguage = appLanguage;
        }
        this.refreshTexts();
    }

    public void refreshData()
    {
        this.refinementTableModel.setRows(this.refinementRepository.findAll());
        resetRowHeights();
    }

    public void refreshTexts()
    {
        this.refinementTableModel.fireTableStructureChanged();
        this.configureTable();
        resetRowHeights();
    }

    private void configureTable()
    {
        this.setFillsViewportHeight(true);
        this.setRowHeight(BASE_ROW_HEIGHT);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.getTableHeader().setReorderingAllowed(false);
        this.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        DefaultTableCellRenderer rightAlignedRenderer = new PaddedDefaultTableCellRenderer(SwingConstants.RIGHT);
        this.getColumnModel().getColumn(0).setCellRenderer(rightAlignedRenderer);
        this.getColumnModel().getColumn(1).setCellRenderer(rightAlignedRenderer);
        this.getColumnModel().getColumn(2).setCellRenderer(rightAlignedRenderer);
        this.getColumnModel().getColumn(3).setCellRenderer(rightAlignedRenderer);

        FontMetrics cellFm = this.getFontMetrics(this.getFont());
        FontMetrics headerFm = this.getTableHeader() != null
            ? this.getTableHeader().getFontMetrics(this.getTableHeader().getFont())
            : cellFm;

        // Rough allowance for renderer border + inter-cell spacing.
        int padding = 24;

        int keyWidth = Math.max(
            cellFm.stringWidth("999"),
            headerFm.stringWidth(this.getColumnName(0))
        ) + padding;

        String moneySample = Currency.format(9_999_999, this.appLanguage);
        String moneySampleNegative = Currency.format(-9_999_999, this.appLanguage);
        int moneyContentWidth = Math.max(
            cellFm.stringWidth(moneySample),
            cellFm.stringWidth(moneySampleNegative)
        );
        int moneyHeaderWidth = Math.max(
            headerFm.stringWidth(this.getColumnName(1)),
            Math.max(
                headerFm.stringWidth(this.getColumnName(2)),
                headerFm.stringWidth(this.getColumnName(3))
            )
        );
        int moneyWidth = Math.max(moneyContentWidth, moneyHeaderWidth) + padding;

        TableColumn keyColumn = this.getColumnModel().getColumn(0);
        keyColumn.setMinWidth(keyWidth);
        keyColumn.setPreferredWidth(keyWidth);
        keyColumn.setMaxWidth(keyWidth);

        for (int i = 1; i <= 3; i++) {
            TableColumn moneyColumn = this.getColumnModel().getColumn(i);
            moneyColumn.setMinWidth(moneyWidth);
            moneyColumn.setPreferredWidth(moneyWidth);
            moneyColumn.setMaxWidth(moneyWidth);
        }

        // Materials should receive most of the remaining width and wrap when long.
        TableColumn materialsColumn = this.getColumnModel().getColumn(MATERIALS_COLUMN_INDEX);
        materialsColumn.setMinWidth(200);
        materialsColumn.setPreferredWidth(800);
        materialsColumn.setCellRenderer(new MultiLineCellRenderer());

        TableColumn actionsColumn = this.getColumnModel().getColumn(ACTIONS_COLUMN_INDEX);
        actionsColumn.setMinWidth(110);
        actionsColumn.setMaxWidth(110);
        actionsColumn.setPreferredWidth(110);
        actionsColumn.setCellRenderer(new ActionButtonsCellRenderer());
        actionsColumn.setCellEditor(new ActionButtonsCellEditor());
    }

    private void resetRowHeights()
    {
        for (int row = 0; row < getRowCount(); row++) {
            setRowHeight(row, BASE_ROW_HEIGHT);
        }
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
    {
        Component component = super.prepareRenderer(renderer, row, column);

        if (column == MATERIALS_COLUMN_INDEX && component instanceof JTextArea textArea) {
            int colWidth = getColumnModel().getColumn(column).getWidth();
            textArea.setSize(new Dimension(colWidth, Short.MAX_VALUE));
            int preferredHeight = textArea.getPreferredSize().height + getRowMargin();
            if (getRowHeight(row) < preferredHeight) {
                setRowHeight(row, preferredHeight);
            }
        }

        return component;
    }

    @Override
    public String getToolTipText(@NonNull MouseEvent event)
    {
        int viewRow = rowAtPoint(event.getPoint());
        int viewColumn = columnAtPoint(event.getPoint());

        if (viewRow < 0 || viewColumn != 0) {
            return super.getToolTipText(event);
        }

        int modelRow = convertRowIndexToModel(viewRow);
        ZonedDateTime createdAt = this.refinementTableModel.getCreatedAtFor(modelRow);
        if (createdAt == null) {
            return null;
        }

        ZonedDateTime displayCreatedAt = createdAt.withZoneSameInstant(ZoneId.systemDefault());
        String formatted = displayCreatedAt.format(CREATED_AT_FORMATTER);

        return "%s: %s".formatted(this.language.translate("createdAt"), formatted);
    }

    private void deleteRefinement(int key)
    {
        int confirmation = JOptionPane.showConfirmDialog(
            this,
            String.format(this.language.translate("deleteRefinementConfirm"), key),
            this.language.translate("deleteRefinementTitle"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirmation == JOptionPane.YES_OPTION) {
            this.refinementRepository.remove(key);
            this.refreshData();
        }
    }

    private static JButton createActionButton(Icon icon, String actionCommand)
    {
        JButton button = new JButton(icon);
        button.setActionCommand(actionCommand);
        button.setFocusable(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        return button;
    }

    private record RefinementTableRow(int key, int cost, int revenue, int profit, String materials, ZonedDateTime createdAt) {}

    private class RefinementTableModel extends AbstractTableModel
    {
        private final List<RefinementTableRow> rows = new ArrayList<>();

        public void setRows(Map<Integer, Refinement> refinements)
        {
            this.rows.clear();
            refinements.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.naturalOrder()))
                .map(entry -> {
                    Refinement refinement = entry.getValue();
                    int revenue = refinement.calculateTotalSellingPrice();
                    return new RefinementTableRow(
                        entry.getKey(),
                        refinement.getCost(),
                        revenue,
                        revenue - refinement.getCost(),
                        formatMaterials(refinement),
                        refinement.getCreatedAt()
                    );
                })
                .forEachOrdered(this.rows::add);
            fireTableDataChanged();
        }

        @Override
        public int getRowCount()
        {
            return this.rows.size();
        }

        @Override
        public int getColumnCount()
        {
            return 6;
        }

        @Override
        public String getColumnName(int column)
        {
            return switch (column) {
                case 0 -> "#";
                case 1 -> RefinementTable.this.language.translate("cost");
                case 2 -> RefinementTable.this.language.translate("revenue");
                case 3 -> RefinementTable.this.language.translate("profit");
                case 4 -> RefinementTable.this.language.translate("materials");
                case 5 -> RefinementTable.this.language.translate("actions");
                default -> "";
            };
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            RefinementTableRow row = this.rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> row.key;
                case 1 -> Currency.format(row.cost, RefinementTable.this.appLanguage);
                case 2 -> Currency.format(row.revenue, RefinementTable.this.appLanguage);
                case 3 -> Currency.format(row.profit, RefinementTable.this.appLanguage);
                case 4 -> row.materials;
                case 5 -> row;
                default -> null;
            };
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return columnIndex == ACTIONS_COLUMN_INDEX;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return switch (columnIndex) {
                case ACTIONS_COLUMN_INDEX -> RefinementTableRow.class;
                case 0 -> Integer.class;
                case 1, 2, 3, 4 -> String.class;
                default -> Object.class;
            };
        }

        ZonedDateTime getCreatedAtFor(int modelRow)
        {
            if (modelRow < 0 || modelRow >= this.rows.size()) {
                return null;
            }
            return this.rows.get(modelRow).createdAt();
        }

        private String formatMaterials(Refinement refinement)
        {
            if (refinement.getMaterials() == null || refinement.getMaterials().isEmpty()) {
                return "";
            }

            Map<Object, MaterialAggregate> combined = new HashMap<>();
            refinement.getMaterials().values().forEach(refinedMaterial -> {
                Material displayMaterial = getDisplayMaterial(refinedMaterial);
                Object materialKey = displayMaterial != null ? displayMaterial.getKey() : null;
                MaterialAggregate aggregate = combined.computeIfAbsent(
                    materialKey,
                    _ -> new MaterialAggregate(displayMaterial, 0)
                );
                aggregate.amountCSCU += refinedMaterial.getAmount();
            });

            return combined.values().stream()
                .sorted(Comparator.comparing(a -> getMaterialDisplayName(a.material), String.CASE_INSENSITIVE_ORDER))
                .map(a -> "%s (%s)".formatted(
                    getMaterialDisplayName(a.material),
                    Cargo.formatFromCSCU(a.amountCSCU)
                ))
                .collect(Collectors.joining(", "));
        }

        private Material getDisplayMaterial(RefinedMaterial refinedMaterial)
        {
            if (refinedMaterial == null) return null;
            Material displayMaterial = refinedMaterial.getBaseMaterial();
            if (displayMaterial.getReferences() != null) {
                displayMaterial = displayMaterial.getReferences();
            }
            return displayMaterial;
        }

        private String getMaterialDisplayName(Material displayMaterial)
        {
            return displayMaterial == null ? "" : Name.fetchTranslatedName(displayMaterial);
        }

        private static class MaterialAggregate
        {
            private final Material material;
            private int amountCSCU;

            private MaterialAggregate(Material material, int amountCSCU)
            {
                this.material = material;
                this.amountCSCU = amountCSCU;
            }
        }
    }

    private static class ActionButtonsCellRenderer extends JPanel implements TableCellRenderer
    {
        private ActionButtonsCellRenderer()
        {
            super(new FlowLayout(FlowLayout.CENTER, 4, 2));
            setOpaque(true);
            setBorder(null);
            add(createActionButton(new PenIcon(), "edit"));
            add(createActionButton(new TrashIcon(), "delete"));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }

            return this;
        }
    }

    private class ActionButtonsCellEditor extends AbstractCellEditor implements TableCellEditor
    {
        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 2));
        private RefinementTableRow currentRow;

        private ActionButtonsCellEditor()
        {
            JButton editButton = createActionButton(new PenIcon(), "edit");
            JButton deleteButton = createActionButton(new TrashIcon(), "delete");
            this.panel.add(editButton);
            this.panel.add(deleteButton);

            editButton.addActionListener(_ -> {
                stopCellEditing();
                if (this.currentRow != null) {
                    antafes.sc.refinery.manager.SCRefineryManager.getDispatcher().dispatch(
                        new antafes.sc.refinery.manager.gui.event.EditRefinementEvent(this.currentRow.key)
                    );
                }
            });
            deleteButton.addActionListener(_ -> {
                stopCellEditing();
                if (this.currentRow != null) {
                    RefinementTable.this.deleteRefinement(this.currentRow.key);
                }
            });
        }

        @Override
        public Object getCellEditorValue()
        {
            return this.currentRow;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
        {
            this.currentRow = (RefinementTableRow) value;
            this.panel.setBackground(table.getSelectionBackground());

            // Keep the editor border empty to avoid shrinking the content area.
            this.panel.setBorder(null);

            return this.panel;
        }
    }
}

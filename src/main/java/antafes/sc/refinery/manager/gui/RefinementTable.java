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
import antafes.sc.refinery.manager.entity.Refinement;
import antafes.sc.refinery.manager.entity.RefinedMaterial;
import antafes.sc.refinery.manager.gui.element.renderer.MultiLineCellRenderer;
import antafes.sc.refinery.manager.gui.icon.PenIcon;
import antafes.sc.refinery.manager.gui.icon.TrashIcon;
import antafes.sc.refinery.manager.repository.RefinementRepository;
import antafes.sc.refinery.manager.util.Cargo;
import antafes.sc.refinery.manager.util.Currency;
import antafes.sc.refinery.manager.util.Name;
import antafes.utilities.language.LanguageInterface;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RefinementTable extends JTable
{
    private static final int ACTIONS_COLUMN_INDEX = 5;
    private static final int MATERIALS_COLUMN_INDEX = 4;
    private static final int BASE_ROW_HEIGHT = 32;

    private final Component parentComponent;
    private final RefinementRepository refinementRepository;
    private final LanguageInterface language;
    private final RefinementTableModel refinementTableModel;

    public RefinementTable(Component parentComponent, RefinementRepository refinementRepository, LanguageInterface language)
    {
        this.parentComponent = parentComponent;
        this.refinementRepository = refinementRepository;
        this.language = language;
        this.refinementTableModel = new RefinementTableModel();
        this.setModel(this.refinementTableModel);
        this.configureTable();
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

        DefaultTableCellRenderer rightAlignedRenderer = new DefaultTableCellRenderer();
        rightAlignedRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
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

        String moneySample = Currency.format(9_999_999);
        String moneySampleNegative = Currency.format(-9_999_999);
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

    private void deleteRefinement(int key)
    {
        int confirmation = JOptionPane.showConfirmDialog(
            this.parentComponent,
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

    private static class RefinementTableRow
    {
        private final int key;
        private final int cost;
        private final int revenue;
        private final int profit;
        private final String materials;

        private RefinementTableRow(int key, int cost, int revenue, int profit, String materials)
        {
            this.key = key;
            this.cost = cost;
            this.revenue = revenue;
            this.profit = profit;
            this.materials = materials;
        }
    }

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
                        formatMaterials(refinement)
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
                case 1 -> Currency.format(row.cost);
                case 2 -> Currency.format(row.revenue);
                case 3 -> Currency.format(row.profit);
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

        private String formatMaterials(Refinement refinement)
        {
            if (refinement.getMaterials() == null || refinement.getMaterials().isEmpty()) {
                return "";
            }

            return refinement.getMaterials().values().stream()
                .sorted(Comparator.comparing(this::getMaterialDisplayName, String.CASE_INSENSITIVE_ORDER))
                .map(material -> "%s (%s)".formatted(
                    getMaterialDisplayName(material),
                    Cargo.formatFromCSCU(material.getAmount())
                ))
                .collect(Collectors.joining(", "));
        }

        private String getMaterialDisplayName(RefinedMaterial refinedMaterial)
        {
            Material displayMaterial = refinedMaterial.getBaseMaterial();
            if (displayMaterial.getReferences() != null) {
                displayMaterial = displayMaterial.getReferences();
            }

            return Name.fetchTranslatedName(displayMaterial);
        }
    }

    private class ActionButtonsCellRenderer extends JPanel implements TableCellRenderer
    {
        private final JButton editButton = createActionButton(new PenIcon(), "edit");
        private final JButton deleteButton = createActionButton(new TrashIcon(), "delete");

        private ActionButtonsCellRenderer()
        {
            super(new FlowLayout(FlowLayout.CENTER, 4, 2));
            setOpaque(true);
            add(this.editButton);
            add(this.deleteButton);
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
        private final JButton editButton = createActionButton(new PenIcon(), "edit");
        private final JButton deleteButton = createActionButton(new TrashIcon(), "delete");
        private RefinementTableRow currentRow;

        private ActionButtonsCellEditor()
        {
            this.panel.add(this.editButton);
            this.panel.add(this.deleteButton);

            this.editButton.addActionListener(_ -> stopCellEditing());
            this.deleteButton.addActionListener(_ -> {
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
            return this.panel;
        }
    }
}

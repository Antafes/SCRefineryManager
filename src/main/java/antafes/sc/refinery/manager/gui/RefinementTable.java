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

import antafes.sc.refinery.manager.entity.Refinement;
import antafes.sc.refinery.manager.gui.icon.PenIcon;
import antafes.sc.refinery.manager.gui.icon.TrashIcon;
import antafes.sc.refinery.manager.repository.RefinementRepository;
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

public class RefinementTable extends JTable
{
    private static final int ACTIONS_COLUMN_INDEX = 4;

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
    }

    public void refreshTexts()
    {
        this.refinementTableModel.fireTableStructureChanged();
        this.configureTable();
    }

    private void configureTable()
    {
        this.setFillsViewportHeight(true);
        this.setRowHeight(32);
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer rightAlignedRenderer = new DefaultTableCellRenderer();
        rightAlignedRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        this.getColumnModel().getColumn(0).setCellRenderer(rightAlignedRenderer);
        this.getColumnModel().getColumn(1).setCellRenderer(rightAlignedRenderer);
        this.getColumnModel().getColumn(2).setCellRenderer(rightAlignedRenderer);
        this.getColumnModel().getColumn(3).setCellRenderer(rightAlignedRenderer);

        TableColumn actionsColumn = this.getColumnModel().getColumn(ACTIONS_COLUMN_INDEX);
        actionsColumn.setMinWidth(110);
        actionsColumn.setMaxWidth(110);
        actionsColumn.setPreferredWidth(110);
        actionsColumn.setCellRenderer(new ActionButtonsCellRenderer());
        actionsColumn.setCellEditor(new ActionButtonsCellEditor());
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

        private RefinementTableRow(int key, int cost, int revenue, int profit)
        {
            this.key = key;
            this.cost = cost;
            this.revenue = revenue;
            this.profit = profit;
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
                    return new RefinementTableRow(entry.getKey(), refinement.getCost(), revenue, revenue - refinement.getCost());
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
            return 5;
        }

        @Override
        public String getColumnName(int column)
        {
            return switch (column) {
                case 0 -> "#";
                case 1 -> RefinementTable.this.language.translate("cost");
                case 2 -> RefinementTable.this.language.translate("revenue");
                case 3 -> RefinementTable.this.language.translate("profit");
                case 4 -> RefinementTable.this.language.translate("actions");
                default -> "";
            };
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            RefinementTableRow row = this.rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> row.key;
                case 1 -> row.cost;
                case 2 -> row.revenue;
                case 3 -> row.profit;
                case 4 -> row;
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
            return columnIndex == ACTIONS_COLUMN_INDEX ? RefinementTableRow.class : Integer.class;
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

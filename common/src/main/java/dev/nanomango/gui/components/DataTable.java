package dev.nanomango.gui.components;

import dev.nanomango.gui.component.Component;
import dev.nanomango.gui.component.Container;
import dev.nanomango.gui.core.GuiContext;
import dev.nanomango.gui.layout.VerticalLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple table with a header row.
 */
public class DataTable extends Container {

    private final List<String> columns = new ArrayList<>();
    private final List<List<String>> rows = new ArrayList<>();
    private float rowHeight = 22.0F;
    private int headerColor = -1;

    public DataTable(String... columns) {
        super(new VerticalLayout(0.0F));
        this.columns.addAll(List.of(columns));
        clip(false);
    }

    public DataTable addRow(String... cells) {
        rows.add(List.of(cells));
        return this;
    }

    public DataTable rowHeight(float rowHeight) {
        this.rowHeight = rowHeight;
        return this;
    }

    public DataTable headerColor(int headerColor) {
        this.headerColor = headerColor;
        return this;
    }

    public List<String> columns() {
        return columns;
    }

    public List<List<String>> rows() {
        return rows;
    }

    @Override
    public void render(GuiContext context) {
        float cursor = y();
        float radius = context.theme().smallRadius();

        int header = headerColor >= 0 ? headerColor : context.theme().surfaceVariant();
        context.draw().fillRounded(x(), cursor, width(), rowHeight, radius, header);
        float cellX = x() + context.theme().padding();
        for (String column : columns) {
            float cellWidth = width() / Math.max(1, columns.size());
            context.draw().text(cellX, cursor + (rowHeight - context.font().height()) * 0.5F, context.boldFont().id(),
                    column, context.theme().text());
            cellX += cellWidth;
        }
        cursor += rowHeight;

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            List<String> row = rows.get(rowIndex);
            if (rowIndex % 2 == 1) {
                context.draw().fillRect(x(), cursor, width(), rowHeight,
                        dev.nanomango.gui.util.Colors.withAlphaFactor(context.theme().surfaceVariant(), 0.35F));
            }
            float rowCellX = x() + context.theme().padding();
            for (int column = 0; column < columns.size(); column++) {
                float cellWidth = width() / Math.max(1, columns.size());
                String value = column < row.size() ? row.get(column) : "";
                context.draw().text(rowCellX, cursor + (rowHeight - context.font().height()) * 0.5F,
                        context.font().id(), value, context.theme().textMuted());
                rowCellX += cellWidth;
            }
            context.draw().fillRect(x(), cursor + rowHeight - 1.0F, width(), 1.0F,
                    dev.nanomango.gui.util.Colors.withAlphaFactor(context.theme().border(), 0.6F));
            cursor += rowHeight;
        }
    }

    @Override
    public float preferredHeight(GuiContext context) {
        return rowHeight * (rows.size() + 1);
    }
}

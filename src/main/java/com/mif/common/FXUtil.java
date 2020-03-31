package com.mif.common;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.util.function.Function;

public class FXUtil {

    public static <S, T> Callback<TableColumn.CellDataFeatures<S, T>, ObservableValue<T>> createArrayValueFactory(Function<S, T[]> arrayExtractor, final int index) {
        if (index < 0) {
            return cd -> null;
        }
        return cd -> {
            T[] array = arrayExtractor.apply(cd.getValue());
            return array == null || array.length <= index ? null : new SimpleObjectProperty<>(array[index]);
        };
    }

    public static void autoResizeColumnsOnTextSize(TableView<?> table) {
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.getColumns().stream().forEach( (column) -> {
            Text t = new Text(column.getText());
            double max = t.getLayoutBounds().getWidth();
            for (int i = 0; i < table.getItems().size(); i++) {
                if (column.getCellData(i) != null) {
                    t = new Text(column.getCellData(i).toString());
                    double calcwidth = t.getLayoutBounds().getWidth();
                    if (calcwidth > max) {
                        max = calcwidth;
                    }
                }
            }
            column.setPrefWidth( max + 10.0d );
        } );
    }

    public static void resizeEquallyTableColumns(TableView<?> table) {
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        double maxWidth = table.getPrefWidth();
        int colCount = table.getColumns().size();
        table.getColumns().stream().forEach(column -> {
            column.setPrefWidth(maxWidth / colCount);
        });
    }

    public static <T extends Pane> void fitChildrenToContainer(T container) {
        ObservableList<Node> children = container.getChildren();
        double maxWidth = container.getPrefWidth();
        double maxHeight = container.getPrefHeight();
        children.forEach(child -> {
            if (child.getClass().equals(TableView.class)) {
                ((TableView) child).setMinSize(maxWidth, maxHeight / children.size());
            }
        });
    }
}

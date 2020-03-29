package com.mif.rm;

import com.mif.FXModel.TablePage;
import com.mif.Main;
import com.mif.common.ByteUtil;
import com.mif.common.FXUtil;
import com.mif.common.Register;
import com.mif.FXModel.RegisterInstance;
import com.mif.vm.VirtualMachine;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class RealMachine {

    private Processor processor;
    private Memory memory;

    private List<RegisterInstance> readonlyRegisters = new ArrayList<>();

    public RealMachine(Stage primaryStage, String params) {
        this.memory = Memory.getInstance();
        this.processor = Processor.getInstance();
        this.getRegisters();
        this.initializeStage(primaryStage);
    }

    private void getRegisters() {
        Field[] fields = processor.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(Register.class)) {
                field.setAccessible(true);
                Register r = null;

                try {
                    r = (Register) field.get(processor);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                readonlyRegisters.add(new RegisterInstance(field, r));
            }
        }
    }

    // TODO cia reiktu kazkaip suglvot kad is cia procesintu komandas (butu whilas)
    //  kad po kiekvienos komandos sustotu ir butu galima step over daryt ir updatint UI
    public void run(String runCommand) {
        String[] command = runCommand.split(" ");
        String program = command[0];
        List<String> params = new ArrayList<>();

        if (command.length > 1) {
            for (int i = 1; i < command.length; i++) {
                params.add(command[i]);
            }
        }

        switch (program) {
            case "./pr1.txt":
                VirtualMachine vm = new VirtualMachine(params);
                vm.loadProgram("/pr1.txt");
                refreshMemoryTable();
                vm.freeMemory();

            case "./pr2.txt":
                VirtualMachine vm2 = new VirtualMachine(params);
                vm2.loadProgram("/pr2.txt");
                vm2.run();
                vm2.freeMemory();

            default:
                return;
        }
    }

    private void refreshMemoryTable() {
        for (int i = 0; i < tablePages.size(); i++) {
            TablePage memoryPage = tablePages.get(i);
            for (int j = 0; j < memoryPage.wordIntValues.length; j++) {
                byte[] memoryWordBytes = memory.getWord(i, j);
                int memoryWordInt = ByteUtil.byteToInt(memoryWordBytes);
                if (memoryPage.getWord(j) != memoryWordInt) {
                    memoryPage.setWord(j, memoryWordInt);
                    tablePages.set(i, memoryPage);
                }
            }
        }
        memoryTable.refresh();
    }

    private void initializeStage(Stage primaryStage) {
        FXMLLoader loader = new FXMLLoader((Main.class.getClassLoader().getResource("FXML/main.fxml")));
        loader.setController(this);

        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        Scene primaryScene = new Scene(root);
        primaryStage.setScene(primaryScene);
    }

    // Runs after constructor
    @FXML
    private void initialize() {
        renderRegisters();
        renderMemory();
    }

    @FXML
    private TextField sumbitField;

    @FXML
    private void submitInput(ActionEvent submitEvent) {
        String input = sumbitField.getText();
        if (input.isBlank())
            return;
        addToOutput("Keyboard input: " + input);
        if (input.contains("./")) {
            run(input);
        }
    }

    @FXML
    private VBox registersContainer;

    @FXML
    private AnchorPane commandContainer;

    private void renderRegisters() {
        for (RegisterInstance readonlyRegister : readonlyRegisters) {

            TextArea registerText = new TextArea(readonlyRegister.field.getName() + " "
                    + readonlyRegister.register.getValue());

            registerText.setEditable(false);

            HBox registerHbox = new HBox(registerText);
            registerHbox.setPrefWidth(registersContainer.getPrefWidth());

            registersContainer.getChildren().add(
                registerHbox
            );
        }

        FXUtil.fitChildrenToContainer(registersContainer);
    }

    @FXML
    private TableView memoryTable;

    @FXML
    private AnchorPane memoryTablePane;

    private List<TablePage> tablePages = new ArrayList<>();

    private void renderMemory() {
//        TableColumn<TablePage, TablePage> indexCol = new TableColumn<>("Page number");
//        indexCol.setCellValueFactory(new PropertyValueFactory<>("pageNumber"));
//        memoryTable.getColumns().add(indexCol);

        memoryTable.setPrefWidth(memoryTablePane.getPrefWidth());

        for (int col = 0; col < Memory.pageSize; col++) {
            TableColumn<TablePage, Integer> column = new TableColumn<>(String.valueOf(col));
            column.setCellValueFactory(FXUtil.createArrayValueFactory(TablePage::getValues, col));

            TablePage tablePage = new TablePage(col);
            for (int row = 0; row < Memory.defaultMemorySize / (Memory.pageSize * Memory.wordLen); row++) {
                tablePage.add(row, memory.getWord(col, row));
            }

            tablePages.add(tablePage);
            memoryTable.getItems().add(tablePage);

            column.setSortable(false);
            column.setEditable(false);
            memoryTable.getColumns().add(column);
        }

        FXUtil.resizeEquallyTableColumns(memoryTable);
    }

    @FXML
    private TextArea outputField;

    private void addToOutput(String text) {
        if (!text.isBlank())
            outputField.appendText("\n" + text);
    }
}

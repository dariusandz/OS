package com.mif.rm;

import com.mif.FXModel.MemoryTableRow;
import com.mif.FXModel.RegisterTableRow;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
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
                while (true) {
                    processor = vm.processCommand(vm.getCommand());
                    // TODO updatinti cia gui, kad rodytu komandas
                    if(!(Processor.processSIValue(processor, vm.virtualMemory)))
                        break;
                }
                refreshMemoryTable();
                vm.freeMemory();
                break;
            case "./pr2.txt":
                VirtualMachine vm2 = new VirtualMachine(params);
                vm2.loadProgram("/pr2.txt");
                while (true) {
                    processor = vm2.processCommand(vm2.getCommand());
                    if(!(Processor.processSIValue(processor, vm2.virtualMemory)))
                        break;
                }
                refreshMemoryTable();
                vm2.freeMemory();
                break;
            default:
                return;
        }
    }

    private void refreshMemoryTable() {
        for (int i = 0; i < memoryTableRows.size(); i++) {
            MemoryTableRow memoryPage = memoryTableRows.get(i);
            for (int j = 0; j < memoryPage.wordIntValues.length; j++) {
                byte[] memoryWordBytes = memory.getWord(i, j);
                int memoryWordInt = ByteUtil.byteToInt(memoryWordBytes);
                if (memoryPage.getWord(j) != memoryWordInt) {
                    memoryPage.setWord(j, memoryWordInt);
                    memoryTableRows.set(i, memoryPage);
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
    private TableView registerTable;

    private List<RegisterTableRow> registerTableRows = new ArrayList<>();

    private void renderRegisters() {
        registerTable.setPrefWidth(registersContainer.getPrefWidth());

        TableColumn<RegisterTableRow, String> column1 = new TableColumn<>("Register");
        column1.setCellValueFactory(new PropertyValueFactory<>("name"));

        column1.setSortable(false);
        column1.setEditable(false);

        TableColumn<RegisterTableRow, String> column2 = new TableColumn<>("Value");
        column2.setCellValueFactory(new PropertyValueFactory<>("value"));

        column2.setSortable(false);
        column2.setEditable(false);

        registerTable.getColumns().addAll(column1, column2);

        for (RegisterInstance readonlyRegister : readonlyRegisters) {
            RegisterTableRow registerTableRow = new RegisterTableRow(
                    readonlyRegister.field.getName(),
                    readonlyRegister.register.getValue()
            );

            registerTableRows.add(registerTableRow);

            registerTable.getItems().add(registerTableRow);
        }

        FXUtil.resizeEquallyTableColumns(memoryTable);
    }

    @FXML
    private TableView memoryTable;

    @FXML
    private AnchorPane memoryTablePane;

    private List<MemoryTableRow> memoryTableRows = new ArrayList<>();

    private void renderMemory() {
//        TableColumn<TablePage, TablePage> indexCol = new TableColumn<>("Page number");
//        indexCol.setCellValueFactory(new PropertyValueFactory<>("pageNumber"));
//        memoryTable.getColumns().add(indexCol);

        memoryTable.setPrefWidth(memoryTablePane.getPrefWidth());

        for (int col = 0; col < Memory.pageSize; col++) {
            TableColumn<MemoryTableRow, Integer> column = new TableColumn<>(String.valueOf(col));
            column.setCellValueFactory(FXUtil.createArrayValueFactory(MemoryTableRow::getValues, col));

            MemoryTableRow memoryTableRow = new MemoryTableRow(col);
            for (int row = 0; row < Memory.defaultMemorySize / (Memory.pageSize * Memory.wordLen); row++) {
                memoryTableRow.add(row, memory.getWord(col, row));
            }

            memoryTableRows.add(memoryTableRow);
            memoryTable.getItems().add(memoryTableRow);

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

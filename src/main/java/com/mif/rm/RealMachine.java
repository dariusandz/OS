package com.mif.rm;

import com.mif.FXModel.CommandTableRow;
import com.mif.FXModel.MemoryTableRow;
import com.mif.FXModel.RegisterTableRow;
import com.mif.Main;
import com.mif.common.ByteUtil;
import com.mif.common.FXUtil;
import com.mif.FXModel.RegisterInstance;
import com.mif.vm.VirtualMachine;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.IntStream;

public class RealMachine {

    private Processor processor;
    private Memory memory;

    private VirtualMachine currentVm = null;
    private Long idOfRunningMachine;
    private List<VirtualMachine> virtualMachines = new ArrayList<>();
    private Map<Long, List<String>> vmCommands = new HashMap<>();

    private List<RegisterInstance> readonlyRegisters;

    public RealMachine(Stage primaryStage) {
        this.memory = Memory.getInstance();
        this.processor = Processor.getInstance();
        this.getRegisters();
        this.initializeStage(primaryStage);
    }

    private void getRegisters() {
        readonlyRegisters = new ArrayList<>();
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


    @FXML
    private Button stepOverBtn;

    @FXML
    private void stepOver() {
        step();
    }

    public void step() {
        if (currentVm != null) {
            currentVm = getRunningVmById(idOfRunningMachine);

            currentVm.processCommand();

            if (!processor.processSIValue(currentVm.virtualMemory)) {
                // TODO clean up virtual machine from real machine?
                currentVm.freeMemory();
                virtualMachines.remove(currentVm);

                // TODO temporary, kol gali veikti tik viena VM
                currentVm = null;

                addToOutput("Virtuali masina su id [" + idOfRunningMachine + "] baige darba.");
            }

            updateUI();
        }
    }

    private void run(String runCommand) {
        initializeVirtualMachine(runCommand);
        renderCommandTable();
    }

    private VirtualMachine getRunningVmById(Long id) {
        OptionalInt optIndex = IntStream.range(0, virtualMachines.size())
            .filter(ind -> idOfRunningMachine == virtualMachines.get(ind).getId())
            .findFirst();

        if (optIndex.isEmpty()) {
            return null;
        }

        return virtualMachines.get(optIndex.getAsInt());
    }

    private void initializeVirtualMachine(String command) {
        String[] splitCommand = command.split(" ");
        List<String> params = getParams(splitCommand);
        String programFileName = splitCommand[0];

        VirtualMachine virtualMachine = new VirtualMachine(programFileName, params);
        Long id = virtualMachine.getId();
        List<String> virtualMachinesCommands = virtualMachine.loadProgram();

        this.vmCommands.put(id, virtualMachinesCommands);
        this.idOfRunningMachine = virtualMachine.getId();
        this.virtualMachines.add(virtualMachine);
        // TODO temporary, kol gali veikti tik viena VM
        this.currentVm = virtualMachine;
    }

    private List<String> getParams(String[] splitCommand) {
        List<String> params = new ArrayList<>();
        if (splitCommand.length > 1) {
            for (int i = 1; i < splitCommand.length; i++) {
                params.add(splitCommand[i]);
            }
        }
        return params;
    }

    private void updateUI() {
        refreshMemoryTable();
        refreshRegisters();
        setNextCommand();
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

    private void refreshRegisters() {
        getRegisters();
        readonlyRegisters.forEach(reg -> {
            Optional<RegisterTableRow> row =
                    registerTableRows.stream()
                        .filter(r -> r.getName().equals(reg.field.getName()))
                        .findFirst();

            if (row.isPresent() && row.get().getValue() != reg.register.getValue()) {
                int i = registerTableRows.indexOf(row.get());
                row.get().setValue(reg.register.getValue());
                registerTableRows.set(i, row.get());
            }
        });
        registerTable.refresh();
    }

    // TODO kai komanda 8 baitu, IC painkrementina po 2, nors UI si komanda tik vienoje eiluteje - del to skipina
    private void setNextCommand() {
        int commandIndex = processor.IC.getValue();
        commandTable.getSelectionModel().clearSelection();
        commandTable.requestFocus();
        commandTable.getSelectionModel().select(commandIndex);
        commandTable.getFocusModel().focus(commandIndex);
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
    private VBox commandContainer;

    @FXML
    private TableView commandTable;

    private void renderCommandTable() {
        List<String> currentCommands = vmCommands.get(this.idOfRunningMachine);

        commandTable.setPrefWidth(commandContainer.getPrefWidth());

        TableColumn<CommandTableRow, String> column = new TableColumn<>("Command");
        column.setCellValueFactory(new PropertyValueFactory<>("command"));

        column.setSortable(false);
        column.setEditable(false);

        commandTable.getColumns().add(column);

        currentCommands.forEach(command -> {
            CommandTableRow tableRow = new CommandTableRow(command);
            commandTable.getItems().add(tableRow);
        });

        setNextCommand();
        FXUtil.fitChildrenToContainer(commandContainer);
        FXUtil.resizeEquallyTableColumns(commandTable);
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

        FXUtil.fitChildrenToContainer(registersContainer);
        FXUtil.resizeEquallyTableColumns(registerTable);
    }

    @FXML
    private TableView memoryTable;

    @FXML
    private VBox memoryTablePane;

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

        FXUtil.fitChildrenToContainer(memoryTablePane);
        FXUtil.resizeEquallyTableColumns(memoryTable);
    }

    @FXML
    private TextArea outputField;

    private void addToOutput(String text) {
        if (!text.isBlank())
            outputField.appendText("\n" + text);
    }
}

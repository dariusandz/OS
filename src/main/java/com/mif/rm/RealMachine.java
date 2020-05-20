package com.mif.rm;

import com.mif.FXModel.CommandTableRow;
import com.mif.FXModel.DeviceTableRow;
import com.mif.FXModel.EditHexCell;
import com.mif.FXModel.MemoryTableRow;
import com.mif.FXModel.PagingTableRow;
import com.mif.FXModel.RegisterTableRow;
import com.mif.Main;
import com.mif.common.ByteUtil;
import com.mif.common.FXUtil;
import com.mif.FXModel.RegisterInstance;
import com.mif.exception.FatalInterruptException;
import com.mif.exception.HarmlessInterruptException;
import com.mif.exception.OutOfMemoryException;
import com.mif.exception.TimerInterruptException;
import com.mif.vm.VirtualMachine;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
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
    private ChannelDevice channelDevice;

    private VirtualMachine currentVm = null;
    private Long idOfRunningMachine;
    private List<VirtualMachine> virtualMachines = new ArrayList<>();
    private Map<Long, List<String>> vmCommands = new HashMap<>();

    private List<RegisterInstance> readonlyRegisters;

    public RealMachine(Stage primaryStage) {
        this.channelDevice = new ChannelDevice();
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
        if(ChannelDevice.DB.getValue() != 2)
            step();
/*        if(!outputField.getText().contains("Type") || outputField.getText().contains("Keyboard")){
            step();
        }*/
    }

    public void step() {
        if (currentVm != null) {
            currentVm = getRunningVmById(idOfRunningMachine);
            currentVm.processCommand();

            try {
                processInterrupts();
            } catch (FatalInterruptException e) {
                cleanupVm(currentVm);
                if (currentVm != null) {
                    loadVMRegisters();
                    return;
                }
            }

            updateUI();
        }
    }

    private void processInterrupts() {
        try {
            Pair<Integer, String> siValuePair = processor.processSIValue(currentVm.virtualMemory);
            if (siValuePair != null) {
                if (siValuePair.getKey() == 1 || siValuePair.getKey() ==  2 || siValuePair.getKey() == 4) {
                    channelDevice.processSIValue(siValuePair);
                    if(ChannelDevice.DB.getValue() == 1) {
                        addToOutput(siValuePair.getValue());
                        ChannelDevice.resetRegisters();
                    }
                } else if (siValuePair.getKey() == 5) {
                    saveVMRegisters();
                    processor.resetRegisterValues();
                    stoppedAtCommand.put(idOfRunningMachine, getCommandIndex());
                    run(siValuePair.getValue());
                } else if (siValuePair.getKey() == 11) {
                    removeFromDeviceTable(siValuePair.getValue());
                }
            }
            processor.processTIValue();
            processor.processPIValue();
        } catch (FatalInterruptException | HarmlessInterruptException e) {
            addToOutput(e.getLocalCause());
            updateUI();
            throw e;
        } catch (TimerInterruptException e) {
            addToOutput(e.getLocalCause());
            Processor.TI.setValue(30);
        }
    }

    private void loadVMRegisters() {
        List<Integer> registerValues = currentVm.virtualMemory.pagingTable.loadVMRegisters();
        Field[] fields = processor.getClass().getDeclaredFields();
        int registerCounter = 0;
        for (int i = 0; i < fields.length; i++) {
            if(fields[i].getType().equals(Register.class)) {
                processor.setRegister(fields[i], ByteUtil.intToBytes(registerValues.get(registerCounter)));
                registerCounter++;
            }
        }
    }

    private void saveVMRegisters() {
        List<Integer> registerValues = new ArrayList<>();
        Field[] fields = processor.getClass().getDeclaredFields();
        for (Field field: fields) {
            if (field.getType().equals(Register.class)) {
                field.setAccessible(true);
                Register r = null;
                try {
                    r = (Register) field.get(processor);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                registerValues.add(r.getValue());
            }
        }
        currentVm.virtualMemory.pagingTable.saveVMRegisters(registerValues);
    }

    private void cleanupVm(VirtualMachine vm) {
        vm.freeMemory();
        vmCommands.remove(vm.getId());
        commandTableRowMap.remove(vm.getId());
        virtualMachines.remove(vm);
        currentVm = null;
        idOfRunningMachine = Long.valueOf(-1);
        processor.resetRegisterValues();
        if (virtualMachines.size() > 0) {
            this.currentVm = virtualMachines.get((int) (vm.getId() - 1));
            this.idOfRunningMachine = virtualMachines.get((int) (vm.getId() - 1)).getId();
        } else
            cleanupUI();
    }

    private void cleanupUI() {
        commandTable.getItems().clear();
        vmChoiceBox.getItems().clear();
    }

    private void run(String runCommand) {
        if (!initializeVirtualMachine(runCommand))
            return;
        putCommandsToTable();
        refreshMemoryTable();
        refreshRegisters();
    }

    private VirtualMachine getRunningVmById(Long id) {
        OptionalInt optIndex = IntStream.range(0, virtualMachines.size())
            .filter(ind -> id == virtualMachines.get(ind).getId())
            .findFirst();

        if (optIndex.isEmpty()) {
            return null;
        }

        return virtualMachines.get(optIndex.getAsInt());
    }

    private boolean initializeVirtualMachine(String command) {
        String[] splitCommand = command.split(" ");
        List<String> params = getParams(splitCommand);
        String programFileName = splitCommand[0];

        VirtualMachine virtualMachine = null;
        try {
            virtualMachine = new VirtualMachine(programFileName, params);
        } catch (OutOfMemoryException e) {
            appendToOutput(e.getMessage());
            return false;
        } catch (NumberFormatException e) {
            appendToOutput("Parameters should be integers");
            return false;
        }

        Long id = virtualMachine.getId();
        List<String> virtualMachinesCommands = virtualMachine.loadProgram();
        if (virtualMachinesCommands == null) {
            addToOutput("Invalid file");
            cleanupVm(virtualMachine);
            return false;
        }

        this.vmCommands.put(id, virtualMachinesCommands);
        this.virtualMachines.add(virtualMachine);
        this.idOfRunningMachine = virtualMachine.getId();
        this.currentVm = virtualMachine;
        this.refreshVirtualMachineList();
        return true;
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
        refreshVirtualMachineList();
        refreshDeviceTable();
        refreshCommandTable();
    }

    private Map<Long, Integer> stoppedAtCommand = new HashMap<>();
    private Map<Long, List<CommandTableRow>> commandTableRowMap = new HashMap<>();

    private void putCommandsToTable() {
        List<String> currentCommands = vmCommands.get(idOfRunningMachine);
        List<CommandTableRow> vmCommands = new ArrayList<>();
        commandTable.getItems().clear();
        currentCommands.forEach(command -> {
            CommandTableRow commandTableRow = new CommandTableRow(command);
            vmCommands.add(commandTableRow);
            commandTable.getItems().add(commandTableRow);
        });
        commandTableRowMap.put(idOfRunningMachine, vmCommands);
        focusCommand(0);
        commandTable.refresh();
    }

    private void refreshCommandTable() {
        List<String> currentCommands = vmCommands.get(idOfRunningMachine);
        if (currentCommands == null)
            return;
        // Nauja masina pereme darba
        if (!commandTableRowMap.containsKey(idOfRunningMachine)) {
            commandTable.getItems().clear();
            List<CommandTableRow> vmCommands = new ArrayList<>();
            currentCommands.forEach(command -> {
                CommandTableRow commandTableRow = new CommandTableRow(command);
                vmCommands.add(commandTableRow);
            });
            commandTable.getItems().addAll(vmCommands);
            commandTableRowMap.put(idOfRunningMachine, vmCommands);
            focusCommand(0);
            return;
        } else if (commandTableRowMap.containsKey(idOfRunningMachine) && stoppedAtCommand.containsKey(idOfRunningMachine)) {
            commandTable.getItems().clear();
            commandTable.getItems().addAll(commandTableRowMap.get(idOfRunningMachine));
            focusCommand(stoppedAtCommand.get(idOfRunningMachine) + 1);
            stoppedAtCommand.remove(idOfRunningMachine);
        }
        if(processor.IC.getValue() != 0)
            focusNextCommand();
    }

    private void refreshMemoryTable() {
        for (int i = 0; i < memoryTableRows.size(); i++) {
            MemoryTableRow memoryTableRow = memoryTableRows.get(i);
            for (int j = 0; j < memoryTableRow.wordHexValues.length; j++) {
                memoryTableRow.setWord(j, ByteUtil.bytesToHex(memory.getWord(i,j)));
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

            if (row.isPresent() && row.get().getValue() != reg.register.getHexValue()) {
                int i = registerTableRows.indexOf(row.get());
                row.get().setValue(reg.register.getHexValue());
                registerTableRows.set(i, row.get());
            }
        });
        registerTable.refresh();
    }

    @FXML
    private ChoiceBox vmChoiceBox;

    private void refreshVirtualMachineList() {
        if (vmChoiceBox.getItems().size() != virtualMachines.size()) {
            vmChoiceBox.getItems().clear();
            virtualMachines.forEach(vm -> {
                vmChoiceBox.getItems().add("VM " + vm.getId().toString());
            });
        }
    }

    List<DeviceTableRow> deviceTableRows = new ArrayList<>();

    // NOT SURE GOING SLEEP
    private void refreshDeviceTable() {
        processor.devices.forEach(device -> {
            Optional<DeviceTableRow> row =
                    deviceTableRows.stream()
                        .filter(r -> r.getId() == device.getId())
                        .findFirst();
            // Naujas devicas
            if (row.isEmpty()) {
                DeviceTableRow deviceTableRow = new DeviceTableRow(device);
                deviceTableRows.add(deviceTableRow);
                deviceTable.getItems().add(deviceTableRow);
            } else { // Paupdeitint devica
                int deviceRowIndex = deviceTableRows.indexOf(row.get());
                deviceTableRows.get(deviceRowIndex).set(device);
            }
        });
        deviceTable.refresh();
    }

    private void removeFromDeviceTable(String deviceId) {
        Long idOfDevice = Long.valueOf(deviceId);
        // Jei istrina is devicu, turi istrint ir is tablo
        if (processor.devices.size() != deviceTableRows.size()) {
            DeviceTableRow removedDeviceTableRow =
                deviceTableRows.stream()
                    .filter(device -> device.getId() == idOfDevice)
                    .findFirst().get();
            deviceTableRows.remove(removedDeviceTableRow);
            deviceTable.getItems().remove(removedDeviceTableRow);
        }
        deviceTable.refresh();
    }

    @FXML
    private HBox deviceTableContainer;

    @FXML
    private TableView deviceTable;

    private void renderDeviceTable() {
        deviceTable.setPrefWidth(deviceTableContainer.getPrefWidth());
        deviceTable.setEditable(false);

        TableColumn<DeviceTableRow, String> deviceTypeCol = new TableColumn<>("Device");
        deviceTypeCol.setCellValueFactory(new PropertyValueFactory<>("Type"));
        deviceTypeCol.setEditable(false);

        TableColumn<DeviceTableRow, Integer> deviceValueCol = new TableColumn<>("Value");
        deviceValueCol.setCellValueFactory(new PropertyValueFactory<>("Value"));
        deviceValueCol.setEditable(false);

        TableColumn<DeviceTableRow, String> deviceStateCol = new TableColumn<>("State");
        deviceStateCol.setCellValueFactory(new PropertyValueFactory<>("State"));
        deviceStateCol.setEditable(false);


        deviceTable.getColumns().addAll(deviceTypeCol, deviceValueCol, deviceStateCol);

        FXUtil.fitChildrenToContainer(deviceTableContainer);
        FXUtil.resizeEquallyTableColumns(deviceTable);
    }

    @FXML
    private Button vmBtn;

    @FXML
    private void renderVm() {
        String vmChoice = (String) vmChoiceBox.getValue();
        if (vmChoice == null) {
            vmBtn.setStyle("-fx-background-color: red;");
            return;
        } else {
            vmBtn.setStyle("");
        }

        Long vmId = Long.valueOf(vmChoice.split(" ")[1]);
        VirtualMachine selectedVm = getRunningVmById(vmId);

        VBox vmVBox = new VBox();
        vmVBox.setPrefHeight(400);
        vmVBox.setPrefWidth(800);

        TableView pagingTable = getPagingTable(selectedVm);

        HBox pagingTableHbox = new HBox(pagingTable);
        pagingTableHbox.setHgrow(pagingTable, Priority.ALWAYS);
        pagingTableHbox.setPrefWidth(vmVBox.getPrefWidth());
        pagingTableHbox.setPrefHeight(55);
        pagingTable.setPrefWidth(pagingTableHbox.getPrefWidth());
        FXUtil.fitChildrenToContainer(pagingTableHbox);
        FXUtil.resizeEquallyTableColumns(pagingTable);
        pagingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        HBox skirtukas = new HBox();
        skirtukas.setPrefHeight(75);

        TableView vmMemoryTable = getVmMemoryTable(selectedVm);

        HBox vmMemoryTableHbox = new HBox(vmMemoryTable);
        vmMemoryTableHbox.setHgrow(vmMemoryTable, Priority.ALWAYS);
        vmMemoryTableHbox.setPrefWidth(vmVBox.getPrefWidth());
        vmMemoryTable.setPrefWidth(vmMemoryTableHbox.getPrefWidth());
        FXUtil.fitChildrenToContainer(vmMemoryTableHbox);
        FXUtil.autoResizeColumnsOnTextSize(vmMemoryTable);

        FXUtil.fitChildrenToContainer(vmVBox);

        vmVBox.getChildren().addAll(pagingTableHbox, skirtukas, vmMemoryTableHbox);

        Scene vmScene = new Scene(vmVBox);

        Stage vmStage = new Stage();
        vmStage.setTitle("Virtual machine [" + vmId + "]");
        vmStage.setScene(vmScene);
        vmStage.show();
    }

    private TableView getPagingTable(VirtualMachine vm) {
        TableView tableView = new TableView();
        tableView.setEditable(false);

        String color;
        for (int col = 0; col < Memory.pageSize; col++) {
            TableColumn<PagingTableRow, Integer> column = new TableColumn<>(String.valueOf(col));
            column.setCellValueFactory(FXUtil.createArrayValueFactory(PagingTableRow::getRmPages, col));
            column.setEditable(false);
            column.setSortable(false);
            tableView.getColumns().add(column);
//            column.setStyle("-fx-background-color: " + getSegmentColor(col));
        }

        PagingTableRow pagingTableRow = new PagingTableRow();
        for (Map.Entry<Integer, Integer> entry : vm.virtualMemory.pagingTable.pageMap.entrySet()) {
            pagingTableRow.add(entry.getKey(), entry.getValue());
        }
        tableView.getItems().add(pagingTableRow);

        return tableView;
    }

    private TableView getVmMemoryTable(VirtualMachine vm) {
        TableView tableView = new TableView();
        tableView.setEditable(false);

        TableColumn<MemoryTableRow, Integer> indexCol = new TableColumn<>("Page");
        indexCol.setCellValueFactory(new PropertyValueFactory<>("pageNumber"));
        indexCol.setEditable(false);
        tableView.getColumns().add(indexCol);


        for (int i = 0; i < Memory.pageSize; i++) {
            TableColumn<MemoryTableRow, String> column = new TableColumn<>(String.valueOf(i));
            column.setCellValueFactory(FXUtil.createArrayValueFactory(MemoryTableRow::getValues, i));
            column.setSortable(false);
            column.setEditable(false);
            tableView.getColumns().add(column);
        }

        for (Map.Entry<Integer, Integer> entry : vm.virtualMemory.pagingTable.pageMap.entrySet()) {
            MemoryTableRow memoryTableRow = new MemoryTableRow(entry.getValue());

            for (int row = 0; row < Memory.pageSize; row++) {
                for (int col = 0; col < Memory.pageSize; col++) {
                    memoryTableRow.add(col, memory.getWord(entry.getValue(), col));
                }
            }
            tableView.getItems().add(memoryTableRow);
        }

        return tableView;
    }

    private String getSegmentColor(int index) {
        if (index == 0)
            return "blue;";
        else if (index >= 1 && index <= 2)
            return "green";
        else
            return "yellow";
    }

    private void focusNextCommand() {
        int commandIndex = commandTable.getSelectionModel().getFocusedIndex();
        focusCommand(commandIndex + 1);
    }

    private void focusCommand(int command) {
        commandTable.getSelectionModel().clearSelection();
        commandTable.requestFocus();
        commandTable.getSelectionModel().select(command);
        commandTable.getFocusModel().focus(command);
    }

    private int getCommandIndex() {
        return commandTable.getSelectionModel().getFocusedIndex() - 1;
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
        renderRealMemory();
        renderDeviceTable();
        renderCommandTable();
    }

    @FXML
    private TextField sumbitField;
    @FXML
    private void submitInput(ActionEvent submitEvent) {
        String input = sumbitField.getText();
        if (input.isBlank())
            return;
        appendToOutput("Keyboard input: " + input);
        if (input.contains("./")) {
            if(currentVm != null){
                saveVMRegisters();
                processor.resetRegisterValues();
            }
            run(input);
        }
        else if (ChannelDevice.DB.getValue() == 2) {
            processSCANCommand(input);
        }
    }

    private void processSCANCommand(String input) {
        if (input.length() != Processor.BX.getValue()) {
            addToOutput("You've typed in incorrect number of symbols \n" + "Type in " +
                    Processor.BX.getValue() + " symbols \n");
        } else {
            ChannelDevice.resetRegisters();
            byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
            currentVm.virtualMemory.putBytesToMemory(Processor.AX.getByteValue()[2], Processor.AX.getByteValue()[3], bytes, bytes.length);
        }
    }

    @FXML
    private VBox commandContainer;

    @FXML
    private TableView commandTable;

    private void renderCommandTable() {
        commandTable.setPrefWidth(commandContainer.getPrefWidth());
        commandTable.setEditable(false);

        TableColumn<CommandTableRow, String> column = new TableColumn<>("Command");
        column.setCellValueFactory(new PropertyValueFactory<>("command"));

        column.setSortable(false);
        column.setEditable(false);

        commandTable.getColumns().add(column);

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
        registerTable.setEditable(true);

        TableColumn<RegisterTableRow, String> column1 = new TableColumn<>("Register");
        column1.setCellValueFactory(new PropertyValueFactory<>("name"));

        column1.setSortable(false);
        column1.setEditable(false);

        Callback<TableColumn<RegisterTableRow, String>, TableCell<RegisterTableRow, String>> cellFactory
                = (TableColumn<RegisterTableRow, String> param) -> new EditHexCell();

        TableColumn<RegisterTableRow, String> column2 = new TableColumn<>("Value");
        column2.setCellValueFactory(new PropertyValueFactory<>("value"));

        column2.setSortable(false);
        column2.setEditable(true);
        column2.setCellFactory(cellFactory);

        column2.setOnEditCommit(
                (TableColumn.CellEditEvent<RegisterTableRow, String> t) -> {
                    final TablePosition pos = t.getTablePosition();
                    editRegister(readonlyRegisters.get(pos.getRow()).field, t.getNewValue());
                }
        );

        registerTable.getColumns().addAll(column1, column2);

        for (RegisterInstance readonlyRegister : readonlyRegisters) {
            RegisterTableRow registerTableRow = new RegisterTableRow(
                    readonlyRegister.field.getName(),
                    readonlyRegister.register.getHexValue()
            );

            registerTableRows.add(registerTableRow);

            registerTable.getItems().add(registerTableRow);
        }

        FXUtil.fitChildrenToContainer(registersContainer);
        FXUtil.resizeEquallyTableColumns(registerTable);
    }

    private void editRegister(Field registerNameField, String value) {
        processor.setRegister(registerNameField, ByteUtil.stringHexToBytes(value));
    }

    @FXML
    private TableView memoryTable;

    @FXML
    private VBox memoryTablePane;

    private List<MemoryTableRow> memoryTableRows = new ArrayList<>();

    private void renderRealMemory() {
        TableColumn<MemoryTableRow, Integer> indexCol = new TableColumn<>("Page");
        indexCol.setCellValueFactory(new PropertyValueFactory<>("pageNumber"));
        memoryTable.getColumns().add(indexCol);

        memoryTable.setPrefWidth(memoryTablePane.getPrefWidth());
        memoryTable.setEditable(true);

        for (int row = 0; row < Memory.defaultMemorySize / (Memory.pageSize * Memory.wordLen); row++) {
            MemoryTableRow memoryTableRow = new MemoryTableRow(row);
            for (int column = 0; column < Memory.pageSize; column++) {
                memoryTableRow.add(column, memory.getWord(row, column));
            }
            memoryTableRows.add(memoryTableRow);
            memoryTable.getItems().add(memoryTableRow);
        }

        Callback<TableColumn<MemoryTableRow, String>, TableCell<MemoryTableRow, String>> cellFactory
                = (TableColumn<MemoryTableRow, String> param) -> new EditHexCell();

        for (int col = 0; col < Memory.pageSize; col++) {
            TableColumn<MemoryTableRow, String> column = new TableColumn<>(String.valueOf(col));

            column.setCellValueFactory(FXUtil.createArrayValueFactory(MemoryTableRow::getValues, col));
            column.setCellFactory(cellFactory);
            column.setSortable(false);
            column.setEditable(true);
            column.setId(String.valueOf(col));
            column.setOnEditCommit(
                (TableColumn.CellEditEvent<MemoryTableRow, String> t) -> {
                    final TablePosition pos = t.getTablePosition();
                    editMemory(pos.getRow(), Integer.parseInt(pos.getTableColumn().getId()), t.getNewValue());
                }
            );

            memoryTable.getColumns().add(column);
        }

        FXUtil.fitChildrenToContainer(memoryTablePane);
        FXUtil.autoResizeColumnsOnTextSize(memoryTable);
    }

    private void editMemory(int page, int block, String word) {
        memory.putWord(page, block, ByteUtil.stringHexToBytes(word));
    }

    @FXML
    private TextArea outputField;

    private void addToOutput(String text) {
        if (!text.isBlank())
            outputField.setText(text);
    }

    private void appendToOutput(String s) {
        if (!s.isBlank())
            outputField.appendText("\n" + s);
    }
}

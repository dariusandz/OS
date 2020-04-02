package com.mif.rm;

import com.mif.FXModel.CommandTableRow;
import com.mif.FXModel.DeviceTableRow;
import com.mif.FXModel.MemoryTableRow;
import com.mif.FXModel.PagingTableRow;
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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
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
    // TODO reikia padaryt kad butu GUI galima pakeisti registru reiksmes ir atminti :(
    // TODO Leistų užkrauti kitas vartotojų programas ir valdymas būtų atiduodamas vėliausiai užkrautajai (jei laisvos atminties nepakanka apie tai pranešama)...... cia :(((((
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
            String command = currentVm.processCommand();

            if (!processInterrupts())
                cleanupVm(currentVm);

            updateUI();
        }
    }

    private boolean processInterrupts() {
        Pair<Integer, String> siValuePair = processor.processSIValue(currentVm.virtualMemory);
        if(siValuePair != null){
            if(siValuePair.getKey() == 3) {
                return false;
            }
            if(siValuePair.getKey() == 5){
                run(siValuePair.getValue());
                processor.SI.setValue(0);
            }
            if(siValuePair.getKey() == 1) {
                addToOutput(siValuePair.getValue());
            }
            if(siValuePair.getKey() ==  2 || siValuePair.getKey() == 4){
                addToOutput(siValuePair.getValue());
            }
            if(siValuePair.getValue().contains("Error")) {
                addToOutput(siValuePair.getValue());
                return false;
            }
        }
        if(processor.processTIValue() != null) {
            addToOutput(processor.processTIValue());
            return false;
        }
        if(processor.processPIValue() != null) {
            addToOutput(processor.processPIValue());
            return false;
        }
        return true;
    }

    private void cleanupVm(VirtualMachine vm) {
        vm.freeMemory();
        vmCommands.remove(vm.getId());
        virtualMachines.remove(vm);
        currentVm = null;
        appendToOutput("Virtuali masina su id [" + idOfRunningMachine + "] baige darba.");
        idOfRunningMachine = Long.valueOf(-1);
        processor.resetRegisterValues();
        if(virtualMachines.size() > 0) {
            this.currentVm = virtualMachines.get(0);
            this.idOfRunningMachine = virtualMachines.get(0).getId();
        }
        cleanupUI();
    }

    private void cleanupUI() {
        commandTable.getItems().clear();
        commandTable.getColumns().clear();
        vmChoiceBox.getItems().clear();
    }

    private void run(String runCommand) {
        if(!initializeVirtualMachine(runCommand))
            return;
        renderCommandTable();
        refreshMemoryTable();
        refreshRegisters();
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

    private boolean initializeVirtualMachine(String command) {
        String[] splitCommand = command.split(" ");
        List<String> params = getParams(splitCommand);
        String programFileName = splitCommand[0];

        VirtualMachine virtualMachine = new VirtualMachine(programFileName, params);
        Long id = virtualMachine.getId();
        List<String> virtualMachinesCommands = virtualMachine.loadProgram();
        if(virtualMachinesCommands == null){
            addToOutput("Invalid file");
            cleanupVm(virtualMachine);
            return false;
        }
        this.vmCommands.put(id, virtualMachinesCommands);
        this.virtualMachines.add(virtualMachine);
        // TODO temporary, kol gali veikti tik viena VM
        if(this.currentVm == null) {
            this.idOfRunningMachine = virtualMachine.getId();
            this.currentVm = virtualMachine;
        }
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
        setNextCommand();
        refreshVirtualMachineMemoryTable();
    }

    private void refreshVirtualMachineMemoryTable() {
        //TODO butu gerai paupdatint cia VM memory.
        // jei per nauja atsidarai langa tai pasupdateina, bet butu gerai ir kiekvieno stepo metu suupdatintu
        // bet ne priority (aktualu kai steka ziuri
    }

    private void refreshMemoryTable() {
        for (int i = 0; i < memoryTableRows.size(); i++) {
            MemoryTableRow memoryTableRow = memoryTableRows.get(i);
            for (int j = 0; j < memoryTableRow.wordHexValues.length; j++) {
                memoryTableRow.setWord(j, ByteUtil.bytesToHex(memory.getWord(i,j)));
            }
            memoryTableRows.set(i,memoryTableRow);
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
            virtualMachines.forEach(vm -> {
                vmChoiceBox.getItems().add("VM " + vm.getId().toString());
            });
        }
    }

    List<DeviceTableRow> deviceTableRows = new ArrayList<>();

    // NOT SURE GOING SLEEP
    private void refreshDeviceTable() {
        processor.devices.forEach(device -> {
            if (!deviceTableRows.stream().anyMatch(row -> row.getId() == device.getId())) {
                DeviceTableRow deviceTableRow = new DeviceTableRow(device);
                deviceTableRows.add(deviceTableRow);
                deviceTable.getItems().add(deviceTableRow);
            }
        });
    }

    @FXML
    private HBox deviceTableContainer;

    @FXML
    private TableView deviceTable;

    private void renderDeviceTable() {
        TableColumn<DeviceTableRow, String> deviceTypeCol = new TableColumn<>("Device");
        deviceTypeCol.setCellValueFactory(new PropertyValueFactory<>("Type"));

        TableColumn<DeviceTableRow, Integer> deviceValueCol = new TableColumn<>("Value");
        deviceValueCol.setCellValueFactory(new PropertyValueFactory<>("Value"));

        TableColumn<DeviceTableRow, String> deviceStateCol = new TableColumn<>("State");
        deviceStateCol.setCellValueFactory(new PropertyValueFactory<>("State"));

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

        String color;
        for (int col = 0; col < Memory.pageSize; col++) {
            TableColumn<PagingTableRow, Integer> column = new TableColumn<>(String.valueOf(col));
            column.setCellValueFactory(FXUtil.createArrayValueFactory(PagingTableRow::getRmPages, col));
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

        TableColumn<MemoryTableRow, Integer> indexCol = new TableColumn<>("Page");
        indexCol.setCellValueFactory(new PropertyValueFactory<>("pageNumber"));
        indexCol.setEditable(false);
        tableView.getColumns().add(indexCol);

        String color;
        for (Map.Entry<Integer, Integer> entry : vm.virtualMemory.pagingTable.pageMap.entrySet()) {
            MemoryTableRow memoryTableRow = new MemoryTableRow(entry.getValue());

            TableColumn<MemoryTableRow, String> column = new TableColumn<>(entry.getKey().toString());
            column.setCellValueFactory(FXUtil.createArrayValueFactory(MemoryTableRow::getValues, entry.getKey()));
            column.setSortable(false);
            column.setEditable(false);
            tableView.getColumns().add(column);

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
        renderRealMemory();
        renderDeviceTable();
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
        else if (outputField.getText().contains("Type")) {
            processSCANCommand(input);
        }
    }

    private void processSCANCommand(String input) {
        int counter = 0;
        String symbolNumber = "";
        while(outputField.getText().charAt(8 + counter) != ' ') {
            symbolNumber = symbolNumber.concat(String.valueOf(outputField.getText().charAt(8 + counter)));
            counter++;
        }
        while (input.length() - 15 != Integer.valueOf(symbolNumber)) { // 15 - "Keyboard input: " length
            addToOutput(outputField.getText());
        }
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        virtualMachines.get(0).virtualMemory.putBytesToMemory(Processor.AX.getByteValue()[2], Processor.AX.getByteValue()[3], bytes, bytes.length);
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
                    readonlyRegister.register.getHexValue()
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

    private void renderRealMemory() {
        TableColumn<MemoryTableRow, Integer> indexCol = new TableColumn<>("Page");
        indexCol.setCellValueFactory(new PropertyValueFactory<>("pageNumber"));
        memoryTable.getColumns().add(indexCol);

        memoryTable.setPrefWidth(memoryTablePane.getPrefWidth());

        for (int row = 0; row < Memory.defaultMemorySize / (Memory.pageSize * Memory.wordLen); row++) {
            MemoryTableRow memoryTableRow = new MemoryTableRow(row);
            for (int column = 0; column < Memory.pageSize; column++) {
                memoryTableRow.add(column, memory.getWord(row, column));
            }
            memoryTableRows.add(memoryTableRow);
            memoryTable.getItems().add(memoryTableRow);
        }

        for (int col = 0; col < Memory.pageSize; col++) {
            TableColumn<MemoryTableRow, String> column = new TableColumn<>(String.valueOf(col));
            column.setCellValueFactory(FXUtil.createArrayValueFactory(MemoryTableRow::getValues, col));
            column.setSortable(false);
            column.setEditable(false);
            memoryTable.getColumns().add(column);
        }

        FXUtil.fitChildrenToContainer(memoryTablePane);
        FXUtil.autoResizeColumnsOnTextSize(memoryTable);
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

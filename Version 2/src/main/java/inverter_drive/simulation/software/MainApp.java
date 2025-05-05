package inverter_drive.simulation.software;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.geometry.Insets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainApp extends Application {
    private InverterPowerStage inverter;
    private VfController controller;
    private InductionMotor motor;
    private SensorModel sensors;
    private FaultSimulator faultSimulator;
    private DataLogger dataLogger;
    private WaveformVisualizer visualizer;
    private double simulationTime = 0.0;
    private boolean isRunning = false;
    private ScheduledExecutorService simulationExecutor;
    private Text speedDisplay;
    private Text torqueDisplay;
    private Text faultDisplay;
    private Text motorTempDisplay;
    private Text inverterTempDisplay;
    private Slider dcLinkSlider;
    private Slider speedRefSlider;
    private Slider accelRateSlider;
    private ComboBox<String> controlModeCombo;
    private ToggleButton directionToggle;
    private ToggleButton enableToggle;
    private ComboBox<String> faultTypeCombo;
    private Button faultClearBtn;
    private ToggleButton autoResetToggle;
    private Slider torqueRefSlider;
    private Slider fluxRefSlider;
    private TextField kpInput;
    private TextField kiInput;
    private TextField ratedVoltageInput;
    private TextField ratedPowerInput;
    private TextField polePairsInput;
    private TextField resistanceInput;
    private TextField inductanceInput;
    private ComboBox<String> loadTypeCombo;
    private Slider loadInertiaSlider;
    private Slider dampingSlider;
    private Slider shaftInertiaSlider;
    private Slider frictionSlider;
    private Slider tempCoeffSlider;
    private Slider couplingStiffnessSlider;
    private Slider pwmFreqSlider;
    private ComboBox<String> pwmTypeCombo;
    private Slider deadTimeSlider;
    private Slider modIndexSlider;
    private ToggleButton harmonicToggle;
    private ToggleButton overmodToggle;
    private Slider fanSpeedSlider;
    private Slider coolantFlowSlider;
    private TextField maxTempInput;
    private ComboBox<String> thermalProtectionCombo;
    private TextField motorThermalResInput;
    private TextField motorThermalCapInput;
    private TextField inverterThermalResInput;
    private TextField inverterThermalCapInput;

    @Override
    public void start(Stage primaryStage) {
        inverter = new InverterPowerStage(400.0, 10000, 1e-6, 0.8, false, false);
        motor = new InductionMotor();
        controller = new VfController(motor);
        sensors = new SensorModel();
        faultSimulator = new FaultSimulator(inverter, sensors);
        dataLogger = new DataLogger();
        visualizer = new WaveformVisualizer();
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #000000;");
        VBox controlPanel = new VBox(5);
        controlPanel.setStyle("-fx-background-color: linear-gradient(to bottom, #000000, #003366); " +
                "-fx-padding: 10; -fx-border-color: #555555; -fx-border-width: 2;");
        controlPanel.setPrefWidth(400);
        Label title = new Label("Inverter Drive Control");
        title.setFont(Font.font("Verdana", 14));
        title.setStyle("-fx-text-fill: #ffffff; -fx-padding: 5;");
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: #20B2AA; -fx-border-color: #555555;");
        Tab controlTab = new Tab("Control");
        controlTab.setClosable(false);
        GridPane controlGrid = new GridPane();
        controlGrid.setHgap(5);
        controlGrid.setVgap(5);
        controlGrid.setPadding(new Insets(5));
        dcLinkSlider = createSlider("DC-Link (V)", 100, 600, 400, controlGrid, 0);
        speedRefSlider = createSlider("Speed Ref (rad/s)", 0, 300, 100, controlGrid, 1);
        accelRateSlider = createSlider("Accel (rad/s²)", 0, 50, 10, controlGrid, 2);
        torqueRefSlider = createSlider("Torque Ref (Nm)", 0, 100, 50, controlGrid, 3);
        fluxRefSlider = createSlider("Flux Ref (Wb)", 0.5, 1.5, 1.0, controlGrid, 4);
        controlModeCombo = new ComboBox<>();
        controlModeCombo.getItems().addAll("V/f", "FOC", "DTC");
        controlModeCombo.setValue("V/f");
        controlModeCombo.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #20B2AA; -fx-pref-width: 100;");
        controlGrid.add(new Label("Control Mode:"), 0, 5);
        controlGrid.add(controlModeCombo, 1, 5);
        faultTypeCombo = new ComboBox<>();
        faultTypeCombo.getItems().addAll("None", "Overcurrent", "Undervoltage", "Phase Loss", "Overheat", "IGBTFailure");
        faultTypeCombo.setValue("None");
        faultTypeCombo.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #ffffff; -fx-pref-width: 100;");
        controlGrid.add(new Label("Fault Type:"), 0, 6);
        controlGrid.add(faultTypeCombo, 1, 6);
        kpInput = createTextField("Kp", "0.1", controlGrid, 7);
        kiInput = createTextField("Ki", "0.01", controlGrid, 8);
        directionToggle = new ToggleButton("FWD");
        directionToggle.setStyle("-fx-background-color: #DC143C; -fx-text-fill: #ffffff; -fx-padding: 3 8; -fx-font-size: 10;");
        directionToggle.setOnAction(e -> directionToggle.setText(directionToggle.isSelected() ? "REV" : "FWD"));
        enableToggle = new ToggleButton("Start");
        enableToggle.setStyle("-fx-background-color: #DC143C; -fx-text-fill: #ffffff; -fx-padding: 3 8; -fx-font-size: 10;");
        String buttonStyle = "-fx-background-color: #DC143C; -fx-text-fill: #ffffff; -fx-padding: 3 8; -fx-font-size: 10; -fx-background-radius: 5;";
        String buttonHoverStyle = "-fx-background-color: #888888;";
        Button faultBtn = new Button("Fault");
        faultBtn.setStyle(buttonStyle);
        faultBtn.setOnMouseEntered(e -> faultBtn.setStyle(buttonStyle + buttonHoverStyle));
        faultBtn.setOnMouseExited(e -> faultBtn.setStyle(buttonStyle));
        faultClearBtn = new Button("Clear");
        faultClearBtn.setStyle(buttonStyle);
        faultClearBtn.setOnMouseEntered(e -> faultClearBtn.setStyle(buttonStyle + buttonHoverStyle));
        faultClearBtn.setOnMouseExited(e -> faultClearBtn.setStyle(buttonStyle));
        autoResetToggle = new ToggleButton("Auto");
        autoResetToggle.setStyle("-fx-background-color: #DC143C; -fx-text-fill: #ffffff; -fx-padding: 3 8; -fx-font-size: 10;");
        autoResetToggle.setOnAction(e -> autoResetToggle.setText(autoResetToggle.isSelected() ? "Auto On" : "Auto"));
        HBox buttonBox = new HBox(5, directionToggle, enableToggle, faultBtn, faultClearBtn, autoResetToggle);
        controlGrid.add(buttonBox, 0, 9, 3, 1);
        controlTab.setContent(controlGrid);
        Tab motorTab = new Tab("Motor/Load");
        motorTab.setClosable(false);
        GridPane motorGrid = new GridPane();
        motorGrid.setHgap(5);
        motorGrid.setVgap(5);
        motorGrid.setPadding(new Insets(5));
        ratedVoltageInput = createTextField("Voltage (V)", "230", motorGrid, 0);
        ratedPowerInput = createTextField("Power (kW)", "5", motorGrid, 1);
        polePairsInput = createTextField("Poles", "2", motorGrid, 2);
        resistanceInput = createTextField("R (Ω)", "0.5", motorGrid, 3);
        inductanceInput = createTextField("L (H)", "0.01", motorGrid, 4);
        loadTypeCombo = new ComboBox<>();
        loadTypeCombo.getItems().addAll("Constant", "Fan/Pump", "Inertia");
        loadTypeCombo.setValue("Constant");
        loadTypeCombo.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #ffffff; -fx-pref-width: 100;");
        motorGrid.add(new Label("Load Type:"), 0, 5);
        motorGrid.add(loadTypeCombo, 1, 5);
        loadInertiaSlider = createSlider("Load J (kg·m²)", 0.01, 1, 0.1, motorGrid, 6);
        dampingSlider = createSlider("Damping (Nm·s/rad)", 0, 0.1, 0.01, motorGrid, 7);
        shaftInertiaSlider = createSlider("Shaft J (kg·m²)", 0.01, 0.5, 0.05, motorGrid, 8);
        frictionSlider = createSlider("Friction (Nm·s/rad)", 0, 0.05, 0.01, motorGrid, 9);
        tempCoeffSlider = createSlider("Temp Coeff (/°C)", 0, 0.01, 0.005, motorGrid, 10);
        couplingStiffnessSlider = createSlider("Stiffness (N·m/rad)", 1000, 10000, 5000, motorGrid, 11);
        motorTab.setContent(motorGrid);
        Tab pwmTab = new Tab("PWM");
        pwmTab.setClosable(false);
        GridPane pwmGrid = new GridPane();
        pwmGrid.setHgap(5);
        pwmGrid.setVgap(5);
        pwmGrid.setPadding(new Insets(5));
        pwmFreqSlider = createSlider("PWM Freq (kHz)", 2, 20, 10, pwmGrid, 0);
        pwmTypeCombo = new ComboBox<>();
        pwmTypeCombo.getItems().addAll("SPWM", "SVPWM");
        pwmTypeCombo.setValue("SPWM");
        pwmTypeCombo.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #ffffff; -fx-pref-width: 100;");
        pwmGrid.add(new Label("PWM Type:"), 0, 1);
        pwmGrid.add(pwmTypeCombo, 1, 1);
        deadTimeSlider = createSlider("Dead Time (µs)", 0, 5, 1, pwmGrid, 2);
        modIndexSlider = createSlider("Mod Index", 0.1, 1.0, 0.8, pwmGrid, 3);
        harmonicToggle = new ToggleButton("Harm");
        harmonicToggle.setStyle("-fx-background-color: #DC143C; -fx-text-fill: #ffffff; -fx-padding: 3 8; -fx-font-size: 10;");
        harmonicToggle.setOnAction(e -> harmonicToggle.setText(harmonicToggle.isSelected() ? "Harm On" : "Harm"));
        overmodToggle = new ToggleButton("Over");
        overmodToggle.setStyle("-fx-background-color: #DC143C; -fx-text-fill: #ffffff; -fx-padding: 3 8; -fx-font-size: 10;");
        overmodToggle.setOnAction(e -> overmodToggle.setText(overmodToggle.isSelected() ? "Over On" : "Over"));
        HBox pwmButtonBox = new HBox(5, harmonicToggle, overmodToggle);
        pwmGrid.add(pwmButtonBox, 0, 4, 3, 1);
        pwmTab.setContent(pwmGrid);
        Tab thermalTab = new Tab("Thermal");
        thermalTab.setClosable(false);
        GridPane thermalGrid = new GridPane();
        thermalGrid.setHgap(5);
        thermalGrid.setVgap(5);
        thermalGrid.setPadding(new Insets(5));
        fanSpeedSlider = createSlider("Fan Speed (%)", 0, 100, 50, thermalGrid, 0);
        coolantFlowSlider = createSlider("Coolant Flow (L/min)", 0, 10, 5, thermalGrid, 1);
        maxTempInput = createTextField("Max Temp (°C)", "150", thermalGrid, 2);
        motorThermalResInput = createTextField("Motor Therm Res (°C/W)", String.valueOf(Config.MOTOR_THERMAL_RESISTANCE), thermalGrid, 3);
        motorThermalCapInput = createTextField("Motor Therm Cap (J/°C)", String.valueOf(Config.MOTOR_THERMAL_CAPACITANCE), thermalGrid, 4);
        inverterThermalResInput = createTextField("Inv Therm Res (°C/W)", String.valueOf(Config.INVERTER_THERMAL_RESISTANCE), thermalGrid, 5);
        inverterThermalCapInput = createTextField("Inv Therm Cap (J/°C)", String.valueOf(Config.INVERTER_THERMAL_CAPACITANCE), thermalGrid, 6);
        thermalProtectionCombo = new ComboBox<>();
        thermalProtectionCombo.getItems().addAll("None", "Warning", "Shutdown");
        thermalProtectionCombo.setValue("Warning");
        thermalProtectionCombo.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #ffffff; -fx-pref-width: 100;");
        thermalGrid.add(new Label("Protection Mode:"), 0, 7);
        thermalGrid.add(thermalProtectionCombo, 1, 7);
        thermalTab.setContent(thermalGrid);
        tabPane.getTabs().addAll(controlTab, motorTab, pwmTab, thermalTab);
        Pane displayPane = new Pane();
        displayPane.setStyle("-fx-background-color: #111111; -fx-border-color: #555555; -fx-border-width: 2; -fx-padding: 8;");
        speedDisplay = new Text(8, 16, "Speed: 0.0 rad/s");
        torqueDisplay = new Text(8, 32, "Torque: 0.0 Nm");
        faultDisplay = new Text(8, 48, "Fault: None");
        motorTempDisplay = new Text(8, 64, "Motor Temp: 25.0 °C");
        inverterTempDisplay = new Text(8, 80, "Inv Temp: 25.0 °C");
        for (Text text : new Text[]{speedDisplay, torqueDisplay, faultDisplay, motorTempDisplay, inverterTempDisplay}) {
            text.setFont(Font.font("Courier New", 12));
            text.setFill(javafx.scene.paint.Color.LIME);
        }
        displayPane.getChildren().addAll(speedDisplay, torqueDisplay, faultDisplay, motorTempDisplay, inverterTempDisplay);
        controlPanel.getChildren().addAll(title, tabPane, displayPane);
        VBox waveformArea = new VBox(5);
        waveformArea.setStyle("-fx-padding: 10;");
        Label waveformLabel = new Label("Waveforms");
        waveformLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 12;");
        waveformArea.getChildren().addAll(waveformLabel, visualizer.getCanvas());
        root.setLeft(controlPanel);
        root.setCenter(waveformArea);
        simulationExecutor = Executors.newScheduledThreadPool(1);
        enableToggle.setOnAction(e -> {
            if (enableToggle.isSelected()) {
                isRunning = true;
                enableToggle.setText("Stop");
                simulationExecutor.scheduleAtFixedRate(this::simulateStep, 0,
                        (long)(Config.SIMULATION_TIME_STEP * 1000 * 1000), TimeUnit.MICROSECONDS);
            } else {
                isRunning = false;
                enableToggle.setText("Start");
                simulationExecutor.shutdownNow();
                try {
                    simulationExecutor.awaitTermination(1, TimeUnit.SECONDS);
                } catch (InterruptedException ignored) {}
                simulationExecutor = Executors.newScheduledThreadPool(1);
            }
        });
        faultBtn.setOnAction(e -> {
            faultSimulator.injectFault(faultTypeCombo.getValue());
            faultDisplay.setText("Fault: " + faultTypeCombo.getValue());
        });
        faultClearBtn.setOnAction(e -> {
            faultSimulator.clearFault();
            faultDisplay.setText("Fault: None");
            faultTypeCombo.setValue("None");
        });
        updateMotorParameters();
        Scene scene = new Scene(root, 1100, 615);
        primaryStage.setTitle("Inverter Drive Simulation Software");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        Platform.runLater(() -> {
            String thumbStyle = "-fx-background-color: #aaaaaa; -fx-padding: 6;";
            String thumbHoverStyle = "-fx-background-color: #FFFF00;";
            for (Slider slider : new Slider[]{dcLinkSlider, speedRefSlider, accelRateSlider, torqueRefSlider, fluxRefSlider,
                    loadInertiaSlider, dampingSlider, shaftInertiaSlider, frictionSlider,
                    tempCoeffSlider, couplingStiffnessSlider, pwmFreqSlider, deadTimeSlider,
                    modIndexSlider, fanSpeedSlider, coolantFlowSlider}) {
                if (slider.lookup(".thumb") != null) {
                    slider.lookup(".thumb").setStyle(thumbStyle);
                    slider.lookup(".thumb").setOnMouseEntered(e -> slider.lookup(".thumb").setStyle(thumbStyle + thumbHoverStyle));
                    slider.lookup(".thumb").setOnMouseExited(e -> slider.lookup(".thumb").setStyle(thumbStyle));
                }
            }
        });
        primaryStage.show();
    }

    private Slider createSlider(String label, double min, double max, double value, GridPane grid, int row) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 10;");
        Slider slider = new Slider(min, max, value);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setStyle("-fx-control-inner-background: #000000; -fx-pref-width: 150;");
        TextField input = new TextField(String.format("%.2f", value));
        input.setStyle("-fx-background-color: #222222; -fx-text-fill: #ffffff; -fx-border-color: #555555; -fx-border-width: 1; -fx-font-size: 10; -fx-pref-width: 50;");
        input.focusedProperty().addListener((obs, old, newVal) -> {
            if (newVal) {
                input.setStyle("-fx-background-color: #222222; -fx-text-fill: #ffffff; -fx-border-color: #aaaaaa; -fx-border-width: 1; -fx-font-size: 10; -fx-pref-width: 50;");
            } else {
                input.setStyle("-fx-background-color: #222222; -fx-text-fill: #ffffff; -fx-border-color: #555555; -fx-border-width: 1; -fx-font-size: 10; -fx-pref-width: 50;");
            }
        });
        slider.valueProperty().addListener((obs, old, newVal) -> input.setText(String.format("%.2f", newVal)));
        input.textProperty().addListener((obs, old, newVal) -> {
            try {
                double val = Double.parseDouble(newVal);
                if (val >= min && val <= max) {
                    slider.setValue(val);
                } else {
                    showError("Invalid Input", label + " must be between " + min + " and " + max);
                    input.setText(String.format("%.2f", slider.getValue()));
                }
            } catch (NumberFormatException e) {
                showError("Invalid Input", label + " must be a number");
                input.setText(String.format("%.2f", slider.getValue()));
            }
        });
        grid.add(lbl, 0, row);
        grid.add(slider, 1, row);
        grid.add(input, 2, row);
        return slider;
    }

    private TextField createTextField(String label, String defaultValue, GridPane grid, int row) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 10;");
        TextField field = new TextField(defaultValue);
        field.setStyle("-fx-background-color: #222222; -fx-text-fill: #ffffff; -fx-border-color: #555555; -fx-border-width: 1; -fx-font-size: 10; -fx-pref-width: 100;");
        field.focusedProperty().addListener((obs, old, newVal) -> {
            if (newVal) {
                field.setStyle("-fx-background-color: #222222; -fx-text-fill: #ffffff; -fx-border-color: #aaaaaa; -fx-border-width: 1; -fx-font-size: 10; -fx-pref-width: 100;");
            } else {
                field.setStyle("-fx-background-color: #222222; -fx-text-fill: #ffffff; -fx-border-color: #555555; -fx-border-width: 1; -fx-font-size: 10; -fx-pref-width: 100;");
            }
        });
        field.textProperty().addListener((obs, old, newVal) -> {
            try {
                Double.parseDouble(newVal);
                updateMotorParameters();
            } catch (NumberFormatException e) {
                showError("Invalid Input", label + " must be a number");
                field.setText(defaultValue);
            }
        });
        grid.add(lbl, 0, row);
        grid.add(field, 1, row);
        return field;
    }

    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void updateMotorParameters() {
        try {
            motor.setParameters(
                    Double.parseDouble(ratedVoltageInput.getText()),
                    Double.parseDouble(ratedPowerInput.getText()),
                    Integer.parseInt(polePairsInput.getText()),
                    Double.parseDouble(resistanceInput.getText()),
                    Double.parseDouble(inductanceInput.getText()),
                    loadTypeCombo.getValue(),
                    loadInertiaSlider.getValue(),
                    dampingSlider.getValue(),
                    shaftInertiaSlider.getValue(),
                    frictionSlider.getValue(),
                    tempCoeffSlider.getValue(),
                    couplingStiffnessSlider.getValue(),
                    fanSpeedSlider.getValue() / 100.0,
                    coolantFlowSlider.getValue()
            );
        } catch (NumberFormatException e) {
            showError("Invalid Motor Parameters", "All motor parameters must be valid numbers");
        }
    }

    private void simulateStep() {
        if (!isRunning || !enableToggle.isSelected()) {
            Platform.runLater(() -> {
                enableToggle.setText("Start");
                enableToggle.setSelected(false);
            });
            return;
        }
        Platform.runLater(() -> {
            inverter.setDcLinkVoltage(dcLinkSlider.getValue());
            inverter.setPwmFrequency(pwmFreqSlider.getValue() * 1000);
            inverter.setDeadTime(deadTimeSlider.getValue() * 1e-6);
            inverter.setModulationIndex(modIndexSlider.getValue());
            inverter.setHarmonicInjection(harmonicToggle.isSelected());
            inverter.setOvermodulation(overmodToggle.isSelected());
            inverter.setCooling(fanSpeedSlider.getValue() / 100.0, coolantFlowSlider.getValue());

            try {
                controller.setGains(
                        Double.parseDouble(kpInput.getText()),
                        Double.parseDouble(kiInput.getText())
                );
            } catch (NumberFormatException e) {
                showError("Invalid Gains", "Kp and Ki must be valid numbers");
                return;
            }
            double[] pwmSignals = controller.updateControl(
                    controlModeCombo.getValue(),
                    speedRefSlider.getValue(),
                    torqueRefSlider.getValue(),
                    fluxRefSlider.getValue(),
                    accelRateSlider.getValue(),
                    directionToggle.isSelected() ? -1 : 1,
                    simulationTime
            );
            double[] phaseVoltages = inverter.generatePhaseVoltages(pwmSignals, pwmTypeCombo.getValue());
            double maxTemp;
            try {
                maxTemp = Double.parseDouble(maxTempInput.getText());
            } catch (NumberFormatException e) {
                showError("Invalid Temperature", "Max Temp must be a valid number");
                return;
            }
            phaseVoltages = faultSimulator.applyFaults(phaseVoltages, autoResetToggle.isSelected(),
                    motor.getTemperature(), inverter.getTemperature(),
                    maxTemp, thermalProtectionCombo.getValue());
            double[] phaseCurrents = sensors.measureCurrents(phaseVoltages, motor);
            motor.updateState(phaseVoltages, phaseCurrents, loadTypeCombo.getValue(), Config.SIMULATION_TIME_STEP);
            speedDisplay.setText(String.format("Speed: %.1f rad/s", motor.getSpeed()));
            torqueDisplay.setText(String.format("Torque: %.1f Nm", motor.getTorque()));
            faultDisplay.setText("Fault: " + faultSimulator.getCurrentFault());
            motorTempDisplay.setText(String.format("Motor Temp: %.1f °C", motor.getTemperature()));
            inverterTempDisplay.setText(String.format("Inv Temp: %.1f °C", inverter.getTemperature()));
            synchronized (dataLogger) {
                dataLogger.logData(simulationTime, phaseVoltages, phaseCurrents, motor.getSpeed(), motor.getTorque());
            }
            synchronized (visualizer) {
                visualizer.updateWaveforms(phaseVoltages, phaseCurrents, motor.getSpeed(), simulationTime);
            }
            simulationTime += Config.SIMULATION_TIME_STEP;
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
package inverter_drive.simulation.software;

import javafx.application.Application;
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
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.application.Platform;
import javafx.geometry.Insets;

public class MainApp extends Application {
    private InverterPowerStage inverter;
    private VfController controller;
    private InductionMotor motor;
    private SensorModel sensors;
    private FaultSimulator faultSimulator;
    private DataLogger dataLogger;
    private WaveformVisualizer visualizer;
    private double simulationTime = 0.0;
    private double timeStep = 0.001; // 1ms
    private boolean isRunning = false;
    private Text speedDisplay;
    private Text torqueDisplay;
    private Text faultDisplay;
    private Slider dcLinkSlider;
    private Slider speedRefSlider;
    private Slider accelRateSlider;
    private ComboBox<String> controlModeCombo;
    private ToggleButton directionToggle;
    private ToggleButton enableToggle;
    private TextField ratedVoltageInput;
    private TextField ratedPowerInput;
    private TextField polePairsInput;
    private TextField resistanceInput;
    private TextField inductanceInput;
    private ComboBox<String> loadTypeCombo;
    private Slider loadInertiaSlider;
    private Slider dampingSlider;
    private Slider shaftInertiaSlider;
    private Slider pwmFreqSlider;
    private ComboBox<String> pwmTypeCombo;
    private Slider deadTimeSlider;

    @Override
    public void start(Stage primaryStage) {
        inverter = new InverterPowerStage(400.0, 10000, 1e-6);
        motor = new InductionMotor();
        controller = new VfController(motor);
        sensors = new SensorModel();
        faultSimulator = new FaultSimulator(inverter, sensors);
        dataLogger = new DataLogger();
        visualizer = new WaveformVisualizer();
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #333333;");
        VBox controlPanel = new VBox(5);
        controlPanel.setStyle("-fx-background-color: linear-gradient(to bottom, #4a4a4a, #2a2a2a); " +
                "-fx-padding: 10; -fx-border-color: #555555; -fx-border-width: 2;");
        controlPanel.setPrefWidth(300);
        Label title = new Label("Inverter Drive Control");
        title.setFont(Font.font("Arial", 14));
        title.setStyle("-fx-text-fill: #ffffff; -fx-padding: 5;");
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: #2a2a2a; -fx-border-color: #555555;");
        Tab controlTab = new Tab("Control");
        controlTab.setClosable(false);
        GridPane controlGrid = new GridPane();
        controlGrid.setHgap(5);
        controlGrid.setVgap(5);
        controlGrid.setPadding(new Insets(10));
        dcLinkSlider = createSlider("DC-Link (V)", 100, 600, 400, controlGrid, 0);
        speedRefSlider = createSlider("Speed Ref (rad/s)", 0, 300, 100, controlGrid, 1);
        accelRateSlider = createSlider("Accel (rad/s²)", 0, 50, 10, controlGrid, 2);
        controlModeCombo = new ComboBox<>();
        controlModeCombo.getItems().addAll("V/f", "FOC", "DTC");
        controlModeCombo.setValue("V/f");
        controlModeCombo.setStyle("-fx-background-color: #222222; -fx-text-fill: #ffffff; -fx-pref-width: 100;");
        controlGrid.add(new Label("Control Mode:"), 0, 3);
        controlGrid.add(controlModeCombo, 1, 3);
        directionToggle = new ToggleButton("FWD");
        directionToggle.setStyle("-fx-background-color: #666666; -fx-text-fill: #ffffff; -fx-padding: 3 8; -fx-font-size: 10;");
        directionToggle.setOnAction(e -> directionToggle.setText(directionToggle.isSelected() ? "REV" : "FWD"));
        enableToggle = new ToggleButton("Start");
        enableToggle.setStyle("-fx-background-color: #666666; -fx-text-fill: #ffffff; -fx-padding: 3 8; -fx-font-size: 10;");
        String buttonStyle = "-fx-background-color: #666666; -fx-text-fill: #ffffff; -fx-padding: 3 8; -fx-font-size: 10; -fx-background-radius: 5;";
        String buttonHoverStyle = "-fx-background-color: #888888;";
        Button faultBtn = new Button("Fault");
        faultBtn.setStyle(buttonStyle);
        faultBtn.setOnMouseEntered(e -> faultBtn.setStyle(buttonStyle + buttonHoverStyle));
        faultBtn.setOnMouseExited(e -> faultBtn.setStyle(buttonStyle));
        HBox buttonBox = new HBox(5, directionToggle, enableToggle, faultBtn);
        controlGrid.add(buttonBox, 0, 4, 2, 1);
        controlTab.setContent(controlGrid);
        Tab motorTab = new Tab("Motor/Load");
        motorTab.setClosable(false);
        GridPane motorGrid = new GridPane();
        motorGrid.setHgap(5);
        motorGrid.setVgap(5);
        motorGrid.setPadding(new Insets(10));
        ratedVoltageInput = createTextField("Voltage (V)", "230", motorGrid, 0);
        ratedPowerInput = createTextField("Power (kW)", "5", motorGrid, 1);
        polePairsInput = createTextField("Poles", "2", motorGrid, 2);
        resistanceInput = createTextField("R (Ω)", "0.5", motorGrid, 3);
        inductanceInput = createTextField("L (H)", "0.01", motorGrid, 4);
        loadTypeCombo = new ComboBox<>();
        loadTypeCombo.getItems().addAll("Constant", "Fan/Pump", "Inertia");
        loadTypeCombo.setValue("Constant");
        loadTypeCombo.setStyle("-fx-background-color: #222222; -fx-text-fill: #ffffff; -fx-pref-width: 100;");
        motorGrid.add(new Label("Load Type:"), 0, 5);
        motorGrid.add(loadTypeCombo, 1, 5);
        loadInertiaSlider = createSlider("Load J (kg·m²)", 0.01, 1, 0.1, motorGrid, 6);
        dampingSlider = createSlider("Damping (Nm·s/rad)", 0, 0.1, 0.01, motorGrid, 7);
        shaftInertiaSlider = createSlider("Shaft J (kg·m²)", 0.01, 0.5, 0.05, motorGrid, 8);
        motorTab.setContent(motorGrid);
        Tab pwmTab = new Tab("PWM");
        pwmTab.setClosable(false);
        GridPane pwmGrid = new GridPane();
        pwmGrid.setHgap(5);
        pwmGrid.setVgap(5);
        pwmGrid.setPadding(new Insets(10));
        pwmFreqSlider = createSlider("PWM Freq (kHz)", 2, 20, 10, pwmGrid, 0);
        pwmTypeCombo = new ComboBox<>();
        pwmTypeCombo.getItems().addAll("SPWM", "SVPWM");
        pwmTypeCombo.setValue("SPWM");
        pwmTypeCombo.setStyle("-fx-background-color: #222222; -fx-text-fill: #ffffff; -fx-pref-width: 100;");
        pwmGrid.add(new Label("PWM Type:"), 0, 1);
        pwmGrid.add(pwmTypeCombo, 1, 1);
        deadTimeSlider = createSlider("Dead Time (µs)", 0, 5, 1, pwmGrid, 2);
        pwmTab.setContent(pwmGrid);
        tabPane.getTabs().addAll(controlTab, motorTab, pwmTab);
        Pane displayPane = new Pane();
        displayPane.setStyle("-fx-background-color: #111111; -fx-border-color: #555555; -fx-border-width: 2; -fx-padding: 8;");
        speedDisplay = new Text(8, 16, "Speed: 0.0 rad/s");
        torqueDisplay = new Text(8, 32, "Torque: 0.0 Nm");
        faultDisplay = new Text(8, 48, "Fault: None");
        for (Text text : new Text[]{speedDisplay, torqueDisplay, faultDisplay}) {
            text.setFont(Font.font("Courier New", 12));
            text.setFill(javafx.scene.paint.Color.LIME);
        }
        displayPane.getChildren().addAll(speedDisplay, torqueDisplay, faultDisplay);
        controlPanel.getChildren().addAll(title, tabPane, displayPane);
        VBox waveformArea = new VBox(5);
        waveformArea.setStyle("-fx-padding: 10;");
        Label waveformLabel = new Label("Waveforms");
        waveformLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 12;");
        waveformArea.getChildren().addAll(waveformLabel, visualizer.getCanvas());
        root.setLeft(controlPanel);
        root.setCenter(waveformArea);
        Timeline simulationLoop = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            if (isRunning && enableToggle.isSelected()) {
                simulateStep();
            } else {
                enableToggle.setText("Start");
                enableToggle.setSelected(false);
            }
        }));
        simulationLoop.setCycleCount(Timeline.INDEFINITE);
        enableToggle.setOnAction(e -> {
            if (enableToggle.isSelected()) {
                isRunning = true;
                enableToggle.setText("Stop");
                simulationLoop.play();
            } else {
                isRunning = false;
                enableToggle.setText("Start");
                simulationLoop.stop();
            }
        });
        faultBtn.setOnAction(e -> {
            faultSimulator.injectOvercurrentFault();
            faultDisplay.setText("Fault: Overcurrent");
        });
        updateMotorParameters();
        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setTitle("Inverter Drive Simulator");
        primaryStage.setScene(scene);
        Platform.runLater(() -> {
            String thumbStyle = "-fx-background-color: #aaaaaa; -fx-padding: 6;";
            String thumbHoverStyle = "-fx-background-color: #cccccc;";
            for (Slider slider : new Slider[]{dcLinkSlider, speedRefSlider, accelRateSlider, loadInertiaSlider, dampingSlider, shaftInertiaSlider, pwmFreqSlider, deadTimeSlider}) {
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
        slider.setStyle("-fx-control-inner-background: #555555; -fx-pref-width: 150;");
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
                slider.setValue(Double.parseDouble(newVal));
            } catch (NumberFormatException ignored) {}
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
        field.textProperty().addListener((obs, old, newVal) -> updateMotorParameters());
        grid.add(lbl, 0, row);
        grid.add(field, 1, row);
        return field;
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
                    shaftInertiaSlider.getValue()
            );
        } catch (NumberFormatException ignored) {}
    }

    private void simulateStep() {
        inverter.setDcLinkVoltage(dcLinkSlider.getValue());
        inverter.setPwmFrequency(pwmFreqSlider.getValue() * 1000);
        inverter.setDeadTime(deadTimeSlider.getValue() * 1e-6);
        double[] pwmSignals = controller.updateControl(
                controlModeCombo.getValue(),
                speedRefSlider.getValue(),
                accelRateSlider.getValue(),
                directionToggle.isSelected() ? -1 : 1,
                simulationTime
        );
        double[] phaseVoltages = inverter.generatePhaseVoltages(pwmSignals, pwmTypeCombo.getValue());
        phaseVoltages = faultSimulator.applyFaults(phaseVoltages);
        double[] phaseCurrents = sensors.measureCurrents(phaseVoltages, motor);
        motor.updateState(phaseVoltages, phaseCurrents, loadTypeCombo.getValue(), timeStep);
        speedDisplay.setText(String.format("Speed: %.1f rad/s", motor.getSpeed()));
        torqueDisplay.setText(String.format("Torque: %.1f Nm", motor.getTorque()));
        dataLogger.logData(simulationTime, phaseVoltages, phaseCurrents, motor.getSpeed(), motor.getTorque());
        visualizer.updateWaveforms(phaseVoltages, phaseCurrents, motor.getSpeed(), simulationTime);
        simulationTime += timeStep;
    }

    public static void main(String[] args) {

        launch(args);

    }
}
package inverter_drive.simulation.software;

public class Config {
    /// Thermal model parameters
    public static final double MOTOR_THERMAL_RESISTANCE = 0.1; // °C/W
    public static final double MOTOR_THERMAL_CAPACITANCE = 100.0; // J/°C
    public static final double INVERTER_THERMAL_RESISTANCE = 0.05; // °C/W
    public static final double INVERTER_THERMAL_CAPACITANCE = 50.0; // J/°C
    /// Simulation timing
    public static final double SIMULATION_TIME_STEP = 0.0001; // 0.1ms
    /// Sensor noise
    public static final double SENSOR_NOISE_STDDEV = 0.01; // Standard deviation for Gaussian noise
    /// Fault parameters
    public static final double OVERCURRENT_VOLTAGE_SCALE = 0.5;
    public static final double UNDERVOLTAGE_VOLTAGE_SCALE = 0.7;
    public static final double IGBT_FAILURE_DUTY_CYCLE = 0.2;
}
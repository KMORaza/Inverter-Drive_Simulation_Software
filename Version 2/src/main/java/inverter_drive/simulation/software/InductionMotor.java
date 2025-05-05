package inverter_drive.simulation.software;

public class InductionMotor {
    private double speed = 0.0;
    private double torque = 0.0;
    private double rotorFlux = 1.0;
    private double ratedVoltage = 230.0;
    private double ratedPower = 5.0;
    private double polePairs = 2;
    private double resistance = 0.5;
    private double inductance = 0.01;
    private String loadType = "Constant";
    private double loadInertia = 0.1;
    private double damping = 0.01;
    private double shaftInertia = 0.05;
    private double friction = 0.01;
    private double tempCoefficient = 0.005;
    private double couplingStiffness = 5000;
    private double temperature = 25.0; // °C
    private double fanSpeed = 0.5; // 0–1
    private double coolantFlow = 5.0; // L/min
    /// d-q axis currents
    private double id = 0.0; // Direct-axis current
    private double iq = 0.0; // Quadrature-axis current
    public void setParameters(double ratedVoltage, double ratedPower, int polePairs,
                              double resistance, double inductance, String loadType,
                              double loadInertia, double damping, double shaftInertia,
                              double friction, double tempCoefficient, double couplingStiffness,
                              double fanSpeed, double coolantFlow) {
        this.ratedVoltage = ratedVoltage;
        this.ratedPower = ratedPower;
        this.polePairs = polePairs;
        this.resistance = resistance;
        this.inductance = inductance;
        this.loadType = loadType;
        this.loadInertia = loadInertia;
        this.damping = damping;
        this.shaftInertia = shaftInertia;
        this.friction = friction;
        this.tempCoefficient = tempCoefficient;
        this.couplingStiffness = couplingStiffness;
        this.fanSpeed = fanSpeed;
        this.coolantFlow = coolantFlow;
    }
    public void updateState(double[] phaseVoltages, double[] phaseCurrents, String loadType, double timeStep) {
        /// Update thermal model
        updateTemperature(phaseCurrents);
        /// Adjust resistance for temperature
        double effectiveResistance = resistance * (1 + tempCoefficient * (temperature - 25.0));
        /// Convert to d-q axis (Clarke-Park transform)
        double vq = (2.0 / 3.0) * (phaseVoltages[0] - 0.5 * (phaseVoltages[1] + phaseVoltages[2]));
        double vd = (1.0 / Math.sqrt(3)) * (phaseVoltages[1] - phaseVoltages[2]);
        double iq = (2.0 / 3.0) * (phaseCurrents[0] - 0.5 * (phaseCurrents[1] + phaseCurrents[2]));
        double id = (1.0 / Math.sqrt(3)) * (phaseCurrents[1] - phaseCurrents[2]);
        this.iq = iq;
        this.id = id;
        /// Torque calculation (d-q model)
        torque = 1.5 * polePairs * rotorFlux * iq;
        /// Slip and rotor dynamics
        double slip = (ratedVoltage / (2 * Math.PI * 50.0) - speed / polePairs) / (ratedVoltage / (2 * Math.PI * 50.0));
        rotorFlux += timeStep * (-rotorFlux / inductance + id);
        /// Load torque
        double loadTorque = 10.0;
        if (loadType.equals("Fan/Pump")) {
            loadTorque = 0.1 * speed * speed;
        } else if (loadType.equals("Inertia")) {
            loadTorque = 0.0;
        }
        /// Mechanical dynamics
        double totalInertia = loadInertia + shaftInertia;
        double couplingEffect = couplingStiffness * speed * timeStep;
        double acceleration = (torque - loadTorque - (damping + friction) * speed - couplingEffect) / totalInertia;
        speed += acceleration * timeStep;
        if (speed < 0) speed = 0;
    }
    private void updateTemperature(double[] phaseCurrents) {
        double iSquaredR = (phaseCurrents[0] * phaseCurrents[0] +
                phaseCurrents[1] * phaseCurrents[1] +
                phaseCurrents[2] * phaseCurrents[2]) * resistance;
        double heatGeneration = iSquaredR * Config.MOTOR_THERMAL_RESISTANCE;
        double coolingEffect = (0.6 * fanSpeed + 0.4 * coolantFlow) / Config.MOTOR_THERMAL_CAPACITANCE;
        temperature += (heatGeneration - coolingEffect) * Config.SIMULATION_TIME_STEP;
        if (temperature < 25.0) temperature = 25.0;
    }
    public double getSpeed() {
        return speed;
    }
    public double getTorque() {
        return torque;
    }
    public double getResistance() {
        return resistance;
    }
    public double getInductance() {
        return inductance;
    }
    public double getTemperature() {
        return temperature;
    }
    public double getRotorFlux() {
        return rotorFlux;
    }
}
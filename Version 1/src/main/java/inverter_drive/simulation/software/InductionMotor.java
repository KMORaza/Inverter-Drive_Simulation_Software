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
        /// Torque calculation
        double electricalTorque = calculateTorque(phaseVoltages, phaseCurrents);
        /// Load torque based on load type
        double loadTorque = 10.0;
        if (loadType.equals("Fan/Pump")) {
            loadTorque = 0.1 * speed * speed;
        } else if (loadType.equals("Inertia")) {
            loadTorque = 0.0;
        }
        torque = electricalTorque;
        /// Mechanical dynamics with friction and coupling
        double totalInertia = loadInertia + shaftInertia;
        double couplingEffect = couplingStiffness * speed * 1e-6;
        double acceleration = (electricalTorque - loadTorque - (damping + friction) * speed - couplingEffect) / totalInertia;
        speed += acceleration * timeStep;
        if (speed < 0) speed = 0;
    }

    private void updateTemperature(double[] phaseCurrents) {
        /// Heat from resistive losses
        double iSquaredR = (phaseCurrents[0] * phaseCurrents[0] +
                phaseCurrents[1] * phaseCurrents[1] +
                phaseCurrents[2] * phaseCurrents[2]) * resistance;
        double heatGeneration = iSquaredR * 0.01;
        double coolingEffect = 0.6 * fanSpeed + 0.4 * coolantFlow; // Fan and coolant dissipation
        temperature += (heatGeneration - coolingEffect) * 0.001; // Thermal mass
        if (temperature < 25.0) temperature = 25.0; // Ambient temperature
    }

    private double calculateTorque(double[] phaseVoltages, double[] phaseCurrents) {
        double avgCurrent = (Math.abs(phaseCurrents[0]) + Math.abs(phaseCurrents[1]) + Math.abs(phaseCurrents[2])) / 3;
        return polePairs * rotorFlux * avgCurrent * 0.1;
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
}
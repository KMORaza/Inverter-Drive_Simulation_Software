package inverter_drive.simulation.software;
public class InductionMotor {
    private double speed = 0.0; // rad/s
    private double torque = 0.0; // Nm
    private double rotorFlux = 1.0; // flux model
    private double ratedVoltage = 230.0; // V
    private double ratedPower = 5.0; // kW
    private double polePairs = 2;
    private double resistance = 0.5; // ohms
    private double inductance = 0.01; // H
    private String loadType = "Constant";
    private double loadInertia = 0.1; // kg·m²
    private double damping = 0.01; // Nm·s/rad
    private double shaftInertia = 0.05; // kg·m²

    public void setParameters(double ratedVoltage, double ratedPower, int polePairs,
                              double resistance, double inductance, String loadType,
                              double loadInertia, double damping, double shaftInertia) {
        this.ratedVoltage = ratedVoltage;
        this.ratedPower = ratedPower;
        this.polePairs = polePairs;
        this.resistance = resistance;
        this.inductance = inductance;
        this.loadType = loadType;
        this.loadInertia = loadInertia;
        this.damping = damping;
        this.shaftInertia = shaftInertia;
    }
    public void updateState(double[] phaseVoltages, double[] phaseCurrents, String loadType, double timeStep) {
        /// Torque and speed calculation
        double electricalTorque = calculateTorque(phaseVoltages, phaseCurrents);
        /// Load torque based on load type
        double loadTorque = 10.0; // Base torque
        if (loadType.equals("Fan/Pump")) {
            loadTorque = 0.1 * speed * speed; // Quadratic
        } else if (loadType.equals("Inertia")) {
            loadTorque = 0.0; // Pure inertia
        }
        torque = electricalTorque;
        /// Mechanical dynamics: d(omega)/dt = (Te - Tl - D*omega) / (J_load + J_shaft)
        double totalInertia = loadInertia + shaftInertia;
        double acceleration = (electricalTorque - loadTorque - damping * speed) / totalInertia;
        speed += acceleration * timeStep;
        if (speed < 0) speed = 0; // Prevent negative speed
    }

    private double calculateTorque(double[] phaseVoltages, double[] phaseCurrents) {
        /// Proportional to current and flux
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
}
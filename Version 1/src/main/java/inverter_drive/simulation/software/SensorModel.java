package inverter_drive.simulation.software;

public class SensorModel {
    private boolean currentSensorFault = false;
    public double[] measureCurrents(double[] phaseVoltages, InductionMotor motor) {
        double[] currents = new double[3];
        if (!currentSensorFault) {
            for (int i = 0; i < 3; i++) {
                currents[i] = phaseVoltages[i] / (motor.getResistance() + motor.getInductance() * 0.1);
            }
        } else {
            currents = new double[] { 0, 0, 0 };
        }
        return currents;
    }
    public void setCurrentSensorFault(boolean fault) {
        this.currentSensorFault = fault;
    }
}
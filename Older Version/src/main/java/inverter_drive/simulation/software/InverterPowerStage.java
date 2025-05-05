package inverter_drive.simulation.software;

public class InverterPowerStage {
    private double dcLinkVoltage;
    private double pwmFrequency;
    private double deadTime;

    public InverterPowerStage(double dcLinkVoltage, double pwmFrequency, double deadTime) {
        this.dcLinkVoltage = dcLinkVoltage;
        this.pwmFrequency = pwmFrequency;
        this.deadTime = deadTime;
    }

    public double[] generatePhaseVoltages(double[] pwmSignals, String pwmType) {
        double[] phaseVoltages = new double[3];
        double deadTimeFactor = 1.0 - deadTime * pwmFrequency; // dead time effect
        for (int i = 0; i < 3; i++) {
            phaseVoltages[i] = pwmSignals[i] * dcLinkVoltage * deadTimeFactor;
        }
        if (pwmType.equals("SVPWM")) {
        }
        return phaseVoltages;
    }

    public void setDcLinkVoltage(double voltage) {

        this.dcLinkVoltage = voltage;

    }

    public void setPwmFrequency(double frequency) {

        this.pwmFrequency = frequency;

    }

    public void setDeadTime(double time) {

        this.deadTime = time;

    }

    public double getDcLinkVoltage() {

        return dcLinkVoltage;

    }
}
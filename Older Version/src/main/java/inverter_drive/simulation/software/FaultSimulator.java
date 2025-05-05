package inverter_drive.simulation.software;

public class FaultSimulator {
    private InverterPowerStage inverter;
    private SensorModel sensors;
    private boolean overcurrentFault = false;
    public FaultSimulator(InverterPowerStage inverter, SensorModel sensors) {
        this.inverter = inverter;
        this.sensors = sensors;
    }
    public double[] applyFaults(double[] phaseVoltages) {
        if (overcurrentFault) {
            for (int i = 0; i < 3; i++) {
                phaseVoltages[i] = Math.min(phaseVoltages[i], inverter.getDcLinkVoltage() * 0.1);
            }
        }
        return phaseVoltages;
    }
    public void injectOvercurrentFault() {

        overcurrentFault = true;

    }
}
package inverter_drive.simulation.software;

public class InverterPowerStage {
    private double dcLinkVoltage;
    private double pwmFrequency;
    private double deadTime;
    private double modulationIndex;
    private boolean harmonicInjection;
    private boolean overmodulation;
    private double temperature = 25.0; // °C
    private double fanSpeed = 0.5; // 0–1
    private double coolantFlow = 5.0; // L/min

    public InverterPowerStage(double dcLinkVoltage, double pwmFrequency, double deadTime,
                              double modulationIndex, boolean harmonicInjection, boolean overmodulation) {
        this.dcLinkVoltage = dcLinkVoltage;
        this.pwmFrequency = pwmFrequency;
        this.deadTime = deadTime;
        this.modulationIndex = modulationIndex;
        this.harmonicInjection = harmonicInjection;
        this.overmodulation = overmodulation;
    }

    public double[] generatePhaseVoltages(double[] pwmSignals, String pwmType) {
        double[] phaseVoltages = new double[3];
        double deadTimeFactor = 1.0 - deadTime * pwmFrequency;
        double modFactor = modulationIndex * (overmodulation ? 1.15 : 1.0);
        for (int i = 0; i < 3; i++) {
            double signal = pwmSignals[i];
            if (harmonicInjection) {
                signal += 0.1 * Math.sin(3 * Math.PI * pwmFrequency * System.currentTimeMillis() / 1000.0);
            }
            phaseVoltages[i] = signal * dcLinkVoltage * deadTimeFactor * modFactor;
        }
        if (pwmType.equals("SVPWM")) {
        }
        updateTemperature(phaseVoltages);
        return phaseVoltages;
    }

    private void updateTemperature(double[] phaseVoltages) {
        double switchingLosses = pwmFrequency * 0.0001 * dcLinkVoltage;
        double heatGeneration = switchingLosses * Config.INVERTER_THERMAL_RESISTANCE;
        double coolingEffect = (0.5 * fanSpeed + 0.3 * coolantFlow) / Config.INVERTER_THERMAL_CAPACITANCE;
        temperature += (heatGeneration - coolingEffect) * Config.SIMULATION_TIME_STEP;
        if (temperature < 25.0) temperature = 25.0;
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

    public void setModulationIndex(double index) {
        this.modulationIndex = index;
    }

    public void setHarmonicInjection(boolean enabled) {
        this.harmonicInjection = enabled;
    }

    public void setOvermodulation(boolean enabled) {
        this.overmodulation = enabled;
    }

    public void setCooling(double fanSpeed, double coolantFlow) {
        this.fanSpeed = fanSpeed;
        this.coolantFlow = coolantFlow;
    }

    public double getDcLinkVoltage() {
        return dcLinkVoltage;
    }

    public double getTemperature() {
        return temperature;
    }
}
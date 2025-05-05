package inverter_drive.simulation.software;

public class VfController {
    private InductionMotor motor;
    private double vPerHz;
    private double maxVoltage = 230;
    private double currentSpeed = 0.0;
    private double kp = 0.1;
    private double ki = 0.01;
    private double integralError = 0.0;

    public VfController(InductionMotor motor) {
        this.motor = motor;
        this.vPerHz = maxVoltage / 50.0;
    }

    public void setGains(double kp, double ki) {
        this.kp = kp;
        this.ki = ki;
    }

    public double[] updateControl(String mode, double speedRef, double torqueRef, double fluxRef,
                                  double accelRate, double direction, double time) {
        if (mode.equals("FOC") || mode.equals("DTC")) {
            return new double[]{0, 0, 0};
        }
        /// V/f control with PI speed control
        double speedError = speedRef - motor.getSpeed();
        integralError += speedError * 0.016;
        double freq = kp * speedError + ki * integralError;
        /// Apply acceleration/deceleration rate
        double maxFreqChange = accelRate * 0.016 / (2 * Math.PI);
        freq = Math.max(Math.min(freq, currentSpeed + maxFreqChange), currentSpeed - maxFreqChange);
        currentSpeed = freq;
        double voltage = freq * vPerHz;
        double omega = 2 * Math.PI * freq * direction;
        double[] pwmSignals = new double[3];
        pwmSignals[0] = 0.5 * (1 + Math.sin(omega * time));
        pwmSignals[1] = 0.5 * (1 + Math.sin(omega * time - 2 * Math.PI / 3));
        pwmSignals[2] = 0.5 * (1 + Math.sin(omega * time + 2 * Math.PI / 3));
        return pwmSignals;
    }
}
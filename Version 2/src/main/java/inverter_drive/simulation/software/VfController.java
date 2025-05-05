package inverter_drive.simulation.software;

public class VfController {
    private final InductionMotor motor;
    private double vPerHz;
    private double maxVoltage = 230;
    private double currentSpeed = 0.0;
    private double kp = 0.1;
    private double ki = 0.01;
    private double integralError = 0.0;
    /// FOC variables
    private double torqueErrorIntegral = 0.0;
    private double fluxErrorIntegral = 0.0;
    private final double kpTorque = 0.5;
    private final double kiTorque = 0.05;
    private final double kpFlux = 0.3;
    private final double kiFlux = 0.03;
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
        if (mode.equals("DTC")) {
            return new double[]{0, 0, 0};
        } else if (mode.equals("FOC")) {
            /// Field-Oriented Control
            double speedError = speedRef - motor.getSpeed();
            double torqueRefAdjusted = kp * speedError + ki * (integralError += speedError * Config.SIMULATION_TIME_STEP);
            double torqueError = torqueRefAdjusted - motor.getTorque();
            double fluxError = fluxRef - motor.getRotorFlux();
            /// PI controllers for torque and flux
            double vq = kpTorque * torqueError + kiTorque * (torqueErrorIntegral += torqueError * Config.SIMULATION_TIME_STEP);
            double vd = kpFlux * fluxError + kiFlux * (fluxErrorIntegral += fluxError * Config.SIMULATION_TIME_STEP);
            /// Convert d-q voltages to three-phase (inverse Park-Clarke)
            double theta = 2 * Math.PI * speedRef * time * direction;
            double va = vd * Math.cos(theta) - vq * Math.sin(theta);
            double vb = vd * Math.cos(theta - 2 * Math.PI / 3) - vq * Math.sin(theta - 2 * Math.PI / 3);
            double vc = vd * Math.cos(theta + 2 * Math.PI / 3) - vq * Math.sin(theta + 2 * Math.PI / 3);
            /// Normalize to PWM signals
            double max = Math.max(Math.abs(va), Math.max(Math.abs(vb), Math.abs(vc)));
            if (max > 0) {
                va /= max;
                vb /= max;
                vc /= max;
            }
            return new double[]{0.5 * (1 + va), 0.5 * (1 + vb), 0.5 * (1 + vc)};
        } else {
            /// V/f control
            double speedError = speedRef - motor.getSpeed();
            integralError += speedError * Config.SIMULATION_TIME_STEP;
            double freq = kp * speedError + ki * integralError;
            double maxFreqChange = accelRate * Config.SIMULATION_TIME_STEP / (2 * Math.PI);
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
}
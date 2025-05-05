package inverter_drive.simulation.software;
import java.io.FileWriter;
import java.io.IOException;

public class DataLogger {
    private StringBuilder log = new StringBuilder();
    public DataLogger() {
        log.append("Time,Va,Vb,Vc,Ia,Ib,Ic,Speed,Torque\n");
    }
    public void logData(double time, double[] voltages, double[] currents, double speed, double torque) {
        log.append(String.format("%.3f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f\n",
                time, voltages[0], voltages[1], voltages[2],
                currents[0], currents[1], currents[2], speed, torque));
    }
    public void exportToCSV() {
        try (FileWriter writer = new FileWriter("simulation_data.csv")) {
            writer.write(log.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
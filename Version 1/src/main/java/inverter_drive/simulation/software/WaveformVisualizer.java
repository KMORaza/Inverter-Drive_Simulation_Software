package inverter_drive.simulation.software;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class WaveformVisualizer {
    private Canvas canvas;
    private double[] voltageData = new double[1000];
    private double[] currentData = new double[1000];
    private double[] speedData = new double[1000];
    private int dataIndex = 0;

    public WaveformVisualizer() {
        canvas = new Canvas(680, 550);
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void updateWaveforms(double[] voltages, double[] currents, double speed, double time) {
        voltageData[dataIndex % 1000] = voltages[0]; // Phase A voltage
        currentData[dataIndex % 1000] = currents[0]; // Phase A current
        speedData[dataIndex % 1000] = speed;
        dataIndex++;
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.web("#000000"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        int maxPoints = Math.min(dataIndex, 1000);
        /// Plot voltage
        gc.setStroke(Color.CYAN);
        gc.setLineWidth(1.5);
        gc.beginPath();
        for (int i = 0; i < maxPoints; i++) {
            double x = i * width / 1000;
            double y = height / 3 - voltageData[i] * 0.1;
            if (i == 0) {
                gc.moveTo(x, y);
            } else {
                double prevX = (i - 1) * width / 1000;
                double prevY = height / 3 - voltageData[i - 1] * 0.1;
                gc.quadraticCurveTo(prevX, prevY, (prevX + x) / 2, (prevY + y) / 2);
                gc.lineTo(x, y);
            }
        }
        gc.stroke();
        /// Plot current
        gc.setStroke(Color.MAGENTA);
        gc.setLineWidth(1.5);
        gc.beginPath();
        for (int i = 0; i < maxPoints; i++) {
            double x = i * width / 1000;
            double y = 2 * height / 3 - currentData[i] * 0.5; // Scale
            if (i == 0) {
                gc.moveTo(x, y);
            } else {
                double prevX = (i - 1) * width / 1000;
                double prevY = 2 * height / 3 - currentData[i - 1] * 0.5;
                gc.quadraticCurveTo(prevX, prevY, (prevX + x) / 2, (prevY + y) / 2);
                gc.lineTo(x, y);
            }
        }
        gc.stroke();
        /// Plot speed
        gc.setStroke(Color.LIME);
        gc.setLineWidth(1.5);
        gc.beginPath();
        for (int i = 0; i < maxPoints; i++) {
            double x = i * width / 1000;
            double y = height - speedData[i] * 0.01;
            if (i == 0) {
                gc.moveTo(x, y);
            } else {
                double prevX = (i - 1) * width / 1000;
                double prevY = height - speedData[i - 1] * 0.01;
                gc.quadraticCurveTo(prevX, prevY, (prevX + x) / 2, (prevY + y) / 2);
                gc.lineTo(x, y);
            }
        }
        gc.stroke();
        gc.setStroke(Color.web("#ffffff"));
        gc.setLineWidth(0.5);
        for (int i = 1; i < 13; i++) {
            double y = i * height / 13;
            gc.strokeLine(0, y, width, y);
        }
        for (int i = 1; i < 15; i++) {
            double x = i * width / 15;
            gc.strokeLine(x, 0, x, height);
        }
    }
}
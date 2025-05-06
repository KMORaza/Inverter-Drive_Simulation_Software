module inverter_drive.simulation.software.inverterdrivesimulationsoftware2 {
    requires javafx.controls;
    requires javafx.fxml;


    opens inverter_drive.simulation.software to javafx.fxml;
    exports inverter_drive.simulation.software;
}
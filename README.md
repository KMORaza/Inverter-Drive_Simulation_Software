## Software zur Simulation eines elektrischen Wechselrichterantriebs und geschrieben in JavaFX (Software for simulation of electrical inverter drive and written in JavaFX)

### First version
* The software simulates an inverter-driven induction motor system, allowing users to configure motor parameters, control strategies, PWM settings, and thermal management.
* It includes fault simulation, real-time data logging, and waveform visualization.
* Generates three-phase voltages based on PWM signals, DC-link voltage, and modulation settings.
* Includes harmonic injection and overmodulation options.
* Models thermal dynamics with switching losses and cooling effects.
* Replicates electromechanical behavior (speed, torque) using simplified torque calculations.
* Models thermal effects based on resistive losses, fan speed, and coolant flow.
* Supports different load types (Constant, Fan/Pump, Inertia).
* Accounts for temperature-dependent resistance changes.
* Implements V/f control with PI speed regulation.
* Supports acceleration/deceleration limits.
* Logs time, phase voltages ($V_a$, $V_b$, $V_c$), currents ($I_a$, $I_b$, $I_c$), speed, and torque to a CSV file.
* Uses StringBuilder for efficient string concatenation.
* Plots phase A voltage, current, and motor speed on a canvas using quadratic curves for smooth rendering.
* Simulates current measurements based on motor impedance.
* Covers electrical (inverter, PWM), mechanical (motor dynamics), and thermal aspects.

| ![](https://github.com/KMORaza/Inverter-Drive_Simulation_Software/blob/main/Version%201/src/screenshot.png?raw=true) |
|----------------------------------------------------------------------------------------------------------------------|

---

### Second version
* Key steps :—
  * Updates inverter and controller parameters from UI inputs.
  * Generates PWM signals using the [`VfController.java`](https://github.com/KMORaza/Inverter-Drive_Simulation_Software/blob/main/Version%202/src/main/java/inverter_drive/simulation/software/VfController.java).
  * Produces phase voltages via [`InverterPowerStage.java`](https://github.com/KMORaza/Inverter-Drive_Simulation_Software/blob/main/Version%202/src/main/java/inverter_drive/simulation/software/InverterPowerStage.java).
  * Applies faults using [`FaultSimulator.java`](https://github.com/KMORaza/Inverter-Drive_Simulation_Software/blob/main/Version%202/src/main/java/inverter_drive/simulation/software/FaultSimulator.java).
  * Measures currents with [`SensorMode.javal`](https://github.com/KMORaza/Inverter-Drive_Simulation_Software/blob/main/Version%202/src/main/java/inverter_drive/simulation/software/SensorModel.java).
  * Updates motor state [`InductionMotor.java`](https://github.com/KMORaza/Inverter-Drive_Simulation_Software/blob/main/Version%202/src/main/java/inverter_drive/simulation/software/InductionMotor.java).
  * Logs data via [`DataLogger.java`](https://github.com/KMORaza/Inverter-Drive_Simulation_Software/blob/main/Version%202/src/main/java/inverter_drive/simulation/software/DataLogger.java) and visualizes waveforms via [`WaveformVisualizer.java`](https://github.com/KMORaza/Inverter-Drive_Simulation_Software/blob/main/Version%202/src/main/java/inverter_drive/simulation/software/WaveformVisualizer.java).
* Model of motor :—
  * Models an induction motor using a simplified d-q axis framework (Clarke-Park transform).
  * Calculates torque, speed, and rotor flux based on phase voltages and currents.
  * Includes thermal modeling, adjusting resistance based on temperature and simulating heat generation and cooling.
  * Supports different load types (Constant, Fan/Pump, Inertia) with configurable mechanical parameters.
* Inverter & Control system :—
  * Generates phase voltages from PWM signals, accounting for dead time, modulation index, harmonic injection, and overmodulation.
  * Maintains a constant voltage-to-frequency ratio with PI control for speed regulation.
  * Uses d-q axis control for precise torque and flux regulation.
  * The inverter includes thermal modeling for switching losses and cooling effects.
* Data logging :—
  * Buffers simulation data and periodically writes to a CSV file (simulation_data.csv), with error handling for file I/O.
* Visualization :—
  * Plots phase A voltage, current, and motor speed on a canvas, using a sliding window of 1000 point salongside smooth rendering.
* Sensor modeling :—
  * Simulates current sensors with Gaussian noise and partial/complete failure modes.
  * Currents are calculated based on motor impedance, adjusted for noise and failure scaling.
* Models a wide range of components (motor, inverter, sensors, faults) with realistic dynamics, including thermal effects and noise.
* Supports multiple control strategies (V/f, FOC) and load types, making it versatile for educational and engineering purposes.

| ![](https://github.com/KMORaza/Inverter-Drive_Simulation_Software/blob/main/Version%202/src/screenshot.png?raw=true) |
|----------------------------------------------------------------------------------------------------------------------|

---

### Third version
* Covers electrical, mechanical, thermal, and fault aspects of an inverter drive system.
* Includes thermal dynamics, PWM effects, and fault simulation
* Plots three-phase voltages, currents, and motor speed on a canvas.
* Logs simulation data (time, voltages, currents, speed, torque, control mode, fault) to a CSV file.
* Uses a buffer to reduce disk I/O, flushing when full.
* Displays error alerts for I/O failures.
* System modeling :—
  * Models electrical dynamics (d-q axis currents, rotor flux, torque) using simplified motor equations.
  * Includes mechanical dynamics (inertia, damping, friction, coupling stiffness).
  * Simulates thermal behavior using heat generation (I²R losses), convection, and radiation, with configurable cooling (fan speed, coolant flow).
  * Supports different load types (constant, fan/pump, inertia).
  * Comprehensive modeling of electrical, mechanical, and thermal aspects; realistic temperature-dependent resistance.
  * Generates three-phase voltages using pulse width modulation (PWM) techniques (SPWM & SVPWM).
  * Supports harmonic injection, overmodulation, and dead-time compensation.
  * Simulates current measurements with Gaussian noise and partial failure scaling.
  * Allows enabling/disabling sensor faults.
* Control planning :—
  * V/f Control
    * Maintains a constant voltage-to-frequency ratio for open-loop speed control.
    * Uses PI control to adjust frequency based on speed error, with acceleration limits.
  * Field-Oriented Control (FOC)
    * Controls torque and flux independently using d-q axis voltages.
    * Employs PI controllers for speed, torque, and flux regulation.
    * Uses inverse Park-Clarke transforms for three-phase voltage generation.
  * Direct Torque Control (DTC)
    * Directly controls torque and flux using a switching table and hysteresis bands.
    * Estimates stator flux angle based on currents and rotor flux.
* Fault simulation :—
  * Simulates faults like overcurrent, undervoltage, phase loss, overheating, and IGBT failure.
  * Applies fault effects to phase voltages (e.g., scaling, zeroing phases).
  * Supports protection modes (warning, shutdown) and auto-reset after a timeout.
  * Comprehensive fault coverage; realistic fault injection (e.g., intermittent IGBT failure).

| ![](https://github.com/KMORaza/Inverter-Drive_Simulation_Software/blob/main/Version%203/001.png) | ![](https://github.com/KMORaza/Inverter-Drive_Simulation_Software/blob/main/Version%203/002.png) |
|--------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------|
| ![](https://github.com/KMORaza/Inverter-Drive_Simulation_Software/blob/main/Version%203/003.png) | ![](https://github.com/KMORaza/Inverter-Drive_Simulation_Software/blob/main/Version%203/004.png) |
| ![](https://github.com/KMORaza/Inverter-Drive_Simulation_Software/blob/main/Version%203/005.png) | ![](https://github.com/KMORaza/Inverter-Drive_Simulation_Software/blob/main/Version%203/006.png) |
| ![](https://github.com/KMORaza/Inverter-Drive_Simulation_Software/blob/main/Version%203/007.png) | ![](https://github.com/KMORaza/Inverter-Drive_Simulation_Software/blob/main/Version%203/008.png) |

---

Diese Software simuliert einen elektrischen Wechselrichterantrieb und ist in JavaFX geschrieben.
Vor einigen Tagen (21. April 2025) ich habe eine ähnliche Desktop-App zur Simulation eines elektrischen Wechselrichterantriebs geschrieben, allerdings in der C-Programmiersprache und mit einem anderen Ansatz.

This software simulates electrical inverter drive and is written in JavaFX. 
Few days back (21 April 2025), I wrote a similar desktop app for simulating electrical inverter drive but I wrote that one in C programming language and with a different implementation.

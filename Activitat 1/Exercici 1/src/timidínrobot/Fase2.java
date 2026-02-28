/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package timidínrobot;

import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import static robocode.util.Utils.normalRelativeAngleDegrees;

/**
 *
 * @author Gerard
 */
public class Fase2 extends Estat {
   
    private boolean destruido = false;
    private boolean detectado = false;
    private double firePower = 0;
     private String objetivoActual = null;  // Guardar el nombre del enemigo actual
    public Fase2(TimidínRobot robot) {
        super(robot);
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
          // Si aún no hemos detectado un objetivo o si es el mismo que ya estamos siguiendo
           System.out.println(objetivoActual + " " + event.getName());
 if (objetivoActual == null || event.getName().equals(objetivoActual)) {
     robot.setAdjustRadarForGunTurn(true);
            double distance = event.getDistance();
        System.out.println("DETECTAR");
        double bearing = event.getBearing();

        // Guardar el nombre del robot detectado como el objetivo actual
        objetivoActual = event.getName();

        // Calcular la posición absoluta del enemigo
        double absoluteBearing = robot.getHeading() + bearing;

        // Girar el cañón hacia el enemigo
        double gunTurn = normalRelativeAngleDegrees(absoluteBearing - robot.getGunHeading());
        robot.setTurnGunRight(gunTurn);  // Ajusta el cañón hacia el enemigo

        // Asegurar que el radar siempre sigue al enemigo
          double radarTurn = normalRelativeAngleDegrees(absoluteBearing - robot.getRadarHeading());
        robot.setTurnRadarRight(radarTurn);  // Girar el radar más rápido para asegurarse de seguir al enemigo

        

        // Espera a que el cañón se alinee con el enemigo antes de disparar
        if (Math.abs(gunTurn) < 10) {
            // Calcular la potencia del disparo en función de la distancia
            firePower = Math.min(3.0, Math.max(0.1, 400 / distance));
            robot.fire(firePower);  // Dispara al enemigo
        }

        detectado = true;  // El enemigo ha sido detectado y sigue siendo rastreado
        robot.execute();   // Ejecuta las acciones
 }
        
    }

    @Override
    public void run() {
          robot.setAdjustRadarForGunTurn(true);
        if(detectado == false && destruido == false)
        {
            objetivoActual = null;
       System.out.println("RUN");
       robot.setTurnRadarRight(360); // Escanear en un círculo completo 
       destruido = true;
    //while true no funciona
        } 
        /*else if(destruido == true)
        {
             robot.fire(firePower); 
        }*/
 
               robot.execute();
        }
    @Override
    void onRobotDeath(RobotDeathEvent event) {
        System.out.println(objetivoActual + " " + event.getName());
           if (event.getName().equals(objetivoActual)) {
            System.out.println("DESTRUIDO");
            destruido = false;
            detectado = false;
            objetivoActual = null;  // Resetear el objetivo para detectar uno nuevo
            //robot.setTurnRadarRight(360);  // Torna a escanejar
            this.run();
        }

    }
    
}
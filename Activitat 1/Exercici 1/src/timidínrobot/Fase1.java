package timidínrobot;

import robocode.HitRobotEvent;
//import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;

/**
 *
 * @author Gerard
 */
public class Fase1 extends Estat {

    private double esquina_x;
    private double esquina_y;
    private double canvi_angle = 0;
    private double i = 0;
   // private int contadorEnemigos = 0; // Contador para encuentros con enemigos
     private int moveDirection = 1;
     
     
    // private double DISTANCIA_ORBITA = 250;
      //private double torretaGirant;

    public Fase1(double esquina_x, double esquina_y, TimidínRobot robot) {
        super(robot);
        this.esquina_x = esquina_x;
        this.esquina_y = esquina_y;
    }

    @Override
    public void run() {
          //  DISTANCIA_ORBITA = (robot.getBattleFieldHeight() + robot.getBattleFieldWidth()) / 18 + 40;
            double dist = Math.sqrt(Math.pow(robot.getX() - esquina_x, 2) + Math.pow(robot.getY() - esquina_y, 2));

            System.out.println(dist);
            // Verificar si estamos cerca de la esquina
            if (dist <= 50) {
                robot.setAhead(0); // Detener el robot
                robot.setStop(); // Detener todos los movimientos
                robot.execute();
                System.out.println("Fase2");
                robot.canviEstat(new Fase2(this.robot));
                
            }
            else
            {
            double dx = esquina_x - robot.getX();
            double dy = esquina_y - robot.getY();
            double angle_esq = Math.toDegrees(Math.atan2(dx, dy));
            angle_esq = robocode.util.Utils.normalRelativeAngleDegrees(angle_esq - robot.getHeading());
            
            if(i == 1) angle_esq = verificarParedes(angle_esq);
            System.out.println("RUN");
            /*double dx = esquina_x - robot.getX();
            double dy = esquina_y - robot.getY();
            double angle_esq = Math.toDegrees(Math.atan2(dx, dy));/*
            
            */
            
            robot.setTurnRight(angle_esq);

            // Ejecutar el giro
            while (robot.getTurnRemaining() != 0) {
                robot.execute();
            } 
            if(i == 0)
            {
                System.out.println("RADAR");
                double angle_radar = Math.toDegrees(Math.atan2(dx, dy));
                angle_radar = robocode.util.Utils.normalRelativeAngleDegrees(angle_radar  - robot.getRadarHeading());
                robot.setTurnRadarRight(angle_radar); 
                while (robot.getRadarTurnRemaining() != 0)
                {
                    robot.execute();
                }   
            }
          robot.setAdjustRadarForRobotTurn(false);
                
            // Avanzar hacia la esquina
            robot.setAhead(dist);
            robot.execute();
            i = 1;
            }   
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        double dist = event.getDistance();
        double angleToEnemy = event.getBearing(); 
        // Verificar si el enemigo está a una distancia cercana
        if (dist < 150 && angleToEnemy < 45 && i != 0 && dist > 5) {
             System.out.println("SCANNED");
            robot.fire(10); 
            // Modificar el ángulo de aproximación
            if(angleToEnemy > 0)canvi_angle = -40; // Ajustar el ángulo de aproximación
            else if(angleToEnemy < 0)canvi_angle = 40; // Ajustar el ángulo de aproximación
            else canvi_angle = 40;
            robot.setTurnRight(canvi_angle); // Ajustar el ángulo del robot                   
            
        // Recalcular la dirección hacia la esquina después de esquivar
        double dx = esquina_x - robot.getX();
        double dy = esquina_y - robot.getY();
        double angle_esq = Math.toDegrees(Math.atan2(dx, dy));

        // Ajustar el ángulo de acuerdo con el movimiento del robot
       // angle_esq = robocode.util.Utils.normalRelativeAngleDegrees(angle_esq - robot.getHeading());
        // Mover hacia adelante
                
             while (robot.getTurnRemaining() != 0) {
                robot.execute();
            }   
             
    double distance = event.getDistance();
    double bearing = event.getBearing();

    // Si el robot está cerca de la pared
    if (robot.getX() < 50 || robot.getX() > robot.getBattleFieldWidth() - 50 || robot.getY() < 50 || robot.getY() > robot.getBattleFieldHeight() - 50) {
        // Mensaje de depuración
        System.out.println("Cerca de la pared, cambiando dirección");
        // Retrocede
        robot.setBack(50);
        // Gira 90 grados
        robot.setTurnRight(90);
    } else {
        // Lógica de movimiento hacia el enemigo
        robot.setTurnRight(bearing);
        robot.setAhead(distance);
    }
           
        } else {
            // Resetear el ángulo y el contador si no hay enemigos cerca
            canvi_angle = 0;
            //contadorEnemigos = 0;
        }
    
    
    
    
    
    }
/*
public void evitaParets(double distSegura) {
    Double hMapa = robot.getBattleFieldHeight();
    Double wMapa = robot.getBattleFieldWidth();
    Double x = robot.getX();
    Double y = robot.getY();

    if (x < distSegura || x > wMapa - distSegura || y < distSegura || y > hMapa - distSegura) {
        System.out.println("EVITA PARET");

        // Calcular un ángulo de escape basado en la posición actual del robot y las paredes cercanas
        double anguloEscape;
        if (x < distSegura) {
            // Cerca de la pared izquierda
            anguloEscape = 90 - robot.getHeading();
        } else if (x > wMapa - distSegura) {
            // Cerca de la pared derecha
            anguloEscape = 270 - robot.getHeading();
        } else if (y < distSegura) {
            // Cerca de la pared inferior
            anguloEscape = 0 - robot.getHeading();
        } else {
            // Cerca de la pared superior
            anguloEscape = 180 - robot.getHeading();
        }

        // Normalizar el ángulo
        anguloEscape = robocode.util.Utils.normalRelativeAngleDegrees(anguloEscape);

        // Girar hacia el ángulo de escape
        robot.setTurnRight(anguloEscape);
        while (robot.getTurnRemaining() != 0) {
            robot.execute();
        }

        // Avanzar una distancia segura para alejarse de la pared
        robot.setAhead(200);
        robot.execute();
    }
}
*/
    
    
    private double verificarParedes(double angle_esq) {
    double safeDistance = 40; // Distancia mínima de seguridad
        System.out.println("EVITA PARET");
         System.out.println("esq" + angle_esq);


    if (robot.getX() < safeDistance && angle_esq < 90 && angle_esq > -90) {
        angle_esq = 90; // Ajustamos el ángulo para evitar la pared izquierda
                System.out.println("j");

    } else if (robot.getX() > robot.getBattleFieldWidth() - safeDistance && (angle_esq > 90 || angle_esq < -90)) {
        angle_esq = -90; // Evitar la pared derecha
                System.out.println("e");

    }

    if (robot.getY() < safeDistance && angle_esq > 0) {
        angle_esq = -180; // Evitar la pared inferior
                System.out.println("s");

    } else if (robot.getY() > robot.getBattleFieldHeight() - safeDistance && angle_esq < 0) {
        angle_esq = 180; // Evitar la pared superior
                System.out.println("i");

    }

    return angle_esq;
}

        
    @Override
    void onHitRobot(HitRobotEvent event) 
    {
         // Obtener el ángulo de colisión con respecto al robot
    double bearing = event.getBearing();   
    System.out.println(bearing);
     if (bearing >= 10 && i == 1) {
        // Si el enemigo está a la derecha
         System.out.println("a");
        robot.setBack(60);
         while (robot.getDistanceRemaining() != 0) {
        robot.execute(); // Continuar executant fins que acabi el moviment
    }
        robot.setTurnLeft(50);  // Girar a la izquierda para esquivar    
        while (robot.getTurnRemaining() != 0) {
        robot.execute(); // Continuar executant fins que acabi el moviment
    }
        robot.setAhead(55);
        while (robot.getDistanceRemaining() != 0) {
        robot.execute(); // Continuar executant fins que acabi el moviment
    }
    } 
    else if (bearing <= -10 && i == 1) {
        // Si el enemigo está a la izquierdaç
        System.out.println("b");
        robot.setBack(60);
         while (robot.getDistanceRemaining() != 0) {
        robot.execute(); // Continuar executant fins que acabi el moviment
    }
        robot.setTurnRight(50);  // Girar a la derecha para esquivar
        while (robot.getTurnRemaining() != 0) {
        robot.execute(); // Continuar executant fins que acabi el moviment
    }
        robot.setAhead(55);
       while (robot.getDistanceRemaining() != 0) {
        robot.execute(); // Continuar executant fins que acabi el moviment
    }
    }
         else if(bearing <= -90 && bearing >= 90 && i == 1) {
             robot.setAhead(75);
             }
         else if(i == 1) robot.setBack(45);  // Girar 90 grados para esquivarlo

    /*while (robot.getTurnRemaining() != 0 && robot.getRadarTurnRemaining() != 0) {
                robot.execute();
            }*/
     //robot.setBack(75);
    // Ejecutar las órdenes
    robot.execute();
  
    }
     public double normalitzarBearing(double angle) {
        while (angle >  180) {
            angle -= 360;
        }
        while (angle < -180) {
            angle += 360;
        }
        return angle;
    }   
}
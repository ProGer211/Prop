/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package timidínrobot;

import robocode.ScannedRobotEvent;

/**
 *
 * @author Gerard
 */
public class Fase0 extends Estat {
    
    public Fase0(TimidínRobot robot) {
        super(robot);
    }

    @Override
    public void run() {
          robot.setTurnRadarRight(360); 
          robot.execute();
    }
    @Override
    public void onScannedRobot(ScannedRobotEvent event) 
    {
        //si angulo_heading + angulo_bearing para saber en que lado esta el enemigo derecha o izquierda
        double angulo = event.getBearing() + robot.getHeading();

        double posicio_enemic_x = robot.getX() + Math.sin(Math.toRadians(angulo))* event.getDistance();
        double posicio_enemic_y = robot.getY() + Math.cos(Math.toRadians(angulo)) * event.getDistance(); 
        
        double altura = robot.getBattleFieldHeight();
        double amplada = robot.getBattleFieldWidth();
        double esquina[][] = { {0,0} , {amplada,0} , {0,altura} , {amplada,altura}};
        
        double maxima_distancia = 0;    
        double esquina_mas_lejana[] = null;
        for(double[] esquines : esquina)
        {
            double distancia = Math.sqrt(Math.pow(esquines[0] - posicio_enemic_x, 2) + Math.pow(esquines[1] - posicio_enemic_y, 2));
            if(distancia > maxima_distancia)
            {
                maxima_distancia = distancia;
                esquina_mas_lejana = esquines;
            }     
        }
         if(esquina_mas_lejana != null)
            { 
              robot.canviEstat(new Fase1(esquina_mas_lejana[0],esquina_mas_lejana[1],this.robot));
            }
    }
    
    
}

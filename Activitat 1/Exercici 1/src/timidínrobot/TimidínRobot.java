/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package timidínrobot;

import java.awt.Graphics2D;
import robocode.AdvancedRobot;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;


/**
 *
 * @author Gerard
 */
public class TimidínRobot extends AdvancedRobot {

   Estat fase_actual;
   
   @Override
    public void run()
    {
        fase_actual = new Fase0(this);
      while(true)
      {    
          fase_actual.run(); 
         execute();
      } 
    }
    
    @Override
    public void onScannedRobot(ScannedRobotEvent event) 
    {
        fase_actual.onScannedRobot(event);
        
    }
    public void canviEstat(Estat fase_nueva)
    {
        fase_actual = fase_nueva;
    }

    @Override
    public void onPaint(Graphics2D g) {
    
    }

    @Override
    public void onHitWall(HitWallEvent event) {
          fase_actual.onHitWall(event);
           }

    @Override
    public void onHitRobot(HitRobotEvent event) {
       fase_actual.onHitRobot(event);
    }

    @Override
    public void onRobotDeath(RobotDeathEvent event) {
        fase_actual.onRobotDeath(event);
    }
    
    
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package timidínrobot;

import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

/**
 *
 * @author Gerard
 */
public abstract class Estat{
    protected TimidínRobot robot;

    public Estat(TimidínRobot robot) {
        this.robot = robot;
    }
    
    public void run()
    {    
     
    }
    public void onScannedRobot(ScannedRobotEvent event) 
    {       
    }

    void onHitWall(HitWallEvent event) {
       
    }

    void onHitRobot(HitRobotEvent event) {
        
    }

    void onRobotDeath(RobotDeathEvent event) {
     
    }
    
}

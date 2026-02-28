/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package followtheleaderteam;

import java.awt.Graphics2D;
import java.io.IOException;
import java.io.Serializable;
import robocode.MessageEvent;
import robocode.TeamRobot;

/**
 *
 * @author Gerard
 */
public class TeamLeader extends TeamRobot {

    Estat_Leader fase_actual;
    
    @Override
    public void run() {
       fase_actual = new Fase0_Leader(this);
      while(true)
      {    
          fase_actual.run(); 
         execute();
      } 
    }

    @Override
    public void onPaint(Graphics2D g) {
        fase_actual.onPaint(g);
    }

    @Override
    public void onMessageReceived(MessageEvent event) {
        fase_actual.onMessageReceived(event);
    }

    @Override
    public String[] getTeammates() {
        fase_actual.getTeammates();
        return super.getTeammates(); 
    }

   


    

}

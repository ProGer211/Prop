/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package followtheleaderteam;

import java.io.IOException;
import java.io.Serializable;
import robocode.Droid;
import robocode.MessageEvent;
import robocode.TeamRobot;

/**
 *
 * @author Gerard
 */
public class TeamBot extends TeamRobot implements Droid {

    
    Estat_Bot fase_actual;
    @Override
    public void run() {
       fase_actual = new Fase0_Bot(this);
       while(true)
       {
          fase_actual.run();
           execute();
       }
    }

    @Override
    public void onMessageReceived(MessageEvent event) {
       fase_actual.onMessageReceived(event);
    }

   public void canviEstat(Estat_Bot fase_nueva)
   {
      fase_actual = fase_nueva;
   }


    
    
}

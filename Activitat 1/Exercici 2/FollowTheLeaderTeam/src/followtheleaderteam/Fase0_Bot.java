/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package followtheleaderteam;

import java.awt.Point;
import java.io.IOException;
import robocode.MessageEvent;
import java.util.ArrayList;  
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Gerard
 */
public class Fase0_Bot extends Estat_Bot {
    
    private boolean env = false;
private int assignedIndex = -1; // Para almacenar el índice asignado
private Point assignedPosition = null; // Para almacenar la posición asignada
    private boolean hasReceivedPosition = false; // Estado para verificar si se recibió la posición



    
    public Fase0_Bot(TeamBot bot) {
        super(bot);
    }

    @Override
    void run() {
      if(env == false)
      {
       double x = bot.getX();
       double y = bot.getY();
        Point position3 =  new Point ((int)x , (int) y);
        assignedPosition = position3;
        // int i = 0;      
          try {
              String leader_name = "followtheleaderteam.TeamLeader*";
              String positionAndName = position3.x + "," + position3.y + "," + bot.getName();
              bot.sendMessage(leader_name, positionAndName);// Enviar posición al líder
          } catch (IOException ex) {
              Logger.getLogger(Fase0_Bot.class.getName()).log(Level.SEVERE, null, ex);
          }
                env = true;  // Marcar como enviada
                  System.out.println(" Nombre: " + bot.getName());  
                System.out.println(" posición: " + position3);       
    }
      if(hasReceivedPosition == true) 
      {
          //Habria que enviarle yo creo el indice
           bot.canviEstat(new Fase1_Bot(assignedIndex,this.bot));
      }
     
      
    }

   // @Override
@Override
void onMessageReceived(MessageEvent event) {
    String message = (String) event.getMessage();  // Recibir el mensaje concatenado
    String[] parts = message.split(",");  // Separar los datos por comas

    // Parsear los valores de la posición
    int posX = Integer.parseInt(parts[0]);
    int posY = Integer.parseInt(parts[1]);
    Point botPosition = new Point(posX, posY);

    // El nombre del bot
    int contador = Integer.parseInt(parts[2]);
     if (!hasReceivedPosition && assignedPosition.x == botPosition.x && assignedPosition.y == botPosition.y) {
           //assignedPosition = position;
            assignedIndex = contador; // Asignar el contador actual como índice
            hasReceivedPosition = true; // Marcar que ya se recibió la posición
            System.out.println("contador: " + contador);
            System.out.println("Posición asignada: " + assignedPosition + " con índice: " + assignedIndex);

            // Aquí puedes hacer que el bot se mueva a su posición asignada si es necesario
            // moveToAssignedPosition(assignedPosition);
        } else {
            System.out.println("Este bot ya recibió su posición.");
        }
}

private void broadcastMessage(Point position) throws IOException {
              // La posición del bot
    bot.broadcastMessage(position);       // Enviar el array como un solo mensaje
}

 

 
    
}

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package followtheleaderteam;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import robocode.MessageEvent;

/**
 *
 * @author Gerard
 */
public class Fase0_Leader extends Estat_Leader {

    private boolean enviado = false;  // Bandera para controlar el envío
    private final ArrayList<Point> list;
    private boolean positionsSent = false;
    private final ArrayList<Point> listOrdenada;
    
    private HashMap<String,Point> robot;
    LinkedHashMap<String, Point> orderedRobots;

     //private boolean enviado2;  // Bandera para controlar el envío

    public Fase0_Leader(TeamLeader leader) {
        super(leader);
        list = new ArrayList<>(); 
        listOrdenada = new ArrayList<>(); 
        robot =  new HashMap<>();
        orderedRobots = new LinkedHashMap<>();
    }

    @Override
    void run() {
       // list = new ArrayList<>();
        if(enviado == false)
        {
            double x = leader.getX();
            double y = leader.getY();
            Point p = new Point ((int)x ,(int)y);
            list.add(p);
            enviado = true;
            System.out.println(p + "posicion lider");
            System.out.println(leader.getName());
        }
        
        if (robot.size() >= 4 && !positionsSent) { 
                 listOrdenada.clear();  // Asegúrate de limpiar la lista antes de empezar
    double xLeader = leader.getX();
    double yLeader = leader.getY();

    // Encuentra el robot más cercano al líder primero
    String closestRobotName = null;
    double minDist = Double.MAX_VALUE;  // Inicializar a un valor alto
    
    // Iterar sobre los elementos del HashMap (sin incluir el líder)
    for (HashMap.Entry<String, Point> entry : robot.entrySet()) {
        if (!entry.getKey().equals(leader.getName())) {  // Excluir al líder
            Point p = entry.getValue();
            double distance = Math.sqrt(Math.pow(p.x - xLeader, 2) + Math.pow(p.y - yLeader, 2));
            System.out.println("Distancia al líder del robot " + entry.getKey() + " dist: " + distance);

            if (distance < minDist) {
                minDist = distance;
                closestRobotName = entry.getKey();  // Guardar el nombre del robot más cercano
            }
        }
    }
        System.out.println("robot mas cercano al lider: "+ robot.get(closestRobotName));
        
        orderedRobots.put(closestRobotName, robot.get(closestRobotName));
        
       
        while (orderedRobots.size() < robot.size()) {
            minDist = Double.MAX_VALUE;
            String nextRobotName = null;

            // Obtener la última posición añadida
            Point lastAdded = orderedRobots.values().toArray(new Point[0])[orderedRobots.size() - 1];

            for (HashMap.Entry<String, Point> entry : robot.entrySet()) {
                String robotName = entry.getKey();
                Point position = entry.getValue();

                // Asegurarse de que no se ha añadido ya este robot
                if (!orderedRobots.containsKey(robotName)) {
                    double distance = position.distance(lastAdded);

                    if (distance < minDist) {
                        minDist = distance;
                        nextRobotName = robotName;
                    }
                }
            }

            // Agregar el robot más cercano al último agregado
            if (nextRobotName != null) {
                orderedRobots.put(nextRobotName, robot.get(nextRobotName));
            }
        }
        //VERIFICAR ORDENACION
        for (HashMap.Entry<String, Point> entry : orderedRobots.entrySet()) {
            System.out.println("Robot: " + entry.getKey() + " - Posición: " + entry.getValue());
        }
         
        try {
            sendPositionsToBots();  // Enviar las posiciones ordenadas
            positionsSent = true;
        } catch (IOException e) {
            System.out.println("Error al enviar las posiciones: " + e.getMessage());
        }
            
            
        }
    }
       
    

    @Override
    void onPaint(Graphics2D g) {
        g.setColor(Color.YELLOW);
        g.drawOval((int)(leader.getX() - 50), (int)(leader.getY() - 50), 100, 100);
    }
/*
private void sendPrivateMessage(String robotName, int i, Point robotPosition) throws IOException {

    leader.sendMessage(robotName, robotPosition);   // Enviar el mensaje solo a ese robot
    System.out.println("Enviando posición al robot: " + robotName + " índice: " + i + "posicion: " + robotPosition);
}

// Método para enviar mensajes individuales
private void sendPositionsToBots() throws IOException {
    for (int i = 0; i < listOrdenada.size(); i++) {
        String robotName = "robot" + i;  // Asumimos que los robots se llaman "robot0", "robot1", etc.
        Point robotPosition = listOrdenada.get(i);

        sendPrivateMessage(robotName, robotPosition);  // Enviar mensaje privado a cada robot
    }
}*/
    
  /*  private void broadcastMessage(Point position) throws IOException {
              // La posición del bot
    bot.broadcastMessage(position);       // Enviar el array como un solo mensaje
}*/
 public void sendMessageToBot(Point position) throws IOException {
    // Código para enviar la posición al bot
    // Esto dependerá de cómo esté implementada la comunicación en tu equipo
    //leader.broadcastMessage(position); // O el método que estés utilizando para enviar mensajes
    //System.out.print("pos enviada " + position );
}

    private void sendPositionsToBots() throws IOException {
    // Suponiendo que tienes una lista de bots, por ejemplo, List<TeamBot> bots
    int contador = 2;
   for (HashMap.Entry<String, Point> entry : orderedRobots.entrySet()) {
        // Suponiendo que bot.sendMessageToBot() envía el mensaje al bot correspondiente
        //sendMessageToBot(position);
       String bot_name = entry.getKey();
       Point p = entry.getValue();
       String mensaje = p.x + "," + p.y + "," + contador; 
        leader.sendMessage(bot_name, mensaje);
        contador++;
    }
}

    
    @Override
    void onMessageReceived(MessageEvent event) {
    String message = (String) event.getMessage();  // Recibir el mensaje concatenado
    String[] parts = message.split(",");  // Separar los datos por comas

    // Parsear los valores de la posición
    int posX = Integer.parseInt(parts[0]);
    int posY = Integer.parseInt(parts[1]);
    Point botPosition = new Point(posX, posY);

    // El nombre del bot
    String botName = parts[2];
    robot.put(botName, botPosition);
    // Añadir la posición a la lista o HashMap
    System.out.println("Posición recibida de " + botName + ": " + botPosition);
    
    }

   

   
   
    
}



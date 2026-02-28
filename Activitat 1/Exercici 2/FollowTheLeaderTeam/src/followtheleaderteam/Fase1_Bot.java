/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package followtheleaderteam;

/**
 *
 * @author Gerard
 */
public class Fase1_Bot extends Estat_Bot{
    
    private int index_bot;

    public Fase1_Bot(int index_bot, TeamBot bot) {
        super(bot);
        this.index_bot = index_bot;
    }
  
    @Override
    void run() {
        //SE REPITE PORQUE RUN EJECUTA CADA TORN, SI QUIERES QUITARLO O PONER UN BOOLEAN
        System.out.println("COMIENZA FASE 1, Nombre: "+ bot.getName() + " Index: " + index_bot);
    }
    
}

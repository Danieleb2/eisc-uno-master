package org.example.eiscuno.model.machine;

import javafx.scene.image.ImageView;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;

import javax.swing.*;

public class ThreadPlayMachine extends Thread {
    private Table table;
    private Player machinePlayer;
    private ImageView tableImageView;
    private volatile boolean hasPlayerPlayed;

    public ThreadPlayMachine(Table table, Player machinePlayer, ImageView tableImageView) {
        this.table = table;
        this.machinePlayer = machinePlayer;
        this.tableImageView = tableImageView;
        this.hasPlayerPlayed = false;
    }

    public void run() {
        while (true){
            if(hasPlayerPlayed){
                try{
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // Aqui iria la logica de colocar la carta
                putCardOnTheTable();
                hasPlayerPlayed = false;
            }
        }
    }

    private void putCardOnTheTable(){
        //carta actual
        //System.out.println(this.table.getCurrentCardOnTheTable().getColor());
        //System.out.println(this.table.getCurrentCardOnTheTable().getValue());
        int index = (int) (Math.random() * machinePlayer.getCardsPlayer().size());
        //REMOVER EL INDEX CUANDO IDENTIFIQUE QUE ESA CARTA NO LE SIRVE
        //CREAR UN LOOP PARA RECORRER TODAS LAS CARTAS DE LA MAQUINA Y SI NO TIENE SACAR UNA DE LA BARAJA
        //SI SACA CARTA DE LA BARAJA DEBE AÑADIRSE A LAS CARTAS DE LA MAQUINA Y VERIFICAR SI PUEDE JUGAR CON ESA
        //SI NO VUELVA Y SAQUE CARTA
        Card card = machinePlayer.getCard(index);

        //esta es la carta de la maquina
        System.out.println(card.getValue());//numero
        System.out.println(card.getColor());
        
        if(this.table.getCurrentCardOnTheTable().getColor().equalsIgnoreCase(card.getColor())){
            //las cartas son del mismo color puede poner la carta
            table.addCardOnTheTable(card);
            tableImageView.setImage(card.getImage());
            machinePlayer.removeCard(index);
        } else if(this.table.getCurrentCardOnTheTable().getValue().equalsIgnoreCase(card.getValue())){
                //puede poner la carta
            table.addCardOnTheTable(card);
            tableImageView.setImage(card.getImage());
            machinePlayer.removeCard(index);
        }
        //else if validar las cartas que tienen poderes WILD
        else{
            //no puedes colocar la carta
            System.out.println("No puede colocar esta carta");
            //this.putCardOnTheTable();
            //busque una carta que si sirva con el index ramdom de arriba
        }

    }

    public void setHasPlayerPlayed(boolean hasPlayerPlayed) {
        this.hasPlayerPlayed = hasPlayerPlayed;
    }
}

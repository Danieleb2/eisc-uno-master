package org.example.eiscuno.controller;


import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.example.eiscuno.model.Observer;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.deck.Deck;
import org.example.eiscuno.model.game.GameUno;
import org.example.eiscuno.model.machine.ThreadPlayMachine;
import org.example.eiscuno.model.machine.ThreadSingUNOMachine;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;

/**
 * Controller class for the Uno game.
 */
public class GameUnoController implements Observer {

    @FXML
    private GridPane gridPaneCardsMachine;

    @FXML
    private GridPane gridPaneCardsPlayer;

    @FXML
    private ImageView tableImageView;

    @FXML
    private Button salirButton;

    @FXML
    private Button deckButton;

    @FXML
    private Button unoButton;

    private Player humanPlayer;
    private Player machinePlayer;
    private Deck deck;
    private Table table;
    private GameUno gameUno;
    private int posInitCardToShow;

    private ThreadSingUNOMachine threadSingUNOMachine;
    private ThreadPlayMachine threadPlayMachine;

    private static final String CARD_BACK_IMAGE_PATH = "/org/example/eiscuno/images/deck.png"; // Asegúrate de que esta ruta sea correcta

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {

        // Añadir el EventHandler al botón "Salir"
        salirButton.setOnAction(this::handleSalirButtonAction);

        initVariables();
        this.gameUno.startGame();
        printCardsHumanPlayer();

        threadSingUNOMachine = new ThreadSingUNOMachine(this.humanPlayer.getCardsPlayer());
        Thread t = new Thread(threadSingUNOMachine, "ThreadSingUNO");
        t.start();

        threadPlayMachine = new ThreadPlayMachine(this.table, this.machinePlayer, this.tableImageView);
        threadPlayMachine.addObserver(this);
        threadPlayMachine.start();

        updateMachineCards(7); // Suponiendo que empiezas con 7 cartas

    }

    private void handleSalirButtonAction(ActionEvent event) {
        // Cerrar el programa
        System.exit(0);
    }

    /**
     * Handles the action of taking a card.
     *
     * @param event the action event
     */
    // Métodos de manejo de otros botones
    @FXML
    private void onHandleTakeCard(ActionEvent event) {
        if (threadPlayMachine.isAlive()) {
            System.out.println("No puedes tomar una carta mientras la máquina está jugando.");
            return;
        }
        // Lógica para manejar la acción de tomar carta
        System.out.println("Tomar carta");
        humanPlayer.addCard(this.deck.takeCard());
        printCardsHumanPlayer();
    }

    /**
     * Handles the action of saying "Uno".
     *
     * @param event the action event
     */
    @FXML
    private void onHandleUno(ActionEvent event) {
        if (threadPlayMachine.isAlive()) {
            System.out.println("No puedes presionar UNO mientras la máquina está jugando.");
            return;
        }
        // Lógica para manejar la acción de botón UNO
        System.out.println("Maquina Toma carta");
        machinePlayer.addCard(this.deck.takeCard());
    }

    /**
     * Initializes the variables for the game.
     */
    private void initVariables() {
        this.humanPlayer = new Player("HUMAN_PLAYER");
        this.machinePlayer = new Player("MACHINE_PLAYER");
        this.deck = new Deck();
        this.table = new Table();
        this.gameUno = new GameUno(this.humanPlayer, this.machinePlayer, this.deck, this.table);
        this.posInitCardToShow = 0;
    }

    /**
     * Prints the human player's cards on the grid pane.
     */
    private void printCardsHumanPlayer() {
        this.gridPaneCardsPlayer.getChildren().clear();
        Card[] currentVisibleCardsHumanPlayer = this.gameUno.getCurrentVisibleCardsHumanPlayer(this.posInitCardToShow);

        for (int i = 0; i < currentVisibleCardsHumanPlayer.length; i++) {
            Card card = currentVisibleCardsHumanPlayer[i];
            ImageView cardImageView = card.getCard();

            cardImageView.setOnMouseClicked((MouseEvent event) -> {
                // Aqui deberian verificar si pueden en la tabla jugar esa carta
                try {
                    if(this.table.getCurrentCardOnTheTable().getColor().equalsIgnoreCase(card.getColor())){
                        //las cartas son del mismo color puede poner la carta
                        gameUno.playCard(card);
                        tableImageView.setImage(card.getImage());
                        humanPlayer.removeCard(findPosCardsHumanPlayer(card));
                        threadPlayMachine.setHasPlayerPlayed(true);
                        printCardsHumanPlayer();
                    } else if(this.table.getCurrentCardOnTheTable().getValue().equalsIgnoreCase(card.getValue())){
                        //puede poner la carta
                        gameUno.playCard(card);
                        tableImageView.setImage(card.getImage());
                        humanPlayer.removeCard(findPosCardsHumanPlayer(card));
                        threadPlayMachine.setHasPlayerPlayed(true);
                        printCardsHumanPlayer();
                    }
                    //else if validar las cartas que tienen poderes WILD
                    else{
                        //no puedes colocar la carta
                        //JOptionPane.showMessageDialog(null, "No puedes colocar esa carta");
                        System.out.println("No puedes colocar esa carta");
                    }
                }catch(IndexOutOfBoundsException e){
                    gameUno.playCard(card);
                    tableImageView.setImage(card.getImage());
                    humanPlayer.removeCard(findPosCardsHumanPlayer(card));
                    threadPlayMachine.setHasPlayerPlayed(true);
                    printCardsHumanPlayer();
                }

            });

            this.gridPaneCardsPlayer.add(cardImageView, i, 0);
        }
    }

    /**
     * Finds the position of a specific card in the human player's hand.
     *
     * @param card the card to find
     * @return the position of the card, or -1 if not found
     */
    private Integer findPosCardsHumanPlayer(Card card) {
        for (int i = 0; i < this.humanPlayer.getCardsPlayer().size(); i++) {
            if (this.humanPlayer.getCardsPlayer().get(i).equals(card)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Handles the "Back" button action to show the previous set of cards.
     *
     * @param event the action event
     */
    @FXML
    void onHandleBack(ActionEvent event) {
        if (this.posInitCardToShow > 0) {
            this.posInitCardToShow--;
            printCardsHumanPlayer();
        }
    }

    /**
     * Handles the "Next" button action to show the next set of cards.
     *
     * @param event the action event
     */
    @FXML
    void onHandleNext(ActionEvent event) {
        if (this.posInitCardToShow < this.humanPlayer.getCardsPlayer().size() - 4) {
            this.posInitCardToShow++;
            printCardsHumanPlayer();
        }
    }

    public void updateMachineCards(int numberOfCards) {
        gridPaneCardsMachine.getChildren().clear(); // Limpia el GridPane
        for (int i = 0; i < numberOfCards; i++) {
            ImageView cardBack = new ImageView(new Image(getClass().getResourceAsStream(CARD_BACK_IMAGE_PATH)));
            cardBack.setFitHeight(100); // Ajusta según el tamaño deseado
            cardBack.setFitWidth(80); // Ajusta según el tamaño deseado
            gridPaneCardsMachine.add(cardBack, i % 7, i / 7); // Coloca la carta en el GridPane
        }
    }

    // Método para actualizar las cartas de la máquina
    public void someMethodThatUpdatesCards() {
        int numberOfMachineCards = 7; // Obtén el número de cartas de la máquina de tu lógica de juego
        updateMachineCards(numberOfMachineCards);
    }

    @Override
    public void update(boolean isThreadRunning) {
        if (isThreadRunning) {
            deckButton.setDisable(true);
            unoButton.setDisable(false);
        } else {
            deckButton.setDisable(false);
            unoButton.setDisable(true);
        }
    }
}

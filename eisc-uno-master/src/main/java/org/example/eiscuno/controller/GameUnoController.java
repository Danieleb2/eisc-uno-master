package org.example.eiscuno.controller;


import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import org.example.eiscuno.model.ObserverPatron.Observer;
import org.example.eiscuno.model.WinnerAlert.Winner;
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

    private boolean unoButtonPressed = false;
    private PauseTransition unoTimer;
    private Card lastPlayedCardByHuman;
    private Card lastPlayedCardByMachine;

    @FXML
    public void initialize() {
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

        updateMachineCards(4); // Suponiendo que empiezas con 6 cartas

        // Inicializar el temporizador
        unoTimer = new PauseTransition(Duration.seconds(2));
        unoTimer.setOnFinished(event -> {
            if (!unoButtonPressed) {
                addTwoCardsToHumanPlayer();
            }
        });
    }
    /**
     * Adds two cards to the human player's hand when the UNO button timer finishes.
     */
    private void addTwoCardsToHumanPlayer() {
        gameUno.eatCard(humanPlayer, 2);
        printCardsHumanPlayer();
        System.out.println("No se presionó UNO a tiempo. Se agregaron dos cartas.");
    }
    /**
     * Sets the flag indicating that the UNO button has been pressed by the human player.
     */
    public void humanPressUnoButton() {
        unoButtonPressed = true;
    }
    /**
     * Starts the UNO button timer to check if the human player presses UNO in time.
     */
    public void startUnoTimer() {
        unoButtonPressed = false;
        unoTimer.playFromStart();
    }
    /**
     * Handles the action when the "Salir" (Exit) button is pressed to close the game.
     *
     * @param event The ActionEvent triggered by pressing the salirButton.
     */
    private void handleSalirButtonAction(ActionEvent event) {
        closeGame();
    }
    /**
     * Closes the UNO game.
     */
    public void closeGame() {
        System.exit(0);
    }
    /**
     * Handles the action when the player wants to draw a card from the deck.
     *
     * @param event The ActionEvent triggered by pressing the deckButton.
     */
    @FXML
    private void onHandleTakeCard(ActionEvent event) {
        if (threadPlayMachine.hasPlayerPlayed()) {
            System.out.println("you cannot take a card");
        } else {
            this.threadPlayMachine.setHasPlayerPlayed(true);
            System.out.println("Se precionó el boton");
            humanPlayer.addCard(deck.takeCard());
            printCardsHumanPlayer();
        }
    }
    /**
     * Handles the action when the player presses the UNO button.
     *
     * @param event The ActionEvent triggered by pressing the unoButton.
     */
    @FXML
    private void onHandleUno(ActionEvent event) {
        if (machinePlayer.getCardsPlayer().size() == 1) {
            gameUno.eatCard(machinePlayer, 2);
            System.out.println("Machine has " + machinePlayer.getCardsPlayer().size() + " cards");
        }
        if (humanPlayer.getCardsPlayer().size() == 1) {
            unoButtonPressed = true;
            System.out.println("UNO presionado a tiempo.");
            if (unoTimer.getStatus() == PauseTransition.Status.RUNNING) {
                unoTimer.stop();
            }
        }
        humanPressUnoButton();
    }
    /**
     * Initializes the game variables and starts the game setup.
     */
    private void initVariables() {
        this.humanPlayer = new Player("HUMAN_PLAYER");
        this.machinePlayer = new Player("MACHINE_PLAYER");
        this.deck = new Deck();
        this.table = new Table();
        this.gameUno = new GameUno(this.humanPlayer, this.machinePlayer, this.deck, this.table);
        this.posInitCardToShow = 0;
        this.lastPlayedCardByHuman = null;
    }
    /**
     * Updates the display of cards in the human player's hand.
     */
    private void printCardsHumanPlayer() {
        Platform.runLater(() -> {
        this.gridPaneCardsPlayer.getChildren().clear();
        Card[] currentVisibleCardsHumanPlayer = this.gameUno.getCurrentVisibleCardsHumanPlayer(this.posInitCardToShow);

        for (int i = 0; i < currentVisibleCardsHumanPlayer.length; i++) {
            Card card = currentVisibleCardsHumanPlayer[i];
            ImageView cardImageView = card.getCard();

            cardImageView.setOnMouseClicked((MouseEvent event) -> {
                System.out.println("Card with value " + card.getValue());
                try {
                    if (canPlayCard(card)) {
                        gameUno.playCard(card);
                        tableImageView.setImage(card.getImage());
                        humanPlayer.removeCard(findPosCardsHumanPlayer(card));
                        threadPlayMachine.setHasPlayerPlayed(true);
                        lastPlayedCardByHuman = card;
                        printCardsHumanPlayer();
                    } else {
                        System.out.println("No puedes colocar esa carta");
                    }
                } catch (IndexOutOfBoundsException e) {
                    gameUno.playCard(card);
                    tableImageView.setImage(card.getImage());
                    humanPlayer.removeCard(findPosCardsHumanPlayer(card));
                    threadPlayMachine.setHasPlayerPlayed(true);
                    lastPlayedCardByHuman = card;
                    printCardsHumanPlayer();
                }
            });
            this.gridPaneCardsPlayer.add(cardImageView, i, 0);
        }
        if (humanPlayer.getCardsPlayer().size() == 1) {
            startUnoTimer();
        }
        showWinner();
        });
    }

    private boolean hasEatenTwoCards = false;
    private boolean hasEatenFourCards = false;
    /**
     * Verifies and handles special actions based on the last card played by the machine player.
     */
    private void verifyLastCardPlayedByMachine() {
        Card card = this.threadPlayMachine.getLastPlayedCard();
        if (card == null) {
            return;
        }

        // Verificar si la última carta jugada por la máquina fue un +2 o +4
        if (card.getValue().equals("+2")) {
            if (!hasEatenTwoCards) {
                gameUno.eatCard(humanPlayer, 2);
                hasEatenTwoCards = true;
                System.out.println("Human player has " + humanPlayer.getCardsPlayer().size() + " cards");
            } else {
                // Resetear la bandera si la máquina juega un nuevo +2
                hasEatenTwoCards = false;
            }
        } else if (card.getValue().equals("+4")) {
            if (!hasEatenFourCards) {
                gameUno.eatCard(humanPlayer, 4);
                hasEatenFourCards = true;
                System.out.println("Human player has " + humanPlayer.getCardsPlayer().size() + " cards");
            } else {
                // Resetear la bandera si la máquina juega un nuevo +4
                hasEatenFourCards = false;
            }
        }

        Platform.runLater(() -> {
            // Llamar a printCardsHumanPlayer() desde aquí
            printCardsHumanPlayer();
        });
    }

    /**
     * Checks if a card can be played by the human player on the current table card.
     *
     * @param card The card to check for playability.
     * @return true if the card can be played, false otherwise.
     */
    private boolean canPlayCard(Card card) {
        Card currentCardOnTheTable = this.table.getCurrentCardOnTheTable();

        // La carta "+2" solo puede ser seguida por otra "+2"
        if (currentCardOnTheTable.getValue().equals("+2") && !currentCardOnTheTable.equals(lastPlayedCardByHuman) && !card.getValue().equals("+2")) {
            return false;
        }

        // La carta "SKIP" solo puede ser seguida por otra "SKIP"
        if (currentCardOnTheTable.getValue().equals("SKIP") && !currentCardOnTheTable.equals(lastPlayedCardByHuman) && !card.getValue().equals("SKIP")) {
            return false;
        }

        // La carta "WILD" es válida para cualquier jugada y permite que cualquier carta la siga
        if (currentCardOnTheTable.getValue().equals("WILD") || card.getValue().equals("WILD")) {
            return true;
        }

        // Si la carta actual en el tablero coincide con la última carta jugada, cualquier carta del mismo color o valor es válida
        if (currentCardOnTheTable.equals(lastPlayedCardByHuman)) {
            return true;
        }

        // Cualquier carta con el mismo valor o color es válida
        return currentCardOnTheTable.getValue().equals(card.getValue())
                || currentCardOnTheTable.getColor().equals(card.getColor())||card.getValue().equals("+4");
    }
    /**
     * Finds the position of a card in the human player's hand.
     *
     * @param card The card to find.
     * @return The index of the card in the player's hand, or -1 if not found.
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
     * Handles the action when the player wants to view the previous set of cards in their hand.
     *
     * @param event The ActionEvent triggered by pressing the back button.
     */
    @FXML
    void onHandleBack(ActionEvent event) {
        if (this.posInitCardToShow > 0) {
            this.posInitCardToShow--;
            printCardsHumanPlayer();
        }
    }
    /**
     * Handles the action when the player wants to view the next set of cards in their hand.
     *
     * @param event The ActionEvent triggered by pressing the next button.
     */
    @FXML
    void onHandleNext(ActionEvent event) {
        if (this.posInitCardToShow < this.humanPlayer.getCardsPlayer().size() - 4) {
            this.posInitCardToShow++;
            printCardsHumanPlayer();
        }
    }
    /**
     * Updates the display of cards in the machine player's hand.
     *
     * @param numberOfCards The number of cards in the machine player's hand.
     */
    public void updateMachineCards(int numberOfCards) {
        gridPaneCardsMachine.getChildren().clear(); // Limpia el GridPane
        for (int i = 0; i < 4; i++) {
            ImageView cardBack = new ImageView(new Image(getClass().getResourceAsStream(CARD_BACK_IMAGE_PATH)));
            cardBack.setFitHeight(110); // Ajusta según el tamaño deseado
            cardBack.setFitWidth(100); // Ajusta según el tamaño deseado
            gridPaneCardsMachine.add(cardBack, i, 0); // Coloca la carta en el GridPane
        }
    }

    /**
     * Shows the winner of the game and closes the game window.
     */
    public void showWinner() {
        if (this.humanPlayer.getCardsPlayer().isEmpty()) {
            unoTimer.stop();
            String tittle = "¡GANADOR";
            String header = "";
            String content = "¡Felicidades has ganado el juego!";
            Winner alertBox = new Winner();
            alertBox.showMessageWinner(tittle, header, content);
            closeGame();
        }
    }
    /**
     * Update method implemented from the Observer interface.
     * Enables game buttons and checks for special actions after the machine player's turn.
     *
     * @param isThreadRunning Indicates if the game thread is still running.
     */
    @Override
    public void update(boolean isThreadRunning) {
        deckButton.setDisable(false);
        unoButton.setDisable(false);
        verifyLastCardPlayedByMachine();
    }
}

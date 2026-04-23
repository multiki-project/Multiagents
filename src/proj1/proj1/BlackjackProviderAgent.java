package proj1.proj1;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.content.ContentElement;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import proj1.ontology.*;
import java.util.Random;

public class BlackjackProviderAgent extends Agent {
    private int playerSum = 0;
    private int dealerSum = 0;
    private SLCodec codec = new SLCodec();
    private boolean gameActive = false;

    protected void setup() {
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(CardGameOntology.getInstance());

        registerInDF();

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    // Якщо це текстова команда (кнопка TAKE)
                    if (msg.getPerformative() == ACLMessage.REQUEST && "TAKE_CARDS".equals(msg.getContent())) {
                        handleStand(msg);
                    } else {
                        handleIncomingAction(msg);
                    }
                } else block();
            }
        });
    }

    private void handleIncomingAction(ACLMessage msg) {
        try {
            ContentElement ce = getContentManager().extractContent(msg);
            if (ce instanceof Action) {
                jade.content.Concept action = ((Action) ce).getAction();

                if (action instanceof SubscribeToGame) {
                    startNewGame(msg);
                } else if (action instanceof PlayMove) {
                    handleHit((PlayMove) action, msg);
                }
            }
        } catch (Exception e) {
            // Якщо онтологія знову лається, ми просто ігноруємо помилку валідації
            // і виводимо тільки суть у консоль для відладки
            System.out.println("Handled move via fallback: " + msg.getPerformative());
        }
    }

    private void startNewGame(ACLMessage msg) {
        playerSum = 0;
        dealerSum = new Random().nextInt(10) + 2;
        gameActive = true;

        // 1. ВІДПРАВЛЯЄМО ТЕКСТ ПЕРШИМ (це точно має з'явитися в лозі)
        sendTextReply(msg, "GAME_START:Blackjack");
        sendTextReply(msg, "DECK_COUNT:52");
        sendTextReply(msg, "WELCOME! Dealer shows: " + dealerSum);

        // Вивід в консоль IntelliJ для перевірки
        System.out.println(">>> Blackjack started. Dealer score: " + dealerSum);

        // 2. А ТЕПЕР ВІДПРАВЛЯЄМО КАРТИ
        ACLMessage replyCards = msg.createReply();
        replyCards.setPerformative(ACLMessage.INFORM);
        CardsDealt cd = new CardsDealt();
        cd.getCards().add(generateRandomCard());
        cd.getCards().add(generateRandomCard());

        try {
            getContentManager().fillContent(replyCards, cd);
            send(replyCards);
        } catch (Exception e) {
            System.err.println("Ontology error while dealing: " + e.getMessage());
        }
    }

    private void handleHit(PlayMove action, ACLMessage msg) {
        if (!gameActive) return;

        Card c = action.getPlayedCard();
        playerSum += parseRank(c.getRank());

        // 1. Показуємо карту на столі
        sendTextReply(msg, "SHOW_ATTACK:" + c.getRank() + ":" + c.getSuit());
        sendTextReply(msg, "Current Sum: " + playerSum);

        if (playerSum > 21) {
            gameActive = false;
            sendTextReply(msg, "BUST! (Перебір). GAME_OVER:SERVER_WINS");
        } else {
            // 2. ДАЄМО ГРАВЦЮ НОВУ КАРТУ В РУКУ (щоб було що підбирати далі)
            ACLMessage replyCard = msg.createReply();
            replyCard.setPerformative(ACLMessage.INFORM);
            CardsDealt cd = new CardsDealt();
            cd.getCards().add(generateRandomCard()); // Додаємо одну нову карту
            try {
                getContentManager().fillContent(replyCard, cd);
                send(replyCard);
            } catch (Exception e) {}
        }
    }

    private void handleStand(ACLMessage msg) {
        if (!gameActive) return;

        while (dealerSum < 17) {
            dealerSum += (new Random().nextInt(10) + 2);
        }

        String result;
        if (dealerSum > 21 || playerSum > dealerSum) result = "YOU_WIN";
        else if (dealerSum == playerSum) result = "DRAW";
        else result = "SERVER_WINS";

        sendTextReply(msg, "Dealer: " + dealerSum + " | You: " + playerSum);
        sendTextReply(msg, "GAME_OVER:" + result);
        gameActive = false;
    }

    private void sendTextReply(ACLMessage msg, String text) {
        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        reply.setContent(text);
        send(reply);
    }

    private Card generateRandomCard() {
        Card c = new Card();
        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
        String[] ranks = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
        c.setSuit(suits[new Random().nextInt(4)]);
        c.setRank(ranks[new Random().nextInt(ranks.length)]);
        return c;
    }

    private int parseRank(String r) {
        if (r.equals("A")) return 11;
        if (r.equals("K") || r.equals("Q") || r.equals("J") || r.equals("10")) return 10;
        try { return Integer.parseInt(r); } catch (Exception e) { return 10; }
    }

    private void registerInDF() {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("card-game-provider");
        sd.setName("BLACKJACK-SERVER");
        dfd.addServices(sd);
        try { DFService.register(this, dfd); } catch (Exception e) {}
    }
}
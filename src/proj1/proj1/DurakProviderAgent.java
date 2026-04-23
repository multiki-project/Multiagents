package proj1.proj1;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import proj1.ontology.*;
import java.util.*;

public class DurakProviderAgent extends Agent {
    private List<Card> deck = new ArrayList<>();
    private List<Card> serverHand = new ArrayList<>();
    private int playerCardCount = 0; // Змінна для відстеження кількості карт у гравця
    private String trumpSuit;
    private Card tableCard = null;
    private SLCodec codec = new SLCodec();

    protected void setup() {
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(CardGameOntology.getInstance());
        initDeck();
        registerInDF();

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) processMessage(msg);
                else block();
            }
        });
    }

    private void processMessage(ACLMessage msg) {
        try {
            Object content = getContentManager().extractContent(msg);
            if (content instanceof Action) {
                jade.content.Concept action = ((Action) content).getAction();
                if (action instanceof SubscribeToGame) {
                    handleStart(msg.getSender());
                } else if (action instanceof PlayMove) {
                    handleMove(((PlayMove) action).getPlayedCard(), msg.getSender());
                }
            } else if (msg.getPerformative() == ACLMessage.REQUEST && "TAKE_CARDS".equals(msg.getContent())) {
                handleTake(msg.getSender());
            }
        } catch (Exception e) {
            // Якщо прийшов неструктурований текст (наприклад, технічні повідомлення JADE)
        }
    }

    private void handleStart(AID player) throws Exception {
        initDeck();
        serverHand.clear();
        playerCardCount = 0;

        // 1. Створюємо об'єкт з картами
        CardsDealt cd = new CardsDealt();
        for (int i = 0; i < 6; i++) {
            Card c = drawCard();
            if (c != null) {
                cd.getCards().add(c);
                playerCardCount++;
            }
        }

        // 2. Роздаємо серверу
        for (int i = 0; i < 6; i++) {
            Card c = drawCard();
            if (c != null) serverHand.add(c);
        }

        // --- ВАЖЛИВО: ПОРЯДОК ВІДПРАВКИ ---

        // ПЕРШЕ: Відправляємо текст про козир (це працює)
        sendText(player, "GAME_START:Trump is " + trumpSuit + " " + getTrumpIcon(trumpSuit));

        // ДРУГЕ: Відправляємо КАРТИ (об'єкт)
        ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
        reply.addReceiver(player);

        // ОСЬ ТУТ СОБАКА ЗАРИТА:
        reply.setLanguage(codec.getName()); // Має бути "fipa-sl"
        reply.setOntology(CardGameOntology.getInstance().getName()); // Має бути "Card-Game-Ontology"

        try {
            getContentManager().fillContent(reply, cd);
            send(reply);
            System.out.println("СЕРВЕР: Карти відправлено гравцеві " + player.getLocalName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ТРЕТЄ: Сервер атакує
        makeServerMove(player);
    }

    private void handleMove(Card pCard, AID player) {
        // Логіка: якщо гравець побив карту сервера
        if (tableCard != null && canBeat(tableCard, pCard)) {
            playerCardCount--;
            sendText(player, "✅ Correct! You beat my " + tableCard.getRank());
            tableCard = null;

            try {
                refillHands(player); // Добираємо карти обом
                makeServerMove(player); // Сервер атакує знову
            } catch (Exception e) { e.printStackTrace(); }
        } else {
            sendText(player, "❌ Can't play this card here!");
        }
    }

    private void refillHands(AID player) throws Exception {
        CardsDealt extra = new CardsDealt();
        boolean needToSend = false;

        // Добираємо гравцю до 6
        while (playerCardCount < 6 && !deck.isEmpty()) {
            Card c = drawCard();
            extra.getCards().add(c);
            playerCardCount++;
            needToSend = true;
        }

        // Добираємо серверу до 6
        while (serverHand.size() < 6 && !deck.isEmpty()) {
            serverHand.add(drawCard());
        }

        if (needToSend) {
            ACLMessage m = new ACLMessage(ACLMessage.INFORM);
            m.addReceiver(player);
            m.setLanguage(codec.getName());
            m.setOntology(CardGameOntology.getInstance().getName());
            getContentManager().fillContent(m, extra);
            send(m);
        }
    }

    private void handleTake(AID player) throws Exception {
        CardsDealt cd = new CardsDealt();

        // Гравець забирає карту зі столу
        if (tableCard != null) {
            cd.getCards().add(tableCard);
            playerCardCount++;
            tableCard = null;
        }

        // Додаткове "покарання" — ще одна карта з колоди, якщо вона є
        Card penalty = drawCard();
        if (penalty != null) {
            cd.getCards().add(penalty);
            playerCardCount++;
        }

        ACLMessage m = new ACLMessage(ACLMessage.INFORM);
        m.addReceiver(player);
        m.setLanguage(codec.getName());
        m.setOntology(CardGameOntology.getInstance().getName());
        getContentManager().fillContent(m, cd);
        send(m);

        sendText(player, "You took the cards. My turn to attack again!");
        makeServerMove(player);
    }

    private void makeServerMove(AID player) {
        if (serverHand.isEmpty() && deck.isEmpty()) {
            sendText(player, "🏆 I have no cards left! Game over.");
            return;
        }

        if (!serverHand.isEmpty()) {
            tableCard = serverHand.remove(0); // Сервер бере першу карту з руки
            sendAttack(player, tableCard);
        }
    }

    private boolean canBeat(Card attack, Card defense) {
        // Якщо масті однакові — порівнюємо вагу
        if (defense.getSuit().equals(attack.getSuit())) {
            return getWeight(defense.getRank()) > getWeight(attack.getRank());
        }
        // Якщо захисна карта — козир, а атакуюча — ні
        if (defense.getSuit().equals(trumpSuit)) {
            return true;
        }
        return false;
    }

    private void sendAttack(AID player, Card c) {
        ACLMessage m = new ACLMessage(ACLMessage.INFORM);
        m.addReceiver(player);
        m.setContent("ATTACK:" + c.getRank() + ":" + c.getSuit());
        send(m);
    }

    private void sendText(AID p, String t) {
        ACLMessage m = new ACLMessage(ACLMessage.INFORM);
        m.addReceiver(p);
        m.setContent(t);
        send(m);
    }

    private void initDeck() {
        deck.clear();
        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
        String[] ranks = {"6","7","8","9","10","J","Q","K","A"};
        for(String s : suits) {
            for(String r : ranks) {
                Card c = new Card();
                c.setSuit(s);
                c.setRank(r);
                deck.add(c);
            }
        }
        Collections.shuffle(deck);
        trumpSuit = suits[new Random().nextInt(4)];
    }

    private Card drawCard() {
        return deck.isEmpty() ? null : deck.remove(0);
    }

    private int getWeight(String r) {
        switch(r) {
            case "J": return 11;
            case "Q": return 12;
            case "K": return 13;
            case "A": return 14;
            default:
                try { return Integer.parseInt(r); }
                catch (Exception e) { return 0; }
        }
    }

    private String getTrumpIcon(String suit) {
        switch(suit) {
            case "Hearts": return "♥";
            case "Diamonds": return "♦";
            case "Clubs": return "♣";
            case "Spades": return "♠";
            default: return "";
        }
    }

    private void registerInDF() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("card-game-provider");
        sd.setName("DURAK-SERVER");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
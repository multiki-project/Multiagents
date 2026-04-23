package proj1.proj1;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import proj1.ontology.*;
import jade.core.AID; // Обов'язково
import jade.core.behaviours.WakerBehaviour; // Обов'язково
import java.util.ArrayList;
import java.util.List;

public class GameProviderAgent extends Agent {
    private List<Card> serverHand = new ArrayList<>();
    private String trumpSuit;
    private boolean isPlayerTurn = true; // Починаємо з ходу гравця

    protected void setup() {
        getContentManager().registerLanguage(new SLCodec());
        getContentManager().registerOntology(CardGameOntology.getInstance());
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
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);

                if (action instanceof SubscribeToGame) {
                    handleJoin(reply);
                } else if (action instanceof PlayMove) {
                    Card pCard = ((PlayMove) action).getPlayedCard();
                    handlePlayerMove(pCard, reply, msg.getSender());
                }
                send(reply);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleJoin(ACLMessage reply) throws Exception {
        serverHand.clear();
        String[] suits = {"Hearts", "Diamonds", "Clubs", "Spades"};
        trumpSuit = suits[(int)(Math.random()*4)];
        isPlayerTurn = true;

        CardsDealt cd = new CardsDealt();
        for(int i=0; i<6; i++) {
            cd.getCards().add(generateCard());
            serverHand.add(generateCard());
        }

        // ВАЖЛИВО: Спочатку очищуємо контент, потім наповнюємо об'єктом
        reply.setContent("");
        getContentManager().fillContent(reply, cd);

        // Щоб гравець знав козир, виведемо це в консоль або надішлемо пізніше
        System.out.println("New Game Started. Trump: " + trumpSuit);
    }
    private void handlePlayerMove(Card pCard, ACLMessage reply, AID player) {
        int pRank = Integer.parseInt(pCard.getRank());

        // Якщо гравець атакує
        if (isPlayerTurn) {
            for (Card sCard : serverHand) {
                if (canBeat(pCard, sCard)) {
                    serverHand.remove(sCard);
                    isPlayerTurn = false; // Тепер черга сервера атакувати
                    reply.setContent("✅ I beat it with " + sCard.getRank() + " of " + sCard.getSuit() + ". My turn to attack!");
                    addBehaviour(new WakerBehaviour(this, 2000) {
                        protected void onWake() { sendServerAttack(player); }
                    });
                    return;
                }
            }
            serverHand.add(pCard);
            reply.setContent("❌ I TAKE IT! You can attack again.");
            isPlayerTurn = true; // Гравець ходить знову, бо сервер взяв
        } else {
            // Якщо гравець відбивається від атаки сервера
            reply.setContent("✅ Nice defense! My turn again.");
            isPlayerTurn = false;
            addBehaviour(new WakerBehaviour(this, 1000) {
                protected void onWake() { sendServerAttack(player); }
            });
        }
    }

    private boolean canBeat(Card attack, Card defense) {
        int aR = Integer.parseInt(attack.getRank());
        int dR = Integer.parseInt(defense.getRank());

        if (defense.getSuit().equals(trumpSuit) && !attack.getSuit().equals(trumpSuit)) return true;
        if (defense.getSuit().equals(attack.getSuit()) && dR > aR) return true;
        return false;
    }

    private void sendServerAttack(AID player) {
        if (serverHand.isEmpty()) return;
        Card c = serverHand.remove(0);
        ACLMessage m = new ACLMessage(ACLMessage.INFORM);
        m.addReceiver(player);
        m.setContent("ATTACK:" + c.getRank() + ":" + c.getSuit());
        send(m);
    }

    private Card generateCard() {
        Card c = new Card();
        c.setSuit(new String[]{"Hearts", "Diamonds", "Clubs", "Spades"}[(int)(Math.random()*4)]);
        c.setRank(String.valueOf((int)(Math.random()*9) + 6));
        return c;
    }

    private void registerInDF() {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("card-game-provider");
        sd.setName(getLocalName());
        dfd.addServices(sd);
        try { DFService.register(this, dfd); } catch (Exception e) {}
    }
}
package proj1.proj1;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import proj1.ontology.*;

public class BlackjackProviderAgent extends Agent {
    private int playerSum = 0;
    private SLCodec codec = new SLCodec();

    protected void setup() {
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(CardGameOntology.getInstance());

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID()); // ВАЖЛИВО: вказати AID агента
        ServiceDescription sd = new ServiceDescription();
        sd.setType("card-game-provider"); // Має точно збігатися з тим, що шукає UserAgent
        sd.setName(getLocalName());
        dfd.addServices(sd);

        sd.setName("BLACKJACK-SERVER");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
            System.out.println(getLocalName() + " registered successfully!");
        } catch (Exception e) {
            System.err.println(getLocalName() + " failed to register: " + e.getMessage());
        }

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    handleGame(msg);
                } else block();
            }
        });
    }

    private void handleGame(ACLMessage msg) {
        try {
            Object content = getContentManager().extractContent(msg);
            if (content instanceof Action) {
                jade.content.Concept action = ((Action) content).getAction();
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);

                if (action instanceof SubscribeToGame) {
                    playerSum = 0;
                    reply.setContent("Welcome to Blackjack! Place your card.");
                } else if (action instanceof PlayMove) {
                    Card c = ((PlayMove) action).getPlayedCard();
                    int val = parseRank(c.getRank());
                    playerSum += val;

                    if (playerSum > 21) reply.setContent("BUST! Sum: " + playerSum + ". You lose!");
                    else if (playerSum == 21) reply.setContent("21! BLACKJACK!");
                    else reply.setContent("Current sum: " + playerSum + ". Hit or Stand?");
                }
                send(reply);
            }
        } catch (Exception e) {}
    }

    private int parseRank(String r) {
        if (r.equals("A")) return 11;
        if (r.equals("K") || r.equals("Q") || r.equals("J")) return 10;
        try { return Integer.parseInt(r); } catch (Exception e) { return 0; }
    }
}
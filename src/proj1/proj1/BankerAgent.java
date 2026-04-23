package proj1.proj1;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class BankerAgent extends Agent {
    protected void setup() {
        // Реєстрація як сервіс "banking"
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("banking-service");
        sd.setName("Global-Banker");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (Exception e) { e.printStackTrace(); }

        // Поведінка очікування повідомлень про виграш (IsWinner)
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null && msg.getPerformative() == ACLMessage.INFORM) {
                    // Просто логуємо виплату
                    System.out.println("[BANKER] Recording transaction for: " + msg.getSender().getLocalName());
                } else { block(); }
            }
        });
    }
}
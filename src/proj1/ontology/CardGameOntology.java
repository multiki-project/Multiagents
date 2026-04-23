package proj1.ontology;
import jade.content.onto.*;
import jade.content.schema.*;

public class CardGameOntology extends Ontology {
    public static final String NAME = "Card-Game-Ontology";
    private static Ontology instance = new CardGameOntology();
    public static Ontology getInstance() { return instance; }

    private CardGameOntology() {
        super(NAME, BasicOntology.getInstance());
        try {

            ConceptSchema csCard = new ConceptSchema("Card");
            add(csCard, Card.class);
            csCard.add("suit", (PrimitiveSchema) getSchema(BasicOntology.STRING));
            csCard.add("rank", (PrimitiveSchema) getSchema(BasicOntology.STRING));
            csCard.add("value", (PrimitiveSchema) getSchema(BasicOntology.INTEGER));

            // GameInfo: gameName, minPlayers, description
            ConceptSchema csGame = new ConceptSchema("GameInfo");
            add(csGame, GameInfo.class);
            csGame.add("gameName", (PrimitiveSchema) getSchema(BasicOntology.STRING));
            csGame.add("minPlayers", (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
            csGame.add("description", (PrimitiveSchema) getSchema(BasicOntology.STRING));

            // Player: name, balance
            ConceptSchema csPlayer = new ConceptSchema("Player");
            add(csPlayer, Player.class);
            csPlayer.add("Name", (PrimitiveSchema) getSchema(BasicOntology.STRING));
            csPlayer.add("balance", (PrimitiveSchema) getSchema(BasicOntology.FLOAT));

            // GameState: currentTurn, isGameOver
            ConceptSchema csStatus = new ConceptSchema("GameState");
            add(csStatus, GameState.class);
            csStatus.add("currentTurn", (PrimitiveSchema) getSchema(BasicOntology.STRING));
            csStatus.add("isGameOver", (PrimitiveSchema) getSchema(BasicOntology.BOOLEAN));

            // --- AGENT ACTIONS ---

            // SubscribeToGame: gameName
            AgentActionSchema asSub = new AgentActionSchema("SubscribeToGame");
            add(asSub, SubscribeToGame.class);
            asSub.add("gameName1", (PrimitiveSchema) getSchema(BasicOntology.STRING));

            // PlayMove: playedCard
            AgentActionSchema asMove = new AgentActionSchema("PlayMove");
            add(asMove, PlayMove.class);
            asMove.add("playedCard", (ConceptSchema) getSchema("Card"));

            // RequestRules
            AgentActionSchema asRules = new AgentActionSchema("RequestRules");
            add(asRules, RequestRules.class);
            asRules.add("gamename", (PrimitiveSchema) getSchema(BasicOntology.STRING));

            // --- PREDICATES ---

            // IsWinner: player, prize
            PredicateSchema psWinner = new PredicateSchema("IsWinner");
            add(psWinner, IsWinner.class);
            psWinner.add("player", (ConceptSchema) getSchema("Player"));
            psWinner.add("prize", (PrimitiveSchema) getSchema(BasicOntology.FLOAT));

            // CardsDealt: cards (List/Multiple)
            PredicateSchema psDealt = new PredicateSchema("CardsDealt");
            add(psDealt, CardsDealt.class);
            psDealt.add("cards", (ConceptSchema) getSchema("Card"), 0, ObjectSchema.UNLIMITED);

        } catch (OntologyException e) {
            e.printStackTrace();
        }
    }
}
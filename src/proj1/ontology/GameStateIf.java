package proj1.ontology;



/**
* Protege name: GameState
* @author ontology bean generator
* @version 2026/04/22, 17:37:03
*/
public interface GameStateIf extends jade.content.Concept {

   /**
   * Protege name: currentTurn
   */
   public void setCurrentTurn(String value);
   public String getCurrentTurn();

   /**
   * Protege name: isGameOver
   */
   public void setIsGameOver(boolean value);
   public boolean getIsGameOver();

}

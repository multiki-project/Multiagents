package proj1.ontology;


/**
* Protege name: GameState
* @author ontology bean generator
* @version 2026/04/22, 17:37:03
*/
public class GameState implements GameStateIf {

  private static final long serialVersionUID = 7810243947702126400L;

  private String _internalInstanceName = null;

  public GameState() {
    this._internalInstanceName = "";
  }

  public GameState(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * Protege name: currentTurn
   */
   private String currentTurn;
   public void setCurrentTurn(String value) { 
    this.currentTurn=value;
   }
   public String getCurrentTurn() {
     return this.currentTurn;
   }

   /**
   * Protege name: isGameOver
   */
   private boolean isGameOver;
   public void setIsGameOver(boolean value) { 
    this.isGameOver=value;
   }
   public boolean getIsGameOver() {
     return this.isGameOver;
   }

}

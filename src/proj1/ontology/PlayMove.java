package proj1.ontology;


/**
* Protege name: PlayMove
* @author ontology bean generator
* @version 2026/04/22, 17:37:03
*/
public class PlayMove implements PlayMoveIf {

  private static final long serialVersionUID = 7810243947702126400L;

  private String _internalInstanceName = null;

  public PlayMove() {
    this._internalInstanceName = "";
  }

  public PlayMove(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * Protege name: playedCard
   */
   private Card playedCard;
   public void setPlayedCard(Card value) { 
    this.playedCard=value;
   }
   public Card getPlayedCard() {
     return this.playedCard;
   }

}

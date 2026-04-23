package proj1.ontology;


/**
* Protege name: IsWinner
* @author ontology bean generator
* @version 2026/04/22, 17:37:03
*/
public class IsWinner implements IsWinnerIf {

  private static final long serialVersionUID = 7810243947702126400L;

  private String _internalInstanceName = null;

  public IsWinner() {
    this._internalInstanceName = "";
  }

  public IsWinner(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * Protege name: prize
   */
   private float prize;
   public void setPrize(float value) { 
    this.prize=value;
   }
   public float getPrize() {
     return this.prize;
   }

   /**
   * Protege name: player
   */
   private Player player;
   public void setPlayer(Player value) { 
    this.player=value;
   }
   public Player getPlayer() {
     return this.player;
   }

}

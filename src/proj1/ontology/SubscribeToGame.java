package proj1.ontology;


/**
* Protege name: SubscribeToGame
* @author ontology bean generator
* @version 2026/04/22, 17:37:03
*/
public class SubscribeToGame implements SubscribeToGameIf {

  private static final long serialVersionUID = 7810243947702126400L;

  private String _internalInstanceName = null;

  public SubscribeToGame() {
    this._internalInstanceName = "";
  }

  public SubscribeToGame(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * Protege name: gameName1
   */
   private String gameName1;
   public void setGameName1(String value) { 
    this.gameName1=value;
   }
   public String getGameName1() {
     return this.gameName1;
   }

}

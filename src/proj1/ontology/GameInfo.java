package proj1.ontology;


/**
* Protege name: GameInfo
* @author ontology bean generator
* @version 2026/04/22, 17:37:03
*/
public class GameInfo implements GameInfoIf {

  private static final long serialVersionUID = 7810243947702126400L;

  private String _internalInstanceName = null;

  public GameInfo() {
    this._internalInstanceName = "";
  }

  public GameInfo(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * Protege name: description
   */
   private String description;
   public void setDescription(String value) { 
    this.description=value;
   }
   public String getDescription() {
     return this.description;
   }

   /**
   * Protege name: gameName
   */
   private String gameName;
   public void setGameName(String value) { 
    this.gameName=value;
   }
   public String getGameName() {
     return this.gameName;
   }

   /**
   * Protege name: minPlayers
   */
   private int minPlayers;
   public void setMinPlayers(int value) { 
    this.minPlayers=value;
   }
   public int getMinPlayers() {
     return this.minPlayers;
   }

}

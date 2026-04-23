package proj1.ontology;



/**
* Protege name: GameInfo
* @author ontology bean generator
* @version 2026/04/22, 17:37:03
*/
public interface GameInfoIf extends jade.content.Concept {

   /**
   * Protege name: description
   */
   public void setDescription(String value);
   public String getDescription();

   /**
   * Protege name: gameName
   */
   public void setGameName(String value);
   public String getGameName();

   /**
   * Protege name: minPlayers
   */
   public void setMinPlayers(int value);
   public int getMinPlayers();

}

package proj1.ontology;



/**
* Protege name: IsWinner
* @author ontology bean generator
* @version 2026/04/22, 17:37:03
*/
public interface IsWinnerIf extends jade.content.Predicate {

   /**
   * Protege name: prize
   */
   public void setPrize(float value);
   public float getPrize();

   /**
   * Protege name: player
   */
   public void setPlayer(Player value);
   public Player getPlayer();

}

package proj1.ontology;



/**
* Protege name: Card
* @author ontology bean generator
* @version 2026/04/22, 17:37:03
*/
public interface CardIf extends jade.content.Concept {

   /**
   * Protege name: suit
   */
   public void setSuit(String value);
   public String getSuit();

   /**
   * Protege name: rank
   */
   public void setRank(String value);
   public String getRank();

   /**
   * Protege name: value
   */
   public void setValue(int value);
   public int getValue();

}

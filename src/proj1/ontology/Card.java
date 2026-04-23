package proj1.ontology;


/**
* Protege name: Card
* @author ontology bean generator
* @version 2026/04/22, 17:37:03
*/
public class Card implements CardIf {

  private static final long serialVersionUID = 7810243947702126400L;

  private String _internalInstanceName = null;

  public Card() {
    this._internalInstanceName = "";
  }

  public Card(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * Protege name: suit
   */
   private String suit;
   public void setSuit(String value) { 
    this.suit=value;
   }
   public String getSuit() {
     return this.suit;
   }

   /**
   * Protege name: rank
   */
   private String rank;
   public void setRank(String value) { 
    this.rank=value;
   }
   public String getRank() {
     return this.rank;
   }

   /**
   * Protege name: value
   */
   private int value;
   public void setValue(int value) { 
    this.value=value;
   }
   public int getValue() {
     return this.value;
   }

}

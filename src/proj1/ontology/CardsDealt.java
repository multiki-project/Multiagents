package proj1.ontology;


import jade.util.leap.*;

/**
* Protege name: CardsDealt
* @author ontology bean generator
* @version 2026/04/22, 17:37:03
*/
public class CardsDealt implements CardsDealtIf {

  private static final long serialVersionUID = 7810243947702126400L;

  private String _internalInstanceName = null;

  public CardsDealt() {
    this._internalInstanceName = "";
  }

  public CardsDealt(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * Protege name: cards
   */
   private List cards = new ArrayList();
   public void addCards(Card elem) { 
     cards.add(elem);
   }
   public boolean removeCards(Card elem) {
     boolean result = cards.remove(elem);
     return result;
   }
   public void clearAllCards() {
     cards.clear();
   }
   public Iterator getAllCards() {return cards.iterator(); }
   public List getCards() {return cards; }
   public void setCards(List l) {cards = l; }

}

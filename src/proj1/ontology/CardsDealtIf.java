package proj1.ontology;


import jade.util.leap.*;

/**
* Protege name: CardsDealt
* @author ontology bean generator
* @version 2026/04/22, 17:37:03
*/
public interface CardsDealtIf extends jade.content.Predicate {

   /**
   * Protege name: cards
   */
   public void addCards(Card elem);
   public boolean removeCards(Card elem);
   public void clearAllCards();
   public Iterator getAllCards();
   public List getCards();
   public void setCards(List l);

}

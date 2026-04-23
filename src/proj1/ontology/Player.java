package proj1.ontology;


/**
* Protege name: Player
* @author ontology bean generator
* @version 2026/04/22, 17:37:03
*/
public class Player implements PlayerIf {

  private static final long serialVersionUID = 7810243947702126400L;

  private String _internalInstanceName = null;

  public Player() {
    this._internalInstanceName = "";
  }

  public Player(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * Protege name: Name
   */
   private String name;
   public void setName(String value) { 
    this.name=value;
   }
   public String getName() {
     return this.name;
   }

   /**
   * Protege name: balance
   */
   private float balance;
   public void setBalance(float value) { 
    this.balance=value;
   }
   public float getBalance() {
     return this.balance;
   }

}

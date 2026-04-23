package proj1.ontology;


/**
* Protege name: RequestRules
* @author ontology bean generator
* @version 2026/04/22, 17:37:03
*/
public class RequestRules implements RequestRulesIf {

  private static final long serialVersionUID = 7810243947702126400L;

  private String _internalInstanceName = null;

  public RequestRules() {
    this._internalInstanceName = "";
  }

  public RequestRules(String instance_name) {
    this._internalInstanceName = instance_name;
  }

  public String toString() {
    return _internalInstanceName;
  }

   /**
   * Protege name: gamename
   */
   private String gamename;
   public void setGamename(String value) { 
    this.gamename=value;
   }
   public String getGamename() {
     return this.gamename;
   }

}

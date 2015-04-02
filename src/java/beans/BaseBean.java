package beans;

import javax.annotation.Resource;
import javax.faces.bean.ManagedProperty;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import models.UserAccount;

public abstract class BaseBean {

    @PersistenceContext(unitName = "Security-lab1PU")
    protected EntityManager em;

    @Resource
    protected UserTransaction utx;
    
    @ManagedProperty(value="#{sessionBean}")
    protected SessionBean sessionBean;
    
    /**
     * Creates a new instance of BaseBean
     */
    public BaseBean() {
    }

    public SessionBean getSessionBean() {
        return sessionBean;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }
    
    public EntityManager getEntityManager() {
        return em;
    }
    
    public boolean isLoggedIn() {
        if (sessionBean != null) {
            if (sessionBean.getUser() != null) {
                return true;
            }
        }
        return false;
    }
}

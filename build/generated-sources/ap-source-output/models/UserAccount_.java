package models;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.5.2.v20140319-rNA", date="2015-02-12T14:04:14")
@StaticMetamodel(UserAccount.class)
public class UserAccount_ { 

    public static volatile SingularAttribute<UserAccount, byte[]> password;
    public static volatile SingularAttribute<UserAccount, byte[]> salt;
    public static volatile SingularAttribute<UserAccount, Long> id;
    public static volatile SingularAttribute<UserAccount, String> username;

}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package validators;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 *
 * @author Franck
 */
@FacesValidator("passwordValidator")
public class PasswordValidator implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        try {
            String password = (String)value;
            
            if (password.length() < 6) {
                FacesMessage message = new FacesMessage("Your password must contain at least 6 characters.");
                throw new ValidatorException(message);
            }
            
        } catch (ClassCastException e) {
            FacesMessage message = new FacesMessage("Value must be a a String.");
            throw new ValidatorException(message);
        }
    }
    
}

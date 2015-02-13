/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package validators;

import beans.CreateAccountBean;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
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
            
            if ("confirm".equals(component.getId())) {
                // Find the confirm component
                UIComponent parent = component.getParent();
                String passwordId = "passwd";
                UIInput textInput = (UIInput) parent.findComponent(passwordId);
                if (textInput == null)
                    throw new IllegalArgumentException(String.format("Unable to find component with id %s",passwordId));
                // Get its value, the entered text of the first field.
                String field1 = (String) textInput.getValue();

                // Cast the value of the entered text of the second field back to String.
                String confirm = (String) value;

                // Check if the first text is actually entered and compare it with second text.
                if (field1 != null && field1.length() != 0 && !field1.equals(confirm)) {
                    throw new ValidatorException(new FacesMessage("Password confirmation does not match."));
                }
            }
            
        } catch (ClassCastException e) {
            FacesMessage message = new FacesMessage("Value must be a a String.");
            throw new ValidatorException(message);
        }
    }
    
}

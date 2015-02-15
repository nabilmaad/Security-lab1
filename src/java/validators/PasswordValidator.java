/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package validators;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Franck
 */
@FacesValidator("passwordValidator")
public class PasswordValidator implements Validator {
    
    private Pattern pattern;
    private Matcher matcher;
    
    private static final String PASSWORD_PATTERN = 
              "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{6,20})";
 
    public PasswordValidator(){
            pattern = Pattern.compile(PASSWORD_PATTERN);
    }

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        try {
            String password = (String)value;
            
            if (!validate(password)) {
                FacesMessage message = new FacesMessage("Your password must contain 6 to 20 characters with at least one digit, one upper case letter, one lower case letter and one of the following characters: @#$%");
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
    
    /**
     * Validate password with regular expression
     * @param password password for validation
     * @return true valid password, false invalid password
     */
    public boolean validate(final String password){

            matcher = pattern.matcher(password);
            return matcher.matches();

    }
    
}

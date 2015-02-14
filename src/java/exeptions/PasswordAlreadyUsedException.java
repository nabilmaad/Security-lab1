/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exeptions;

/**
 *
 * @author Franck
 */
public class PasswordAlreadyUsedException extends Exception {
    public PasswordAlreadyUsedException(String message) {
       super(message);
    }
}

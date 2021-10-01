package com.vijay.jsonwizard.validators.textinputlayout;

import androidx.annotation.NonNull;

public class EmailValidator extends TILValidator{

    public EmailValidator(String errorMessage) {
        super(errorMessage);
    }

    @Override
    public boolean isValid(@NonNull CharSequence var1, boolean isEmpty) {
        if(!isEmpty) {
            String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
            if (!var1.toString().matches(emailPattern) && var1.toString().length() > 0) {
                return false;
            }
        }
        return true;
    }
}

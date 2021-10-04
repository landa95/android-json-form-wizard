package com.vijay.jsonwizard.validators.textinputlayout;

import androidx.annotation.NonNull;

import java.util.regex.Pattern;

public class RegexpValidator extends TILValidator{
    private Pattern pattern;

    public RegexpValidator(String errorMessage, String pattern){
        super(errorMessage);
        this.pattern = Pattern.compile(pattern);
    }

    @Override
    public boolean isValid(@NonNull CharSequence var1, boolean var2) {
        return pattern.matcher(var1.toString()).matches();
    }
}

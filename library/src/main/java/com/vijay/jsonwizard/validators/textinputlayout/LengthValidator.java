package com.vijay.jsonwizard.validators.textinputlayout;


/**
 * Created by landa95 04/10/2021
 * Copyright 2014 rengwuxian
 * com.rengwuxian.materialedittext.validation.edittext.LengthValidator
 */

public class LengthValidator extends TILValidator {

    private int minLength = 0;
    private int maxLength = Integer.MAX_VALUE;

    public LengthValidator(String errorMessage, int minLength, int maxLength) {
        super(errorMessage);
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    @Override
    public boolean isValid(CharSequence charSequence, boolean isEmpty) {
        if (!isEmpty) {
            if (charSequence.length() >= minLength && charSequence.length() <= maxLength) {
                return true;
            }
            return false;
        } else {
            return true;
        }
    }
}

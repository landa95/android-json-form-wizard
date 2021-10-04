package com.vijay.jsonwizard.validators.textinputlayout;


/**
 * Created by landa95 04/10/2021
 * Copyright 2014 rengwuxian
 * com.rengwuxian.materialedittext.validation.edittext.MaxLengthValidator
 */

public class MaxLengthValidator extends LengthValidator{

    public MaxLengthValidator(String errorMessage, int maxLength) {
        super(errorMessage, 0, maxLength);
    }

}

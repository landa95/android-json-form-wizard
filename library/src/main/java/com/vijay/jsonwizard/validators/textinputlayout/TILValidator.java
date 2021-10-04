package com.vijay.jsonwizard.validators.textinputlayout;

import androidx.annotation.NonNull;

    /**
     * Created by landa95 30/09/2021
     * Copyright 2014 rengwuxian
     * com.rengwuxian.materialedittext.validation..edittext.METValidator
     */
public abstract class TILValidator {

        protected String errorMessage;

        public TILValidator(@NonNull String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public void setErrorMessage(@NonNull String errorMessage) {
            this.errorMessage = errorMessage;
        }

        @NonNull
        public String getErrorMessage() {
            return this.errorMessage;
        }

        public abstract boolean isValid(@NonNull CharSequence var1, boolean var2);

}

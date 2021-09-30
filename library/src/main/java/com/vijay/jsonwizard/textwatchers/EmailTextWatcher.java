package com.vijay.jsonwizard.textwatchers;

import android.content.Context;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

public class EmailTextWatcher implements TextWatcher {

    private TextInputLayout textInputLayout;
    private Context context;

    public EmailTextWatcher (){
        super();
    }

    public EmailTextWatcher (Context c, TextInputLayout textInputLayout){
        super();
        this.context = c;
        this.textInputLayout = textInputLayout;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        String inputText = editable.toString().trim();
        String  emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if(!(inputText.matches(emailPattern)) && (inputText.length() > 0)){
            textInputLayout.setErrorEnabled(true);
            textInputLayout.setError("Is not a valid email");
        }else{
            textInputLayout.setErrorEnabled(false);
        }
    }
}

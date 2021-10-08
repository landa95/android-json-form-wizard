package com.vijay.jsonwizard.presenters;

import static com.vijay.jsonwizard.constants.JsonFormConstants.MAX_PARCEL_SIZE;
import static com.vijay.jsonwizard.maps.MapsActivity.EXTRA_CONFIG_DEFAULT_ZOOM;
import static com.vijay.jsonwizard.maps.MapsActivity.EXTRA_CONFIG_MAX_ZOOM;
import static com.vijay.jsonwizard.maps.MapsActivity.EXTRA_CONFIG_MIN_ZOOM;
import static com.vijay.jsonwizard.maps.MapsActivity.EXTRA_CUSTOM_MARKER_ICON;
import static com.vijay.jsonwizard.maps.MapsActivity.EXTRA_INITIAL_LOCATION;
import static com.vijay.jsonwizard.maps.MapsActivity.EXTRA_RESULT_LOCATION;
import static com.vijay.jsonwizard.maps.MapsActivity.EXTRA_USE_ACCURACY;
import static com.vijay.jsonwizard.resourceviewer.WebViewActivity.EXTRA_RESOURCE;
import static com.vijay.jsonwizard.resourceviewer.WebViewActivity.EXTRA_TITLE;
import static com.vijay.jsonwizard.widgets.LocationPickerFactory.KEY_SUFFIX_ACCURACY;
import static com.vijay.jsonwizard.widgets.LocationPickerFactory.KEY_SUFFIX_LATITUDE;
import static com.vijay.jsonwizard.widgets.LocationPickerFactory.KEY_SUFFIX_LONGITUDE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.Switch;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.barcode.LivePreviewActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.customviews.CheckBox;
import com.vijay.jsonwizard.customviews.MaterialTextInputLayout;
import com.vijay.jsonwizard.customviews.RadioButton;
import com.vijay.jsonwizard.demo.resources.ResourceResolver;
import com.vijay.jsonwizard.expressions.JsonExpressionResolver;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.i18n.JsonFormBundle;
import com.vijay.jsonwizard.interactors.JsonFormInteractor;
import com.vijay.jsonwizard.maps.MapsActivity;
import com.vijay.jsonwizard.maps.MapsUtils;
import com.vijay.jsonwizard.mvp.MvpBasePresenter;
import com.vijay.jsonwizard.resourceviewer.WebViewActivity;
import com.vijay.jsonwizard.state.StateProvider;
import com.vijay.jsonwizard.utils.CarouselAdapter;
import com.vijay.jsonwizard.utils.DateUtils;
import com.vijay.jsonwizard.utils.ImagePicker;
import com.vijay.jsonwizard.utils.ImageUtils;
import com.vijay.jsonwizard.utils.JsonFormUtils;
import com.vijay.jsonwizard.utils.ResourceViewer;
import com.vijay.jsonwizard.utils.ValidationStatus;
import com.vijay.jsonwizard.views.JsonFormFragmentView;
import com.vijay.jsonwizard.viewstates.JsonFormFragmentViewState;
import com.vijay.jsonwizard.widgets.BarcodeTextFactory;
import com.vijay.jsonwizard.widgets.CarouselFactory;
import com.vijay.jsonwizard.widgets.DatePickerFactory;
import com.vijay.jsonwizard.widgets.EditTextFactory;
import com.vijay.jsonwizard.widgets.ImagePickerFactory;
import com.vijay.jsonwizard.widgets.LocationPickerFactory;
import com.vijay.jsonwizard.widgets.MaterialEditTextFactory;
import com.vijay.jsonwizard.widgets.SpinnerFactory;
import com.yarolegovich.discretescrollview.DiscreteScrollView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import fr.ganfra.materialspinner.MaterialSpinner;

/**
 * Created by vijay on 5/14/15.
 */
public class JsonFormFragmentPresenter extends MvpBasePresenter<JsonFormFragmentView<JsonFormFragmentViewState>> {
    private static final String TAG = "FormFragmentPresenter";
    private static final int RESULT_LOAD_IMG = 1;
    private static final int RESULT_LOAD_BARCODE = 2;
    private static final int RESULT_LOAD_LOCATION = 3;
    private static final int RESULT_RESOURCE_VIEW = 4;

    private static final String PARAM_BARCODE = "barcode";
    private static final String PARAM_ERROR = "error";
    private static final Pattern URI_PATTERN = Pattern.compile("^\\w+:[^\\s]+$");
    private static final Pattern INTENT_PATTERN = Pattern.compile("^intent:\\/\\/[^\\s]+\\/[^\\s|\\?]+(\\?([^\\s|=]+=[^\\s|&]+(&[^\\s|=]+=[^\\s|&]+)*)?)?$");

    private String mStepName;
    private JSONObject mStepDetails;
    private String mCurrentKey;
    private int mVisualizationMode;
    private JsonFormInteractor mJsonFormInteractor = JsonFormInteractor.getInstance();

    public void addFormElements() {
        switch (mVisualizationMode) {
            case JsonFormConstants.VISUALIZATION_MODE_READ_ONLY:
                addFormReadOnlylements();
                break;
            default:
                addFormEditionElements();
        }
    }

    private void addFormEditionElements() {
        mStepName = getView().getArguments().getString("stepName");
        JSONObject step = getView().getStep(mStepName);
        JsonFormBundle bundle = getView().getBundle(getView().getContext().getResources().getConfiguration().locale);
        JsonExpressionResolver resolver = getView().getExpressionResolver();
        ResourceResolver resourceResolver = getView().getResourceResolver();

        try {
            mStepDetails = new JSONObject(step.toString());
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        List<View> views = getStepFormElements(mStepName, mStepDetails, bundle, resolver, resourceResolver);
        getView().addFormElements(views);
    }

    private void addFormReadOnlylements() {
        String stepName = getView().getArguments().getString("stepName");
        JSONObject step = getView().getStep(stepName);
        JsonFormBundle bundle = getView().getBundle(getView().getContext().getResources().getConfiguration().locale);
        JsonExpressionResolver resolver = getView().getExpressionResolver();
        ResourceResolver resourceResolver = getView().getResourceResolver();

        List<View> views = getStepFormElements(stepName, step, bundle, resolver, resourceResolver);
        while (step.has("next")) {
            try {
                String nextStep = JsonFormUtils.resolveNextStep(step, resolver,
                        new JSONObject(getView().getCurrentJsonState()));
                if (JsonFormConstants.END_STEP_NAME.equals(nextStep)) {
                    // Break while loop, "next" step is fake
                    mStepDetails = step;
                    break;
                } else {
                    step = getView().getStep(nextStep);
                    views.addAll(
                            getStepFormElements(stepName, step, bundle, resolver, resourceResolver));
                }
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
        getView().addFormElements(views);
    }

    private List<View> getStepFormElements(String stepName, JSONObject stepDetails, JsonFormBundle bundle,
                                           JsonExpressionResolver resolver, ResourceResolver resourceResolver) {
        List<View> views = mJsonFormInteractor.fetchFormElements(stepName, getView().getContext(), stepDetails,
                getView().getCommonListener(), bundle, resolver, resourceResolver, mVisualizationMode);
        return views;
    }

    @SuppressLint("ResourceAsColor")
    public void setUpToolBar() {
        if (mVisualizationMode == JsonFormConstants.VISUALIZATION_MODE_EDIT) {
            if (!mStepName.equals(JsonFormConstants.FIRST_STEP_NAME)) {
                getView().setUpBackButton();
            }
            JsonFormBundle bundle = getView().getBundle(
                    getView().getContext().getResources().getConfiguration().locale);
            getView().setActionBarTitle(bundle.resolveKey(mStepDetails.optString("title")));
            if (mStepDetails.has("next")) {
                getView().updateVisibilityOfNextAndSave(true, false);
            } else {
                getView().updateVisibilityOfNextAndSave(false, true);
            }
        } else {
            getView().setActionBarTitle(getView().getContext().getResources().getString(R.string.summary));
            if (mStepDetails != null && mStepDetails.has("next")) {
                // Special case: form with next step "end" should show "next" option
                getView().updateVisibilityOfNextAndSave(true, false);
            } else {
                getView().updateVisibilityOfNextAndSave(false, false);
            }
        }
        setUpToolBarTitleColor();
    }

    public void setUpToolBarTitleColor() {
        getView().setToolbarTitleColor(R.color.white);
    }

    public void onBackClick() {
        getView().historyPop();
        getView().hideKeyBoard();
        getView().backClick();
    }

    /*private String resolveNextStep(JSONObject mStepDetails) {
        try {
            JSONObject nextObject = mStepDetails.optJSONObject("next");
            if (nextObject != null) {
                JSONArray names = nextObject.names();
                for (int i = 0; i < names.length(); i++) {
                    if (isDefaultStep(nextObject, names.optString(i))) {
                        return names.optString(i);
                    } else {
                        JsonExpressionResolver resolver = getView().getExpressionResolver();
                        String expression = nextObject.optString(names.optString(i));
                        if (resolver.isValidExpression(expression)) {
                            boolean eval = resolver.existsExpression(nextObject.optString(names.optString(i)),
                                getCurrentValues());
                            if (eval) {
                                return names.optString(i);
                            }
                        } else {
                            Log.e(TAG, "resolveNextStep: Error evaluating next step - Expression is not valid");
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "resolveNextStep: Error evaluating next step", e);
        }
        return mStepDetails.optString("next");
    }


    private boolean isDefaultStep(JSONObject steps, String key) {
        try {
            return steps.getBoolean(key);
        } catch (JSONException e) {
            return false;
        }
    }*/

    public void onNextClick(LinearLayout mainView) {
        // Special case, when form has a next step "end", we allow visualization mode to have a next menu option
        if (mVisualizationMode == JsonFormConstants.VISUALIZATION_MODE_READ_ONLY) {
            Intent returnIntent = new Intent();
            getView().finishWithResult(returnIntent);
            return;
        }
        try {
            ValidationStatus validationStatus = writeValuesAndValidate(mainView);
            if (validationStatus.isValid()) {
                getView().historyPush(mStepName);
                String nextStep = JsonFormUtils.resolveNextStep(mStepDetails, getView().getExpressionResolver(), new JSONObject(getView().getCurrentJsonState()));
                if (JsonFormConstants.END_STEP_NAME.equals(nextStep)) {
                    onSaveClick(mainView);
                } else {
                    JsonFormFragment next = JsonFormFragment.getFormFragment(nextStep);
                    getView().hideKeyBoard();
                    getView().transactThis(next);
                }
            } else {
                getView().showToast(validationStatus.getErrorMessage());
            }
        } catch (JSONException e) {
            Log.e(TAG, "onNextClick: Error evaluating next step", e);
        }
    }

    public ValidationStatus writeValuesAndValidate(LinearLayout mainView) {
        String type = (String) mainView.getTag(R.id.type);

        int childCount = mainView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = mainView.getChildAt(i);
            String key = (String) childAt.getTag(R.id.key);
            if (childAt instanceof MaterialEditText) {
                MaterialEditText editText = (MaterialEditText) childAt;
                if (editText.getTag(R.id.type).equals(JsonFormConstants.EDIT_TEXT)) {
                    ValidationStatus validationStatus = EditTextFactory.validate(editText);
                    if (!validationStatus.isValid()) {
                        return validationStatus;
                    }
                    if (JsonFormConstants.EDIT_GROUP.equals(type)) {
                        String parentKey = (String) mainView.getTag(R.id.key);
                        String childKey = (String) childAt.getTag(R.id.key);
                        getView().writeValue(mStepName, parentKey, JsonFormConstants.FIELDS_FIELD_NAME, childKey,
                                editText.getText().toString());
                    } else {
                        getView().writeValue(mStepName, key, editText.getText().toString());
                    }
                } else if (editText.getTag(R.id.type).equals(JsonFormConstants.DATE_PICKER)) {
                    ValidationStatus validationStatus = DatePickerFactory.validate(editText);
                    if (!validationStatus.isValid()) {
                        return validationStatus;
                    }
                    Date date = DateUtils.parseDate(editText.getText().toString(),
                            (String) editText.getTag(R.id.v_pattern));
                    if (JsonFormConstants.EDIT_GROUP.equals(type)) {
                        String parentKey = (String) childAt.getTag(R.id.key);
                        String childKey = (String) childAt.getTag(R.id.childKey);
                        getView().writeValue(mStepName, parentKey, JsonFormConstants.FIELDS_FIELD_NAME, childKey,
                                DateUtils.toJSONDateFormat(date));
                    } else {
                        getView().writeValue(mStepName, key, DateUtils.toJSONDateFormat(date));
                    }
                } /*else if (editText.getTag(R.id.type).equals(JsonFormConstants.BARCODE_TEXT)) {
                    ValidationStatus validationStatus = BarcodeTextFactory.validate(editText);
                    if (!validationStatus.isValid()) {
                        return validationStatus;
                    }
                    if (JsonFormConstants.EDIT_GROUP.equals(type)) {
                        String parentKey = (String) mainView.getTag(R.id.key);
                        String childKey = (String) childAt.getTag(R.id.key);
                        getView().writeValue(mStepName, parentKey, JsonFormConstants.FIELDS_FIELD_NAME, childKey,
                                editText.getText().toString());
                    } else {
                        getView().writeValue(mStepName, key, editText.getText().toString());
                    }
                }*/ else if (editText.getTag(R.id.type).equals(JsonFormConstants.TIME_PICKER)) {
                    ValidationStatus validationStatus = EditTextFactory.validate(editText);
                    if (!validationStatus.isValid()) {
                        return validationStatus;
                    }
                    if (JsonFormConstants.EDIT_GROUP.equals(type)) {
                        String parentKey = (String) mainView.getTag(R.id.key);
                        String childKey = (String) childAt.getTag(R.id.key);
                        getView().writeValue(mStepName, parentKey, JsonFormConstants.FIELDS_FIELD_NAME, childKey,
                                editText.getText().toString());
                    } else {
                        getView().writeValue(mStepName, key, editText.getText().toString());
                    }
                }

            } else if (childAt instanceof ImageView) {
                ValidationStatus validationStatus = ImagePickerFactory.validate((ImageView) childAt);
                if (!validationStatus.isValid()) {
                    return validationStatus;
                }
                Object path = childAt.getTag(R.id.imagePath);
                if (path instanceof String) {
                    getView().writeValue(mStepName, key, (String) path);
                } else {
                    getView().writeValue(mStepName, key, null);
                }
            } else if (childAt instanceof CheckBox) {
                String parentKey = (String) childAt.getTag(R.id.key);
                String childKey = (String) childAt.getTag(R.id.childKey);
                getView().writeValue(mStepName, parentKey, JsonFormConstants.OPTIONS_FIELD_NAME, childKey,
                        String.valueOf(((CheckBox) childAt).isChecked()));
            } else if (childAt instanceof RadioButton) {
                String parentKey = (String) childAt.getTag(R.id.key);
                String childKey = (String) childAt.getTag(R.id.childKey);
                if (((RadioButton) childAt).isChecked()) {
                    getView().writeValue(mStepName, parentKey, childKey);
                }
            } else if (childAt instanceof MaterialSpinner) {
                MaterialSpinner spinner = (MaterialSpinner) childAt;
                ValidationStatus validationStatus = SpinnerFactory.validate(spinner);
                if (!validationStatus.isValid()) {
                    spinner.setError(validationStatus.getErrorMessage());
                    return validationStatus;
                } else {
                    spinner.setError(null);
                }
            } else if (childAt instanceof DiscreteScrollView) {
                DiscreteScrollView dsv = (DiscreteScrollView) childAt;
                ValidationStatus validationStatus = CarouselFactory.validate(dsv);
                if (!validationStatus.isValid()) {
                    return validationStatus;
                }
            } else if (childAt instanceof MaterialTextInputLayout){
                MaterialTextInputLayout textInputLayout = (MaterialTextInputLayout) childAt;
                EditText editText = textInputLayout.getEditText();
                if(editText.getTag(R.id.type).equals(JsonFormConstants.EDIT_TEXT)){
                    ValidationStatus validationStatus = MaterialEditTextFactory.validate(textInputLayout);
                    if(!validationStatus.isValid()){
                        return validationStatus;
                    }
                }else if(editText.getTag(R.id.type).equals(JsonFormConstants.BARCODE_TEXT)){
                    ValidationStatus validationStatus = BarcodeTextFactory.validate(textInputLayout);
                    if(!validationStatus.isValid()){
                        return validationStatus;
                    }
                }


            } else if (childAt instanceof LinearLayout) {
                if (JsonFormConstants.LOCATION_PICKER.equals(childAt.getTag(R.id.type))) {
                    ValidationStatus validationStatus = writeValuesAndValidateLocationPicker((LinearLayout) childAt, key);
                    if (!validationStatus.isValid()) {
                        return validationStatus;
                    }
                } else {
                    ValidationStatus validationStatus = writeValuesAndValidate((LinearLayout) childAt);
                    if (!validationStatus.isValid()) {
                        return validationStatus;
                    }
                }
            }
        }
        return new ValidationStatus(true, null);
    }

    private ValidationStatus writeValuesAndValidateLocationPicker(LinearLayout widget, String key) {
        MaterialEditText etLatitude = null;
        MaterialEditText etLongitude = null;
        MaterialEditText etAccuracy = null;
        LinearLayout valueContainer = widget.findViewById(R.id.value_container);
        for (int i = 0; i < valueContainer.getChildCount(); i++) {
            View childView = valueContainer.getChildAt(i);
            if (childView instanceof MaterialEditText) {
                String childKey = (String) childView.getTag(R.id.key);
                MaterialEditText met = (MaterialEditText) childView;
                if (childKey.endsWith(KEY_SUFFIX_LATITUDE)) {
                    etLatitude = met;
                } else if (childKey.endsWith(KEY_SUFFIX_LONGITUDE)) {
                    etLongitude = met;
                } else if (childKey.endsWith(KEY_SUFFIX_ACCURACY)) {
                    etAccuracy = met;
                }
                ValidationStatus validationStatus = LocationPickerFactory.validate(met);
                if (!validationStatus.isValid()) {
                    return validationStatus;
                }
            }
        }

        String value;
        if (etAccuracy != null && !etAccuracy.getText().toString().isEmpty()) {
            value = MapsUtils.toString(etLatitude.getText().toString(),
                    etLongitude.getText().toString(), etAccuracy.getText().toString());
        } else {
            value = MapsUtils.toString(etLatitude.getText().toString(),
                    etLongitude.getText().toString());
        }
        getView().writeValue(mStepName, key, value);
        return new ValidationStatus(true, null);
    }

    public void onSaveClick(LinearLayout mainView) {
        ValidationStatus validationStatus = writeValuesAndValidate(mainView);
        if (validationStatus.isValid()) {
            getView().historyPush(mStepName);
            Intent returnIntent = new Intent();
            String json = getView().getCurrentJsonState();
            // Avoid sending more than 200Kb as intent extra
            if (json != null && json.length() >= MAX_PARCEL_SIZE) {
                Uri uri = StateProvider.saveState(getView().getContext(), json);
                returnIntent.putExtra("uri", uri);
            } else {
                returnIntent.putExtra("json", json);
            }
            getView().finishWithResult(returnIntent);
        } else {
            Toast.makeText(getView().getContext(), validationStatus.getErrorMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_LOAD_IMG) {
            Context context = getView().getContext();
            Bitmap bitmap = ImagePicker.getImageFromResult(context, resultCode, data);
            //
            if (bitmap != null) {
                File image = new File(context.getExternalCacheDir(), System.currentTimeMillis() + ".jpg");
                ImageUtils.saveToFile(bitmap, image);
                getView().updateRelevantImageView(bitmap, image.getAbsolutePath(), mCurrentKey,mStepName);
            }
        }

        if (requestCode == RESULT_LOAD_BARCODE) {
            if (data != null && data.hasExtra(PARAM_BARCODE)) {

                String value = data.getStringExtra(PARAM_BARCODE);
                mStepName = getView().getArguments().getString("stepName");
                getView().writeValue(mStepName, mCurrentKey, value);
                getView().updateRelevantEditText(mCurrentKey, value);
            }
            if (data != null && data.hasExtra(PARAM_ERROR)) {
                Toast.makeText(getView().getContext(), R.string.mlkit_not_found, Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == RESULT_LOAD_LOCATION) {
            handleResultLocation(resultCode, data);
        }
    }

    private void handleResultLocation(int resultCode, Intent data) {
        Log.i(TAG, "LOCATION RESULT: " + resultCode);
        if (resultCode == Activity.RESULT_OK && data != null) {
            String location = data.getStringExtra(EXTRA_RESULT_LOCATION);
            Log.i(TAG, "LOCATION RESULT: " + location);
            mStepName = getView().getArguments().getString("stepName");
            getView().writeValue(mStepName, mCurrentKey, location);
            String[] parts = location.split(MapsUtils.COORD_SEPARATOR);
            if (parts.length > 0) {
                getView().updateRelevantEditText(mCurrentKey + KEY_SUFFIX_LATITUDE, parts[0].trim());
            }
            if (parts.length > 1) {
                getView().updateRelevantEditText(mCurrentKey + KEY_SUFFIX_LONGITUDE, parts[1].trim());
            }
            if (parts.length > 2) {
                getView().updateRelevantEditText(mCurrentKey + KEY_SUFFIX_ACCURACY, parts[2].trim());
            }
            getView().updateRelevantMap(mCurrentKey, location);
        }
    }

    public void onClick(View v) {

        String key = (String) v.getTag(R.id.key);
        String type = (String) v.getTag(R.id.type);
        if (checkFormPermissions()) {
            if (JsonFormConstants.CHOOSE_IMAGE.equals(type)) {
                mCurrentKey = key;
                if (v.getTag(R.id.btn_clear) != null) {
                    getView().updateRelevantImageView(null, null, key, mStepName);
                    v.setVisibility(View.GONE);
                } else {
                    getView().hideKeyBoard();
                    Intent pickerIntent = ImagePicker.getPickImageIntent(v.getContext());
                    getView().startActivityForResult(pickerIntent, RESULT_LOAD_IMG);
                }
            }

            if (JsonFormConstants.BARCODE_TEXT.equals(type)) {
                Log.d(TAG, "onClick: barcode");
                getView().hideKeyBoard();
                Intent barcodeIntent = new Intent(v.getContext(), LivePreviewActivity.class);
                mCurrentKey = key;
                getView().startActivityForResult(barcodeIntent, RESULT_LOAD_BARCODE);
            }
        } else {
            Log.w(TAG, "CAMERA and STORAGE permissions required to use IMAGE or BARCODE widgets");
        }

        if (JsonFormConstants.LOCATION_PICKER.equals(type)) {
            Log.d(TAG, "onClick: location");
            getView().hideKeyBoard();
            Intent intent = new Intent(v.getContext(), MapsActivity.class);
            String value = (String) v.getTag(R.id.value);
            boolean useAccuracy = (boolean) v.getTag(R.id.accuracy);
            if (value != null && MapsUtils.isValidPositionString(value)) {
                intent.putExtra(EXTRA_INITIAL_LOCATION, value);
            }
            String customIcon = (String) v.getTag(R.id.custom_icon);
            if (customIcon != null) {
                intent.putExtra(EXTRA_CUSTOM_MARKER_ICON, customIcon);
            }
            intent.putExtra(EXTRA_USE_ACCURACY, useAccuracy);
            intent.putExtra(EXTRA_CONFIG_MIN_ZOOM, (Double) v.getTag(R.id.map_min_zoom));
            intent.putExtra(EXTRA_CONFIG_MAX_ZOOM, (Double) v.getTag(R.id.map_max_zoom));
            Double defaultZoom = (Double) v.getTag(R.id.map_default_zoom);
            if (defaultZoom != null && !defaultZoom.isNaN()) {
                intent.putExtra(EXTRA_CONFIG_DEFAULT_ZOOM, defaultZoom);
            }
            mCurrentKey = key;
            getView().startActivityForResult(intent, RESULT_LOAD_LOCATION);
        }

        if (JsonFormConstants.RESOURCE_VIEWER.equals(type)) {
            mCurrentKey = key;
            getView().hideKeyBoard();
            String resource = (String) v.getTag(R.id.value);
            if (resource.endsWith(".html") || resource.endsWith(".htm")) {
                Intent intent = new Intent(v.getContext(), WebViewActivity.class);
                String title = (String) v.getTag(R.id.label);
                intent.putExtra(EXTRA_TITLE, title);
                intent.putExtra(EXTRA_RESOURCE, resource);
                getView().startActivityForResult(intent, RESULT_RESOURCE_VIEW);
            } else if (new File(resource).exists()) {
                // Attempt to launch open file intent
                Intent intent = ResourceViewer.getOpenFileIntent(getView().getContext(), resource);
                getView().startActivityForResult(intent, RESULT_RESOURCE_VIEW);
            } else if (INTENT_PATTERN.matcher(resource).matches()) {
                Uri uri = Uri.parse(resource);
                Intent intent = ResourceViewer.getCustomUriIntent(v.getContext(), uri);
                if (intent != null) {
                    getView().startActivityForResult(intent, RESULT_RESOURCE_VIEW);
                } else {
                    Log.w(TAG, "Cannot launch intent: " + resource);
                }
            } else if (URI_PATTERN.matcher(resource).matches()) {
                Uri uri = Uri.parse(resource);
                Intent intent = ResourceViewer.getOpenUriIntent(uri);
                getView().startActivityForResult(intent, RESULT_RESOURCE_VIEW);
            } else {
                Log.w(TAG, "Unsupported resource: " + resource);
            }
        }
    }

    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (compoundButton instanceof CheckBox) {
            String parentKey = (String) compoundButton.getTag(R.id.key);
            String childKey = (String) compoundButton.getTag(R.id.childKey);
            getView().writeValue(mStepName, parentKey, JsonFormConstants.OPTIONS_FIELD_NAME, childKey,
                    String.valueOf(((CheckBox) compoundButton).isChecked()));
        } else if (compoundButton instanceof RadioButton) {
            if (isChecked) {
                String parentKey = (String) compoundButton.getTag(R.id.key);
                String childKey = (String) compoundButton.getTag(R.id.childKey);
                getView().unCheckAllExcept(parentKey, childKey);
                getView().writeValue(mStepName, parentKey, childKey);
            }
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String parentKey = (String) parent.getTag(R.id.key);
        if (position >= 0) {
            Object value = parent.getItemAtPosition(position + 1);
            if (value instanceof SpinnerFactory.ValueLabelPair) {
                getView().writeValue(mStepName, parentKey,
                        ((SpinnerFactory.ValueLabelPair) value).getValue());
            } else {
                getView().writeValue(mStepName, parentKey, (String) value);
            }
        }
    }

    public void onCurrentItemChanged(@Nullable CarouselAdapter.ViewHolder holder, int position) {
        if (holder != null) {
            ViewParent parent = holder.itemView.getParent();
            if (parent != null && parent instanceof DiscreteScrollView) {
                DiscreteScrollView dsvParent = (DiscreteScrollView) parent;
                String parentKey = (String) dsvParent.getTag(R.id.key);
                View view = holder.itemView.findViewById(R.id.value);
                if (view != null && view instanceof TextView) {
                    TextView textView = (TextView) view;
                    String value = textView.getText().toString();
                    JsonFormFragmentView jffview = getView();
                    if (jffview != null) {
                        jffview.writeValue(mStepName, parentKey, value);
                    }
                }
            }
        }
    }

    public void onSwitchOnOrOff(Switch v, boolean checked) {
        String key = (String) v.getTag(R.id.key);
        getView().writeValue(mStepName, key, String.valueOf(checked));
    }

    public String getCurrentKey() {
        return mCurrentKey;
    }

    public void setCurrentKey(String key) {
        this.mCurrentKey = key;
    }

    public String getStepName() {
        return mStepName;
    }

    public void setVisualizationMode(int visualizationMode) {
        this.mVisualizationMode = visualizationMode;
    }

    private boolean checkFormPermissions() {

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

        if (!hasPermissions(getView().getContext(), PERMISSIONS)) {
            JsonFormFragment formFragment = (JsonFormFragment) getView();
            ActivityCompat.requestPermissions(formFragment.getActivity(), PERMISSIONS, PERMISSION_ALL);
        } else {
            return true;
        }
        return false;
    }

    private boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}

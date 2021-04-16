package smallville7123.AndroidDAW.SDK.UI;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import androidx.annotation.StyleableRes;

import smallville7123.AndroidDAW.SDK.UI.R;

import static android.R.layout.simple_spinner_dropdown_item;
import static android.R.layout.simple_spinner_item;

/**
 * TODO: document your custom view class.
 */
public class SpinnerView extends FrameLayout {
    public SpinnerView(Context context) {
        super(context);
        init(null, 0);
    }

    public SpinnerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SpinnerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    Spinner spinner;
    SpinnerAdapter spinnerAdapter;
    FrameLayout frameLayout;

    CharSequence[] names = null;
    ArrayAdapter<CharSequence> adapterNames;

    int[] layouts = null;

    int position = -1;
    int oldViewResId = 0;
    View oldView = null;
    int newViewResId = 0;
    View newView = null;

    public int[] getReferenceArray(TypedArray typedArray, @StyleableRes int res) {
        int resourceId = typedArray.getResourceId(res, 0);
        try {
            TypedArray refs = typedArray.getResources().obtainTypedArray(resourceId);
            int len = refs.length();
            int[] a = new int[len];
            for (int i = 0; i < len; i++) a[i] = refs.getResourceId(i, 0);
            refs.recycle();
            return a;
        } catch (Resources.NotFoundException e) {
            // not an array reference, must be a layout reference instead
            int[] a = new int[1];
            a[0] = typedArray.getResourceId(R.styleable.SpinnerView_layouts, 0);
            return a;
        }
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
        spinner.setSelection(position);
    }

    private void init(AttributeSet attrs, int defStyle) {
        inflate(getContext(), R.layout.spinnerview, this);
        spinner = findViewById(R.id.spinner);
        frameLayout = findViewById(R.id.frameLayout);

        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.SpinnerView, defStyle, 0);

        names = a.getTextArray(R.styleable.SpinnerView_names);
        layouts = getReferenceArray(a, R.styleable.SpinnerView_layouts);

        if (names == null || layouts == null) {
            if (names == null) {
                throw new RuntimeException("xml app:names parameter not specified");
            }
            throw new RuntimeException("xml app:layouts parameter not specified");
        } else if (names.length != layouts.length && layouts.length > 1) {
            throw new RuntimeException(
                    "the array referenced by xml app:names parameter, " +
                            "and the array referenced by xml app:values parameter, " +
                            "have different lengths, and the xml app:values parameter, " +
                            "has more than layout"
            );
        }

        adapterNames = new ArrayAdapter<>(getContext(), simple_spinner_item, names);
        adapterNames.setDropDownViewResource(simple_spinner_dropdown_item);
        spinner.setAdapter(adapterNames);

        spinnerAdapter = spinner.getAdapter();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (frameLayout.getChildCount() == 1) frameLayout.removeViewAt(0);
                SpinnerView.this.position = position;
                oldViewResId = newViewResId;
                if (layouts.length == 1) {
                    newViewResId = layouts[0];
                    inflate(getContext(), layouts[0], frameLayout);
                } else {
                    newViewResId = layouts[position];
                    inflate(getContext(), layouts[position], frameLayout);
                }
                oldView = newView;
                newView = frameLayout.getChildAt(0);
                if (onViewSelected != null) onViewSelected.run(position, oldView, oldViewResId, newView, newViewResId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                oldViewResId = newViewResId;
                oldView = newView;
                if (frameLayout.getChildCount() == 1) frameLayout.removeViewAt(0);
                newViewResId = 0;
                newView = null;

                if (onViewSelected != null) onViewSelected.run(-1, oldView, oldViewResId, newView, newViewResId);
            }
        });

        a.recycle();
    }

    public interface OnViewSelected {
        void run(int position, View oldView, int oldViewResId, View newView, int newViewResId);
    }

    OnViewSelected onViewSelected;

    public void setOnViewSelected(OnViewSelected onViewSelected) {
        this.onViewSelected = onViewSelected;
        onViewSelected.run(position, oldView, oldViewResId, newView, newViewResId);
    }
}
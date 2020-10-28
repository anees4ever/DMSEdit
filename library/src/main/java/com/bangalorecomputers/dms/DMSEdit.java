package com.bangalorecomputers.dms;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;

public class DMSEdit extends FrameLayout {
    private RelativeLayout mRootView;
    private Button btnDirection;
    private EditText edDegree;
    private EditText edMinute;
    private EditText edSecond;

    private String mDirections= "NS";
    private OnChangeListener mOnChangeListener;
    private boolean bAllowChanges= true;

    private int mViewSize= 1;//1- Large, 2-Small
    private int mColorAccent= 0;
    private int mColorPrimary= 0;

    private int mLabelViewID= 0;
    private int mPrevViewID= 0;
    private int mNextViewID= 0;

    private int mDegreeSize= 2;
    private int mMinuteSize= 2;
    private int mSecondSize= 1;

    public interface OnChangeListener {
        void onChange(View view);
    }

    @TargetApi(21)
    public DMSEdit(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs, defStyleAttr, defStyleRes);
    }
    public DMSEdit(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr, 0);
    }

    public DMSEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs, 0, 0);
    }

    public DMSEdit(Context context) {
        super(context);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        if(direction==FOCUS_UP) {
            edSecond.requestFocus();
        } else {
            edDegree.requestFocus();
        }
        return true;
    }

    private void initColors(Context context) {
        try {
            TypedValue typedValue= new TypedValue();
            TypedArray a= context.obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorAccent});
            mColorAccent= a.getColor(0, 0);
            a.recycle();

            a= context.obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimaryDark});
            mColorPrimary= a.getColor(0, 0);
            a.recycle();
        } catch (Exception e) {
            //ignore
        }
    }

    private void setLabelColor(boolean set) {
        try {
            if(mLabelViewID == 0 || mColorAccent == 0) {
                return;
            }
            TextView mLabelView= ((Activity) getContext()).getWindow().getDecorView().findViewById(mLabelViewID);
            if(mLabelView!=null) {
                mLabelView.setTextColor(set ? mColorAccent : mColorPrimary);
            }
        } catch (Exception e) {
            //ignore
        }
    }
    private void initView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setFocusable(true);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DMSEdit, defStyleAttr, defStyleRes);
        mDirections= a.getString(R.styleable.DMSEdit_directions);
        mDirections= mDirections==null||mDirections.isEmpty()?"NS":mDirections;

        mViewSize= a.getInt(R.styleable.DMSEdit_viewSize, 1);
        mLabelViewID= a.getResourceId(R.styleable.DMSEdit_label, 0);
        mPrevViewID= a.getResourceId(R.styleable.DMSEdit_previousFocus, 0);
        mNextViewID= a.getResourceId(R.styleable.DMSEdit_nextFocus, 0);
        mDegreeSize= a.getInt(R.styleable.DMSEdit_degreeSize, 2);
        mMinuteSize= a.getInt(R.styleable.DMSEdit_minuteSize, 2);
        mSecondSize= a.getInt(R.styleable.DMSEdit_secondSize, 1);
        a.recycle();

        mRootView = (RelativeLayout) inflate(getContext(), mViewSize==2?R.layout.dmsedit_small:R.layout.dmsedit, null);
        addView(mRootView);

        initColors(context);

        LinearLayout container= (LinearLayout) mRootView.getChildAt(0);
        for(int i=0; i<container.getChildCount(); i++) {
            View view= container.getChildAt(i);
            if(view instanceof Button) {
                btnDirection= (Button) view;
            } else if (view instanceof EditText) {
                if(edDegree==null) {
                    edDegree= (EditText) view;
                } else if(edMinute==null) {
                    edMinute= (EditText) view;
                } else if(edSecond==null) {
                    edSecond= (EditText) view;
                }
            }
        }

        btnDirection.setText(mDirections.substring(0, 1));
        btnDirection.setTag("0");

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M && mViewSize==2) {
            Drawable arrow_top= getResources().getDrawable(R.drawable.arrow_top);
            Drawable arrow_bottom= getResources().getDrawable(R.drawable.arrow_bottom);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                arrow_top.setTint(getResources().getColor(R.color.dark_blue));
                arrow_bottom.setTint(getResources().getColor(R.color.dark_blue));
            } else {
                arrow_top.setColorFilter(getResources().getColor(R.color.dark_blue), PorterDuff.Mode.SCREEN);
                arrow_bottom.setColorFilter(getResources().getColor(R.color.dark_blue), PorterDuff.Mode.SCREEN);
            }
            btnDirection.setCompoundDrawablesWithIntrinsicBounds(null, arrow_top, null, arrow_bottom);
        }

        btnDirection.setOnClickListener((view)->{
            try {
                int tag= Integer.parseInt(btnDirection.getTag().toString()) + 1;
                tag= tag >= mDirections.length()?0:tag;
                btnDirection.setText(mDirections.substring(tag, tag+1));
                btnDirection.setTag(tag);
                onChange();
            } catch (Exception e) {/**/}
        });

        OnFocusChangeListener mFocusChangeListener= (v, hasFocus)->{
            setLabelColor(hasFocus);
            //if(hasFocus) { edDegree.selectAll(); }
        };
        edDegree.setOnFocusChangeListener(mFocusChangeListener);
        edMinute.setOnFocusChangeListener(mFocusChangeListener);
        edSecond.setOnFocusChangeListener(mFocusChangeListener);

        edDegree.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                if(!bAllowChanges) {
                    return;
                }
                String str= edDegree.getText().toString();
                int val= Math.abs(Integer.parseInt(TextUtils.isEmpty(str)?"0":str));
                if(str.length() >= mDegreeSize) {
                    bAllowChanges= false;
                    edDegree.setText(String.valueOf(val));
                    bAllowChanges= true;
                    edMinute.requestFocus();
                }
                onChange();
            }
        });
        edDegree.setOnKeyListener((v, keyCode, event)->{
            if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_DEL)) {
                if(edDegree.getText().toString().isEmpty()) {
                    moveToPrevFocus();
                }
            }
            return false;
        });


        edMinute.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                if(!bAllowChanges) {
                    return;
                }
                String str= edMinute.getText().toString();
                int val= Math.abs(Integer.parseInt(TextUtils.isEmpty(str)?"0":str));
                if(str.length() >= mMinuteSize) {
                    bAllowChanges= false;
                    edMinute.setText(String.valueOf(val));
                    bAllowChanges= true;
                    edSecond.requestFocus();
                }
                onChange();
            }
        });
        edMinute.setOnKeyListener((v, keyCode, event)->{
            if((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_DEL)) {
                if(edMinute.getText().toString().isEmpty()) {
                    edDegree.requestFocus();
                }
            }
            return false;
        });

        edSecond.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                if(!bAllowChanges) {
                    return;
                }
                String str= edSecond.getText().toString();
                if(str.contains(".")) {
                    float val= Math.abs(Float.parseFloat(TextUtils.isEmpty(str)||TextUtils.equals(".",str)?"0":str));
                    String[] temp= str.split("\\.");
                    if(temp.length>1 && temp[1].length()>=mSecondSize) {
                        bAllowChanges = false;
                        edSecond.setText(String.format("%."+mSecondSize+"f", val));
                        bAllowChanges = true;
                        edSecond.requestFocus();
                        moveToNextFocus();
                    }
                }
                onChange();
            }
        });
        edSecond.setOnKeyListener((v, keyCode, event)->{
            if(event.getAction() == KeyEvent.ACTION_DOWN) {
                if(keyCode == KeyEvent.KEYCODE_DEL) {
                    if (edSecond.getText().toString().isEmpty()) {
                        edMinute.requestFocus();
                    }
                }
            }
            return false;
        });
        edSecond.setOnEditorActionListener((v, actionId, event)-> {
            if(actionId== EditorInfo.IME_ACTION_NEXT) {
                moveToNextFocus();
                return true;
            }
            return false;
        });

    }

    private void moveToNextFocus() {
        try {
            View view= ((Activity) getContext()).getWindow().getDecorView().findViewById(mNextViewID);
            if(view!=null) {
                view.requestFocus(FOCUS_DOWN);
            }
        } catch (Exception e) {
            //ignore
        }
    }
    private void moveToPrevFocus() {
        try {
            View view= ((Activity) getContext()).getWindow().getDecorView().findViewById(mPrevViewID);
            if(view!=null) {
                view.requestFocus(FOCUS_UP);
            }
        } catch (Exception e) {
            //ignore
        }
    }

    private void onChange() {
        if(mOnChangeListener != null && bAllowChanges) {
            mOnChangeListener.onChange(mRootView);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public DMSEdit setPrimaryColor(int color) {
        mColorPrimary= color;
        return this;
    }

    public DMSEdit setAccentColor(int color) {
        mColorAccent= color;
        return this;
    }

    public void setLocation(double location) {
        DMS dmsObj= DMS.parseDDtoDMSObj(location, mDirections);
        bAllowChanges= false;
        btnDirection.setText(dmsObj.direction);
        btnDirection.setTag(mDirections.indexOf(dmsObj.direction));
        edDegree.setText(String.valueOf(dmsObj.degree));
        edMinute.setText(String.valueOf(dmsObj.minute));
        edSecond.setText(String.format(Locale.getDefault(), "%.1f", dmsObj.second));
        bAllowChanges= true;
    }

    public double getLocation() {
        String deg= edDegree.getText().toString();        deg= deg.isEmpty()?"0":deg;
        String min= edMinute.getText().toString();        min= min.isEmpty()?"0":min;
        String sec= edSecond.getText().toString();        sec= sec.isEmpty()?"0":sec;
        return DMS.convertDMSToDD(Double.parseDouble(deg),
                Double.parseDouble(min),
                Double.parseDouble(sec),
                btnDirection.getText().toString());
    }

    public void setOnChangeListener(OnChangeListener onChangeListener) {
        mOnChangeListener= onChangeListener;
    }
}

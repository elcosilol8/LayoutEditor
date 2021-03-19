package com.tyron.layouteditor.editor.widget;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionSet;
import androidx.transition.TransitionManager;

import com.tyron.layouteditor.EditorContext;
import com.tyron.layouteditor.ViewManager;
import com.tyron.layouteditor.WidgetFactory;
import com.tyron.layouteditor.editor.PropertiesView;
import com.tyron.layouteditor.models.Widget;
import com.tyron.layouteditor.models.Attribute;
import com.tyron.layouteditor.util.AndroidUtilities;
import com.tyron.layouteditor.values.Layout;
import com.tyron.layouteditor.values.Null;
import com.tyron.layouteditor.values.ObjectValue;
import com.tyron.layouteditor.values.Primitive;
import com.tyron.layouteditor.values.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


@SuppressLint("ViewConstructor")
public class LinearLayoutItem extends LinearLayout implements BaseWidget, View.OnLongClickListener, View.OnClickListener {

    private View shadow;
    private WidgetFactory widgetFactory;

    private final Paint backgroundPaint = new Paint();

    private boolean isRoot = false;

    public LinearLayoutItem(Context context) {
        super(context);
        init();
    }

    public boolean isRootView(){
        return isRoot;
    }

    private void init(){
         
		//setWillNotDraw(false);

        backgroundPaint.setColor(0xfffe6262);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setAntiAlias(true);

        backgroundPaint.setStrokeWidth(AndroidUtilities.dp(4));

        setOnLongClickListener(this);
        setOnClickListener(this);
        setBackgroundColor(0xffffffff);

        shadow = new View(getContext());
        shadow.setBackgroundColor(0x52000000);
        shadow.setLayoutParams(new LayoutParams(AndroidUtilities.dp(100), AndroidUtilities.dp(50)));
        shadow.setMinimumWidth(AndroidUtilities.dp(50));
        shadow.setMinimumHeight(AndroidUtilities.dp(50));
		
		GradientDrawable gd = new GradientDrawable();
		gd.setStroke(AndroidUtilities.dp(2), 0xfffe6262);
		setBackground(gd);

        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        layoutTransition.disableTransitionType(LayoutTransition.DISAPPEARING);
        layoutTransition.setDuration(180L);
        setLayoutTransition(layoutTransition);
        setAnimationCacheEnabled(true);

    }

    @Override
    public boolean onDragEvent(DragEvent event) {
        switch(event.getAction()){
            case DragEvent.ACTION_DROP : {
                removeView(shadow);
                Object object = event.getLocalState();

                View view;

                if(object instanceof Widget){
                    view = ((EditorContext)getContext()).getWidgetFactory().createWidget(getContext(), (Widget)object);
                }else{
                    view = (View) object;
                }

                if(view.getParent() != null){
                    ((ViewGroup)view.getParent()).removeView(view);
                }
                addView(view);

                break;
            }
            case DragEvent.ACTION_DRAG_ENDED : {
                removeView(shadow);
                break;
            }
            case DragEvent.ACTION_DRAG_ENTERED : {
                removeView(shadow);
                addView(shadow);
                break;
            }
            case DragEvent.ACTION_DRAG_EXITED : {
                removeView(shadow);
                break;
            }
        }
		//((View)getParent()).requestLayout();
		//((View)getParent()).postInvalidate();
        return true;
    }

    Rect rect = new Rect();
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

      //  getLocalVisibleRect(rect);
      //  canvas.drawRect(rect, backgroundPaint);
//
//        path.moveTo(rect.left, rect.top);
//        path.lineTo(rect.right, rect.top);
//        path.lineTo(rect.right, rect.bottom);
//        path.lineTo(rect.left, rect.bottom);
//        path.lineTo(rect.left, rect.top);
//
//        canvas.drawPath(path, backgroundPaint);
    }

    public void setRoot(boolean val){
        isRoot = val;
    }

    @Override
    public boolean onLongClick(View v) {
        if(isRoot) return false;

        ViewCompat.startDragAndDrop(this, null, new DragShadowBuilder(this), this, 0);
        ((ViewGroup)getParent()).removeView(this);
        return true;
    }

    @Override
    public void onClick(View v) {
        final PropertiesView d = new PropertiesView(this);
        d.show(AndroidUtilities.getActivity(getContext()).getSupportFragmentManager(), "null");
    }


    @Override
    public View getAsView(){
        return this;
    }

    @NonNull
    @Override
    public ArrayList<Attribute> getAttributes() {
        ArrayList<Attribute> arrayList = new ArrayList<>();

        arrayList.add(new Attribute(Attributes.View.Width, new Primitive(getLayoutParams().width)));
        arrayList.add(new Attribute(Attributes.View.Height, new Primitive(getLayoutParams().height)));
	    arrayList.add(new Attribute( Attributes.LinearLayout.Orientation, new Primitive(getOrientation())));
		
		if(getParent() instanceof LinearLayoutItem){
			arrayList.add(new Attribute(Attributes.View.Weight, new Primitive(((LinearLayout.LayoutParams)getLayoutParams()).weight)));
		}
		if(getParent() instanceof RelativeLayoutItem){

		    int[] rules = ((RelativeLayout.LayoutParams)getLayoutParams()).getRules();

		    for(int rule : rules){
		        Value val = (rule == 0 ? new Null() : new Primitive(rule));
                switch(rule){
                    case RelativeLayout.ALIGN_PARENT_BOTTOM:
                        arrayList.add(new Attribute(Attributes.View.AlignParentBottom, val));
                        continue;
                    case RelativeLayout.ALIGN_PARENT_TOP:
                        arrayList.add(new Attribute(Attributes.View.AlignParentTop, val));
                }
            }
        }
	    return arrayList;
    }
}

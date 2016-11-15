package com.example.zackzhu.androidmelonbasicsample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.axio.melonplatformkit.AnalysisResult;
import com.axio.melonplatformkit.ISignalAnalyzerListener;
import com.axio.melonplatformkit.SignalAnalyzer;


/**
 * Created by purnimam on 3/6/15.
 */
public class RawVoltageView extends View implements ISignalAnalyzerListener {//}, RawVoltageActivity.IGraphStatusListener {
    //    private float[] _data = null;
    private SkinContact mSkinContact = null;
    private float[] _dataLeftChannel = null;
    private float[] _dataRightChannel = null;
    private int LEFT = 0;
    private int RIGHT = 1;
    Paint _paint, _paintsecmitrans; //, _paintzeroline;
    Path _mPath;
    boolean _mPause = false;
    boolean _mFirstRender = true;
    private String TAG = "RawVoltageView";

    private Context mContext;
    private Bitmap mSavedBitmap;
    private Canvas mSavedCanvas;
    private final float PAINT_DP = 1.0f;

    public interface IGraphStatusListener {
        public void onStatusChanged(RawVoltageView source, boolean status);
    }

    private IGraphStatusListener mGraphStatusListener = null;


    Runnable displayGraph;

    public RawVoltageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initialise();
    }

    public void setSkinContact(SkinContact skinContact) {
        mSkinContact = skinContact;
    }

    private void initialise() {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        int paintWidth = (int) ((float) metrics.heightPixels / 300f);

        _paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        _paint.setColor(Color.WHITE);
        _paint.setStyle(Paint.Style.STROKE);
        //      _paint.setStrokeWidth(3);
        _paint.setStrokeWidth(paintWidth);

        _paintsecmitrans = new Paint(Paint.ANTI_ALIAS_FLAG);
        _paintsecmitrans.setColor(Color.LTGRAY);
        _paintsecmitrans.setStyle(Paint.Style.STROKE);

        //      _paintsecmitrans.setStrokeWidth(3);
        _paintsecmitrans.setStrokeWidth(paintWidth);

 /*       _paintzeroline = new Paint(Paint.ANTI_ALIAS_FLAG);
        _paintzeroline.setColor(Color.YELLOW);
        _paintzeroline.setStyle(Paint.Style.STROKE);
        _paintzeroline.setStrokeWidth(3);  */

        _mPath = new Path();
    }

    void setData(final float[] leftData, final float[] rightData) {
        displayGraph = new Runnable() {
            @Override
            public void run() {
                _dataLeftChannel = leftData;
                _dataRightChannel = rightData;
                if (!_mPause) {
                    RawVoltageView.this.invalidate();
                }
            }
        };
    }

    public void onAnalyzedSamples(SignalAnalyzer analyzer, AnalysisResult leftChannelResult, AnalysisResult rightChannelResult) {
//pp       float[] d = leftChannelResult.filteredSignal;
        if (mSkinContact != null) {
            mSkinContact.callback(leftChannelResult.getHeadbandAlert());
        }
        float[] leftChannelData = leftChannelResult.getFilteredSignal();
        float[] rightChannelData = rightChannelResult.getFilteredSignal(); //.getFilteredSignal();
        Log.d("*****", "onAnalyzedSamples");

        //pp       setData(d);
        setData(leftChannelData, rightChannelData);
        removeCallbacks(displayGraph);
        post(displayGraph);
    }

    @Override
    public void onDraw(Canvas canvas) {
 /*       if (_data == null) {
            return;
        } */

        if (!_mPause) {
            drawData(canvas, _dataLeftChannel, _paint, true);
            drawData(canvas, _dataRightChannel, _paintsecmitrans, false);
            //ppp          drawDataLeft(canvas, _dataLeftChannel, _paint);
            //ppp          drawDataRight(canvas, _dataRightChannel, _paintsecmitrans);
        } else if (mSavedBitmap != null) {
            canvas.drawBitmap(mSavedBitmap, 0.0f, 0.0f, null);
        }

 /*       float w  = canvas.getWidth();
        float h = canvas.getHeight();
        float xF = _data.length - 1.0f;
        float scale = 0.0002f;

        for (int i = 1; i < _data.length; i++) {
            float v1 = _data[i-1];
            float v2 =  _data[i];

            int y1 = (int)((0.5f + ((v1*scale)*0.5f)) * h);
            int x1 =  (int)(w*(((float)i-1.0f)/xF));
            int y2 = (int)((0.5f + ((v2*scale)*0.5f)) * h);
            int x2 =(int)(w*(((float)i)/xF));
            if (!_mPause) {
                canvas.drawLine(x1, y1, x2, y2, _paint);
            }

     //       _mPath.moveTo(x1, y1);
     //       _mPath.lineTo(x2, y2);
     //       canvas.drawPath(_mPath,_paint);
        } */
    }

    public void drawData(Canvas canvas, float[] data, Paint paint, boolean refreshBitmap) {
        if (data == null) {
            Log.d(TAG, "Data Is null");
            return;
        }

        if (refreshBitmap) {
            if (mSavedBitmap != null) {
                mSavedBitmap.recycle();
            }
            mSavedBitmap = null;
            mSavedBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
            mSavedCanvas = new Canvas(mSavedBitmap);
        }

        float w = canvas.getWidth();
        float h = canvas.getHeight();
        float xF = data.length - 1.0f;
        float scale = 0.0125f;
        for (int i = 1; i < data.length; i++) {
            float v1 = data[i - 1];
            float v2 = data[i];
            int y1 = (int) ((0.5f + ((v1 * scale) * 0.5f)) * h);
            int x1 = (int) (w * (((float) i - 1.0f) / xF));
            int y2 = (int) ((0.5f + ((v2 * scale) * 0.5f)) * h);
            int x2 = (int) (w * (((float) i) / xF));

            if (!_mPause) {
                canvas.drawLine(x1, y1, x2, y2, paint);
                mSavedCanvas.drawLine(x1, y1, x2, y2, paint);
            }
        }
    }

    public void oldDrawData(Canvas canvas, float[] data, Paint paint, boolean refreshBitmap) {
        if (data == null) {
            Log.d(TAG, "Data Is null");
            return;
        }

        if (refreshBitmap) {
            if (mSavedBitmap != null) {
                mSavedBitmap.recycle();
            }
            mSavedBitmap = null;
            mSavedBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
            mSavedCanvas = new Canvas(mSavedBitmap);
        }

        float w = canvas.getWidth();
        float h = canvas.getHeight();
        float xF = data.length - 1.0f;
        float scale = 0.08f;
        for (int i = 1; i < data.length; i++) {
            float v1 = data[i - 1];
            float v2 = data[i];
            int y1 = (int) ((0.5f + ((v1 * scale) * 0.5f)) * h);
            int x1 = (int) (w * (((float) i - 1.0f) / xF));
            int y2 = (int) ((0.5f + ((v2 * scale) * 0.5f)) * h);
            int x2 = (int) (w * (((float) i) / xF));
    /*        if (_mFirstRender){
                _paintzeroline.setPathEffect(new DashPathEffect(new float[] {10,10}, 5));
                canvas.drawLine(x1, y1, x1+w, y1, _paintzeroline);
                _mFirstRender = false;
            } */
            if (!_mPause) {
                canvas.drawLine(x1, y1, x2, y2, paint);
                mSavedCanvas.drawLine(x1, y1, x2, y2, paint);
            }
             /*       _mPath.moveTo(x1, y1);
            _mPath.lineTo(x2, y2);
            canvas.drawPath(_mPath,_paint); */
        }
    }

    public void drawDataLeft(Canvas canvas, float[] data, Paint paint) {
        if (data == null) {
            Log.d(TAG, "Data Is null");
            return;
        }

        float w = canvas.getWidth();
        float h = canvas.getHeight();
        float xF = data.length - 1.0f;
        float scale = 0.08f;
        for (int i = 1; i < data.length; i++) {
            float v1 = data[i - 1];
            float v2 = data[i];
            String dataout = "V1 " + data[i - 1] + " V2 " + data[i];
            Log.d(TAG, dataout);
            int y1 = (int) ((0.5f + ((v1 * scale) * 0.5f)) * h);
            int x1 = (int) (w * (((float) i - 1.0f) / xF));
            int y2 = (int) ((0.5f + ((v2 * scale) * 0.5f)) * h);
            int x2 = (int) (w * (((float) i) / xF));

            if (!_mPause) {
                canvas.drawLine(x1, y1, x2, y2, paint);

            }
             /*       _mPath.moveTo(x1, y1);
            _mPath.lineTo(x2, y2);
            canvas.drawPath(_mPath,_paint); */
        }
    }

    public void drawDataRight(Canvas canvas, float[] data, Paint paint) {
        if (data == null) {
            return;
        }

        float w = canvas.getWidth();
        float h = canvas.getHeight();
        float xF = data.length - 1.0f;
        float scale = 0.000008f;
        for (int i = 1; i < data.length; i++) {
            float v1 = data[i - 1];
            float v2 = data[i];

            int y1 = (int) ((0.5f + ((v1 * scale) * 0.5f)) * h);
            int x1 = (int) (w - (w * (((float) i - 1.0f) / xF)));
            int y2 = (int) ((0.5f + ((v2 * scale) * 0.5f)) * h);
            int x2 = (int) (w - (w * (((float) i) / xF)));

            if (!_mPause) {
                canvas.drawLine(x1, y1, x2, y2, paint);
            }
             /*       _mPath.moveTo(x1, y1);
            _mPath.lineTo(x2, y2);
            canvas.drawPath(_mPath,_paint); */
        }
    }

    public void onStatusChanged(boolean status) {
        this._mPause = status;
        if (mGraphStatusListener != null) {
            mGraphStatusListener.onStatusChanged(this, status);
        }
        if (!_mPause) {
            invalidate();
        }
    }

    /**
     * Register a callback to be invoked when the currently selected item changes.
     *
     * @param mGraphStatusListener Can be null.
     *                             The current item changed listener to attach to this view.
     */
    public void setOnGraphStatusChangeListener(IGraphStatusListener mGraphStatusListener) {
        this.mGraphStatusListener = mGraphStatusListener;
    }

    private float calculateStrokeWidth(float dpSz) {
        // Get the screen's density scale
        final float scale = getResources().getDisplayMetrics().density;
        float value = (int) (dpSz * scale + 0.5f);
        return value;
    }

    public interface SkinContact {
        void callback(boolean lostContact);
    }
}

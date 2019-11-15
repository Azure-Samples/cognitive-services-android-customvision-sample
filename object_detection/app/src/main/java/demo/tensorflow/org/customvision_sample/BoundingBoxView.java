/* Copyright 2015 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package demo.tensorflow.org.customvision_sample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.graphics.RectF;

import demo.tensorflow.org.customvision_sample.ObjectDetector.BoundingBox;

import java.util.List;

public class BoundingBoxView extends View implements ResultsView {
    private static final float TEXT_SIZE_DIP = 16;
    private static final float BOX_STROKE_WITDTH = 8;
    private static final float LABEL_HORIZONTAL_PADDING = 16;
    private static final float LABEL_VERTICAL_PADDING = 4;
    private List<BoundingBox> results;
    private final Paint fgPaint;
    private final int[] colors = {
        Color.CYAN,
        Color.MAGENTA,
        Color.YELLOW,
        Color.BLUE,
        Color.RED,
        Color.GREEN,
    };

    public BoundingBoxView(final Context context, final AttributeSet set) {
        super(context, set);

        final float textSizePx =
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DIP, getResources().getDisplayMetrics());
        fgPaint = new Paint();
        fgPaint.setTextSize(textSizePx);
        fgPaint.setStrokeWidth(BOX_STROKE_WITDTH);
    }

    @Override
    public void setResults(final List<BoundingBox> results) {
        synchronized (this) {
            this.results = results;
        }
        postInvalidate();
    }

    @Override
    public void onDraw(final Canvas canvas) {

        List<BoundingBox> results;
        synchronized (this) {
            results = this.results;
        }

        if (results != null) {
            for (int i = 0; i < results.size(); i++) {
                final BoundingBox boundingBox = results.get(i);
                final String label = boundingBox.getClassIdentifier();
                final int classColor = colors[boundingBox.getClassIndex() % colors.length];

                // Get location in view.  ObjectDetector returns object location in normalized coordinate system.
                RectF location = boundingBox.getLocation();
                location.left *= canvas.getWidth();
                location.top *= canvas.getHeight();
                location.right *= canvas.getWidth();
                location.bottom *= canvas.getHeight();

                // draw box
                fgPaint.setColor(classColor);
                fgPaint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(location, fgPaint);

                // draw label
                RectF labelBounds = new RectF();
                Paint.FontMetrics fm = fgPaint.getFontMetrics();
                labelBounds.left = location.left;
                labelBounds.top = location.top - (-fm.ascent + fm.descent + LABEL_VERTICAL_PADDING * 2);
                labelBounds.right = location.left + (fgPaint.measureText(label) + LABEL_HORIZONTAL_PADDING * 2);
                labelBounds.bottom = location.top;

                fgPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawRect(labelBounds, fgPaint);

                fgPaint.setColor(Color.WHITE);
                fgPaint.setStyle(Paint.Style.FILL);
                canvas.drawText(
                    label, labelBounds.left + LABEL_HORIZONTAL_PADDING, labelBounds.bottom - (fm.descent + LABEL_VERTICAL_PADDING), fgPaint);
            }
        }
    }
}

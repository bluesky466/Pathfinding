package me.linjw.pathfinding;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

public class MapView extends View implements
        GestureDetector.OnGestureListener,
        ScaleGestureDetector.OnScaleGestureListener {
    private int mTextColor = Color.BLACK;
    private int mColumn;
    private int mRow;
    private int mCellLength;
    private int mOriginX;
    private int mOriginY;
    private float mScale = 1.0f;

    private Paint mPaint = new Paint();
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    private Map<Integer, Map<Integer, Integer>> mCellColorMap = new HashMap<>();
    private Map<Integer, Map<Integer, String>> mCellNumberMap = new HashMap<>();

    private OnCellClickedListener mOnCellClickedListener;

    public MapView(Context context) {
        this(context, null);
    }

    public MapView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mGestureDetector = new GestureDetector(context, this);
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mColumn == 0 || mRow == 0 || mCellLength == 0) {
            return;
        }

        int cellLength = (int) (mCellLength * mScale);
        int beginRow = clamp(mOriginY / cellLength, 0, mRow);
        int beginColumn = clamp(mOriginX / cellLength, 0, mColumn);
        int numColumnShow = mColumn - beginColumn;
        int numRowShow = mRow - beginRow;
        int beginX = mOriginX > 0 ? -(mOriginX % cellLength) : -mOriginX;
        int beginY = mOriginY > 0 ? -(mOriginY % cellLength) : -mOriginY;

        drawCellColor(canvas, beginX, beginY, beginRow, beginColumn, cellLength, numColumnShow, numRowShow);
        drawCellNumber(canvas, beginX, beginY, beginRow, beginColumn, cellLength, numColumnShow, numRowShow);
        drawGrid(canvas, cellLength, beginX, beginY, numColumnShow, numRowShow);
    }

    private void drawCellColor(
            Canvas canvas,
            int beginX,
            int beginY,
            int beginRow,
            int beginColumn,
            int cellLength,
            int numColumnShow,
            int numRowShow) {
        mPaint.setStyle(Paint.Style.FILL);
        for (Map.Entry<Integer, Map<Integer, Integer>> itRow : mCellColorMap.entrySet()) {
            int row = itRow.getKey();
            for (Map.Entry<Integer, Integer> itColumn : itRow.getValue().entrySet()) {
                int column = itColumn.getKey();
                if (isInEyeShot(row, column, beginRow, beginColumn, numColumnShow, numRowShow)) {
                    int left = beginX + (column - beginColumn) * cellLength;
                    int top = beginY + (row - beginRow) * cellLength;
                    mPaint.setColor(itColumn.getValue());
                    canvas.drawRect(
                            left,
                            top,
                            left + cellLength,
                            top + cellLength,
                            mPaint);
                }
            }
        }
    }

    private void drawCellNumber(
            Canvas canvas,
            int beginX,
            int beginY,
            int beginRow,
            int beginColumn,
            int cellLength,
            int numColumnShow,
            int numRowShow) {
        int size = getTextSize(beginRow, beginColumn, cellLength, numColumnShow, numRowShow);
        mPaint.setTextSize(size);
        mPaint.setColor(mTextColor);

        for (Map.Entry<Integer, Map<Integer, String>> itRow : mCellNumberMap.entrySet()) {
            int row = itRow.getKey();
            for (Map.Entry<Integer, String> itColumn : itRow.getValue().entrySet()) {
                int column = itColumn.getKey();
                if (isInEyeShot(row, column, beginRow, beginColumn, numColumnShow, numRowShow)) {
                    int x = beginX + (column - beginColumn) * cellLength;
                    int y = beginY + (row - beginRow) * cellLength + cellLength;
                    canvas.drawText(itColumn.getValue(), x, y, mPaint);
                }
            }
        }
    }

    private int getTextSize(
            int beginRow,
            int beginColumn,
            int cellLength,
            int numColumnShow,
            int numRowShow) {
        int length = 1;
        for (Map.Entry<Integer, Map<Integer, String>> itRow : mCellNumberMap.entrySet()) {
            int row = itRow.getKey();
            for (Map.Entry<Integer, String> itColumn : itRow.getValue().entrySet()) {
                int column = itColumn.getKey();
                if (isInEyeShot(row, column, beginRow, beginColumn, numColumnShow, numRowShow)
                        && itColumn.getValue().length() > length) {
                    length = itColumn.getValue().length();
                }
            }
        }
        return cellLength / length;
    }

    private boolean isInEyeShot(int row,
                                int column,
                                int beginRow,
                                int beginColumn,
                                int numColumnShow,
                                int numRowShow) {
        return row >= beginRow
                && row <= beginRow + numRowShow
                && column >= beginColumn
                && column <= beginColumn + numColumnShow;
    }

    private void drawGrid(
            Canvas canvas,
            int cellLength,
            int beginX,
            int beginY,
            int numColumnShow,
            int numRowShow) {
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);

        int endX = beginX + numColumnShow * cellLength;
        int enxY = beginY + numRowShow * cellLength;

        for (int column = 0; column <= numColumnShow; column++) {
            int x = beginX + column * cellLength;
            canvas.drawLine(x, beginY, x, enxY, mPaint);
        }

        for (int row = 0; row <= numRowShow; row++) {
            int y = beginY + row * cellLength;
            canvas.drawLine(beginX, y, endX, y, mPaint);
        }
    }

    private int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        } else {
            return value;
        }
    }

    public void setColumn(int column) {
        mColumn = column;
        invalidate();
    }

    public void setRow(int row) {
        mRow = row;
        invalidate();
    }

    public void setCellLength(int cellLength) {
        mCellLength = cellLength;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        mGestureDetector.onTouchEvent(event);
        mScaleGestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if (mOnCellClickedListener == null) {
            return false;
        }

        int cellLength = (int) (mCellLength * mScale);

        int column = (int) ((mOriginX + e.getX()) / cellLength);
        int row = (int) ((mOriginY + e.getY()) / cellLength);
        if (row >= 0
                && row < mRow
                && column >= 0
                && column < mColumn) {
            mOnCellClickedListener.onCellClicked(this, row, column);
        }

        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        mOriginX += distanceX;
        mOriginY += distanceY;
        invalidate();
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        mScale *= detector.getScaleFactor();
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    public void setCellColor(int row, int column, int color) {
        Map<Integer, Integer> rowCellColor = getRowCellColor(row, mCellColorMap);
        rowCellColor.put(column, color);
        invalidate();
    }

    public Integer getCellColor(int row, int column) {
        Map<Integer, Integer> rowCellColor = getRowCellColor(row, mCellColorMap);
        return rowCellColor.get(column);
    }

    public void clearCellColor(int row, int column) {
        Map<Integer, Integer> rowCellColor = getRowCellColor(row, mCellColorMap);
        rowCellColor.remove(column);
        invalidate();
    }

    public void clearAllCellColor() {
        mCellColorMap.clear();
        invalidate();
    }

    public void setCellNumber(int row, int column, int number) {
        Map<Integer, String> rowCellNumber = getRowCellNumber(row, mCellNumberMap);
        rowCellNumber.put(column, String.valueOf(number));
        invalidate();
    }

    public Integer getCellNumber(int row, int column) {
        Map<Integer, String> rowCellNumber = getRowCellNumber(row, mCellNumberMap);
        String num = rowCellNumber.get(column);
        return num == null ? null : Integer.valueOf(num);
    }

    public void clearCellNumber(int row, int column) {
        Map<Integer, String> rowCellNumber = getRowCellNumber(row, mCellNumberMap);
        rowCellNumber.remove(column);
        invalidate();
    }

    public void clearAllCellNumber() {
        mCellNumberMap.clear();
        invalidate();
    }

    private Map<Integer, Integer> getRowCellColor(int row, Map<Integer, Map<Integer, Integer>> map) {
        Map<Integer, Integer> rowCell = map.get(row);
        if (rowCell == null) {
            rowCell = new HashMap<>();
            map.put(row, rowCell);
        }
        return rowCell;
    }


    private Map<Integer, String> getRowCellNumber(int row, Map<Integer, Map<Integer, String>> map) {
        Map<Integer, String> rowCell = map.get(row);
        if (rowCell == null) {
            rowCell = new HashMap<>();
            map.put(row, rowCell);
        }
        return rowCell;
    }

    public void setOnCellClickedListener(OnCellClickedListener onCellClickedListener) {
        mOnCellClickedListener = onCellClickedListener;
    }

    public interface OnCellClickedListener {
        void onCellClicked(MapView mapView, int row, int column);
    }
}
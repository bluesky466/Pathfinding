package me.linjw.pathfinding;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int COLOR_START = Color.GREEN;
    private static final int COLOR_END = Color.RED;
    private static final int COLOR_WALL = Color.GRAY;
    private static final int COLOR_PATH = Color.YELLOW;
    private static final int CELL_LENGTH = 100;
    private static final int MODE_WALL = 0x0;
    private static final int MODE_START = 0x1;
    private static final int MODE_END = 0x2;

    private int mMode = 0;
    private Point mStart;
    private Point mEnd;
    private MapView mMapView;
    private boolean mIsFound = false;
    private int mWidth = 10;
    private int mHeight = 10;
    private EditText mWidthEdit;
    private EditText mHeightEdit;

    private IPathFinder mFinder = new AStartFinder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mWidthEdit = findViewById(R.id.width);
        mHeightEdit = findViewById(R.id.height);

        mFinder.setSize(mWidth, mHeight);

        mMapView = (MapView) findViewById(R.id.map);
        mMapView.setRow(mHeight);
        mMapView.setColumn(mWidth);
        mMapView.setCellLength(CELL_LENGTH);
        mMapView.setOnCellClickedListener(new MapView.OnCellClickedListener() {
            @Override
            public void onCellClicked(MapView mapView, int row, int column) {
                if (mIsFound) {
                    return;
                }

                if (mMode == MODE_WALL) {
                    clickOnWallMode(row, column);
                } else if (mMode == MODE_START) {
                    clickOnStartMode(row, column);
                } else if (mMode == MODE_END) {
                    clickOnEndMode(row, column);
                }
            }
        });
    }

    private boolean isStartOrEnd(int row, int column) {
        return (mEnd != null && mEnd.x == column && mEnd.y == row)
                || (mStart != null && mStart.x == column && mStart.y == row);
    }

    private void clickOnWallMode(int row, int column) {
        if (isStartOrEnd(row, column)) {
            return;
        }

        Integer color = mMapView.getCellColor(row, column);
        if (color == null) {
            mFinder.addUnreachable(new Point(column, row));
            mMapView.setCellColor(row, column, COLOR_WALL);
        } else {
            mFinder.removeUnreachable(new Point(column, row));
            mMapView.clearCellColor(row, column);
        }
    }

    private void clickOnStartMode(int row, int column) {
        if (isStartOrEnd(row, column)) {
            return;
        }

        if (mStart == null) {
            mStart = new Point(column, row);
        } else {
            //如果之前已经设置过起点,清除原来的起点
            mMapView.clearCellColor(mStart.y, mStart.x);
            mStart.x = column;
            mStart.y = row;
        }

        //该点可能之前是墙,先清除
        mFinder.removeUnreachable(new Point(column, row));

        mMapView.setCellColor(row, column, COLOR_START);
    }

    private void clickOnEndMode(int row, int column) {
        if (isStartOrEnd(row, column)) {
            return;
        }

        if (mEnd == null) {
            mEnd = new Point(column, row);
        } else {
            //如果之前已经设置过终点,清除原来的终点
            mMapView.clearCellColor(mEnd.y, mEnd.x);
            mEnd.x = column;
            mEnd.y = row;
        }

        //该点可能之前是墙,先清除
        mFinder.removeUnreachable(new Point(column, row));

        mMapView.setCellColor(row, column, COLOR_END);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start:
                mMode = MODE_START;
                break;
            case R.id.end:
                mMode = MODE_END;
                break;
            case R.id.wall:
                mMode = MODE_WALL;
                break;
            case R.id.find:
                find();
                break;
            case R.id.reset:
                reset();
                break;
            case R.id.size:
                setSize();
                break;
        }
    }

    private void find() {
        if (mStart == null || mEnd == null) {
            Toast.makeText(this, R.string.no_start_or_end, Toast.LENGTH_SHORT).show();
            return;
        }

        mIsFound = true;
        List<Point> path = mFinder.getPath(mStart, mEnd);

        if (mFinder instanceof AStartFinder){
            AStartFinder finder = (AStartFinder) mFinder;
            for (AStartFinder.Compute c : finder.getOpenPoints()) {
                mMapView.setCellNumber(c.getPoint().y, c.getPoint().x, c.getDistanceTotal());
            }
            for (AStartFinder.Compute c : finder.getClosePoints()) {
                mMapView.setCellNumber(c.getPoint().y, c.getPoint().x, c.getDistanceTotal());
            }
        }

        if (path != null) {
            for (int i = 1; i < path.size() - 1; i++) {
                Point point = path.get(i);
                mMapView.setCellColor(point.y, point.x, COLOR_PATH);
            }
        } else {
            Toast.makeText(this, R.string.no_path, Toast.LENGTH_SHORT).show();
        }
    }

    private void reset() {
        mStart = null;
        mEnd = null;
        mMapView.clearAllCellColor();
        mMapView.clearAllCellNumber();
        mFinder.clearUnreachable();
        mMode = MODE_WALL;
        mIsFound = false;
    }

    private void setSize() {
        mWidth = Integer.parseInt(mWidthEdit.getText().toString());
        mHeight = Integer.parseInt(mHeightEdit.getText().toString());
        mFinder.setSize(mWidth, mHeight);
        mMapView.setRow(mHeight);
        mMapView.setColumn(mWidth);
        reset();
    }
}

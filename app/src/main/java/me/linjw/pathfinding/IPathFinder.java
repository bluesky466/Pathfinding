package me.linjw.pathfinding;

import android.graphics.Point;

import java.util.List;

public interface IPathFinder {
    void setSize(int width, int height);

    boolean addUnreachable(Point point);

    boolean removeUnreachable(Point point);

    void clearUnreachable();

    List<Point> getPath(Point start, Point end);
}

package me.linjw.pathfinding;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class AStartFinder implements IPathFinder {
    private static final int INVALID_INDEX = -1;
    private int mWidth;
    private int mHeight;
    private Map<Integer, Point> mUnreachableMap = new HashMap<>();

    private LinkedList<Compute> mOpenPoints = new LinkedList<>();
    private Map<Integer, Compute> mOpenPointMap = new HashMap<>();
    private List<Compute> mClosePoints = new ArrayList<>();
    private Map<Integer, Compute> mClosePointMap = new HashMap<>();

    public List<Compute> getOpenPoints() {
        return mOpenPoints;
    }

    public List<Compute> getClosePoints() {
        return mClosePoints;
    }

    @Override
    public void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;
        mUnreachableMap.clear();
    }

    @Override
    public boolean addUnreachable(Point point) {
        if (point.x < 0 || point.x >= mWidth || point.y < 0 || point.y >= mHeight) {
            return false;
        }
        int index = computeIndex(point);
        if (!mUnreachableMap.containsKey(index)) {
            mUnreachableMap.put(index, point);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeUnreachable(Point point) {
        if (point.x < 0 || point.x >= mWidth || point.y < 0 || point.y >= mHeight) {
            return false;
        }
        int index = computeIndex(point);
        if (mUnreachableMap.containsKey(index)) {
            mUnreachableMap.remove(index);
            return true;
        }
        return false;
    }

    @Override
    public void clearUnreachable() {
        mUnreachableMap.clear();
    }

    @Override
    public List<Point> getPath(Point start, Point end) {
        mOpenPoints.clear();
        mOpenPointMap.clear();
        mClosePoints.clear();
        mClosePointMap.clear();

        if (start.x == end.x && start.y == end.y) {
            List<Point> path = new ArrayList<>();
            path.add(start);
            return path;
        }

        Compute compute = new Compute(start, null, 0, computeDistance(start, end));
        mOpenPoints.add(compute);
        mOpenPointMap.put(computeIndex(start), compute);

        final int[][] move = {
                {-1, 0}, // 往左走
                {1, 0}, // 往右走
                {0, -1}, // 往上走
                {0, 1} // 往下走
        };

        boolean findPath = false;
        while (!mOpenPoints.isEmpty() && !findPath) {
            compute = mOpenPoints.remove(0); // 开放列表是用插入排序排好序的,第一个就是目前的最优路径
            mClosePointMap.put(computeIndex(compute.getPoint()), compute);
            mClosePoints.add(compute);

            for (int i = 0; i < move.length; i++) {
                findPath = move(compute, move[i][0], move[i][1], end, mOpenPoints, mOpenPointMap, mClosePointMap, mUnreachableMap);

                if (!findPath) {
                    continue;
                }
                // 走到终点了
                mClosePoints.add(new Compute(end, compute, computeDistance(start, end), 0));
                break;
            }
        }

        //找到路径的话终点就会放到关闭列表的末尾
        if (!mClosePoints.isEmpty() && mClosePoints.get(mClosePoints.size() - 1).getPoint() == end) {
            compute = mClosePoints.get(mClosePoints.size() - 1);
            List<Point> path = new ArrayList<>();
            while (compute != null) {
                path.add(compute.getPoint());
                compute = compute.getParent();
            }
            return path;
        }

        return null;
    }

    private boolean move(Compute currentPosition,
                         int dx,
                         int dy,
                         Point end,
                         LinkedList<Compute> openPoints,
                         Map<Integer, Compute> openPointMap,
                         Map<Integer, Compute> closePointMap,
                         Map<Integer, Point> unreachableMap) {
        int nextX = currentPosition.getPoint().x + dx;
        int nextY = currentPosition.getPoint().y + dy;
        if (nextX == end.x && nextY == end.y) {
            return true;
        }
        int indexNextPoint = computeIndex(nextX, nextY);
        if (indexNextPoint != INVALID_INDEX
                && !closePointMap.containsKey(indexNextPoint)
                && !unreachableMap.containsKey(indexNextPoint)) {

            Compute nextStep = openPointMap.get(indexNextPoint);
            int nextDistanceToStartPoint = currentPosition.getDistanceToStart() + 1;
            if (nextStep == null) {
                // 如果之前没有探索过这个格子,直接放到开放列表
                Point nextPoint = new Point(nextX, nextY);
                nextStep = new Compute(
                        nextPoint,
                        currentPosition,
                        nextDistanceToStartPoint,
                        computeDistance(nextPoint, end));
                addToOpenList(nextStep, openPoints);
                openPointMap.put(computeIndex(nextStep.getPoint()), nextStep);
            } else if (nextStep.getDistanceToStart() > nextDistanceToStartPoint) {
                // 如果之前探索过这个格子,如果新路径可以更快的从起点走到该格子.则更新路径
                openPoints.remove(nextStep);
                nextStep.setDistanceToStart(nextDistanceToStartPoint);
                nextStep.setParent(currentPosition);
                addToOpenList(nextStep, openPoints);
            }
        }
        return false;
    }

    /**
     * 　插入排序,保证开放列表的第一个路径是目前最优解
     */
    private void addToOpenList(Compute point, LinkedList<Compute> openList) {
        ListIterator<Compute> it = openList.listIterator();
        while (it.hasNext()) {
            Compute cmp = it.next();
            if (point.getDistanceTotal() > cmp.getDistanceTotal()) {
                continue;
            }

            if ((point.getDistanceTotal() < cmp.getDistanceTotal())
                    || point.getDistanceToStart() < cmp.getDistanceToStart()) {
                it.previous();
                it.add(point);
                return;
            }
        }
        it.add(point);
    }

    private int computeDistance(Point a, Point b) {
        return Math.abs(b.x - a.x) + Math.abs(b.y - a.y);
    }

    private int computeIndex(Point point) {
        return computeIndex(point.x, point.y);
    }

    private int computeIndex(int x, int y) {
        if (x < 0 || x >= mWidth || y < 0 || y >= mHeight) {
            return INVALID_INDEX;
        }
        return x + y * mWidth;
    }

    public static class Compute {
        private Point point;
        private Compute parent;

        private int distanceToStart;
        private int distanceToEnd;
        private int distanceTotal;

        Compute(Point point, Compute parent, int distanceToStart, int distanceToEnd) {
            this.point = point;
            this.parent = parent;
            this.distanceToStart = distanceToStart;
            this.distanceToEnd = distanceToEnd;
            this.distanceTotal = distanceToStart + distanceToEnd;
        }

        Point getPoint() {
            return point;
        }

        Compute getParent() {
            return parent;
        }

        void setParent(Compute parent) {
            this.parent = parent;
        }

        int getDistanceToStart() {
            return distanceToStart;
        }

        void setDistanceToStart(int distanceToStart) {
            this.distanceToStart = distanceToStart;
            this.distanceTotal = this.distanceToStart + this.distanceToEnd;
        }

        int getDistanceTotal() {
            return distanceTotal;
        }
    }
}
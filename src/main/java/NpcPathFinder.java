import java.util.LinkedList;

public class NpcPathFinder {
    public static LinkedList<int[]> findPathForNpc(NPC npc, int destX, int destY) {
        final int MAP_SIZE = 104;
        int[][] via = new int[MAP_SIZE][MAP_SIZE];
        int[][] cost = new int[MAP_SIZE][MAP_SIZE];
        LinkedList<int[]> queue = new LinkedList<>();
        LinkedList<int[]> path = new LinkedList<>();

        int baseX = npc.absX - MAP_SIZE / 2;
        int baseY = npc.absY - MAP_SIZE / 2;

        int localStartX = npc.absX - baseX;
        int localStartY = npc.absY - baseY;
        int localDestX = destX - baseX;
        int localDestY = destY - baseY;

        if (localDestX < 0 || localDestY < 0 || localDestX >= MAP_SIZE || localDestY >= MAP_SIZE)
            return path;

        for (int x = 0; x < MAP_SIZE; x++) {
            for (int y = 0; y < MAP_SIZE; y++) {
                cost[x][y] = Integer.MAX_VALUE;
            }
        }

        queue.add(new int[]{localStartX, localStartY});
        cost[localStartX][localStartY] = 0;
        via[localStartX][localStartY] = -1;

        while (!queue.isEmpty()) {
            int[] pos = queue.poll();
            int cx = pos[0];
            int cy = pos[1];

            if (cx == localDestX && cy == localDestY) {
                LinkedList<int[]> reverse = new LinkedList<>();
                int x = cx, y = cy;
                while (via[x][y] != -1) {
                    reverse.addFirst(new int[]{baseX + x, baseY + y});
                    int dir = via[x][y];
                    x -= getDeltaX(dir);
                    y -= getDeltaY(dir);
                }
                path.addAll(reverse);
                break;
            }

            for (int d = 0; d < 8; d++) {
                int dx = getDeltaX(d);
                int dy = getDeltaY(d);
                int nx = cx + dx;
                int ny = cy + dy;

                if (nx < 0 || ny < 0 || nx >= MAP_SIZE || ny >= MAP_SIZE)
                    continue;
                if (cost[nx][ny] != Integer.MAX_VALUE)
                    continue;

                int worldX = baseX + cx;
                int worldY = baseY + cy;

                int toX = baseX + nx;
                int toY = baseY + ny;

                if (!canMove(npc.heightLevel, worldX, worldY, toX, toY, dx, dy))
                    continue;

                queue.add(new int[]{nx, ny});
                cost[nx][ny] = cost[cx][cy] + 1;
                via[nx][ny] = d;
            }
        }

        return path;
    }

    private static int getDeltaX(int dir) {
        return new int[]{-1, 1, 0, 0, -1, -1, 1, 1}[dir];
    }

    private static int getDeltaY(int dir) {
        return new int[]{0, 0, -1, 1, -1, 1, 1, -1}[dir];
    }

    private static boolean canMove(int z, int fromX, int fromY, int toX, int toY, int dx, int dy) {
        int clip = Region.getClipping(fromX, fromY, z);

        if (dx == -1 && dy == 0) return (clip & 0x1280108) == 0; // West
        if (dx == 1 && dy == 0) return (clip & 0x1280180) == 0; // East
        if (dx == 0 && dy == -1) return (clip & 0x1280102) == 0; // South
        if (dx == 0 && dy == 1) return (clip & 0x1280120) == 0; // North

        if (dx == -1 && dy == -1)
            return (clip & 0x128010e) == 0 &&
                    canMove(z, fromX, fromY, fromX - 1, fromY, -1, 0) &&
                    canMove(z, fromX, fromY, fromX, fromY - 1, 0, -1);

        if (dx == -1 && dy == 1)
            return (clip & 0x128013) == 0 &&
                    canMove(z, fromX, fromY, fromX - 1, fromY, -1, 0) &&
                    canMove(z, fromX, fromY, fromX, fromY + 1, 0, 1);

        if (dx == 1 && dy == -1)
            return (clip & 0x1280183) == 0 &&
                    canMove(z, fromX, fromY, fromX + 1, fromY, 1, 0) &&
                    canMove(z, fromX, fromY, fromX, fromY - 1, 0, -1);

        if (dx == 1 && dy == 1)
            return (clip & 0x12801e0) == 0 &&
                    canMove(z, fromX, fromY, fromX + 1, fromY, 1, 0) &&
                    canMove(z, fromX, fromY, fromX, fromY + 1, 0, 1);

        return false;
    }

}

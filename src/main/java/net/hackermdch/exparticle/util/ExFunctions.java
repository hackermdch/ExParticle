package net.hackermdch.exparticle.util;

import org.joml.Quaterniond;

import static net.minecraft.util.Mth.floor;

@SuppressWarnings("unused")
public class ExFunctions {
    private static final double[][] E3 = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};
    private static final double[][] E4 = {{1, 0, 0, 0}, {0, 1, 0, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}};

    public static int lerpInt(double delta, int start, int end) {
        return start + floor(delta * (double) (end - start));
    }

    public static double lerp(double delta, double start, double end) {
        return start + delta * (end - start);
    }

    public static double quadraticLerp(double delta, double start, double end, double x1, double y1) {
        if (delta <= 0) return start;
        if (delta >= 1) return end;
        x1 = Math.clamp(x1, 0, 1);
        var tMin = 0.0;
        var tMax = 1.0;
        var t = 0.5;
        while (tMax - tMin > 1e-10) {
            var xT = 2 * (1 - t) * t * x1 + t * t;
            if (Math.abs(xT - delta) < 1e-10) break;
            if (xT < delta) tMin = t;
            else tMax = t;
            t = (tMin + tMax) * 0.5;
        }
        return start + (2 * (1 - t) * t * y1 + t * t) * (end - start);
    }

    public static double cubicLerp(double delta, double start, double end, double x1, double y1, double x2, double y2) {
        if (delta <= 0) return start;
        if (delta >= 1) return end;
        x1 = Math.clamp(x1, 0, 1);
        x2 = Math.clamp(x2, 0, 1);
        var tMin = 0.0;
        var tMax = 1.0;
        var t = 0.5;
        while (tMax - tMin > 1e-10) {
            var oMT = 1.0 - t;
            var xT = 3 * oMT * oMT * t * x1 + 3 * oMT * t * t * x2 + t * t * t;
            if (Math.abs(xT - delta) < 1e-10) break;
            if (xT < delta) tMin = t;
            else tMax = t;
            t = (tMin + tMax) * 0.5;
        }
        var oMT = 1.0 - t;
        return start + (3 * oMT * oMT * t * y1 + 3 * oMT * t * t * y2 + t * t * t) * (end - start);
    }

    public static Quaterniond slerp(double delta, Quaterniond start, Quaterniond end) {
        if (delta <= 0) return start;
        if (delta >= 1) return end;
        return start.slerp(end, delta, new Quaterniond());
    }

    public static double[][] transpose(double[][] matrix) {
        if (matrix == null || matrix.length == 0) return new double[0][0];
        int originalRows = matrix.length;
        int originalCols = matrix[0].length;
        var transposed = new double[originalCols][originalRows];
        for (int i = 0; i < originalRows; i++) {
            for (int j = 0; j < originalCols; j++) transposed[j][i] = matrix[i][j];
        }
        return transposed;
    }

    public static int[][] transpose(int[][] matrix) {
        if (matrix == null || matrix.length == 0) return new int[0][0];
        int originalRows = matrix.length;
        int originalCols = matrix[0].length;
        var transposed = new int[originalCols][originalRows];
        for (int i = 0; i < originalRows; i++) {
            for (int j = 0; j < originalCols; j++) transposed[j][i] = matrix[i][j];
        }
        return transposed;
    }

    public static double[][] invert(double[][] matrix) {
        var n = matrix.length;
        if (n == 0) return new double[0][0];
        for (double[] row : matrix) if (row.length != n) return new double[0][0];
        var augmented = new double[n][2 * n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(matrix[i], 0, augmented[i], 0, n);
            augmented[i][n + i] = 1.0;
        }
        for (int i = 0; i < n; i++) {
            int maxRow = i;
            var maxVal = Math.abs(augmented[i][i]);
            for (int j = i + 1; j < n; j++) {
                if (Math.abs(augmented[j][i]) > maxVal) {
                    maxVal = Math.abs(augmented[j][i]);
                    maxRow = j;
                }
            }
            if (maxVal < 1e-10) return new double[0][0];
            if (maxRow != i) {
                var temp = augmented[i];
                augmented[i] = augmented[maxRow];
                augmented[maxRow] = temp;
            }
            var pivot = augmented[i][i];
            for (int j = i; j < 2 * n; j++) augmented[i][j] /= pivot;
            for (int j = 0; j < n; j++) {
                if (j != i && Math.abs(augmented[j][i]) > 1e-10) {
                    double factor = augmented[j][i];
                    for (int k = i; k < 2 * n; k++) {
                        augmented[j][k] -= factor * augmented[i][k];
                    }
                }
            }
        }
        var inverse = new double[n][n];
        for (int i = 0; i < n; i++) System.arraycopy(augmented[i], n, inverse[i], 0, n);
        return inverse;
    }

    public static double[][] translate(double x, double y, double z) {
        var mat = new double[4][4];
        mat[0][0] = 1.0;
        mat[1][1] = 1.0;
        mat[2][2] = 1.0;
        mat[3][3] = 1.0;
        mat[3][0] = x;
        mat[3][1] = y;
        mat[3][2] = z;
        return mat;
    }

    public static double[][] rotateDeg(double pitch, double yaw, double roll) {
        return rotate(Math.toRadians(pitch), Math.toRadians(yaw), Math.toRadians(roll));
    }

    public static double[][] rotate(double pitch, double yaw, double roll) {
        var cy = Math.cos(yaw * 0.5);
        var sy = Math.sin(yaw * 0.5);
        var cp = Math.cos(pitch * 0.5);
        var sp = Math.sin(pitch * 0.5);
        var cr = Math.cos(roll * 0.5);
        var sr = Math.sin(roll * 0.5);
        var crcp = cr * cp;
        var srcp = sr * cp;
        var crsp = cr * sp;
        var srsp = sr * sp;
        return rotate(new Quaterniond(crsp * cy - srcp * sy, crcp * sy, srcp * cy + crsp * sy, crcp * cy));
    }

    public static double[][] rotate(Quaterniond q) {
        var x = q.x;
        var y = q.y;
        var z = q.z;
        var w = q.w;
        var xx = x * x;
        var yy = y * y;
        var zz = z * z;
        var xy = x * y;
        var xz = x * z;
        var yz = y * z;
        var wx = w * x;
        var wy = w * y;
        var wz = w * z;
        var mat = new double[4][4];
        mat[0][0] = 1.0 - 2.0 * (yy + zz);
        mat[1][0] = 2.0 * (xy + wz);
        mat[2][0] = 2.0 * (xz - wy);
        mat[3][0] = 0.0;
        mat[0][1] = 2.0 * (xy - wz);
        mat[1][1] = 1.0 - 2.0 * (xx + zz);
        mat[2][1] = 2.0 * (yz + wx);
        mat[3][1] = 0.0;
        mat[0][2] = 2.0 * (xz + wy);
        mat[1][2] = 2.0 * (yz - wx);
        mat[2][2] = 1.0 - 2.0 * (xx + yy);
        mat[3][2] = 0.0;
        mat[0][3] = mat[1][3] = mat[2][3] = 0.0;
        mat[3][3] = 1.0;
        return mat;
    }

    public static double[][] scale(double x, double y, double z) {
        var mat = new double[4][4];
        mat[0][0] = x;
        mat[1][1] = y;
        mat[2][2] = z;
        mat[3][3] = 1;
        return mat;
    }

    public static double[][] identity(int n) {
        if (n <= 0) throw new IllegalArgumentException();
        if (n == 3) return E3;
        if (n == 4) return E4;
        var mat = new double[n][n];
        for (int i = 0; i < n; ++i) mat[i][i] = 1;
        return mat;
    }
}

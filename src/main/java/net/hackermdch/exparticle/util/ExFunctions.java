package net.hackermdch.exparticle.util;

import static net.minecraft.util.Mth.floor;

@SuppressWarnings("unused")
public class ExFunctions {
    public static int lerpInt(double delta, int start, int end) {
        return start + floor(delta * (double) (end - start));
    }

    public static double lerp(double delta, double start, double end) {
        return start + delta * (end - start);
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
        var cosP = Math.cos(pitch);
        var sinP = Math.sin(pitch);
        var cosY = Math.cos(yaw);
        var sinY = Math.sin(yaw);
        var cosR = Math.cos(roll);
        var sinR = Math.sin(roll);
        var mat = new double[4][4];
        mat[0][0] = cosY * cosR + sinP * sinY * sinR;
        mat[0][1] = -cosY * sinR + sinP * sinY * cosR;
        mat[0][2] = cosP * sinY;
        mat[0][3] = 0;
        mat[1][0] = cosP * sinR;
        mat[1][1] = cosP * cosR;
        mat[1][2] = -sinP;
        mat[1][3] = 0;
        mat[2][0] = -sinY * cosR + sinP * cosY * sinR;
        mat[2][1] = sinY * sinR + sinP * cosY * cosR;
        mat[2][2] = cosP * cosY;
        mat[2][3] = 0;
        mat[3][0] = 0;
        mat[3][1] = 0;
        mat[3][2] = 0;
        mat[3][3] = 1;
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
}

package net.hackermdch.exparticle.util;

public class MatrixUtil {
    public static int[][] matAdd(int[][] lmat, int r) {
        var result = new int[lmat.length][lmat[0].length];
        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = lmat[i][j] + r;
            }
        }
        return result;
    }

    public static double[][] matAdd(int[][] lmat, double r) {
        var result = new double[lmat.length][lmat[0].length];
        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = (double) lmat[i][j] + r;
            }
        }
        return result;
    }

    public static double[][] matAdd(double[][] lmat, int r) {
        var result = new double[lmat.length][lmat[0].length];
        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = lmat[i][j] + (double) r;
            }
        }
        return result;
    }

    public static double[][] matAdd(double[][] lmat, double r) {
        var result = new double[lmat.length][lmat[0].length];
        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = lmat[i][j] + r;
            }
        }
        return result;
    }

    public static int[][] matAdd(int[][] lmat, int[][] rmat) {
        var result = new int[lmat.length][lmat[0].length];
        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = lmat[i][j] + rmat[i][j];
            }
        }
        return result;
    }

    public static double[][] matAdd(int[][] lmat, double[][] rmat) {
        var result = new double[lmat.length][lmat[0].length];
        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = (double) lmat[i][j] + rmat[i][j];
            }
        }
        return result;
    }

    public static double[][] matAdd(double[][] lmat, int[][] rmat) {
        var result = new double[lmat.length][lmat[0].length];
        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = lmat[i][j] + (double) rmat[i][j];
            }
        }
        return result;
    }

    public static double[][] matAdd(double[][] lmat, double[][] rmat) {
        var result = new double[lmat.length][lmat[0].length];
        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = lmat[i][j] + rmat[i][j];
            }
        }
        return result;
    }

    public static int[][] matSub(int[][] lmat, int r) {
        var result = new int[lmat.length][lmat[0].length];
        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = lmat[i][j] - r;
            }
        }
        return result;
    }

    public static double[][] matSub(int[][] lmat, double r) {
        double[][] result = new double[lmat.length][lmat[0].length];

        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = (double) lmat[i][j] - r;
            }
        }

        return result;
    }

    public static double[][] matSub(double[][] lmat, int r) {
        double[][] result = new double[lmat.length][lmat[0].length];

        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = lmat[i][j] - (double) r;
            }
        }

        return result;
    }

    public static double[][] matSub(double[][] lmat, double r) {
        double[][] result = new double[lmat.length][lmat[0].length];

        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = lmat[i][j] - r;
            }
        }

        return result;
    }

    public static int[][] matSub(int[][] lmat, int[][] rmat) {
        int[][] result = new int[lmat.length][lmat[0].length];

        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = lmat[i][j] - rmat[i][j];
            }
        }

        return result;
    }

    public static double[][] matSub(int[][] lmat, double[][] rmat) {
        double[][] result = new double[lmat.length][lmat[0].length];

        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = (double) lmat[i][j] - rmat[i][j];
            }
        }

        return result;
    }

    public static double[][] matSub(double[][] lmat, int[][] rmat) {
        double[][] result = new double[lmat.length][lmat[0].length];

        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = lmat[i][j] - (double) rmat[i][j];
            }
        }

        return result;
    }

    public static double[][] matSub(double[][] lmat, double[][] rmat) {
        double[][] result = new double[lmat.length][lmat[0].length];

        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = lmat[i][j] - rmat[i][j];
            }
        }

        return result;
    }

    public static int[][] matMul(int[][] lmat, int r) {
        int[][] result = new int[lmat.length][lmat[0].length];

        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = lmat[i][j] * r;
            }
        }

        return result;
    }

    public static double[][] matMul(int[][] lmat, double r) {
        double[][] result = new double[lmat.length][lmat[0].length];

        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = (double) lmat[i][j] * r;
            }
        }

        return result;
    }

    public static double[][] matMul(double[][] lmat, int r) {
        double[][] result = new double[lmat.length][lmat[0].length];

        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = lmat[i][j] * (double) r;
            }
        }

        return result;
    }

    public static double[][] matMul(double[][] lmat, double r) {
        double[][] result = new double[lmat.length][lmat[0].length];

        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = lmat[i][j] * r;
            }
        }

        return result;
    }

    public static int[][] matMul(int[][] lmat, int[][] rmat) {
        int[][] result = new int[lmat.length][rmat[0].length];

        for (int i = 0; i < result.length; ++i) {
            for (int j = 0; j < result[i].length; ++j) {
                for (int k = 0; k < lmat[0].length; ++k) {
                    result[i][j] += lmat[i][k] * rmat[k][j];
                }
            }
        }

        return result;
    }

    public static double[][] matMul(int[][] lmat, double[][] rmat) {
        double[][] result = new double[lmat.length][rmat[0].length];

        for (int i = 0; i < result.length; ++i) {
            for (int j = 0; j < result[i].length; ++j) {
                for (int k = 0; k < lmat[0].length; ++k) {
                    result[i][j] += (double) lmat[i][k] * rmat[k][j];
                }
            }
        }

        return result;
    }

    public static double[][] matMul(double[][] lmat, int[][] rmat) {
        double[][] result = new double[lmat.length][rmat[0].length];

        for (int i = 0; i < result.length; ++i) {
            for (int j = 0; j < result[i].length; ++j) {
                for (int k = 0; k < lmat[0].length; ++k) {
                    result[i][j] += lmat[i][k] * (double) rmat[k][j];
                }
            }
        }

        return result;
    }

    public static double[][] matMul(double[][] lmat, double[][] rmat) {
        double[][] result = new double[lmat.length][rmat[0].length];

        for (int i = 0; i < result.length; ++i) {
            for (int j = 0; j < result[i].length; ++j) {
                for (int k = 0; k < lmat[0].length; ++k) {
                    result[i][j] += lmat[i][k] * rmat[k][j];
                }
            }
        }

        return result;
    }

    public static int[][] matDiv(int[][] lmat, int r) {
        int[][] result = new int[lmat.length][lmat[0].length];

        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = lmat[i][j] / r;
            }
        }

        return result;
    }

    public static double[][] matDiv(int[][] lmat, double r) {
        double[][] result = new double[lmat.length][lmat[0].length];

        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = (double) lmat[i][j] / r;
            }
        }

        return result;
    }

    public static double[][] matDiv(double[][] lmat, int r) {
        double[][] result = new double[lmat.length][lmat[0].length];

        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = lmat[i][j] / (double) r;
            }
        }

        return result;
    }

    public static double[][] matDiv(double[][] lmat, double r) {
        double[][] result = new double[lmat.length][lmat[0].length];

        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = lmat[i][j] / r;
            }
        }

        return result;
    }

    public static int[][] matMod(int[][] lmat, int r) {
        int[][] result = new int[lmat.length][lmat[0].length];

        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = lmat[i][j] % r;
            }
        }

        return result;
    }

    public static double[][] matMod(int[][] lmat, double r) {
        double[][] result = new double[lmat.length][lmat[0].length];

        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = (double) lmat[i][j] % r;
            }
        }

        return result;
    }

    public static double[][] matMod(double[][] lmat, int r) {
        double[][] result = new double[lmat.length][lmat[0].length];

        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = lmat[i][j] % (double) r;
            }
        }

        return result;
    }

    public static double[][] matMod(double[][] lmat, double r) {
        double[][] result = new double[lmat.length][lmat[0].length];

        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = lmat[i][j] % r;
            }
        }

        return result;
    }

    public static int[][] matNeg(int[][] lmat) {
        int[][] result = new int[lmat.length][lmat[0].length];

        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = -lmat[i][j];
            }
        }

        return result;
    }

    public static double[][] matNeg(double[][] lmat) {
        double[][] result = new double[lmat.length][lmat[0].length];

        for (int i = 0; i < lmat.length; ++i) {
            for (int j = 0; j < lmat[i].length; ++j) {
                result[i][j] = -lmat[i][j];
            }
        }

        return result;
    }

    public static int[][] matPow(int[][] mat, int k) {
        int[][] result = new int[mat.length][mat.length];

        for (int i = 0; i < result.length; ++i) {
            result[i][i] = 1;
        }

        while (k != 0) {
            if ((k & 1) == 1) {
                result = matMul(result, mat);
            }

            mat = matMul(mat, mat);
            k >>>= 1;
        }

        return result;
    }

    public static double[][] matPow(double[][] mat, int k) {
        double[][] result = new double[mat.length][mat.length];

        for (int i = 0; i < result.length; ++i) {
            result[i][i] = 1.0F;
        }

        while (k != 0) {
            if ((k & 1) == 1) {
                result = matMul(result, mat);
            }

            mat = matMul(mat, mat);
            k >>>= 1;
        }

        return result;
    }

    public static int[][] getConfactor(int[][] mat, int row, int col) {
        int[][] result = new int[mat.length - 1][mat[0].length - 1];

        for (int i = 0; i < result.length; ++i) {
            if (i < row - 1) {
                for (int j = 0; j < result[i].length; ++j) {
                    if (j < col - 1) {
                        result[i][j] = mat[i][j];
                    } else {
                        result[i][j] = mat[i][j + 1];
                    }
                }
            } else {
                for (int j = 0; j < result[i].length; ++j) {
                    if (j < col - 1) {
                        result[i][j] = mat[i + 1][j];
                    } else {
                        result[i][j] = mat[i + 1][j + 1];
                    }
                }
            }
        }

        return result;
    }

    public static double[][] getConfactor(double[][] mat, int row, int col) {
        double[][] result = new double[mat.length - 1][mat[0].length - 1];

        for (int i = 0; i < result.length; ++i) {
            if (i < row - 1) {
                for (int j = 0; j < result[i].length; ++j) {
                    if (j < col - 1) {
                        result[i][j] = mat[i][j];
                    } else {
                        result[i][j] = mat[i][j + 1];
                    }
                }
            } else {
                for (int j = 0; j < result[i].length; ++j) {
                    if (j < col - 1) {
                        result[i][j] = mat[i + 1][j];
                    } else {
                        result[i][j] = mat[i + 1][j + 1];
                    }
                }
            }
        }

        return result;
    }

    public static int[][] toMat(int n) {
        return new int[][]{{n}};
    }

    public static double[][] toMat(double n) {
        return new double[][]{{n}};
    }

    public static double[][] toMat(String str) {
        if (str != null && !str.isEmpty() && !str.equals("E3")) {
            if (str.startsWith("E")) {
                int size = Integer.parseInt(str.substring(1));
                double[][] result = new double[size][size];

                for (int i = 0; i < size; ++i) {
                    result[i][i] = 1.0F;
                }

                return result;
            } else {
                while (str.startsWith("(")) {
                    str = str.substring(1, str.length() - 1);
                }

                String[] rowstr = str.split(",,");
                int rows = rowstr.length;
                double[][] result = new double[rows][];
                int cols = -1;

                for (int i = 0; i < rows; ++i) {
                    String[] colstr = rowstr[i].split(",");
                    if (cols == -1) {
                        cols = colstr.length;
                    } else if (cols != colstr.length) {
                        throw new RuntimeException("matrix create error: " + str);
                    }

                    result[i] = new double[cols];

                    for (int j = 0; j < cols; ++j) {
                        result[i][j] = Double.parseDouble(colstr[j]);
                    }
                }

                return result;
            }
        } else {
            return new double[][]{{(double) 1.0F, (double) 0.0F, (double) 0.0F}, {(double) 0.0F, (double) 1.0F, (double) 0.0F}, {(double) 0.0F, (double) 0.0F, (double) 1.0F}};
        }
    }

    public static int toNumber(int[][] mat) {
        if (mat.length != mat[0].length) {
            return 0;
        } else if (mat.length == 1) {
            return mat[0][0];
        } else if (mat.length == 2) {
            return mat[0][0] * mat[1][1] - mat[0][1] * mat[1][0];
        } else {
            int result = 0;

            for (int i = 0; i < mat.length; ++i) {
                if (i % 2 == 0) {
                    result += mat[0][i] * toNumber(getConfactor(mat, 1, i + 1));
                } else {
                    result -= mat[0][i] * toNumber(getConfactor(mat, 1, i + 1));
                }
            }

            return result;
        }
    }

    public static double toNumber(double[][] mat) {
        if (mat.length != mat[0].length) {
            return 0.0F;
        } else if (mat.length == 1) {
            return mat[0][0];
        } else if (mat.length == 2) {
            return mat[0][0] * mat[1][1] - mat[0][1] * mat[1][0];
        } else {
            double result = 0.0F;

            for (int i = 0; i < mat.length; ++i) {
                if (i % 2 == 0) {
                    result += mat[0][i] * toNumber(getConfactor(mat, 1, i + 1));
                } else {
                    result -= mat[0][i] * toNumber(getConfactor(mat, 1, i + 1));
                }
            }

            return result;
        }
    }

    public static double[][] matToMat(int[][] mat) {
        double[][] result = new double[mat.length][];

        for (int i = 0; i < mat.length; ++i) {
            result[i] = new double[mat[i].length];

            for (int j = 0; j < mat[i].length; ++j) {
                result[i][j] = mat[i][j];
            }
        }

        return result;
    }

    public static int[][] matToMat(double[][] mat) {
        int[][] result = new int[mat.length][];

        for (int i = 0; i < mat.length; ++i) {
            result[i] = new int[mat[i].length];

            for (int j = 0; j < mat[i].length; ++j) {
                result[i][j] = (int) mat[i][j];
            }
        }

        return result;
    }

    public static double pow(int l, int r) {
        return Math.pow(l, r);
    }
}

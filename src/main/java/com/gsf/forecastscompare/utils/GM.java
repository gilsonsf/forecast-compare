package com.gsf.forecastscompare.utils;

public class GM {

    /**
     * @param args
     */
    public static void main(String[] args) {
        double []arr={2.67,3.13,3.25,3.36,3.56,3.72,3.12,3.33,2.12};
        System.out.println(gm(arr,3));
    }

    public static double gm(double[] fs, int T) {

        // Predictive model function
        int size = fs.length;
        int tsize = fs.length - 1;
        double[] arr = fs;//  Original array
        double[] arr1 = new double[size];//  Accumulate the array once
        double sum = 0;
        for (int i = 0; i < size; i++) {
            sum += arr[i];
            arr1[i] = sum;
        }
        double[] arr2 = new double[tsize];//  Arr1's immediate mean array
        for (int i = 0; i < tsize; i++) {
            arr2[i] = (double) (arr1[i] + arr1[i + 1]) / 2;
        }
        /*
         *
         * Next, set up vectors B and YN to solve the parameter vector to be estimated, that is, find the parameters a, b
         */
        /*
         * Create a vector B below. B is a matrix with 5 rows and 2 columns, which is equivalent to a two-dimensional array.
         */
        double[][] B = new double[tsize][2];
        for (int i = 0; i < tsize; i++) {
            for (int j = 0; j < 2; j++) {
                if (j == 1)
                    B[i][j] = 1;
                else
                    B[i][j] = -arr2[i];
            }

        }
        /*
         * Create the vector YN below
         */
        double[][] YN = new double[tsize][1];
        for (int i = 0; i < tsize; i++) {
            for (int j = 0; j < 1; j++) {
                YN[i][j] = arr[i + 1];
            }
        }

        /*
         * B's transposed matrix BT, a matrix of 2 rows and 5 columns
         */
        double[][] BT = new double[2][tsize];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < tsize; j++) {
                BT[i][j] = B[j][i];
            }
        }
        /*
         * Record the result of the product of BT and B as the array B2T, then B2T is a 2*2 matrix
         */
        double[][] B2T = new double[2][2];
        for (int i = 0; i < 2; i++) {// rows of BT

            {
                for (int j = 0; j < 2; j++)// cloums of B
                {
                    for (int k = 0; k < tsize; k++)// cloums of BT=rows of B
                    {
                        B2T[i][j] = B2T[i][j] + BT[i][k] * B[k][j];
                    }
                }

            }
        }
        /*  Next, find the inverse matrix of B2T and set it as B_2T. How does it apply to a general matrix? */
        double[][] B_2T = new double[2][2];
        B_2T[0][0] = (1 / (B2T[0][0] * B2T[1][1] - B2T[0][1] * B2T[1][0]))
                * B2T[1][1];
        B_2T[0][1] = (1 / (B2T[0][0] * B2T[1][1] - B2T[0][1] * B2T[1][0]))
                * (-B2T[0][1]);
        B_2T[1][0] = (1 / (B2T[0][0] * B2T[1][1] - B2T[0][1] * B2T[1][0]))
                * (-B2T[1][0]);
        B_2T[1][1] = (1 / (B2T[0][0] * B2T[1][1] - B2T[0][1] * B2T[1][0]))
                * B2T[0][0];
        /*
         * Find the unknown quantities a and b of the parameters to be estimated according to the known quantities obtained above, and the vector matrix to be estimated is equal to B_2T*BT*YN
         * Below we find the product of these matrices separately, first find B_2T*BT, let the product of B_2T*BT be A matrix, then A is a 2*5 matrix
         */
        /*
         *
         *
         *
         * First find the A matrix below
         */
        double[][] A = new double[2][tsize];
        for (int i = 0; i < 2; i++) {// rows of B_2T
            {
                for (int j = 0; j < tsize; j++)// cloums of BT
                {
                    for (int k = 0; k < 2; k++)// cloums of B_2T=rows of BT
                    {
                        A[i][j] = A[i][j] + B_2T[i][k] * BT[k][j];
                    }
                }

            }
        }
        /*
         *
         *
         * Next, find the product of A and YN matrix, and let the product be a C matrix, then C is a 2*1 matrix
         */
        double[][] C = new double[2][1];
        for (int i = 0; i < 2; i++) {// rows of A

            {
                for (int j = 0; j < 1; j++)// cloums of YN
                {
                    for (int k = 0; k < tsize; k++)// cloums of A=rows of YN
                    {
                        C[i][j] = C[i][j] + A[i][k] * YN[k][j];
                    }
                }

            }
        }
        /*  According to the above, a=C[0][0], b=C[1][0]; */
        double a = C[0][0], b = C[1][0];
        int i = T;//  Read a value
        double Y = (arr[0] - b / a) * Math.exp(-a * (i + 1)) - (arr[0] - b / a)
                * Math.exp(-a * i);

        return Y;
    }

}
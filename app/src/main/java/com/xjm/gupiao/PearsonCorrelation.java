package com.xjm.gupiao;

import java.util.ArrayList;

public class PearsonCorrelation {
    public static float getPearson(ArrayList<PearsonShares> list1, ArrayList<PearsonShares> list2) {
        int length_1 = list1.size();
        int length_2 = list2.size();
        float[] openData_1 = new float[length_1];
        float[] openData_2 = new float[length_2];
        float[] closeData_1 = new float[length_1];
        float[] closeData_2 = new float[length_2];
        float[] heightData_1 = new float[length_1];
        float[] heightData_2 = new float[length_2];
        float[] lowData_1 = new float[length_1];
        float[] lowData_2 = new float[length_2];
        for (int i = 0; i < length_1; i++) {
            PearsonShares shares = list1.get(i);
            openData_1[i] = shares.getOpen();
            closeData_1[i] = shares.getClose();
            heightData_1[i] = shares.getHeight();
            lowData_1[i] = shares.getLow();
        }
        for (int i = 0; i < length_2; i++) {
            PearsonShares shares = list2.get(i);
            openData_2[i] = shares.getOpen();
            closeData_2[i] = shares.getClose();
            heightData_2[i] = shares.getHeight();
            lowData_2[i] = shares.getLow();
        }

        float open_data_1_means = getMeans(openData_1);
        float open_data_2_means = getMeans(openData_2);
        float open_data_numerator = generateNumerator(openData_1, openData_2, open_data_1_means, open_data_2_means);
        float open_data_denominator = generateDenomiator(openData_1, openData_2, open_data_1_means, open_data_2_means);
        float open_pearson_number = open_data_numerator / open_data_denominator;

        float close_data_1_means = getMeans(closeData_1);
        float close_data_2_means = getMeans(closeData_2);
        float close_data_numerator = generateNumerator(closeData_1, closeData_2, close_data_1_means, close_data_2_means);
        float close_data_denominator = generateDenomiator(closeData_1, closeData_2, close_data_1_means, close_data_2_means);
        float close_pearson_number = close_data_numerator / close_data_denominator;

        float height_data_1_means = getMeans(heightData_1);
        float height_data_2_means = getMeans(heightData_2);
        float height_data_numerator = generateNumerator(heightData_1, heightData_2, height_data_1_means, height_data_2_means);
        float height_data_denominator = generateDenomiator(heightData_1, heightData_2, height_data_1_means, height_data_2_means);
        float height_pearson_number = height_data_numerator / height_data_denominator;

        float low_data_1_means = getMeans(lowData_1);
        float low_data_2_means = getMeans(lowData_2);
        float low_data_numerator = generateNumerator(lowData_1, lowData_2, low_data_1_means, low_data_2_means);
        float low_data_denominator = generateDenomiator(lowData_1, lowData_2, low_data_1_means, low_data_2_means);
        float low_pearson_number = low_data_numerator / low_data_denominator;

        //四个皮尔逊系数的平均值
        return (open_pearson_number + close_pearson_number + height_pearson_number + low_pearson_number) / 4.0f;
    }

    //生成分子
    private static float generateNumerator(float[] data_1, float[] data_2, float means_1, float means_2) {
        float sum = 0.0f;
        for (int i = 0; i < data_1.length; i++) {
            sum += (data_1[i] - means_1) * (data_2[i] - means_2);
        }
        return sum;
    }

    //生成分母
    private static float generateDenomiator(float[] data_1, float[] data_2, float means_1, float means_2) {
        float xSum = 0.0f;
        for (int i = 0; i < data_1.length; i++) {
            xSum += (data_1[i] - means_1) * (data_1[i] - means_1);
        }
        float ySum = 0.0f;
        for (int i = 0; i < data_2.length; i++) {
            ySum += (data_2[i] - means_2) * (data_2[i] - means_2);
        }
        return (float) (Math.sqrt(xSum) * Math.sqrt(ySum));
    }

    //进行平均值计算
    private static float getMeans(float[] data) {
        float sum = 0.0f;
        for (int i = 0; i < data.length; i++) {
            sum += data[i];
        }
        return sum / data.length;
    }

    public class PearsonShares {
        private float open;    //开盘价
        private float close;   //收盘价
        private float height;  //最高价
        private float low;     //最低价

        public PearsonShares(float open, float close, float height, float low) {
            this.open = open;
            this.close = close;
            this.height = height;
            this.low = low;
        }

        public float getOpen() {
            return open;
        }

        public float getClose() {
            return close;
        }

        public float getHeight() {
            return height;
        }

        public float getLow() {
            return low;
        }
    }
}

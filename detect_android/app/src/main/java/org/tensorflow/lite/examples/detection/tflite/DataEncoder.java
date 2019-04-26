package org.tensorflow.lite.examples.detection.tflite;

import android.util.Log;

import java.util.ArrayList;

public class DataEncoder {
    private static final String TAG = "TFObjectDetection";
    private int boxesNum = 21824;
    private int layersNum = 3;

    private float scale;
    private float[] steps = new float[layersNum];
    private float[] sizes = new float[layersNum];
    private ArrayList<Integer[]> aspect_ratios = new ArrayList<Integer[]>();
    private int[] feature_map_sizes = new int[layersNum];
    private ArrayList<Integer[]> density = new ArrayList<Integer[]>();
    private float[][] boxes = new float[this.boxesNum][4];     // [21824, 4] (cx, cy, w, h)

    public DataEncoder(float imageSize) {
        this.scale = imageSize;

        // steps = [s / scale for s in (32, 64, 128)]      // [0.03125, 0.0625, 0.125]
        this.steps[0] = (float)(32.0 / this.scale);
        this.steps[1] = (float)(64.0 / this.scale);
        this.steps[2] = (float)(128.0 / this.scale);

        // sizes = [s / scale for s in (32, 256, 512)]     // [0.03125, 0.25, 0.5]     当32改为64时，achor与label匹配的正样本数目更多
        this.sizes[0] = (float)(32.0 / this.scale);
        this.sizes[1] = (float)(256.0 / this.scale);
        this.sizes[2] = (float)(512.0 / this.scale);

        // aspect_ratios = ((1, 2, 4), (1,), (1,))
        this.aspect_ratios.add(new Integer[]{1, 2, 4});
        this.aspect_ratios.add(new Integer[]{1});
        this.aspect_ratios.add(new Integer[]{1});

        // feature_map_sizes = (32, 16, 8)
        this.feature_map_sizes[0] = 32;
        this.feature_map_sizes[1] = 16;
        this.feature_map_sizes[2] = 8;

        //density = [[-3, -1, 1, 3], [-1, 1], [0]]        // density for output layer1
        this.density.add(new Integer[]{-3, -1, 1, 3});
        this.density.add(new Integer[]{-1, 1});
        this.density.add(new Integer[]{0});

        int curBoxIndex = 0;

        for (int layerIndex = 0; layerIndex < this.layersNum; layerIndex++) {    // 遍历3层中的每一层
            int fmsize = this.feature_map_sizes[layerIndex];        // 分别为32, 16, 8

            // 生成32×32个，16×16个, 8×8个二元组，如：(0,0), (0,1), (0,2), ... (1,0), (1,1), ..., (32,32)
            for (float box_y = 0; box_y < fmsize; box_y++) {
                for (float box_x = 0; box_x < fmsize; box_x++) {
                    // cx = (w + 0.5)*steps[i]                     # 中心点坐标x
                    // cy = (h + 0.5)*steps[i]                     # 中心点坐标y
                    float center_x = (float)((box_x + 0.5) * this.steps[layerIndex]);   // 中心点坐标x
                    float center_y = (float)((box_y + 0.5) * this.steps[layerIndex]);   // 中心点坐标y

                    float s = this.sizes[layerIndex];
                    for (int ratios_i = 0; ratios_i < this.aspect_ratios.get(layerIndex).length; ratios_i++) {
                        Integer ar = this.aspect_ratios.get(layerIndex)[ratios_i];

                        if (layerIndex == 0) {
                            for (float dy = 0; dy < this.density.get(ratios_i).length; dy++) {
                                for (float dx = 0; dx < this.density.get(ratios_i).length; dx++) {
                                    this.boxes[curBoxIndex][0] = (float)(center_x + dx / 8.0 * s * ar);
                                    this.boxes[curBoxIndex][1] = (float)(center_y + dy / 8.0 * s * ar);
                                    this.boxes[curBoxIndex][2] = s * ar;
                                    this.boxes[curBoxIndex][3] = s * ar;
                                    curBoxIndex++;
                                }
                            }
                        } else  {
                            this.boxes[curBoxIndex][0] = center_x;
                            this.boxes[curBoxIndex][1] = center_y;
                            this.boxes[curBoxIndex][2] = s * ar;
                            this.boxes[curBoxIndex][3] = s * ar;
                            curBoxIndex++;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < 5000; i++) {
            Log.i(TAG, String.format("%d: %f, %f, %f, %f", i, this.boxes[i][0], this.boxes[i][1],
                    this.boxes[i][2], this.boxes[i][3]));
        }
        Log.i(TAG, String.format("curBoxIndex %d", curBoxIndex));
//        Log.i(TAG, String.format("aspect_ratios %d", this.aspect_ratios.get(0).length));
//        Log.i(TAG, String.format("aspect_ratios %d", this.aspect_ratios.get(1).length));
//        Log.i(TAG, String.format("aspect_ratios %d", this.aspect_ratios.get(2).length));
//
//        Log.i(TAG, String.format("density %d", this.density.get(0).length));
//        Log.i(TAG, String.format("density %d", this.density.get(1).length));
//        Log.i(TAG, String.format("density %d", this.density.get(2).length));
    }

    public int getBoxesNum() {return this.boxesNum;}
}

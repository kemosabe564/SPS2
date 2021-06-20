package com.example.sps_2;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.io.BufferedReader;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import biz.source_code.dsp.math.Complex;
import biz.source_code.dsp.transform.Dft;


public class MainActivity extends AppCompatActivity implements SensorEventListener,View.OnClickListener {
    // true for parallel
    public static final boolean MODE = true;
    public static boolean TERMINATED = false;
    public static boolean GAUSSION_FIT = true;
    public static boolean STOP = false;



    private SensorManager mSensorMgr;
    private WifiManager wifiManager;
    private WifiInfo wifiInfo;
    // for printing wifi info
    private TextView wifi_info;

    private TextView testing_overview;
    // for printing final result
    private TextView result;

    // for entering cell number when training
    private EditText cellInfo;
    // for entering action
    private EditText experimentInfo;
    // for entering cell number when testing
    private EditText testcellInfo;
    // for entering action
//    private EditText testexperimentInfo;

    public String TAG="my_app";

    public String Rawdata;
    int index;
    // result
    int AP = 11;
    int CellNO = 8;
    int Train_MAX = 100;
    int Test_MAX = 40;
    int test_count;


    int test_round = 0;
    int TEST_ROUND = 20;
    int[] test_result = new int[CellNO];


    DecimalFormat df = new DecimalFormat("0.0000");

    // 判断training和testing
    boolean states;

    // 存储一个AP的信息
    public class oneAP {
        String MacAdd = "";
        int APindex = 0;
        int RSSIValue = 0;
        int number = 0;
    }
    // 存储先验
    // TODO: 修改Piror长度
    double[] piror = new double[CellNO];
    double[] newPrior = new double[CellNO];

    // 存储一个AP下，一次贝叶斯过滤前后的结果
    public class oneBF{
        double[] post = new double[CellNO];
        int count = 0;
    }


//    String w1 = "star_platinum", w2 = "Heaven's_Door";

    // TODO:
    // 1. adding a array of AP, OK
    // 2. modifying the file name movement
    // 3. 修改参数，采样间隔，采样次数
    // 4. 修改textview，删除多余的变量


    String[] Wifi_list = new String[AP];
    oneAP[] testScanAP = new oneAP[AP];
    oneBF[] BF = new oneBF[AP];

    // old below

    WifiManager wifia;

    // for files
//    File RootFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"SPS2");
    File RawRootFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"SPS2/raw");  // see if the required folders already exist. >>
    // TODO: 维护文件存储
    File MapRootFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"SPS2/map");  // see if the required folders already exist. >>
    File TestingRootFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"SPS2/test");

//    Map<Integer, Integer> roomMap = new HashMap<>();
//    Map<Integer, Double> distanceMap = new HashMap<>();
//    Map<Integer, Integer> numberMap = new HashMap<>();
//    Map<Integer, String> actionMap = new HashMap<>();
//    Map<Integer, Double> distanceMap_cell = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button bt=findViewById(R.id.bt_start);
        bt.setOnClickListener(this);

        Button bt_stop=findViewById(R.id.bt_stop);
        bt_stop.setOnClickListener(this);

        Button bt_mapCreate=findViewById(R.id.bt_map_create);
        bt_mapCreate.setOnClickListener(this);

        Button bt_mapUpdate=findViewById(R.id.bt_map_update);
        bt_mapUpdate.setOnClickListener(this);

        Button bt_testReset=findViewById(R.id.bt_test_reset);
        bt_testReset.setOnClickListener(this);

        Button bt_testing=findViewById(R.id.bt_testing);
        bt_testing.setOnClickListener(this);

        wifi_info = findViewById(R.id.wifi_info);
        wifi_info.setMovementMethod(ScrollingMovementMethod.getInstance());


        cellInfo = findViewById(R.id.cell);
        experimentInfo = findViewById(R.id.experimentNo);
        testcellInfo = findViewById(R.id.testcell);
        testing_overview = findViewById(R.id.testing_overview);
        result = findViewById(R.id.result);
        result.setMovementMethod(ScrollingMovementMethod.getInstance());


        // TODO: 清理不用的变量
//        // adding our own APs
//        // "SKY"
//        Wifi_list[0] = "26:c6:3b:61:cc:89";
//        // HONOR
//        Wifi_list[1] = "02:5e:20:b2:48:73";
//        // ""
//        Wifi_list[2] = "04:18:d6:22:98:8c";
//        // ""
//        Wifi_list[3] = "06:18:d6:21:51:49";
//        // "LYD"
//        Wifi_list[4] = "5e:87:9c:21:4c:d3";
////        Wifi_list[5] = "0e:ec:da:ad:f8:29";
//        //
//        Wifi_list[5] = "16:18:d6:21:51:49";
////        Wifi_list[7] = "16:18:d6:21:98:87";
//        Wifi_list[6] = "16:18:d6:21:98:8c";
//        Wifi_list[7] = "06:18:d6:21:98:8c";
//        //
//        Wifi_list[8] = "06:18:d6:22:a4:70";
//        Wifi_list[9] = "06:18:d6:22:98:8c";
//        // no
//        Wifi_list[10] = "fc:ec:da:ad:f8:29";
//        //
//        Wifi_list[11] = "04:18:d6:e2:65:8b";
//

//        // "SKY"
        Wifi_list[0] = "26:c6:3b:61:cc:89";
//        Wifi_list[0] = "04:18:d6:22:51:49";
        //"P10"
        Wifi_list[1] = "78:62:56:2c:f8:e7";
        // HONOR
        Wifi_list[2] = "12:35:10:ce:3c:e2";
        // ""
//        Wifi_list[2] = "04:18:d6:22:98:8c";
        // ""
        Wifi_list[3] = "06:18:d6:21:51:49";

        Wifi_list[4] = "c4:36:55:3c:de:49";
//        Wifi_list[5] = "0e:ec:da:ad:f8:29";
        //
        Wifi_list[5] = "16:18:d6:21:51:49";
//        Wifi_list[7] = "16:18:d6:21:98:87";
        Wifi_list[6] = "16:18:d6:21:98:8c";
        Wifi_list[7] = "06:18:d6:21:98:8c";
        //
        Wifi_list[8] = "06:18:d6:22:a4:70";
        Wifi_list[9] = "06:18:d6:22:98:8c";
        // no
        Wifi_list[10] = "16:18:d6:22:51:49";

        //
        for (int i = 0; i < AP; i++)
        {
            testScanAP[i] = new oneAP();
            testScanAP[i].MacAdd = Wifi_list[i];
            testScanAP[i].APindex = i+1;
            BF[i] = new oneBF();
        }
        // 初始化prior
        for (int i = 0; i < CellNO; i++)
        {
            piror[i] = 1.0/CellNO;
//            Log.i(TAG,Double.toString(piror[i]) + "\n");
        }



        //

        mSensorMgr=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        wifia = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        index = 0;
        test_count = 0;
        test_round = 0;
    }

    protected void onPause()
    {
        super.onPause();
        mSensorMgr.unregisterListener(this);
    }
    protected void onResume()
    {
        super.onResume();
    }
    protected void onStop()
    {
        super.onStop();
        mSensorMgr.unregisterListener(this);

    }
    public void onAccuracyChanged(Sensor sensor,int accuracy)
    {
        return;
    }
    public void onSensorChanged(SensorEvent event)
    {
        String wifi = "";

        if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER) {
            Rawdata = " ";

            wifia.startScan();

            List<ScanResult> scanResult = wifia.getScanResults();
            List<String> list = Arrays.asList(Wifi_list);
            //

            for (int i = 0; i < AP; i++) {
                boolean Flag = false;
                for (int j = 0; j < scanResult.size(); j++) {
                    if (scanResult.get(j).BSSID.equals(Wifi_list[i])) {
                        if (i == 0) {
                            wifi = scanResult.get(j).BSSID + ", " + scanResult.get(j).level + ", ";
                        } else {
                            wifi += scanResult.get(j).BSSID + ", " + scanResult.get(j).level + ", ";
                        }
                        Flag = true;
                    }
                }
                if (!Flag) {
                    wifi += Wifi_list[i] + ", " + "-300" + ", ";
                }

            }
//            for (int j = 0; j < scanResult.size(); j++) {
//                wifi += scanResult.get(j).SSID + ", " + scanResult.get(j).BSSID + ", " + scanResult.get(j).level;
//            }


//            Rawdata = Float.toString(index) +", " + wifi + Long.toString(System.currentTimeMillis());
            wifi_info.setText(wifi + "\n");
            int N = Train_MAX;
            if (!states) {
                N = Test_MAX;
            }
            String filename;
            File RootF;
            if (states) {
                String cell = cellInfo.getText().toString();
                String experiment = experimentInfo.getText().toString();
                Rawdata = Float.toString(index) + ", " + cell + ", " + wifi + Long.toString(System.currentTimeMillis());
                filename = "cell" + "_" + cell + "_" + "No" + "_" + experiment + "_" + "CollectedData.txt";
                RootF = RawRootFile;
                if (index < N) {
                    addToFile(Rawdata, RootF, filename, true);
                    index = index + 1;
                } else {
                    mSensorMgr.unregisterListener(this);
                    wifi_info.append(Integer.toString(index) + " data stored for " + filename);
                    index = 0;
                    return;
                }
            } else {
                // 存储数据然后计算平均数
                while (!TERMINATED) {
                    String cell = testcellInfo.getText().toString();
                    Rawdata = Float.toString(index) + ", " + cell + ", " + wifi + Long.toString(System.currentTimeMillis());
                    String[] content = wifi.split(", ");
                    // 计算平均数
                    for (int i = 0; i < AP; i++) {
                        if (Integer.parseInt(content[2 * i + 1]) != -300 && index < N) {
                            testScanAP[i].RSSIValue += Integer.parseInt(content[2 * i + 1]);
                            testScanAP[i].number += 1;
                        }
                        if (index == N) {
                            if (testScanAP[i].number != 0) {
                                testScanAP[i].RSSIValue = testScanAP[i].RSSIValue / testScanAP[i].number;
                            } else {
                                testScanAP[i].RSSIValue = -300;
                            }
                        }
                        Log.i(TAG, testScanAP[4].MacAdd + " " + testScanAP[4].RSSIValue + " " + testScanAP[4].number+ "\n");
                    }

                    // 存储
                    filename = "cell" + "_" + cell + "_" + "TestingData.txt";
                    RootF = TestingRootFile;
                    if (index < N) {
                        addToFile(Rawdata, RootF, filename, true);
                        index = index + 1;
                    }
                    else {
                        test_count++;
                        mSensorMgr.unregisterListener(this);
//                    Log.i(TAG, Integer.toString(index));
                        testing_overview.setText(Integer.toString(index) + " data stored for " + filename);
//                    result.append(testScanAP[1].MacAdd + " " + testScanAP[1].RSSIValue + " " + testScanAP[1].number+ "\n");
                        // 排列，从大到小RSSI负
                        sort(testScanAP);
                        Log.i(TAG, testScanAP[0].MacAdd + " " + testScanAP[0].RSSIValue + " " + testScanAP[0].number+ "\n");
                        // parallel
                        if (MODE) {
                            // 暂时只对前三高RSSI做BF
                            for (int i = 0; i < 3; i++) {
                                int j = 2*i + 1;
                                j = i;
                                if (testScanAP[j].RSSIValue <= -300) {
                                    BF[testScanAP[j].APindex - 1].post = piror;
                                } else {
                                    result.append(Integer.toString(testScanAP[j].APindex) + ", " + testScanAP[j].RSSIValue + ", " + testScanAP[j].MacAdd + "\n");
                                    Log.i(TAG, Integer.toString(testScanAP[j].APindex) + ", " + testScanAP[j].RSSIValue + ", " + testScanAP[j].MacAdd + "\n");
                                    Log.i(TAG, piror[0] + piror[1] + piror[2] + " " + piror[3] + piror[4] + piror[5] + piror[6] + piror[7] + "\n");
                                    BayesFilter(testScanAP[j].APindex, testScanAP[j].RSSIValue, BF[testScanAP[j].APindex - 1], piror);
                                }
//                        result.append(Integer.toString(testScanAP[i].RSSIValue) + "\n");
                                BF[testScanAP[j].APindex - 1].post = normalization(BF[testScanAP[j].APindex - 1].post);
                                BF[testScanAP[j].APindex - 1].count = test_count;
                            }
                            // 归一化
                            piror = normalization(piror);
//                    BF.post = normalization(BF.post);
//                            Log.i(TAG, "newprior: 1: " + BF[0].post + ", 2: " + newPrior[1] + ", 3: " + newPrior[2] + ", 4: " + newPrior[3] + ", 5: " + newPrior[4] + ", 6: " + newPrior[5] + ", 7: " + newPrior[6] + ", 8: " + newPrior[7] + " " + "\n");

                            updatePrior(BF);
                        }
                        //  serial
                        else {
                            for (int i = 0; i < 3; i++) {
                                Log.i(TAG,"piror:" + piror[0] + "," + piror[1] + "," + piror[2] + "," + piror[3] + "," + piror[4] + "," + piror[5] + "," + piror[6] + "," + piror[7] + "\n");
                                if (testScanAP[i].RSSIValue <= -300) {
                                    BF[testScanAP[i].APindex - 1].post = piror;
                                } else {
                                    result.append(Integer.toString(testScanAP[i].APindex) + ", " + testScanAP[i].RSSIValue + ", " + testScanAP[i].MacAdd + "\n");
                                    BayesFilter(testScanAP[i].APindex, testScanAP[i].RSSIValue, BF[testScanAP[i].APindex - 1], piror);

                                }
                                Log.i(TAG,"piror:" + piror[0] + "," + piror[1] + "," + piror[2] + "," + piror[3] + "," + piror[4] + "," + piror[5] + "," + piror[6] + "," + piror[7] + "\n");

                                BF[testScanAP[i].APindex - 1].post = normalization(BF[testScanAP[i].APindex - 1].post);
                                BF[testScanAP[i].APindex - 1].count = test_count;

                                piror = updateArray(piror, BF[testScanAP[i].APindex - 1].post);
//                                piror = BF[testScanAP[i].APindex - 1].post;
                                piror = normalization(piror);
                                newPrior = updateArray(newPrior, piror);
                            }

                        }

                        // 展示数据
                        Log.i(TAG, "1 error 1");

//                    for (int i = 0; i < AP; i++){
//                        result.append("post for AP" + i +": ");
//                        for (int j = 0; j < AP; j++){
//                            result.append(BF[i].post[j] + ", ");
//                        }
//                        result.append(BF[i].count + "\n");
//                    }
//
//                    result.append("prior: ");
//                    for (int j = 0; j < AP; j++){
//                        result.append(piror[j] + ", ");
//                    }
//                    result.append("\n");
//                    result.append("new prior: ");
//                    for (int j = 0; j < AP; j++){
//                        result.append(df.format(newPrior[j]) + ", ");
//                    }
//                    result.append("\n");
                        Log.i(TAG, newPrior[0] + "\n");
                        Log.i(TAG, df.format(newPrior[0]) + "\n");

                        // 检查是否可以中止
                        Log.i(TAG, "newprior: 1: " + newPrior[0] + ", 2: " + newPrior[1] + ", 3: " + newPrior[2] + ", 4: " + newPrior[3] + ", 5: " + newPrior[4] + ", 6: " + newPrior[5] + ", 7: " + newPrior[6] + ", 8: " + newPrior[7] + " " + "\n");

                        checkTernimation(piror, newPrior);
                        // 更新先验
                        piror = updateArray(piror, newPrior);
                        index = 0;
                        Log.i(TAG, "newpr");
                        wifiInfo = wifiManager.getConnectionInfo();
                        mSensorMgr.unregisterListener(this,mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
                        mSensorMgr.registerListener(this,
                                mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                                SensorManager.SENSOR_DELAY_NORMAL);
                    }


                }
                mSensorMgr.unregisterListener(this);
                return;
            }

        }
    }

    // 将test中一次扫描的结果按照RSSI从大到小排列
    public oneAP[] sort(oneAP[] testScanAP)
    {
        oneAP temp = new oneAP();
        boolean swap;
        for (int i = AP-1; i > 0; i--)
        {
            swap=false;
            for (int j = 0; j < i; j++)
            {
                if(-testScanAP[j].RSSIValue > -testScanAP[j+1].RSSIValue)
                {
                    temp = testScanAP[j];
                    testScanAP[j] = testScanAP[j+1];
                    testScanAP[j+1] = temp;
                    swap = true;
                }
            }
            if (swap == false){
                break;
            }
        }
        return testScanAP;
    }
    // 将归一化的所有的后验概率进行比较，找到最大的概率,并且更新为新的先验
    public double[] updatePrior (oneBF[] BF)
    {
        double[][] content = new double[3][CellNO];
        int num = 0;
        // 存储更新过的后验
        for (int i = 0; i < AP; i++){
            if (BF[i].count == test_count){
                if(num < 3){
                    content[num] = updateArray(content[num], BF[i].post);
//                    Log.i(TAG,content[num][0] + " " + content[num][1] + " " + content[num][2] + " " + content[num][3] + " " + content[num][4] + " " + content[num][5] + " " + content[num][6] + " " + content[num][7] + " " +"\n");

//                    content[num] = BF[i].post;
                    num ++;
                }
            }
        }
        Log.i(TAG,"之前：" + content[2][0] + " " + content[2][1] + " " + content[2][2] + " " + content[2][3] + " " + content[2][4] + " " + content[2][5] + " " + content[2][6] + " " + content[2][7] + " " +"\n");
        double[] weight = new double[3];
        double summation = 0.0;
        for (int i = 0; i < 3; i++){
            weight[i] = 1.0*(testScanAP[i].RSSIValue + 100);
            summation += weight[i];
        }
        for (int i = 0; i < 3; i++){
            weight[i] /= summation;
        }
        // 求和
        Log.i(TAG, "权重" + weight[0] + " " + weight[1] + " " + weight[2] + "\n");
        for (int j = 0; j < CellNO; j++){
            for (int i = 0; i < 2; i++){
                content[2][j] += weight[i]*content[i][j];
            }
        }
        Log.i(TAG,"更新后" + content[2][0] + " " + content[2][1] + " " + content[2][2] + " " + content[2][3] + " " + content[2][4] + " " + content[2][5] + " " + content[2][6] + " " + content[2][7] + " " +"\n");

        newPrior = updateArray(newPrior,content[2]);

        newPrior = normalization(content[2]);

        return newPrior;
    }
    public double[] normalization(double[] array)
    {
        double summation = 0.0;
        for(int i = 0; i < array.length; i++)
        {
            summation += array[i];
        }
        if (summation == 0) {
            summation = 1;
        }
        for(int i = 0; i < array.length; i++)
        {
            array[i] /= summation;
        }
        return array;
    }
    public void  BayesFilter(int APindex, int RSSI, oneBF BF,double[] piror)
    {
        FileReader reader;
        BufferedReader inReader;
        File[] files = MapRootFile.listFiles();
        String str;
        for (File file : files) {
            int APcurrent = Integer.parseInt(file.getName().split("_")[1]);

            Log.i(TAG,"APcurrent " + APcurrent + "\n");
            Log.i(TAG,"APindex " + APindex + "\n");
            int CellCurrent = 0; //int count = 0;
            if (file.exists() && (APcurrent == APindex) )
                try {
                    reader = new FileReader(file);    // open the file
                    inReader = new BufferedReader(reader);
                    while ((str = inReader.readLine()) != null) {
//                        if (count == 0) {
//                            count ++;
//                            continue;
//                        }
//                        else {

                        String[] oneline = str.split(",");

                        Log.i(TAG,"oneline " + oneline[1] + "\n");
                        Log.i(TAG,"RSSI:  " + RSSI + "\n");
                        Log.i(TAG,"RSSI:  " + oneline[1-RSSI] + "\n");

                        CellCurrent = Integer.parseInt(oneline[0]);
//                                BF.post[CellCurrent - 1] = piror[CellCurrent - 1] * Double.parseDouble(oneline[35]);
                        if (Double.parseDouble(oneline[1]) !=0) {
                            BF.post[CellCurrent - 1] = piror[CellCurrent - 1] * Double.parseDouble(oneline[1 - RSSI]);
                            BF.post[CellCurrent - 1] /= Double.parseDouble(oneline[1]);
                        }
                        else {
                            BF.post[CellCurrent - 1] = 0;

                        }
                        Log.i(TAG,"Post:  " + BF.post[CellCurrent - 1] + "\n");

                            // 如有需要可以反注释
//                                result.append("post: " + BF.post[count-1] + "\n");
//                                result.append("prior: " + piror[count-1] + "\n");
//                            count ++;
                        }
//                    }
                } catch (IOException e) {
                    Log.i(TAG, e.toString());
                }
        }

    }



    public void checkTernimation(double[] arrayold, double[] arraynew){
        double error = 0.0;
        for (int i = 0; i < CellNO; i++){
            error += Math.pow((arrayold[i] - arraynew[i]), 2);
            Log.i(TAG, "error: " + error + "\n");
        }

        double max = 0.0;
        int number = 0;
        for (int i = 0; i < arraynew.length; i++){
            if (max <= arraynew[i]){
                max = arraynew[i];
                number = i;
            }
        }
        for (int j = 0; j < AP; j++){
            testScanAP[j].RSSIValue = 0;
            testScanAP[j].number = 0;
            testScanAP[j].MacAdd = Wifi_list[j];
            testScanAP[j].APindex = j+1;

        }
        if (test_count >= 50 || error <= 1E-4){
            if(max == 0){
                result.append("unable to locate" + "\n");
            }
            else{
                if (max >= 0.85){
                    result.append("maximum scan reached" + "\n");
                }
                else if(test_count >= 50){
                    result.append("The probability of one cell is high enough" + "\n");
                }
            }
//
            TERMINATED = true;
            // 初始化prior

            for (int i = 0; i < CellNO; i++)
            {
                piror[i] = 1.0/CellNO;
                newPrior[i] = Double.parseDouble(df.format(newPrior[i]));
//            Log.i(TAG,Double.toString(piror[i]) + "\n");
            }
            Log.i(TAG, "ended, newprior: 1: " + newPrior[0] + ", 2: " + newPrior[1] + ", 3: " + newPrior[2] + ", 4: " + newPrior[3] + ", 5: " + newPrior[4] + ", 6: " + newPrior[5] + ", 7: " + newPrior[6] + ", 8: " + newPrior[7] + " " + "\n");
            result.append("newprior: 1: " + newPrior[0] + ", 2: " + newPrior[1] + ", 3: " + newPrior[2] + ", 4: " + newPrior[3] + ", 5: " + newPrior[4] + ", 6: " + newPrior[5] + ", 7: " + newPrior[6] + ", 8: " + newPrior[7] + " " + "\n");
            result.append("cell: " + (number+1) + ", P = " +  max + "\n");
            Log.i(TAG, "------------------one test ended-----------------");
            mSensorMgr.unregisterListener(this);

        }


    }
    public double[] updateArray(double[] newArray, double[] oldArray)
    {
        if (newArray.length != oldArray.length){
            return newArray;
        }
        else {
            for (int i = 0; i < oldArray.length; i++){
                newArray[i] = oldArray[i];
            }
        }
        return newArray;
    }

    //添加数据到文件夹中

    public void addToFile(String raw_data, File RootFile, String filename, Boolean flag){
        if (!RootFile.exists()) {
            if(!RootFile.mkdirs()) {
                Log.i(TAG, RootFile.toString());
            }
        }
//        filename = cell + "_" + movement + "_" + "CollectedData.txt";
        File myFile = new File(RootFile, filename);
        try {
            myFile.createNewFile();
            Log.i(TAG,myFile + " added");
            FileWriter fw = new FileWriter(myFile,flag);
            fw.write( raw_data + "\n");
            fw.close();
        }
        catch (IOException ioe) {
            Log.i(TAG,"ERROR ADDING");
            Log.i(TAG,ioe.toString());
        }
    }

    // --------------------------- Map operation -------------------------------------------------------
    public void map_reset(File root)
    {
        File files[] = root.listFiles();
        Log.i(TAG,"deleting start");
        if (files != null)
            for (File f : files) {
                if (f.isDirectory()) {
                    map_reset(f);
                    try {
                        f.delete();
                        Log.i(TAG, "Files deleted");
                    } catch (Exception e) {
                        Log.i(TAG, "ERROR deleting");
                        Log.i(TAG, e.toString());
                    }
                } else {
                    if (f.exists()) {
                        map_reset(f);
                        try {
                            f.delete();
                            Log.i(TAG, "Files deleted");
                        } catch (Exception e) {
                            Log.i(TAG, "ERROR deleting");
                            Log.i(TAG, e.toString());
                        }
                    }
                }
            }
        Log.i(TAG,"deleting completed");
    }

    public void map_create()
    {
        String filename;
        for (int i = 0; i < AP; i++)
        {
            filename = "AP" + "_" + Integer.toString(i+1) + "_" + "Map.txt";
            addToFile("cellNo, number of points, rssi = -1, rssi = -2,.....", MapRootFile, filename, false);
            for (int j = 0; j < CellNO; j++)
            {
                String[] content = new String[258];
                content[0] = Integer.toString(j+1);
                for (int k = 0; k < 257; k++) {
                    content[k+1] = "0";
                }
                String str1 = String.join(", ", content);

                addToFile(str1, MapRootFile, filename, true);
            }
        }
    }

    public void map_updated()
    {
        FileReader reader;
        BufferedReader inReader;
        File[] MapDataFiles = MapRootFile.listFiles();
        String str;
        int current_AP = 0;
        for (File file : MapDataFiles)
        {
            if (file.exists())
                Log.i(TAG,"file exists");
            try {
                reader = new FileReader(file);    // open the file
                inReader = new BufferedReader(reader);
                String[] map_modified = new String[CellNO];
                int i = 0;
                while ((str = inReader.readLine()) != null) {
                    if(i == 0)
                    {
                        i++;
                        continue;
                    }
                    else {
                        String[] s = str.split(", ");
                        s = update_onerecord(s, current_AP);
                        String str1 = String.join(", ", s);
                        //对于map的每一行，比如该AP下的第一行，就是指cell1的情况，通过上述函数修改，存储这一string，每个AP的map都有CellNO个string
                        map_modified[i - 1] = str1;
                        Log.i(TAG, "modified");
                    }
//                    String[] s = str.split(", ");
//                    s = update_onerecord(s, current_AP);
//                    String str1 = String.join(", ", s);
//                    //对于map的每一行，比如该AP下的第一行，就是指cell1的情况，通过上述函数修改，存储这一string，每个AP的map都有CellNO个string
//                    map_modified[i] = str1;
//                    Log.i(TAG, "modified");
                    i++;
                }
                addToFile("cellNo, number of points, rssi=-1, rssi=-2,.....", MapRootFile, file.getName().toString(), false);
                for (int j = 0; j < CellNO; j++)
                {
                    addToFile(map_modified[j], MapRootFile, file.getName().toString(), true);
                }
                current_AP++;
            }
            catch (IOException e) {
                Log.i(TAG, e.toString());
            }

        }
    }

    public String[] update_onerecord(String[] s, int current_AP)
    {
        //        s[0]是cell number, s[1]是测试的点个数
        FileReader reader;
        BufferedReader inReader;
        File[] files = RawRootFile.listFiles();
        String str;
        int cell_map = Integer.parseInt(s[0]);
        for (File file : files) {
            if (file.exists())
                try {
                    int cell_raw = Integer.parseInt(file.getName().split("_")[1]);
                    Log.i(TAG,file.toString() + " file exists " + cell_raw + ", " + cell_map);
                    //如果修改的map的cell不是当前读取的文件的cell则跳过
                    if(cell_raw != cell_map)
                    {
                        continue;
                    }
                    else
                    {
                        reader = new FileReader(file);    // open the file
                        inReader = new BufferedReader(reader);
                        while ((str = inReader.readLine()) != null) {
                            String[] onescan = str.split(", ");

                            int current_RSSI = Integer.parseInt(onescan[2*current_AP+3]);
//                        Log.i(TAG, " current_RSSI " + current_AP);
                            Log.i(TAG, " current_RSSI " + current_RSSI);
//                        Log.i(TAG, " current_RSSI " + onescan[0]);
                            if (current_RSSI != -300)
                            {
                                Log.i(TAG, " current " + s[1]);

                                s[1] = Integer.toString((Integer.parseInt(s[1]) + 1));
                                s[1-current_RSSI] = Integer.toString((Integer.parseInt(s[1-current_RSSI]) + 1));
                            }
                        }
                    }
                } catch (IOException e) {
                    Log.i(TAG, e.toString());
                }
        }
        return s;
    }

    public void onClick(View v)
    {
        // 开始收集
        if(v.getId()==R.id.bt_start)
        {
            states = true;
            wifiInfo = wifiManager.getConnectionInfo();
            mSensorMgr.unregisterListener(this,mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
            mSensorMgr.registerListener(this,
                    mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL);
            return;
        }
        // 停止
        if(v.getId()==R.id.bt_stop)
        {
            mSensorMgr.unregisterListener(this);

            wifi_info.append(Integer.toString(index) + " data stored for " + cellInfo.getText().toString() + ", " + experimentInfo.getText().toString());
            index = 0;
            return;
        }
        // TODO:
        if(v.getId()==R.id.bt_test_reset)
        {
            mSensorMgr.unregisterListener(this);
            for (int i = 0; i < AP; i++){
                BF[i].count = 0;
            }
            test_count = 0;
            TERMINATED = false;
            result.setText("");
            result.append("new round, Ps: if the app terminated before you press Where am I, please press Reset test" + "\n");
            // TODO: 重置所有的概率
            for (int i = 0; i < CellNO; i++)
            {
                piror[i] = 1.0/CellNO;
            }
        }
        //
        if(v.getId()==R.id.bt_map_update)
        {
            map_updated();
        }
        //
        if(v.getId()==R.id.bt_map_create)
        {
            map_reset(MapRootFile);
            map_create();
        }
        if(v.getId() == R.id.bt_testing)
        {
            if (!TERMINATED) {
                states = false;
                for (int i = 0; i < AP; i++) {
                    testScanAP[i].RSSIValue = 0;
                    testScanAP[i].number = 0;
                }

                // get the current wifi RSSI
                wifiInfo = wifiManager.getConnectionInfo();
                mSensorMgr.unregisterListener(this, mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
                mSensorMgr.registerListener(this,
                        mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                        SensorManager.SENSOR_DELAY_NORMAL);
            }
            else {
                result.append("Scan had been terminated, please reset" + "\n");
            }
            return;
        }
    }
}
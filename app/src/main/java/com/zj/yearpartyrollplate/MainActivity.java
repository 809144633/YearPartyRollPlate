package com.zj.yearpartyrollplate;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements YearPartyRollPlate.PrizeListener {
    private YearPartyRollPlate rollPlate;
    private List<String> prizeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rollPlate = findViewById(R.id.rolling);
        prizeList = new ArrayList<>();
        prizeList.add("MacBook Pro 2020");
        prizeList.add("iPhoneXs Max");
        prizeList.add("上海外滩别墅");
        prizeList.add("玛莎拉蒂");
        prizeList.add("刮刮卡一张");
        prizeList.add("发红包");
        rollPlate.setPrizeList(prizeList);
        rollPlate.setPrizeListener(this);
    }

    @Override
    public void getPrize(String str) {
        Toast.makeText(this, "恭喜获得奖品:" + str, Toast.LENGTH_LONG).show();
    }
}

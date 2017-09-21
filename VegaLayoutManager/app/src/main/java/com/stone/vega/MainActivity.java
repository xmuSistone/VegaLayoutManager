package com.stone.vega;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.stone.vega.library.VegaLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<StockEntity> dataList = new ArrayList<>();
    private int redColor, greenColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. 沉浸式状态栏 + dark模式
        View positionView = findViewById(R.id.main_position_view);
        boolean immerse = Utils.immerseStatusBar(this);
        boolean darkMode = Utils.setDarkMode(this);
        if (immerse) {
            ViewGroup.LayoutParams lp = positionView.getLayoutParams();
            lp.height = Utils.getStatusBarHeight(this);
            positionView.setLayoutParams(lp);
            if (!darkMode) {
                positionView.setBackgroundColor(Color.BLACK);
            }
        } else {
            positionView.setVisibility(View.GONE);
        }

        // 2. toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // 3. recyclerView数据填充
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
        recyclerView.setLayoutManager(new VegaLayoutManager());
        RecyclerView.Adapter adapter = getAdapter();
        recyclerView.setAdapter(adapter);

        redColor = getResources().getColor(R.color.red);
        greenColor = getResources().getColor(R.color.green);

        prepareDataList();
        adapter.notifyDataSetChanged();
    }

    private void prepareDataList() {
        for (int i = 0; i < 5; i++) {
            dataList.add(new StockEntity("Google Inc.", 921.59f, 1, "+6.59 (+0.72%)"));
            dataList.add(new StockEntity("Apple Inc.", 158.73f, 1, "+0.06 (+0.04%)"));
            dataList.add(new StockEntity("Vmware Inc.", 109.74f, -1, "-0.24 (-0.22%)"));
            dataList.add(new StockEntity("Microsoft Inc.", 75.44f, 1, "+0.28 (+0.37%)"));
            dataList.add(new StockEntity("Facebook Inc.", 172.52f, 1, "+2.51 (+1.48%)"));
            dataList.add(new StockEntity("IBM Inc.", 144.40f, -1, "-0.15 (-0.10%)"));
            dataList.add(new StockEntity("Alibaba Inc.", 180.04f, 1, "+0.06 (+0.03%)"));
            dataList.add(new StockEntity("Tencent Inc.", 346.400f, 1, "+2.200 (+0.64%)"));
            dataList.add(new StockEntity("Baidu Inc.", 237.92f, -1, "-1.15 (-0.48%)"));
            dataList.add(new StockEntity("Amazon Inc.", 969.47f, -1, "-4.72 (-0.48%)"));
            dataList.add(new StockEntity("Oracle Inc.", 48.03f, -1, "-0.30 (-0.62%)"));
            dataList.add(new StockEntity("Intel Inc.", 37.22f, 1, "+0.22 (+0.61%)"));
            dataList.add(new StockEntity("Cisco Systems Inc.", 32.49f, -1, "-0.03 (-0.08%)"));
            dataList.add(new StockEntity("Qualcomm Inc.", 52.30f, 1, "+0.05 (+0.10%)"));
            dataList.add(new StockEntity("Sony Inc.", 37.65f, -1, "-0.74 (-1.93%)"));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:// 点击返回图标事件
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private RecyclerView.Adapter getAdapter() {
        final LayoutInflater inflater = LayoutInflater.from(this);
        RecyclerView.Adapter adapter = new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = inflater.inflate(R.layout.recycler_item, parent, false);
                return new MyViewHolder(view);
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                MyViewHolder myHolder = (MyViewHolder) holder;
                myHolder.bindData(dataList.get(position));
            }

            @Override
            public int getItemCount() {
                return dataList.size();
            }
        };
        return adapter;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView nameTv;
        TextView currentPriceTv;
        ImageView trendFlagIv;
        TextView grossTv;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.nameTv = (TextView) itemView.findViewById(R.id.item_name_tv);
            this.currentPriceTv = (TextView) itemView.findViewById(R.id.item_current_price);
            this.trendFlagIv = (ImageView) itemView.findViewById(R.id.item_trend_flag);
            this.grossTv = (TextView) itemView.findViewById(R.id.item_gross);
        }

        public void bindData(StockEntity stockEntity) {
            nameTv.setText(stockEntity.getName());
            currentPriceTv.setText("$" + stockEntity.getPrice());
            trendFlagIv.setImageResource(stockEntity.getFlag() > 0 ? R.drawable.up_red : R.drawable.down_green);
            grossTv.setText(stockEntity.getGross());
            grossTv.setTextColor(stockEntity.getFlag() > 0 ? redColor : greenColor);
        }
    }
}

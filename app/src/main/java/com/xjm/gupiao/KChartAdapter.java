package com.xjm.gupiao;

import com.github.tifezh.kchartlib.chart.BaseKChartAdapter;
import java.util.Date;
import java.util.List;

public class KChartAdapter extends BaseKChartAdapter {

    private List<KLineEntity> datas;
    public KChartAdapter(List<KLineEntity> datas){
        this.datas = datas;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public Date getDate(int position) {
        try {
            String s = datas.get(position).Date;
            String[] split = s.split("-");
            Date date = new Date();
            date.setYear(Integer.parseInt(split[0]) - 1900);
            date.setMonth(Integer.parseInt(split[1]) - 1);
            date.setDate(Integer.parseInt(split[2]));
            return date;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

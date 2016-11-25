package com.gu.swiperefreshplush;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recycleContent;
    private SimpleRecycleAdapter recycleAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recycleContent= (RecyclerView) findViewById(R.id.content);
        iniView();
    }
    private void iniView(){
        recycleContent.setLayoutManager(new LinearLayoutManager(this));
        recycleAdapter=new SimpleRecycleAdapter();
        recycleAdapter.setData(generatorData());
        recycleContent.setAdapter(recycleAdapter);

    }
    private List generatorData(){
        List<String> resul=new ArrayList<>();
        for(int i=0;i<30;i++){
            resul.add("ewetwes");
        }
        return resul;
    }
}

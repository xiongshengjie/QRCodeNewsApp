package cn.xcloude.qrcodenewsapp.activity;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.xcloude.qrcodenewsapp.R;

public class ImproveInformationActivity extends AppCompatActivity {

    private String mobile;

    @BindView(R.id.improve_toolbar)
    Toolbar improveToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_improve_information);
        ButterKnife.bind(this);
        Intent intent  = getIntent();
        mobile = intent.getStringExtra("userMobile");
        initViews();
    }

    private void initViews(){
        setSupportActionBar(improveToolbar);
        improveToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImproveInformationActivity.this.finish();
            }
        });
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

    }
}

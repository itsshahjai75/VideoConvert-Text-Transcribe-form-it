package videoeditor.jayshah.com.ffmpegvideoeditor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class NLApiData extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nlapi_data);

        TextView tvNLresult = (TextView)this.findViewById(R.id.tvNLresult);
        String getData = getIntent().getExtras().get("data").toString();
        tvNLresult.setText(getData);
    }
}

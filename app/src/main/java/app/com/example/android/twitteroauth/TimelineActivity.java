package app.com.example.android.twitteroauth;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListViewCompat;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

import twitter4j.JSONObject;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

public class TimelineActivity extends AppCompatActivity {
 private static final String LOG_TAG = TimelineActivity.class.getSimpleName();
    String timelineList;
    String[] timelineArray = new String[20];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            timelineArray = b.getStringArray("statuses");
        }
//        Log.v(LOG_TAG, "TIMELINE: " + timelineList);
//        JsonParser parser = new JsonParser();
//        JsonObject jsonObj = (JsonObject) parser.parse(timelineList);
//        Log.v(LOG_TAG, "JSON OBJECT: " + jsonObj.toString());

        //timelineList = (ArrayList<Status>)getIntent().getExtra("statuses");
        //timelineArray[0] = jsonObj.get("text").toString();
        //Status[] timelineArray = (Status[]) timelineList.toArray();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,  R.layout.activity_listview, R.id.list_item, timelineArray);
        ListView listView = (ListView)findViewById(R.id.timelineList);
        listView.setAdapter(adapter);
    }

    private void loadOwnerAvatar() {
        int avatarSideDimen = 75dp;

    }

}

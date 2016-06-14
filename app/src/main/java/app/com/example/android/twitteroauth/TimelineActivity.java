package app.com.example.android.twitteroauth;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ListViewCompat;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;

public class TimelineActivity extends AppCompatActivity {

    ArrayList<Status> timelineList = new ArrayList<Status>();
    String[] timelineArray = {"a", "b", "c", "d", "e"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        //Status[] timelineArray = (Status[]) timelineList.toArray();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, timelineArray);
        ListView listView = (ListView)findViewById(R.id.timelineList);
        listView.setAdapter(adapter);
    }


}

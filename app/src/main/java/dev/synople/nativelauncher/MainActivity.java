package dev.synople.nativelauncher;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {

    private List<CardBuilder> mCards;
    private CardScrollView mCardScrollView;
    private ExampleCardScrollAdapter mAdapter;
    private List<ApplicationInfo> packages = new ArrayList<ApplicationInfo>();;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createCards();
        mCardScrollView = new CardScrollView(this);
        mAdapter = new ExampleCardScrollAdapter();
        mCardScrollView.setAdapter(mAdapter);
        mCardScrollView.activate();
        setupClickListener();
        setContentView(mCardScrollView);
    }

    private void setupClickListener() {
        mCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            int taps = 0;

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(Sounds.TAP);
                taps++;

                try
                {

                    String appName = (packages
                                .get(mCardScrollView.getSelectedItemPosition())
                                .packageName);
                    Intent intent = new Intent();
                    intent.setPackage(appName);
                    PackageManager pm = getPackageManager();
                    List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
                    Collections.sort(resolveInfos, new ResolveInfo.DisplayNameComparator(pm));

                    if(resolveInfos.size() > 0) {
                        ResolveInfo launchable = resolveInfos.get(0);
                        ActivityInfo activity = launchable.activityInfo;
                        ComponentName name=new ComponentName(activity.applicationInfo.packageName,
                                activity.name);
                        Intent i=new Intent(Intent.ACTION_MAIN);

                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        i.setComponent(name);
                        startActivity(i);
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void createCards() {
        mCards = new ArrayList<CardBuilder>();
        final PackageManager pm = getPackageManager();
        List<ApplicationInfo> temPackages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : temPackages) {
            if (pm.getLaunchIntentForPackage(packageInfo.packageName) != null) {
                if ((packageInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                    // updated system apps
                } else if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                    // system apps
                } else {
                    // user installed apps
                    this.packages.add(packageInfo);
                    mCards.add(new CardBuilder(this, CardBuilder.Layout.MENU)
                            .setText(packageInfo.packageName)
                            .setIcon(pm.getApplicationIcon(packageInfo))
                            .setFootnote("TAP TO START"));
                }
            }
        }
    }

    private class ExampleCardScrollAdapter extends CardScrollAdapter {
        @Override
        public int getPosition(Object item) {
            return mCards.indexOf(item);
        }

        @Override
        public int getCount() {
            return mCards.size();
        }

        @Override
        public Object getItem(int position) {
            return mCards.get(position);
        }

        @Override
        public int getViewTypeCount() {
            return CardBuilder.getViewTypeCount();
        }

        @Override
        public int getItemViewType(int position){
            return mCards.get(position).getItemViewType();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return mCards.get(position).getView(convertView, parent);
        }
    }
}

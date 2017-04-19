package net.orgizm.imgshr;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class Preferences {
    final String PREFERENCES_NAME = "imgshr";
    final String LAST_SLUGS_KEY = "lastSlugs";

    private SharedPreferences preferences;

    public Preferences(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public String[] getLastSlugs() {
        Set<String> set = preferences.getStringSet(LAST_SLUGS_KEY, null);

        if (set == null) {
            return null;
        } else {
            return set.toArray(new String[set.size()]);
        }
    }

    public void setLastSlugs(String slug) {
        SharedPreferences.Editor editor = preferences.edit();
        Set<String> set = preferences.getStringSet(LAST_SLUGS_KEY, null);

        Set<String> setNew;
        if (set == null) {
            setNew = new HashSet<String>();
        } else {
            setNew = new HashSet<String>(set);
        }

        setNew.add(slug);

        editor.putStringSet(LAST_SLUGS_KEY, setNew);
        editor.commit();
    }
}

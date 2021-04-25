package com.example.bos_4;

import android.app.Application;
import androidx.room.Room;

public class App extends Application {
    public static App instance;
    private AppDatabase database;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        database = Room.databaseBuilder(this, AppDatabase.class, "database")
                .allowMainThreadQueries() // В этом случае не будут получаться Exception при работе в UI потоке.
                .build();
    }
    public void update() {
        instance = this;
        database = Room.databaseBuilder(this, AppDatabase.class, "database")
                .allowMainThreadQueries() // В этом случае не будут получаться Exception при работе в UI потоке.
                .build();
    }
    public static App getInstance() {
        return instance;
    }
    public AppDatabase getDatabase() {
        return database;
    }
}
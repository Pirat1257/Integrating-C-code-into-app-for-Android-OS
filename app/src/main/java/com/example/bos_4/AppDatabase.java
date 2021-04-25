package com.example.bos_4;
import androidx.room.Database;
import androidx.room.RoomDatabase;

/*
 Аннотацией Database помечаем основной класс по работе с базой данных.
 Этот класс должен быть абстрактным и наследовать RoomDatabase.
8
 В параметрах аннотации Database указываем, какие Entity будут использоваться,
 и версию базы. Для каждого Entity класса из списка entities будет создана
 таблица.
 В Database классе необходимо описать абстрактные методы для получения Dao
 объектов, которые вам понадобятся.
*/
@Database(entities = {SomeInfo.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract SomeInfoDao someInfoDao();
}

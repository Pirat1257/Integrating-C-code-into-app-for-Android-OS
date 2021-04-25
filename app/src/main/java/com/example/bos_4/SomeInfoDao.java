package com.example.bos_4;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

/*
    В объекте Dao мы будем описывать методы для работы с базой данных.
    Нам нужны функции для получения списка ресурсов и для добавления/изменения/
    удаления информации о них.
    В качестве имени таблицы используется SomeInfo. Имя таблицы равно имени
    Entity класса, причем регистр не важен в именах таблицы.
*/
@Dao
public interface SomeInfoDao {
    @Query("SELECT * FROM SomeInfo")
    List<SomeInfo> getAll();

    @Query("SELECT * FROM SomeInfo WHERE id = :id")
    SomeInfo getByResource(int id);

    @Insert
    void insert(SomeInfo someInfo);

    @Update
    void update(SomeInfo someInfo);

    @Delete
    void delete(SomeInfo someInfo);
}
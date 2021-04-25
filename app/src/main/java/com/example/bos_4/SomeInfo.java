package com.example.bos_4;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/*
    Аннотацией Entity нам необходимо пометить объект, который мы хотим
    хранить в базе данных. Для этого создаем класс SomeInfo, который
    будет представлять собой следующие данные: Ресурс, Логин, Пароль, Заметки.
    М.б. первым элементом хранить определенную инфу для проверки пароля.
    Аннотацией PrimaryKey мы помечаем поле, которое будет ключом в таблице.
*/
@Entity
public class SomeInfo {
    @PrimaryKey
    public int id; // Номер записи
    public String hash; // Хеш пароля
    public String name; // Название зашифрованного файла
    public int size; // Размер файла до шифрования
}

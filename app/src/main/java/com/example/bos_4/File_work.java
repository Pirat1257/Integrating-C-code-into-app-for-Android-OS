package com.example.bos_4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class File_work extends AppCompatActivity {

    private ListView files_in_directory;
    private Button decrypt_button;
    private Button change_pass_button;
    private ArrayList<String> array_list; // Для вывода названий файлов
    private ArrayAdapter adapter; // Для строк
    private String pass_hash;
    static final private int NEW_PASS = 1; // Для ответа о изменении пароля
    private AppDatabase db;
    private SomeInfoDao someInfoDao;
    private List<SomeInfo> si_list; // Лист бд
    private Iterator<SomeInfo> it; // Итератор для работы с листом бд

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_work);
        // Инициализируем базу данных
        db = App.getInstance().getDatabase();
        someInfoDao = db.someInfoDao();
        // Привязываем элементы
        files_in_directory = findViewById(R.id.files_recycler);
        decrypt_button = findViewById(R.id.decrypt_button);
        change_pass_button = findViewById(R.id.change_button);
        array_list = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, array_list);
        files_in_directory.setAdapter(adapter);
        // Проверка существования директории encrypt
        if(!new File(Environment.getExternalStorageDirectory().getAbsoluteFile() +
                "/Download/" + "/encrypt/").exists()) {

            new File(Environment.getExternalStorageDirectory().getAbsoluteFile() +
                    "/Download/" + "/encrypt/").mkdir();
        }
        // Проверка существования директории decrypt
        if(!new File(Environment.getExternalStorageDirectory().getAbsoluteFile() +
                "/Download/" + "/decrypt/").exists()) {

            new File(Environment.getExternalStorageDirectory().getAbsoluteFile() +
                    "/Download/" + "/decrypt/").mkdir();
        }
        // Получаем хеш пароля с экрана ввода пароля
        Bundle arguments = getIntent().getExtras();
        pass_hash = arguments.get("pass_hash").toString();
        // Проводим чистку файлов, которых нет в диреектории, но есть в базе данных
        ///////////////////////////////////////////////////////////////////////////
        // Проведение зашифрования незашифрованных файлов в директории decrypt
        encrypt_directory();
        // Выводим список файлов
        update_list();
        // Обработка нажатия на кнопку DECRYPT
        decrypt_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Обновляем информацию о содержимом папки
                if (files_in_directory != null) {
                    File encrypt_dir = new File(Environment.getExternalStorageDirectory().
                            getAbsoluteFile() + "/Download/" + "/encrypt/");
                    File files_in_encrypt = new File(encrypt_dir.toString());
                    File[] files_names = files_in_encrypt.listFiles();
                    // Смотрим сколько элементов выбрано
                    SparseBooleanArray selected = files_in_directory.getCheckedItemPositions();
                    if (files_names != null) {
                        for (int i = 0; i < files_names.length; i++) {
                            // Нашли выбранный элемент
                            if (selected.get(i)) {
                                // Получаем длину исходного файла
                                int size = get_size_from_db(files_names[i].getName());
                                // Меняем название
                                File new_name = new File(Environment.
                                        getExternalStorageDirectory().getAbsoluteFile() +
                                        "/Download/" + "/decrypt/" + files_names[i].getName().
                                        replace(".bos4", ""));
                                files_names[i].renameTo(new_name);
                                AES(new_name.getAbsolutePath(), 1, size);
                                // Сбрасываем
                                files_in_directory.setItemChecked(i, false);
                            }
                        }
                    }
                    // Обновляем список файлов
                    update_list();
                }
            }
        });
        // Обработка нажатия на клавишу CHANGE PASSWORD
        change_pass_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(File_work.this, Change_pass.class);
                intent.putExtra("pass_hash", pass_hash);
                startActivityForResult(intent, NEW_PASS);
            }
        });
    }

    // Обработка результата от активити
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // В нашем случае кроме изменения пароял ничего нет
        if (requestCode == NEW_PASS) {
            if (resultCode == RESULT_OK) {
                pass_hash = data.getStringExtra("new_hash");
                Toast.makeText(File_work.this, "Password updated", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Производим зашифрование файлов в директории decrypt
    private void encrypt_directory() {
        File encrypt_dir = new File(Environment.getExternalStorageDirectory().
                getAbsoluteFile() + "/Download/" + "/encrypt/");
        File files_in_encrypt = new File(encrypt_dir.toString());
        File[] files_names = files_in_encrypt.listFiles();
        // Если файлы существуют
        if (files_names != null) {
            for(int i = 0; i < files_names.length; i++) {
                // Если в названии нет метки
                if(!files_names[i].getName().contains(".bos4")) {
                    // Заносим информацию с новым названием и длиной исходного файла в базу данных
                    int size = (int) files_names[i].length();
                    add_size_to_db(files_names[i].getName() + ".bos4", size);
                    // Меняем название
                    File new_name = new File(Environment.getExternalStorageDirectory().
                            getAbsoluteFile() + "/Download/" + "/encrypt/" +
                            files_names[i].getName() + ".bos4");
                    files_names[i].renameTo(new_name);
                    // Шифруем
                    AES(new_name.getAbsolutePath(), 0, 0);
                }
                array_list.add(files_names[i].getName());
            }
        }
    }

    // Сохранение длины шифруемого файла
    private void add_size_to_db(String name, int size) {
        SomeInfo si = new SomeInfo();
        si.name = name;
        si.size = size;
        si.id = get_free_id();
        someInfoDao.insert(si);
    }

    // Вернем возможный id для bd
    private int get_free_id() {
        si_list = someInfoDao.getAll();
        it = si_list.iterator();
        SomeInfo si = null;
        int id = 0;
        while (it.hasNext()) {
            si = it.next();
            // Если нашли несовпадение идентификатора
            if (si.id != id) {
                return id;
            }
            id++;
        }
        return id;
    }

    // Достаем длину из базы данных и удаляем ее от туда
    private int get_size_from_db(String name) {
        si_list = someInfoDao.getAll();
        it = si_list.iterator();
        SomeInfo si = null;
        // Проходим по всем элементам списка
        while (it.hasNext()) {
            si = it.next();
            if (si.id != 0) {
                // Нашли название
                if (name.compareTo(si.name) == 0) {
                    int size = si.size;
                    someInfoDao.delete(si);
                    return size;
                }
            }
        }
        return 0;
    }

    // Обновление списка файлов
    private void update_list() {
        // Выводим список зашифрованных файлов
        array_list.clear();
        File encrypt_dir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() +
                "/Download/" + "/encrypt/");
        File files_in_encrypt = new File(encrypt_dir.toString());
        File[] files_names = files_in_encrypt.listFiles();
        if (files_names != null) {
            for (int i = 0; i < files_names.length; i++) {
                array_list.add(files_names[i].getName());
            }
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    private native boolean AES(String _path, int _what, int size);
}
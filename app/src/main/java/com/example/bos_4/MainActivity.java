package com.example.bos_4;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    private AppDatabase db;
    private SomeInfoDao someInfoDao;
    private Button confirm_button;
    private EditText pass_field;
    String hash = "";

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Инициализируем базу данных
        db = App.getInstance().getDatabase();
        someInfoDao = db.someInfoDao();
        SomeInfo si_head = someInfoDao.getByResource(0);
        // Если элемента не существует, значит и базы нет и вообще это первый запуск
        if (si_head == null) {
            // Создаем элемент с нашим хешем
            si_head = new SomeInfo();
            si_head.id = 0;
        }
        // В противном случае получаем хеш пароля
        else {
            hash = si_head.hash;
        }
        // Привязываем элементы
        confirm_button = findViewById(R.id.confirm_button);
        pass_field = findViewById(R.id.password_field);
        // Запрашиваем разрешения
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }
        // Обработка нажатия кнопки CONFIRM
        SomeInfo finalSi_head = si_head;
        confirm_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Проверка пустой ли пароль
                if(pass_field.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Password is empty", Toast.LENGTH_LONG).show();
                    return;
                }
                // Считываем пароль
                String password = pass_field.getText().toString();
                // Генерируем хеш
                String dummy_hash = get_hash(password);
                // Хеш отсутствует, значит первый запуск
                if (hash.isEmpty()) {
                    finalSi_head.hash = dummy_hash;
                    // Добавляем в базу проверочный элемент
                    someInfoDao.insert(finalSi_head);
                    // Вызываем новое активити
                    Intent intent = new Intent(MainActivity.this, File_work.class);
                    intent.putExtra("pass_hash", dummy_hash);
                    startActivity(intent);
                    finish();
                    return;
                }
                // Если пароль не пустой и есть хеш, то сравниваем с хешем
                else {
                    // Проверка хешей
                    if (dummy_hash.equals(hash)) {
                        // Вызываем новое активити
                        Intent intent = new Intent(MainActivity.this, File_work.class);
                        intent.putExtra("pass_hash", dummy_hash);
                        startActivity(intent);
                        finish();
                        return;
                    }
                    else {
                        pass_field.setText("");
                        Toast.makeText(MainActivity.this, "Incorrect password", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    // utility function
    private static String bytesToHexString(byte[] bytes) {
        // http://stackoverflow.com/questions/332079
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    // Вычисление хеша
    private String get_hash(String text) {
        // Генерируем хеш
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            digest.update(text.getBytes());
            return bytesToHexString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

}
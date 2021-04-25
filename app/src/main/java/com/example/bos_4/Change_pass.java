package com.example.bos_4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Change_pass extends AppCompatActivity {

    private EditText old_pass;
    private EditText new_pass;
    private Button save_button;
    private Button cancel_button;
    private AppDatabase db;
    private SomeInfoDao someInfoDao;
    private String pass_hash;
    private Intent answerIntent; // Для ответа

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);
        // Инициализируем базу данных
        db = App.getInstance().getDatabase();
        someInfoDao = db.someInfoDao();
        // Привязываем элемененты
        old_pass = findViewById(R.id.old_pass);
        new_pass = findViewById(R.id.new_pass);
        save_button = findViewById(R.id.save_button);
        cancel_button = findViewById(R.id.cancel_button);
        // Получаем хеш старого пароля
        answerIntent = new Intent();
        Bundle arguments = getIntent().getExtras();
        pass_hash = arguments.get("pass_hash").toString();
        // Действия при нажатии на клавишу SAVE
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (old_pass.getText().toString().isEmpty()) {
                    new_pass.setText("");
                    Toast.makeText(Change_pass.this, "Incorrect old password",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                else if (get_hash(old_pass.getText().toString()).compareTo(pass_hash) != 0) {
                    old_pass.setText("");
                    new_pass.setText("");
                    Toast.makeText(Change_pass.this, "Incorrect old password",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                else if (new_pass.getText().toString().isEmpty()) {
                    old_pass.setText("");
                    Toast.makeText(Change_pass.this, "Incorrect new password",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                else if (new_pass.getText().toString().compareTo(
                        old_pass.getText().toString()) == 0) {
                    old_pass.setText("");
                    new_pass.setText("");
                    Toast.makeText(Change_pass.this, "Incorrect new password",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                // Прошли через все проверки, получаем хеш
                String new_hash = get_hash(new_pass.getText().toString());
                // Обновляем базу данных
                SomeInfo si_head = someInfoDao.getByResource(0);
                si_head.hash = new_hash;
                someInfoDao.update(si_head);
                // Передаем новый хеш
                answerIntent.putExtra("new_hash", new_hash);
                setResult(RESULT_OK, answerIntent);
                finish();
                return;
            }
        });
        // Действия при нажатии на кнопку CANCEL
        cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED, answerIntent);
                finish();
                return;
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
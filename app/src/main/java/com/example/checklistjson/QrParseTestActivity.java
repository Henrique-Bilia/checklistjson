package com.example.checklistjson;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QrParseTestActivity extends AppCompatActivity {

    public static final String EXTRA_QR_RAW = "qr_raw";

    private EditText etQrText;
    private TextView tvResultado;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_parse_test);

        etQrText = findViewById(R.id.etQrText);
        tvResultado = findViewById(R.id.tvResultado);
        Button btnInterpretar = findViewById(R.id.btnInterpretar);
        Button btnExemplo = findViewById(R.id.btnExemplo);

        btnExemplo.setOnClickListener(v -> etQrText.setText(
                "TIPO=MANOMETRO;NUM=MN1234;DATA_CAL=05/03/2026;DESCRICAO=Manômetro 0-500 psi"
        ));

        btnInterpretar.setOnClickListener(v -> interpretarQr());

        // Se veio de um scan por câmera, já preenche e interpreta
        Intent intent = getIntent();
        if (intent != null) {
            String raw = intent.getStringExtra(EXTRA_QR_RAW);
            if (!TextUtils.isEmpty(raw)) {
                etQrText.setText(raw);
                interpretarQr();
            }
        }
    }

    private void interpretarQr() {
        String input = etQrText.getText() != null ? etQrText.getText().toString().trim() : "";
        if (TextUtils.isEmpty(input)) {
            tvResultado.setText("Cole aqui o texto do QR Code para interpretar.");
            return;
        }

        Map<String, String> dados = parseQr(input);

        String tipo = dados.getOrDefault("TIPO", "");
        String num = dados.getOrDefault("NUM", "");
        String dataCal = dados.getOrDefault("DATA_CAL", "");
        String descricao = dados.getOrDefault("DESCRICAO", "");

        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(tipo)) {
            sb.append("TIPO: ").append(tipo).append("\n");
        }
        if (!TextUtils.isEmpty(num)) {
            sb.append("NUM: ").append(num).append("\n");
        }
        if (!TextUtils.isEmpty(dataCal)) {
            sb.append("DATA_CAL: ").append(dataCal).append("\n");
        }
        if (!TextUtils.isEmpty(descricao)) {
            sb.append("DESCRICAO: ").append(descricao).append("\n");
        }

        if (sb.length() == 0) {
            sb.append("Nenhum dado reconhecido no QR Code.");
        }

        tvResultado.setText(sb.toString());
    }

    private Map<String, String> parseQr(String qrText) {
        Map<String, String> map = new HashMap<>();
        if (qrText == null) return map;

        // 1) Formato recomendado: CHAVE=VALOR;CHAVE=VALOR...
        if (qrText.contains("=")) {
            String[] partes = qrText.split(";");
            for (String p : partes) {
                String[] kv = p.split("=", 2);
                if (kv.length == 2) {
                    String chave = kv[0].trim().toUpperCase();
                    String valor = kv[1].trim();
                    if (!chave.isEmpty()) {
                        map.put(chave, valor);
                    }
                }
            }
        }

        // 2) Formatos “livres” (ex.: "Calibrado em: 03/12/2024") – tenta extrair o que conseguir.
        // DATA_CAL
        if (!map.containsKey("DATA_CAL")) {
            Matcher mData = Pattern.compile("(\\d{2}/\\d{2}/\\d{4})").matcher(qrText);
            if (mData.find()) {
                map.put("DATA_CAL", mData.group(1));
            }
        }

        // NUM (ex.: "Nº: MN1234" / "N° MN1234" / "No=MN1234")
        if (!map.containsKey("NUM")) {
            Matcher mNum = Pattern.compile("(?i)\\bN[º°o]?\\s*[:=]?\\s*([A-Za-z0-9._-]+)").matcher(qrText);
            if (mNum.find()) {
                map.put("NUM", mNum.group(1));
            }
        }

        // TIPO (ex.: "TIPO: MANOMETRO" em texto livre)
        if (!map.containsKey("TIPO")) {
            Matcher mTipo = Pattern.compile("(?i)\\bTIPO\\s*[:=]\\s*([A-Za-z0-9_ -]+)").matcher(qrText);
            if (mTipo.find()) {
                map.put("TIPO", mTipo.group(1).trim().toUpperCase());
            }
        }

        // DESCRICAO (ex.: "DESCRICAO: ..." em texto livre)
        if (!map.containsKey("DESCRICAO")) {
            Matcher mDesc = Pattern.compile("(?i)\\bDESCRICAO\\s*[:=]\\s*(.+)$").matcher(qrText);
            if (mDesc.find()) {
                map.put("DESCRICAO", mDesc.group(1).trim());
            }
        }

        return map;
    }
}


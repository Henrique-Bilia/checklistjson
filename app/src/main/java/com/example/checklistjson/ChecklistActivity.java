package com.example.checklistjson;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class ChecklistActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChecklistAdapter adapter;
    private final List<ChecklistItem> itens = new ArrayList<>();
    private String checklistId;
    private EditText etResponsavel;
    private EditText etData;
    private EditText etObservacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);

        TextView tvTitulo = findViewById(R.id.tvChecklistTitulo);
        recyclerView = findViewById(R.id.recyclerViewChecklist);
        etResponsavel = findViewById(R.id.etResponsavel);
        etData = findViewById(R.id.etData);
        etObservacao = findViewById(R.id.etObservacao);
        Button btnSalvarCabecalho = findViewById(R.id.btnSalvarCabecalho);
        Button btnAdicionarItem = findViewById(R.id.btnAdicionarItem);
        Button btnSalvarObservacao = findViewById(R.id.btnSalvarObservacao);
        Button btnExportarPdf = findViewById(R.id.btnExportarPdf);

        checklistId = getIntent().getStringExtra("checklist_id");
        String checklistNome = getIntent().getStringExtra("checklist_nome");

        if (checklistNome != null) {
            tvTitulo.setText(checklistNome);
        }

        // Carrega itens e cabeçalho (responsável/data)
        carregarItensDoChecklist();
        carregarCabecalho();
        carregarObservacao();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChecklistAdapter(itens, checklistId, this, new ChecklistAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position, ChecklistItem item) {
                confirmarRemocaoItem(position, item);
            }
        });
        recyclerView.setAdapter(adapter);

        btnSalvarCabecalho.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarCabecalho();
            }
        });

        btnAdicionarItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogAdicionarItem();
            }
        });

        btnSalvarObservacao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarObservacao();
            }
        });

        btnExportarPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportarChecklistParaPdf();
            }
        });

        etData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDatePicker();
            }
        });
    }

    private void carregarItensDoChecklist() {
        try {
            String json = lerArquivoAssets("checklist.json");
            JSONObject root = new JSONObject(json);
            JSONArray checklistsArray = root.getJSONArray("checklists");

            JSONObject checklistSelecionado = null;

            for (int i = 0; i < checklistsArray.length(); i++) {
                JSONObject obj = checklistsArray.getJSONObject(i);
                if (obj.getString("id").equals(checklistId)) {
                    checklistSelecionado = obj;
                    break;
                }
            }

            if (checklistSelecionado == null) return;

            JSONArray itensArray = checklistSelecionado.getJSONArray("itens");
            SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);

            List<String> idsRemovidos = carregarIdsRemovidos();

            for (int i = 0; i < itensArray.length(); i++) {
                JSONObject itemObj = itensArray.getJSONObject(i);
                String idItem = itemObj.getString("id");
                String titulo = itemObj.getString("titulo");

                if (idsRemovidos.contains(idItem)) {
                    continue;
                }

                String chaveStatus = gerarChaveStatus(checklistId, idItem);
                String status = prefs.getString(chaveStatus, "");

                // Compatibilidade com estado antigo (boolean SIM)
                if (status.isEmpty()) {
                    String chaveAntiga = gerarChavePref(checklistId, idItem);
                    boolean antigoChecked = prefs.getBoolean(chaveAntiga, false);
                    if (antigoChecked) {
                        status = "SIM";
                    }
                }

                itens.add(new ChecklistItem(idItem, titulo, status));
            }

            // Itens customizados adicionados pelo usuário
            List<ChecklistItem> itensCustom = carregarItensCustomizados();
            for (ChecklistItem customItem : itensCustom) {
                String chaveStatus = gerarChaveStatus(checklistId, customItem.getId());
                String status = prefs.getString(chaveStatus, "");
                customItem.setStatus(status);
                itens.add(customItem);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void carregarCabecalho() {
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        String chaveResp = gerarChaveResp(checklistId);
        String chaveData = gerarChaveData(checklistId);

        String responsavel = prefs.getString(chaveResp, "");
        String data = prefs.getString(chaveData, "");

        etResponsavel.setText(responsavel);
        etData.setText(data);
    }

    private void salvarCabecalho() {
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String chaveResp = gerarChaveResp(checklistId);
        String chaveData = gerarChaveData(checklistId);

        String responsavel = etResponsavel.getText().toString();
        String data = etData.getText().toString();

        editor.putString(chaveResp, responsavel);
        editor.putString(chaveData, data);
        editor.apply();
    }

    private void carregarObservacao() {
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        String chaveObs = gerarChaveObs(checklistId);
        String obs = prefs.getString(chaveObs, "");
        etObservacao.setText(obs);
    }

    private void salvarObservacao() {
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String chaveObs = gerarChaveObs(checklistId);
        String obs = etObservacao.getText().toString();
        editor.putString(chaveObs, obs);
        editor.apply();
    }

    private void mostrarDatePicker() {
        final Calendar calendario = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String dia = String.format("%02d", dayOfMonth);
                        String mes = String.format("%02d", month + 1);
                        String dataFormatada = dia + "/" + mes + "/" + year;
                        etData.setText(dataFormatada);
                    }
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void mostrarDialogAdicionarItem() {
        final EditText input = new EditText(this);
        input.setHint("Descrição do novo item");

        new AlertDialog.Builder(this)
                .setTitle("Adicionar item")
                .setView(input)
                .setPositiveButton("Adicionar", (dialog, which) -> {
                    String titulo = input.getText().toString().trim();
                    if (!titulo.isEmpty()) {
                        String novoId = "user_" + System.currentTimeMillis();
                        ChecklistItem novoItem = new ChecklistItem(novoId, titulo, "");
                        itens.add(novoItem);
                        salvarItensCustomizados();
                        adapter.notifyItemInserted(itens.size() - 1);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarRemocaoItem(int position, ChecklistItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Remover item")
                .setMessage("Deseja remover o item selecionado?")
                .setPositiveButton("Remover", (dialog, which) -> {
                    marcarItemComoRemovidoOuCustom(position, item);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void marcarItemComoRemovidoOuCustom(int position, ChecklistItem item) {
        if (item.getId().startsWith("user_")) {
            removerItemCustom(item.getId());
        } else {
            List<String> idsRemovidos = carregarIdsRemovidos();
            if (!idsRemovidos.contains(item.getId())) {
                idsRemovidos.add(item.getId());
                salvarIdsRemovidos(idsRemovidos);
            }
        }

        // Opcional: limpar estado salvo do item removido
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String chaveStatus = gerarChaveStatus(checklistId, item.getId());
        editor.remove(chaveStatus);
        editor.apply();

        itens.remove(position);
        adapter.notifyItemRemoved(position);
    }

    private List<String> carregarIdsRemovidos() {
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        String chave = "checklist_" + checklistId + "_removed_ids";
        String valor = prefs.getString(chave, "");
        List<String> ids = new ArrayList<>();
        if (!valor.isEmpty()) {
            ids.addAll(Arrays.asList(valor.split(",")));
        }
        return ids;
    }

    private void salvarIdsRemovidos(List<String> ids) {
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String chave = "checklist_" + checklistId + "_removed_ids";
        String valor = String.join(",", ids);
        editor.putString(chave, valor);
        editor.apply();
    }

    private List<ChecklistItem> carregarItensCustomizados() {
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        String chave = "checklist_" + checklistId + "_custom_items";
        String valor = prefs.getString(chave, "");
        List<ChecklistItem> lista = new ArrayList<>();
        if (!valor.isEmpty()) {
            String[] partes = valor.split(";;");
            for (String parte : partes) {
                String[] campos = parte.split("\\|", 2);
                if (campos.length == 2) {
                    String id = campos[0];
                    String titulo = campos[1];
                    lista.add(new ChecklistItem(id, titulo, ""));
                }
            }
        }
        return lista;
    }

    private void salvarItensCustomizados() {
        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String chave = "checklist_" + checklistId + "_custom_items";

        StringBuilder sb = new StringBuilder();
        for (ChecklistItem item : itens) {
            if (item.getId().startsWith("user_")) {
                if (!sb.toString().isEmpty()) {
                    sb.append(";;");
                }
                sb.append(item.getId())
                        .append("|")
                        .append(item.getTitulo());
            }
        }

        editor.putString(chave, sb.toString());
        editor.apply();
    }

    private void removerItemCustom(String itemId) {
        List<ChecklistItem> custom = carregarItensCustomizados();
        List<ChecklistItem> atualizada = new ArrayList<>();
        for (ChecklistItem c : custom) {
            if (!c.getId().equals(itemId)) {
                atualizada.add(c);
            }
        }

        SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String chave = "checklist_" + checklistId + "_custom_items";

        StringBuilder sb = new StringBuilder();
        for (ChecklistItem item : atualizada) {
            if (!sb.toString().isEmpty()) {
                sb.append(";;");
            }
            sb.append(item.getId())
                    .append("|")
                    .append(item.getTitulo());
        }

        editor.putString(chave, sb.toString());
        editor.apply();
    }

    private String getHeaderKeyForChecklist(String checklistId) {
        if (checklistId.startsWith("checklist_irbr_")) {
            return "irbr";
        } else if (checklistId.startsWith("checklist_ucabr_")) {
            return "ucabr";
        } else if (checklistId.startsWith("checklist_edbrse_")) {
            return "edbrse";
        } else if (checklistId.startsWith("checklist_esbrag_")) {
            return "esbrag";
        } else if ("checklist_manutencao".equals(checklistId)) {
            return "manutencao";
        }
        return "generico";
    }

    private void exportarChecklistParaPdf() {
        try {
            SharedPreferences prefs = getSharedPreferences("checklists_prefs", MODE_PRIVATE);

            String headerKey = getHeaderKeyForChecklist(checklistId);

            String clienteObra = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKey, "cliente_obra"), "");
            String modelo = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKey, "modelo"), "");
            String sn = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKey, "sn"), "");
            String fluido = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKey, "fluido"), "");
            String tensao = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKey, "tensao"), "");
            String op = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKey, "op"), "");
            String tag = prefs.getString(ChecklistHeaderActivity.gerarChaveEquip(headerKey, "tag"), "");

            String responsavel = etResponsavel.getText().toString();
            String data = etData.getText().toString();
            String obsGeral = etObservacao.getText().toString();

            PdfDocument document = new PdfDocument();

            // Página A4 em pontos (aprox 595 x 842)
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();

            Paint titlePaint = new Paint();
            titlePaint.setTextSize(16);
            titlePaint.setFakeBoldText(true);

            Paint textPaint = new Paint();
            textPaint.setTextSize(10);

            int x = 40;
            int y = 50;
            int lineHeight = 14;

            canvas.drawText("Checklist: " + (getIntent().getStringExtra("checklist_nome")), x, y, titlePaint);
            y += lineHeight * 2;

            canvas.drawText("Cliente/Obra: " + clienteObra, x, y, textPaint); y += lineHeight;
            canvas.drawText("Modelo: " + modelo, x, y, textPaint); y += lineHeight;
            canvas.drawText("S/N: " + sn, x, y, textPaint); y += lineHeight;
            canvas.drawText("Fluído: " + fluido, x, y, textPaint); y += lineHeight;
            canvas.drawText("Tensão: " + tensao, x, y, textPaint); y += lineHeight;
            canvas.drawText("OP: " + op, x, y, textPaint); y += lineHeight;
            canvas.drawText("TAG: " + tag, x, y, textPaint); y += lineHeight * 2;

            canvas.drawText("Responsável: " + responsavel, x, y, textPaint); y += lineHeight;
            canvas.drawText("Data: " + data, x, y, textPaint); y += lineHeight * 2;

            canvas.drawText("Itens:", x, y, titlePaint); y += lineHeight;

            for (ChecklistItem item : itens) {
                String prefixo;
                if (item.isSim()) {
                    prefixo = "[SIM] ";
                } else if (item.isNa()) {
                    prefixo = "[N/A] ";
                } else {
                    prefixo = "[   ] ";
                }

                String linha = prefixo + item.getTitulo();

                // Quebra de linha simples se for muito grande
                if (linha.length() > 90) {
                    String parte1 = linha.substring(0, 90);
                    String parte2 = linha.substring(90);
                    canvas.drawText(parte1, x, y, textPaint); y += lineHeight;
                    canvas.drawText("    " + parte2, x, y, textPaint); y += lineHeight;
                } else {
                    canvas.drawText(linha, x, y, textPaint); y += lineHeight;
                }

                if (y > 800) {
                    document.finishPage(page);
                    pageInfo = new PdfDocument.PageInfo.Builder(595, 842, document.getPages().size() + 1).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = 50;
                }
            }

            y += lineHeight;
            canvas.drawText("Observação geral:", x, y, titlePaint); y += lineHeight;

            if (!obsGeral.isEmpty()) {
                String[] linhasObs = obsGeral.split("\n");
                for (String l : linhasObs) {
                    canvas.drawText(l, x, y, textPaint);
                    y += lineHeight;
                }
            }

            document.finishPage(page);

            File dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (dir == null) {
                dir = getFilesDir();
            }

            String fileName = "checklist_" + checklistId + "_" + System.currentTimeMillis() + ".pdf";
            File file = new File(dir, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();

            Toast.makeText(this, "PDF salvo em: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

            // Opcional: abrir diálogo de compartilhamento
            try {
                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/pdf");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Compartilhar checklist em PDF"));
            } catch (Exception e) {
                // Se o FileProvider não estiver configurado, pelo menos o arquivo já foi salvo
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao gerar PDF", Toast.LENGTH_LONG).show();
        }
    }

    private String lerArquivoAssets(String nomeArquivo) throws Exception {
        AssetManager manager = getAssets();
        InputStream is = manager.open(nomeArquivo);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String linha;
        while ((linha = br.readLine()) != null) {
            sb.append(linha);
        }
        br.close();
        return sb.toString();
    }

    public static String gerarChavePref(String checklistId, String itemId) {
        return "checklist_" + checklistId + "_item_" + itemId;
    }

    public static String gerarChaveStatus(String checklistId, String itemId) {
        return "checklist_" + checklistId + "_item_" + itemId + "_status";
    }

    public static String gerarChaveResp(String checklistId) {
        return "checklist_" + checklistId + "_responsavel";
    }

    public static String gerarChaveData(String checklistId) {
        return "checklist_" + checklistId + "_data";
    }

    public static String gerarChaveObs(String checklistId) {
        return "checklist_" + checklistId + "_obs";
    }
}


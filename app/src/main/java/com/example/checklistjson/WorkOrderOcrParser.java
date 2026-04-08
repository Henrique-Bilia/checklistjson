package com.example.checklistjson;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WorkOrderOcrParser {

    private WorkOrderOcrParser() {}

    public static final class Parsed {
        @Nullable public final String op;
        @NonNull public final List<ModelUnits> modelUnits;
        @Nullable public final String clienteObra;
        @Nullable public final String fluido;
        @Nullable public final String tensao;

        Parsed(@Nullable String op,
               @NonNull List<ModelUnits> modelUnits,
               @Nullable String clienteObra,
               @Nullable String fluido,
               @Nullable String tensao) {
            this.op = op;
            this.modelUnits = modelUnits;
            this.clienteObra = clienteObra;
            this.fluido = fluido;
            this.tensao = tensao;
        }
    }

    public static final class ModelUnits {
        @NonNull public final String modelKey;
        public final int units;

        ModelUnits(@NonNull String modelKey, int units) {
            this.modelKey = modelKey;
            this.units = units;
        }
    }

    @NonNull
    public static Parsed parse(@Nullable String ocrText) {

        String text = ocrText == null ? "" : ocrText;

        String op = extractOp(text);
        String cliente = extractClienteObra(text);
        String fluido = extractFluido(text);
        String tensao = extractTensao(text);
        List<ModelUnits> models = extractModelUnits(text);

        return new Parsed(op, models, cliente, fluido, tensao);
    }

    // ===============================
    // EXTRAÇÃO DE OP
    // ===============================

    private static String extractOp(String text) {

        Matcher m = Pattern.compile("(?i)\\bOP\\b[^0-9]{0,8}([0-9OI]{3,8})").matcher(text);

        if (m.find()) {
            return normalizeDigits(m.group(1));
        }

        return null;
    }

    // ===============================
    // EXTRAÇÃO DE MODELOS + UNIDADES
    // ===============================

    private static List<ModelUnits> extractModelUnits(String text) {

        Map<String, Integer> map = new HashMap<>();

        String[] linhas = text.split("\\r?\\n");

        Pattern codigoPattern = Pattern.compile("\\b[A-Z]{4}[A-Z0-9]{6,}\\b");
        Pattern unPattern = Pattern.compile("\\b(\\d{1,2})\\s*UN\\b|\\bUN\\s*(\\d{1,2})\\b", Pattern.CASE_INSENSITIVE);

        for (String linha : linhas) {

            Matcher codigoMatcher = codigoPattern.matcher(linha);

            if (!codigoMatcher.find()) continue;

            String codigo = codigoMatcher.group();

            String prefix = extrairPrefixoModelo(codigo);

            String modelKey = mapPrefixToModelKey(prefix);

            if (modelKey == null) continue;

            Matcher unMatcher = unPattern.matcher(linha);

            int units = 1;

            if (unMatcher.find()) {

                String before = unMatcher.group(1);
                String after = unMatcher.group(2);

                units = safeInt(before != null ? before : after);

                if (units <= 0) units = 1;
            }

            map.put(modelKey, map.getOrDefault(modelKey, 0) + units);
        }

        List<ModelUnits> result = new ArrayList<>();

        for (Map.Entry<String, Integer> e : map.entrySet()) {
            result.add(new ModelUnits(e.getKey(), e.getValue()));
        }

        return result;
    }

    // ===============================
    // FLUIDO
    // ===============================

    private static String extractFluido(String text) {

        Matcher m = Pattern.compile("(?i)\\bR\\s*([0-9]{3})\\s*([A-Z])?\\b").matcher(text);

        if (m.find()) {

            String num = m.group(1);
            String suf = m.group(2);

            return "R" + num + (suf == null ? "" : suf.toUpperCase());
        }

        return null;
    }

    // ===============================
    // TENSÃO
    // ===============================

    private static String extractTensao(String text) {

        Matcher m = Pattern.compile("\\b(110|127|220|380|440|480)\\s*V\\b", Pattern.CASE_INSENSITIVE).matcher(text);

        if (m.find()) {
            return m.group(1) + "V";
        }

        return null;
    }

    // ===============================
    // CLIENTE / OBRA
    // ===============================

    private static String extractClienteObra(String text) {

        Matcher m = Pattern.compile("(?i)CLIENTE\\s*[:\\-]\\s*(.+)").matcher(text);

        if (m.find()) {
            return m.group(1).trim();
        }

        return null;
    }

    // ===============================
    // UTILIDADES
    // ===============================

    private static String normalizeDigits(String s) {

        return s.replace('O', '0')
                .replace('I', '1');
    }

    private static String extrairPrefixoModelo(String codigo) {

        StringBuilder sb = new StringBuilder();

        for (char c : codigo.toCharArray()) {

            if (Character.isLetter(c)) {
                sb.append(c);
            } else {
                break;
            }
        }

        return sb.toString();
    }

    private static String mapPrefixToModelKey(String prefix) {

        prefix = prefix.toUpperCase();

        if (prefix.startsWith("CABR")) return "cabr";
        if (prefix.startsWith("IRBR")) return "irbr";
        if (prefix.startsWith("UCABR") || prefix.startsWith("UCL")) return "ucabr";
        if (prefix.startsWith("EDBR") || prefix.startsWith("EUBR")) return "edbrse";
        if (prefix.startsWith("ESBR")) return "esbrag";
        if (prefix.startsWith("ESLA")) return "esbrla";
        if (prefix.startsWith("WALL") || prefix.startsWith("WUBR") || prefix.startsWith("WDBR")) return "wall";
        if (prefix.startsWith("EDBRAG") || prefix.startsWith("ESBRAG")) return "EDBRAG";
        if (prefix.startsWith("DCBR")) return "dcbr";
        return null;
    }

    private static int safeInt(String s) {

        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
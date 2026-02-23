package com.example.drive_kit.Data;

import android.content.Context;

import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import java.io.File;

public class ManualTextExtractor {

    public interface Callback {
        void onSuccess(String text);
        void onError(Exception e);
    }

    public static void extractTextAsync(Context ctx, File pdfFile, Callback cb) {
        new Thread(() -> {
            try {
                if (cb == null) return;
                if (ctx == null) throw new Exception("Context is null");
                if (pdfFile == null || !pdfFile.exists()) throw new Exception("PDF file missing");

                PDFBoxResourceLoader.init(ctx.getApplicationContext());

                String text;
                try (PDDocument doc = PDDocument.load(pdfFile)) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    text = stripper.getText(doc);
                }

                if (text == null) text = "";
                cb.onSuccess(text);

            } catch (Exception e) {
                if (cb != null) cb.onError(e);
            }
        }).start();
    }
}

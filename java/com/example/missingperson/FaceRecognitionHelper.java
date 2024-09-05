package com.example.missingperson;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class FaceRecognitionHelper {
    private Interpreter interpreter;

    public FaceRecognitionHelper(Context context, String modelPath) throws IOException {
        interpreter = new Interpreter(loadModelFile(context, modelPath));
    }

    private MappedByteBuffer loadModelFile(Context context, String modelPath) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(context.getAssets().openFd(modelPath).getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = context.getAssets().openFd(modelPath).getStartOffset();
        long declaredLength = context.getAssets().openFd(modelPath).getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public float[] recognizeImage(Bitmap bitmap) {
        // Resize the bitmap to the required input size for the model
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 112, 112, false);

        // Prepare the input buffer
        float[][][][] input = new float[1][112][112][3];
        for (int y = 0; y < 112; y++) {
            for (int x = 0; x < 112; x++) {
                int pixel = resizedBitmap.getPixel(x, y);
                input[0][y][x][0] = Color.red(pixel) / 255.0f;
                input[0][y][x][1] = Color.green(pixel) / 255.0f;
                input[0][y][x][2] = Color.blue(pixel) / 255.0f;
            }
        }

        // Prepare the output buffer
        float[][] output = new float[1][192]; // Assuming the model outputs 192-dimensional embeddings

        // Run inference
        interpreter.run(input, output);

        // Return the embeddings
        return output[0];
    }

    public static float cosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }

        float dotProduct = 0.0f;
        float normA = 0.0f;
        float normB = 0.0f;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorB[i];
            normB += vectorB[i] * vectorB[i];
        }
        return dotProduct / (float) (Math.sqrt(normA) * Math.sqrt(normB));
    }
}

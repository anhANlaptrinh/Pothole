package com.example.pothole.network;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;

public class PotholeWebSocketClient extends WebSocketClient {

    public interface WebSocketListener {
        void onNewPothole(double latitude, double longitude, int severity);
    }

    private final WebSocketListener listener;

    public PotholeWebSocketClient(URI serverUri, WebSocketListener listener) {
        super(serverUri);
        this.listener = listener;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.d("WebSocket", "Connected to server");
    }

    @Override
    public void onMessage(String message) {
        try {
            // Parse JSON message
            JSONObject json = new JSONObject(message);
            double latitude = json.getDouble("latitude");
            double longitude = json.getDouble("longitude");
            int severity = json.getInt("severity");

            // Gọi listener để cập nhật map
            listener.onNewPothole(latitude, longitude, severity);
        } catch (JSONException e) {
            Log.e("WebSocket", "Error parsing WebSocket message: " + e.getMessage());
        }
    }


    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.d("WebSocket", "Disconnected from server: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        Log.e("WebSocket", "WebSocket error: " + ex.getMessage());
    }
}

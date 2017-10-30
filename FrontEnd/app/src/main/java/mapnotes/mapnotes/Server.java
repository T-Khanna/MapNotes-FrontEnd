package mapnotes.mapnotes;

import android.content.Context;
import android.net.Uri;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import mapnotes.mapnotes.data_classes.Function;

/**
 *  A server class used to send RESTful requests to the server
 *  Has a copy of the server address stored privately, currently
 *  need to provide the navigation string when using the methods
 */

public class Server {
    private final String IP = "https://mapnotes-backend.herokuapp.com/";
    private Context context;
    private RequestQueue requests;

    public Server (Context context) {
        this.context = context;
        requests = Volley.newRequestQueue(context);

        VolleyLog.DEBUG = true;

        requests.start();
    }

    public void getStringRequest(String serverLocation, final Function<String> onResponse) {
        getStringRequest(serverLocation, null, onResponse);
    }

    public void getStringRequest(String serverLocation, final Map<String, String> params, final Function<String> onResponse) {
        genericStringRequest(Request.Method.GET, serverLocation, params, onResponse);
    }

    private void genericStringRequest(int method, String serverLocation, final Map<String, String> params, final Function<String> onResponse) {
        Uri.Builder builder = Uri.parse(IP + serverLocation).buildUpon();
        if (method == Request.Method.GET && params != null) {
            for (Map.Entry<String, String> element : params.entrySet()) {
                builder.appendQueryParameter(element.getKey(), element.getValue());
            }
        }
        StringRequest stringRequest = new StringRequest(method, builder.build().toString(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        onResponse.run(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams (){
                return params;
            }
        };
        requests.add(stringRequest);

    }

    public void postStringRequest(String serverLocation, final Map<String, String> params, final Function<String> onResponse) {
        genericStringRequest(Request.Method.POST, serverLocation, params, onResponse);
    }

    public void getJSONRequest(String serverLocation, Map<String, String> params, final Function<JSONObject> onResponse) {
        Uri.Builder builder = Uri.parse(IP + serverLocation).buildUpon();
        if (params != null) {
            for (Map.Entry<String, String> element : params.entrySet()) {
                builder.appendQueryParameter(element.getKey(), element.getValue());
            }
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, IP + serverLocation,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                onResponse.run(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        requests.add(request);
    }

    public void postJSONRequest(String serverLocation, JSONObject params, final Map<String, String> headers, final Function<JSONObject> onResponse) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, IP + serverLocation,
                params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                onResponse.run(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }){
        @Override
        public Map<String, String> getHeaders() {
            if (headers != null) return headers;
            return new HashMap<>();
        }};

        requests.add(request);
    }

    public void getJSONArrayRequest(String serverLocation, JSONArray params, final Function<JSONArray> onResponse) {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, IP + serverLocation,
                params, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                onResponse.run(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        requests.add(request);
    }



}

package mapnotes.mapnotes;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

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

    }

    public void getStringRequest(String serverLocation, final Function<String> onResponse) {
        getStringRequest(serverLocation, null, onResponse);
    }

    public void getStringRequest(String serverLocation, final Map<String, String> params, final Function<String> onResponse) {
        genericStringRequest(Request.Method.GET, serverLocation, params, onResponse);
    }

    private void genericStringRequest(int method, String serverLocation, final Map<String, String> params, final Function<String> onResponse) {
        StringRequest stringRequest = new StringRequest(method, IP + serverLocation,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        onResponse.run(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO: Error response
            }
        }) {
            @Override
            public Map<String, String> getParams (){
                return params;
            }
        };
        requests.add(stringRequest);
        requests.start();

    }

    public void postStringRequest(String serverLocation, final Map<String, String> params, final Function<String> onResponse) {
        genericStringRequest(Request.Method.POST, serverLocation, params, onResponse);
    }

    public void getJSONRequest(String serverLocation, final Function<JSONObject> onResponse) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, IP + serverLocation,
                null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            onResponse.run(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO: Error response
            }
        });

        requests.add(request);
        requests.start();
    }





}

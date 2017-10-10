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

public class Server {
    private final String IP = "localhost";
    private Context context;
    private RequestQueue requests;

    public Server (Context context) {
        this.context = context;
        requests = Volley.newRequestQueue(context);

    }

    public void getStringRequest(String url, final Function<String> onResponse) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                onResponse.run(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO: Error response
            }
        });
        requests.add(stringRequest);
    }

    public void getJSONRequest(String url, final Function<JSONObject> onResponse) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
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
    }



}

package example.com.t.twitterstart;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

class TwitterAdapter extends BaseAdapter {

    private ArrayList<String> textos, autores;
    Context context;
    private static LayoutInflater inflater = null;

    public TwitterAdapter (MainActivity main, ArrayList<String> textos, ArrayList<String> autores) {
        this.textos = textos;
        this.autores = autores;
        context = main;
        inflater = (LayoutInflater)context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return textos.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder
    {
        TextView TwitterTexto;
        TextView TwitteAutor;
        TextView TwitterNumb;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Log.v("getView","OK");
        // TODO Auto-generated method stub
        Holder holder = new Holder();
        View rowView;
        //usa os valores passados por parametros e contadores para adicionar a cada linha da listview da maneira desejada
        rowView = inflater.inflate(R.layout.row, null);
        holder.TwitterTexto = (TextView) rowView.findViewById(R.id.texto);
        holder.TwitteAutor = (TextView) rowView.findViewById(R.id.autor);
        holder.TwitterNumb = (TextView) rowView.findViewById(R.id.count);

        holder.TwitterTexto.setText(textos.get(position));
        holder.TwitteAutor.setText(autores.get(position));
        holder.TwitterNumb.setText("Tweet "+ (position+1));
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        return rowView;
    }
}


public class MainActivity extends AppCompatActivity {

    public String ServerURL = "http://10.0.3.2:8888/Twitter/twitterconn.php";
    public ArrayList<String> tTextos = new ArrayList<>();
    public ArrayList<String> tAutores = new ArrayList<>();
    private TwitterAdapter adapter;
    private ListView list;
    public JSONArray twittes,twittes_sort;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(this, "Baixando Twittes...", Toast.LENGTH_LONG).show();

        try {

            //twittes_sort = new JSONArray(new getTwittesAsync().execute());

            //chama AsyncTask para fazer o download e processamento dos twittes
            new getTwittesAsync().execute();

            //inicia a ListView com um adaptador customizado
            list = (ListView) findViewById(R.id.twitter_listview);
            adapter = new TwitterAdapter(this, tTextos, tAutores);
            list.setAdapter(adapter);

        }catch(Exception e){
            e.printStackTrace();
        }
    }


    public class getTwittesAsync extends AsyncTask<String,Void,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... params) {

            Log.v("getDateEvents","OK");
            InputStream input;

            try{
                URL url = new URL(ServerURL);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                //Configura POST Request
                conn.setReadTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setInstanceFollowRedirects(true);
                conn.setUseCaches(false);
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(10000 /* milliseconds */);
                JSONObject obj = new JSONObject();
                obj.put("action","getTwittes");
                Log.d("Request",obj.toString());

                //adiciona mensagem a post request para validacao do servidor
                ContentValues val = new ContentValues();
                val.put("rpc_message",obj.toString());



                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(val.toString());

                writer.flush();
                writer.close();

                conn.connect();
                int response = conn.getResponseCode();
                Log.v("Result:", "" + response);

                StringBuilder sbuilder = new StringBuilder();

                if (response == HttpURLConnection.HTTP_OK) {

                    input = conn.getInputStream();
                    String line;
                    BufferedReader buf = new BufferedReader(new InputStreamReader(input));
                    while ((line = buf.readLine()) != null) {
                        sbuilder.append(line);
                    }

                }
                //com o resultado, gera uma JSONArray
                twittes = new JSONArray(sbuilder.toString());
                JSONArray temp_array = new JSONArray();


                //Processa o array para adicionar em outro array as entradas ordenadas.
                //O processamento é ver quem tem a menor timestamp, ou seja, foi criado primeiro.
                while(twittes.length() > 0) {
                    Log.e("Faltam:",""+twittes.length());
                    long min = 9999999999l;
                    int index = -1;
                    JSONObject json_obj = new JSONObject();

                    for (int i = 0; i < twittes.length(); i++) {

                        JSONObject temp = new JSONObject(twittes.get(i).toString());
                        long temp_time =  Long.parseLong(temp.getString("time"));
                        if(temp_time < min){
                            min = temp_time;
                            index = i;
                            json_obj = temp;
                        }
                    }
                    temp_array.put(json_obj);
                    twittes.remove(index);
                }
                Log.e("Sorted",temp_array.toString()+"---Size: "+temp_array.length());

                return  temp_array.toString();


            }catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {

                //adiciona autores e textos a 2 arrays para uso no Adapter
                twittes_sort = new JSONArray(s);
                for (int i = 0; i < twittes_sort.length(); i++) {
                    JSONObject obj = twittes_sort.getJSONObject(i);
                    tAutores.add(obj.getString("autor"));
                    tTextos.add(obj.getString("texto"));
                }
                list.setAdapter(adapter);
            }catch (Exception e){

            }
        }
    }

}

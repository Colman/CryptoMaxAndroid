package com.maxtechnologies.cryptomax.Other;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.maxtechnologies.cryptomax.Callbacks.ArticlesCallback;
import com.maxtechnologies.cryptomax.Callbacks.BasicCallback;
import com.maxtechnologies.cryptomax.Callbacks.ProfilesCallback;
import com.maxtechnologies.cryptomax.Exchanges.Exchange;
import com.maxtechnologies.cryptomax.Objects.Article;
import com.maxtechnologies.cryptomax.Objects.Transaction;
import com.maxtechnologies.cryptomax.Wallets.Wallet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Colman on 19/05/2018.
 */

public class CryptoMaxApi {

    //Domain declaration
    private final static String domain = "http://gator4026.temp.domains/~cekoivisto/";

    //Filename declaration
    private final static String PROFILEFILENAME = "profile.sav";
    private final static String IMAGEFILENAME = "image.png";
    private final static String WALLETSFILENAME = "wallets.sav";

    //API declarations
    private static String id = "";
    private static String name = "";
    private static Bitmap image = null;
    private static ArrayList<Wallet> wallets = new ArrayList<>();

    //Transaction declaration
    public static ArrayList<Transaction> pendingTxs = new ArrayList<>();



    public static void init(final Context context, final BasicCallback callback) {
        loadProfile(context);
        loadImage(context);
        loadWallets(context);

        getInitData(new BasicCallback() {
            @Override
            public void onFailure(String reason) {
                callback.onFailure("Failed to get init data for reason: " + reason);
            }

            @Override
            public void onSuccess() {
                callback.onSuccess();
            }
        });
    }



    private static void loadProfile(Context context) {
        try {
            FileInputStream stream = context.openFileInput(PROFILEFILENAME);
            BufferedReader in = new BufferedReader(new InputStreamReader(stream));
            JSONObject jObj = new JSONObject(in.readLine());
            in.close();
            stream.close();
            id = jObj.getString("profile_id");
            name = jObj.getString("name");
        }

        catch(java.io.IOException | JSONException e) {
            byte[] bytes = new byte[16];
            new Random().nextBytes(bytes);
            CryptoMaxApi.id = Wallet.byteArrayToHexString(bytes);
            name = "";
        }
    }



    private static void saveProfile(Context context) {
        context.deleteFile(PROFILEFILENAME);

        JSONObject jObj = new JSONObject();
        try {
            FileOutputStream stream = context.openFileOutput(PROFILEFILENAME, 0);
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            jObj.put("profile_id", id);
            jObj.put("name", name);
            writer.write(jObj.toString());
            writer.close();
            stream.close();
        }

        catch(java.io.IOException | JSONException e) {
            //Do nothing
        }

        String url = domain + "api/app/profile.php";
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jObj.toString());
        Request request = new Request.Builder().url(url).post(body).build();
        StaticVariables.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //Do nothing
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                response.body().close();
            }
        });
    }



    private static void loadImage(Context context) {
        try {
            FileInputStream stream = context.openFileInput(IMAGEFILENAME);
            image = BitmapFactory.decodeStream(stream);
            stream.close();
        }

        catch(java.io.IOException e) {
            image = null;
        }
    }



    private static void saveImage(Context context) {
        context.deleteFile(IMAGEFILENAME);
        if(image != null) {
            try {
                FileOutputStream stream = context.openFileOutput(IMAGEFILENAME, 0);
                image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.close();
            }

            catch (java.io.IOException e) {
                //Do nothing
            }
        }


        String imageStr = null;
        if(image != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, stream);
            imageStr = Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
        }
        JSONObject jObj = new JSONObject();
        try {
            jObj.put("profile_id", id);
            jObj.put("image", imageStr);
        }
        catch(JSONException e) {
            //Do nothing
        }


        String url = domain + "api/app/image.php";
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jObj.toString());
        Request request = new Request.Builder().url(url).post(body).build();
        StaticVariables.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //Do nothing
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                response.body().close();
            }
        });
    }



    private static void loadWallets(Context context) {
        try {
            FileInputStream stream = context.openFileInput(WALLETSFILENAME);
            ObjectInputStream objStream = new ObjectInputStream(stream);
            wallets = (ArrayList<Wallet>) objStream.readObject();
            stream.close();
            objStream.close();
        }
        catch(java.io.IOException | java.lang.ClassNotFoundException e) {
            wallets = new ArrayList<>();
        }
    }



    public static void saveWallets(Context context, boolean upload) {
        context.deleteFile(WALLETSFILENAME);
        try {
            FileOutputStream fileStream = context.openFileOutput(WALLETSFILENAME, 0);
            ObjectOutputStream objStream = new ObjectOutputStream(fileStream);
            objStream.writeObject(wallets);
            objStream.close();
            fileStream.close();
        }

        catch(java.io.IOException e) {
            //Do nothing
        }


        if(upload) {
            JSONArray jArr = new JSONArray();
            for (Wallet wallet : wallets) {
                jArr.put(wallet.address);
            }
            JSONObject jObj = new JSONObject();
            try {
                jObj.put("profile_id", id);
                jObj.put("addresses", jArr);
            }
            catch (JSONException e) {
                //Do nothing
            }


            String url = domain + "api/app/wallet.php?mode=save";
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(JSON, jObj.toString());
            Request request = new Request.Builder().url(url).post(body).build();
            StaticVariables.client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    //Do nothing
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    response.body().close();
                }
            });
        }
    }



    private static void getInitData(final BasicCallback callback) {
        String url = domain + "api/app/init.txt";
        Request request = new Request.Builder().url(url).build();
        StaticVariables.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();

                try {
                    JSONObject initObj = new JSONObject(responseStr);
                    JSONObject fiatsObj = initObj.getJSONObject("fiats");
                    for(int j = 0; j < Exchange.fiats.length; j++) {
                        Exchange.fiats[j].perUS = BigDecimal.valueOf(fiatsObj.getDouble(Exchange.fiats[j].symbol)).floatValue();
                    }

                    JSONArray coinsArr = initObj.getJSONArray("coins");
                    Exchange.coinNames = new String[coinsArr.length()][4];
                    for(int i = 0; i < coinsArr.length(); i++) {
                        Exchange.coinNames[i][0] = coinsArr.getJSONObject(i).getString("symbol");
                        Exchange.coinNames[i][1] = coinsArr.getJSONObject(i).getString("name");
                        Exchange.coinNames[i][2] = coinsArr.getJSONObject(i).getString("market_cap_usd");
                        Exchange.coinNames[i][3] = coinsArr.getJSONObject(i).getString("available_supply");
                    }

                    callback.onSuccess();
                }

                catch(JSONException e) {
                    callback.onFailure("Invalid response JSON");
                }
            }
        });
    }



    public static String getId() {
        return id;
    }



    public static String getName() {
        return name;
    }



    public static void setName(String name, Context context) {
        CryptoMaxApi.name = name;
        saveProfile(context);
    }



    public static Bitmap getImage() {
        return image;
    }



    public static void setImage(Bitmap image, Context context) {
        CryptoMaxApi.image = image;
        saveImage(context);
    }



    public static Wallet getWallet(int index) {
        return wallets.get(index);
    }



    public static int getWalletsSize() {
        return wallets.size();
    }



    public static void addWallet(Wallet wallet, Context context) {
        wallets.add(wallet);
        saveWallets(context, true);
    }



    public static void removeWallets(int[] indices, Context context) {
        ArrayList<Wallet> newWallets = new ArrayList<>();
        for(int i = 0; i < wallets.size(); i++) {
            boolean in = false;
            for(int j : indices) {
                if(j == i) {
                    in = true;
                    break;
                }
            }
            if(!in) {
                newWallets.add(wallets.get(i));
            }
        }
        wallets = newWallets;

        saveWallets(context, true);
    }



    public static void sendEmail(String email, String code) {
        JSONObject jObj = new JSONObject();
        try {
            jObj.put("email", email);
            jObj.put("code", code);
        }
        catch(JSONException e) {
            //Do nothing
        }

        String url = domain + "api/app/wallet.php?mode=send";
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jObj.toString());
        final Request request = new Request.Builder().url(url).post(body).build();
        StaticVariables.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //Do nothing
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                response.body().close();
            }
        });
    }



    public static void addView(int articleId) {
        JSONObject jObj = new JSONObject();
        try {
            jObj.put("article_id", articleId);
            jObj.put("profile_id", id);
        }
        catch(JSONException e) {
            //Do nothing
        }


        String url = domain + "api/app/articles.php?mode=view";
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jObj.toString());
        Request request = new Request.Builder().url(url).post(body).build();
        StaticVariables.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //Do nothing
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                response.body().close();
            }
        });
    }



    public static void addVote(int articleId, int vote) {
        JSONObject jObj = new JSONObject();
        try {
            jObj.put("article_id", articleId);
            jObj.put("profile_id", id);
            jObj.put("vote", vote);
        }
        catch(JSONException e) {
            //Do nothing
        }


        String url = domain + "api/app/articles.php?mode=vote";
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jObj.toString());
        Request request = new Request.Builder().url(url).post(body).build();
        StaticVariables.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //Do nothing
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                response.body().close();
            }
        });
    }



    public static void getArticles(int start, int end, String source, int sort, String topic, final ArticlesCallback callback) {
        JSONObject jObj = new JSONObject();
        try {
            jObj.put("start_index", start);
            jObj.put("end_index", end);
            jObj.put("profile_id", id);
            jObj.put("source", source);
            jObj.put("sort", sort);
            jObj.put("topic", topic);
        }
        catch(JSONException e) {
            //Do nothing
        }


        String url = domain + "api/app/articles.php?mode=get";
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jObj.toString());
        Request request = new Request.Builder().url(url).post(body).build();
        StaticVariables.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();

                try {
                    JSONObject jObj = new JSONObject(responseStr);
                    if(jObj.getString("response").equals("Success")) {
                        JSONArray jArr = jObj.getJSONArray("message");
                        ArrayList<Article> newArticles = new ArrayList<>();
                        for(int i = 0; i < jArr.length(); i++) {
                            JSONObject jObj2 = jArr.getJSONObject(i);
                            int id = jObj2.getInt("article_id");
                            String source = jObj2.getString("src");
                            String headline = jObj2.getString("headline");
                            String imageStr = jObj2.getString("image");
                            String author = jObj2.getString("author");
                            String dateStr = jObj2.getString("date_posted");
                            String bodyHTML = jObj2.getString("body");
                            int views = jObj2.getInt("view_count");
                            int votes = jObj2.getInt("vote_count");
                            int myVote = jObj2.getInt("my_vote");

                            byte[] decodedString = Base64.decode(imageStr, Base64.DEFAULT);
                            Bitmap image = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                            format.setTimeZone(TimeZone.getTimeZone("UTC"));
                            Date date;
                            try {
                                date = format.parse(dateStr);
                            }
                            catch(ParseException e) {
                                date = new Date();
                            }

                            Article article = new Article(id, source, headline, image, author, date, bodyHTML, views, votes, myVote);
                            newArticles.add(article);
                        }

                        callback.onSuccess(newArticles);
                    }

                    else {
                        callback.onFailure(jObj.getString("message"));
                    }
                }

                catch(JSONException e) {
                    callback.onFailure("Invalid response JSON");
                }
            }
        });
    }



    public static void getProfiles(ArrayList<String> addresses, final ProfilesCallback callback) {
        JSONArray jArr = new JSONArray(addresses);
        String url = domain + "api/app/wallet.php?mode=profile";
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, jArr.toString());
        Request request = new Request.Builder().url(url).post(body).build();
        StaticVariables.client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseStr = response.body().string();

                try {
                    JSONObject jObj = new JSONObject(responseStr);
                    if(jObj.getString("response").equals("Failure")) {
                        callback.onFailure(jObj.getString("message"));
                    }

                    else {
                        JSONObject jObj2 = jObj.getJSONObject("message");
                        JSONArray jArr = jObj2.getJSONArray("names");
                        JSONArray jArr2 = jObj2.getJSONArray("images");
                        String[] names = new String[jArr.length()];
                        Bitmap[] images = new Bitmap[jArr2.length()];
                        for(int i = 0; i < jArr.length(); i++) {
                            names[i] = jArr.getString(i);
                            String imageStr = jArr2.getString(i);
                            byte[] bytes = Base64.decode(imageStr, Base64.DEFAULT);
                            images[i] = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        }
                        callback.onSuccess(names, images);
                    }
                }

                catch(org.json.JSONException e) {
                    callback.onFailure("Invalid response JSON");
                }
            }
        });
    }
}

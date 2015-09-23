package com.brandstore1.asynctasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.brandstore1.entities.User;
import com.brandstore1.interfaces.LoginAsyncResponse;
import com.brandstore1.interfaces.SignupAsyncResponse;
import com.brandstore1.model.Connection;
import com.brandstore1.utils.CircularProgressDialog;
import com.brandstore1.utils.Connections;
import com.brandstore1.utils.MySharedPreferences;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by I076630 on 06-May-15.
 */
public class SignupAsyncTask extends AsyncTask<Void,Void,String> {


    String name;
    String emailId;
    String password;
    String genderCode;
    String dobString;
    Context mContext;
    User user ;
    CircularProgressDialog circularProgressDialog;
    public SignupAsyncResponse signupAsyncResponseDelegate =null;

    public SignupAsyncTask(String name,
                           String emailId,
                           String password,
                           String genderCode,
                           String dobString,
                           Context mContext)
    {
        this.name = name;
        this.emailId=emailId;
        this.password=password;
        this.genderCode = genderCode;
        this.dobString = dobString;
        this.mContext = mContext;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        circularProgressDialog = new CircularProgressDialog(this.mContext);
        circularProgressDialog = CircularProgressDialog.show(this.mContext,"","");
    }

    @Override
    protected String doInBackground(Void... params) {
        StringBuilder builder = null;
        try {
            String urlString = new Connections().getSignUpURL(name, emailId, password, genderCode,dobString);
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            String line;
            builder = new StringBuilder();
            InputStreamReader isr = new InputStreamReader(
                    urlConnection.getInputStream()
            );
            BufferedReader reader = new BufferedReader(isr);
            while ((line = reader.readLine()) != null) builder.append(line);

            return (builder.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String returnValue) {
        super.onPostExecute(returnValue);
        try {
            JSONObject jsonObject = new JSONObject(returnValue);
            Log.i("Signup", "JSON :" + jsonObject.toString());

            // If jsonArray.length is 0, user does not exist. So show Toast that user/password combination is invalid.
            // If jsonArray.length is 1, extract and add to user entity. Save userid and Go to MainActivity.
            // TODO: If jsonArray.length is more than 1, do as previous , but there is an error


            if(jsonObject==null ){
                Toast.makeText(mContext, "Connection Error.", Toast.LENGTH_LONG).show();
            }
            else{
                if(jsonObject.getString("responseState").equals("error")){
                    Toast.makeText(mContext, "Email exists", Toast.LENGTH_LONG).show();
                }
                else if(jsonObject.getString("responseState").equals("created")) {
                    JSONObject userObject = jsonObject.getJSONObject("responseDetails");
                    user = new User();
                    user.setUserId(userObject.getString("userID"));
                    user.setEmailId(userObject.getString("emailid"));
                    user.setName(userObject.getString("name"));

                    //user.setPassword(jsonObject.getString("password"));

                    // Convert user object to json object
                    Gson gson = new Gson();
                    String userJsonObject = gson.toJson(user);

                    // Save userid and userJsonObject to SharedPreferences
                    MySharedPreferences.setUserId(mContext, user.getUserId());
                    MySharedPreferences.setUserJsonObjectString(mContext, userJsonObject);
                    MySharedPreferences.setHasLoggedIn(mContext, true);

                    Connections.setUserIdFromSharedPreferences(mContext);

                    signupAsyncResponseDelegate.updateSuggestionInSQLite();
                    signupAsyncResponseDelegate.goToMainActivityScreen();
                }

            }

            circularProgressDialog.dismiss();









        } catch (JSONException e) {
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }


    }
}

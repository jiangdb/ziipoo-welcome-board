package com.vehicle.framework.api.requests;

import android.util.Log;

import com.vehicle.framework.api.ApiCallback;
import com.vehicle.framework.api.ApiClient;
import com.vehicle.framework.api.ApiError;
import com.vehicle.framework.api.ApiInterface;
import com.vehicle.framework.api.responses.BaseResponse;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BaseRequest {
    protected ApiCallback apiCallback;
    protected ApiInterface apiInterface;
    private static String error_message = "Couldn\\'t connect to server. Please try again later!";

    protected BaseRequest(ApiCallback apiCallback) {
        this.apiCallback = apiCallback;
        apiInterface = ApiClient.getInstance().getApiInterface();
    }

    protected Callback callback = new Callback() {
        @Override
        public void onResponse(Call call, Response response) {
            Log.d("WeatherActivity",response.toString());
            if (response.isSuccessful() && response.body() != null && response.body() instanceof BaseResponse) {
                BaseResponse res = (BaseResponse) response.body();
                if (res.getStatus().equals("1000")) {
                    onApiSuccess(response.body());
                } else {
                    onApiError(ApiError.ERROR.REQUEST_ERROR, response.code(), res.getDescription());
                }
                return;
            }
            handleError(response.code(), response.errorBody());
        }

        @Override
        public void onFailure(Call call, Throwable t) {
            onApiError(ApiError.ERROR.NETWORK_ERROR, -1, error_message);
        }
    };

    protected void handleError(int responseCode, ResponseBody errorBody) {
        String errorMessage = "parsing error";
        try {
            JSONObject jObjError = new JSONObject(errorBody.string());
            errorMessage = jObjError.getString("desc");

        } catch (Exception e) {
        }
        onApiError(ApiError.ERROR.SERVER_ERROR, responseCode, errorMessage);
    }

    protected void onApiError(ApiError.ERROR error, int responseCode, String message) {
        if (apiCallback != null) {
            apiCallback.onError(new ApiError(error, responseCode, message));
        }
    }

    protected void onApiSuccess(Object response) {
    }
}

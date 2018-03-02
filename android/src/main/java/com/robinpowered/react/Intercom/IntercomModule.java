package com.robinpowered.react.Intercom;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.intercom.android.sdk.experimental.Intercom;
import io.intercom.android.sdk.Intercom.Visibility;
import io.intercom.android.sdk.UserAttributes;
import io.intercom.android.sdk.experimental.IntercomSettings;
import io.intercom.android.sdk.identity.Registration;
import io.intercom.android.sdk.push.IntercomPushClient;

public class IntercomModule extends ReactContextBaseJavaModule {

    private static final String MODULE_NAME = "IntercomWrapper";
    public static final String TAG = "Intercom";

    private final IntercomPushClient intercomPushClient = new IntercomPushClient();

    public IntercomModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @ReactMethod
    public void boot(ReadableMap options, Callback callback) {
        String apiKey = options.hasKey("apiKey") ? options.getString("apiKey") : null;
        String appId = options.hasKey("appId") ? options.getString("appId") : null;
        String userId = options.hasKey("userId") ? options.getString("userId") : null;
        String email = options.hasKey("email") ? options.getString("email") : null;
        String userHash = options.hasKey("userHash") ? options.getString("userHash") : null;

        IntercomSettings settings = IntercomSettings.create();

        if (!TextUtils.isEmpty(apiKey) && !TextUtils.isEmpty(appId)) {
            settings = settings
                    .withApiKey(apiKey)
                    .withAppId(appId);
        } else {
            callback.invoke("Invalid apiKey or appId");
            return;
        }

        Application application = getCurrentActivity() != null ? getCurrentActivity().getApplication() : null;
        if (application == null) {
            callback.invoke("Current activity is null.");
            return;
        }

        if (TextUtils.isEmpty(userId) && TextUtils.isEmpty(email)) {
            // Boot with an unidentified user.
            Intercom.boot(application, settings);
            Log.i(TAG, "Boot with unidentified user.");
            callback.invoke(null, null);
            return;
        }

        if (!TextUtils.isEmpty(userId))
            settings = settings.withUserId(userId);

        if (!TextUtils.isEmpty(email))
            settings = settings.withUserId(email);

        if (!TextUtils.isEmpty(userHash))
            settings = settings.withUserHash(userHash);

        // Boot with an identified user.
        Intercom.boot(application, settings);
        Log.i(TAG, "Boot with identified user.");
        callback.invoke(null, null);
    }

    @ReactMethod
    public void shutdown(@Nullable Callback callback) {
        Intercom.client().shutdown();
        Log.i(TAG, "shutdown");
        if (callback != null) {
            callback.invoke(null, null);
        }
    }

    @ReactMethod
    public void sendTokenToIntercom(String token, Callback callback) {
        if (getCurrentActivity() != null) {
            intercomPushClient.sendTokenToIntercom(getCurrentActivity().getApplication(), token);
            Log.i(TAG, "sendTokenToIntercom");
            callback.invoke(null, null);
        } else {
            Log.e(TAG, "sendTokenToIntercom; getCurrentActivity() is null");
        }
    }

    @ReactMethod
    public void updateUser(ReadableMap options, Callback callback) {
        try {
            UserAttributes userAttributes = convertToUserAttributes(options);
            Intercom.client().updateUser(userAttributes);
            Log.i(TAG, "updateUser");
            callback.invoke(null, null);
        } catch (Exception e) {
            Log.e(TAG, "updateUser - unable to deconstruct argument map");
            callback.invoke(e.toString());
        }
    }

    @ReactMethod
    public void logEvent(String eventName, @Nullable ReadableMap metaData, Callback callback) {
        try {
            if (metaData == null) {
                Intercom.client().logEvent(eventName);
            }
            if (metaData != null) {
                Map<String, Object> deconstructedMap = recursivelyDeconstructReadableMap(metaData);
                Intercom.client().logEvent(eventName, deconstructedMap);
            }
            Log.i(TAG, "logEvent");
            callback.invoke(null, null);
        } catch (Exception e) {
            Log.e(TAG, "logEvent - unable to deconstruct metaData");
            callback.invoke(e.toString());
        }
    }

    @ReactMethod
    public void handlePushMessage(Callback callback) {
        Intercom.client().handlePushMessage();
        callback.invoke(null, null);
    }

    @ReactMethod
    public void displayMessenger(Callback callback) {
        Intercom.client().displayMessenger();
        callback.invoke(null, null);
    }

    @ReactMethod
    public void hideMessenger(Callback callback) {
        Intercom.client().hideMessenger();
        callback.invoke(null, null);
    }

    @ReactMethod
    public void displayMessageComposer(Callback callback) {
        Intercom.client().displayMessageComposer();
        callback.invoke(null, null);
    }

    @ReactMethod
    public void displayMessageComposerWithInitialMessage(String message, Callback callback) {
        Intercom.client().displayMessageComposer(message);
        callback.invoke(null, null);
    }

    @ReactMethod
    public void setUserHash(String userHash, Callback callback) {
        Intercom.client().setUserHash(userHash);
        callback.invoke(null, null);
    }

    @ReactMethod
    public void displayConversationsList(Callback callback) {
        Intercom.client().displayConversationsList();
        callback.invoke(null, null);
    }

    @ReactMethod
    public void getUnreadConversationCount(Callback callback) {

        try {
            int conversationCount = Intercom.client().getUnreadConversationCount();

            callback.invoke(null, conversationCount);
        } catch (Exception ex) {
            Log.e(TAG, "logEvent - unable to get conversation count");
            callback.invoke(ex.toString());
        }
    }

    @ReactMethod
    public void displayHelpCenter(Callback callback) {
        Intercom.client().displayHelpCenter();
        callback.invoke(null, null);
    }

    private Visibility visibilityStringToVisibility(String visibility) {
      if (visibility.equalsIgnoreCase("VISIBLE")) {
        return Visibility.VISIBLE;
      } else {
        return Visibility.GONE;
      }
    }

    @ReactMethod
    public void setLauncherVisibility(String visibility, Callback callback) {
        Visibility intercomVisibility = visibilityStringToVisibility(visibility);

        try {
            Intercom.client().setLauncherVisibility(intercomVisibility);

            callback.invoke(null, null);
        } catch (Exception ex) {
            callback.invoke(ex.toString());
        }
    }

    @ReactMethod
    public void setInAppMessageVisibility(String visibility, Callback callback) {
        Visibility intercomVisibility = visibilityStringToVisibility(visibility);

        try {
            Intercom.client().setInAppMessageVisibility(intercomVisibility);

            callback.invoke(null, null);
        } catch (Exception ex) {
            callback.invoke(ex.toString());
        }
    }
    
    @ReactMethod
    public void setBottomPadding( Integer padding, Callback callback) {
         Intercom.client().setBottomPadding(padding);
         Log.i(TAG, "setBottomPadding");
         callback.invoke(null, null);
    }

    private UserAttributes convertToUserAttributes(ReadableMap readableMap) {
        Map<String, Object> map = recursivelyDeconstructReadableMap(readableMap);
        UserAttributes.Builder builder = new UserAttributes.Builder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key.equals("email")) {
                builder.withEmail((String)value);
            } else if (key.equals("userId")) {
                builder.withUserId((String)value);
            } else if (key.equals("name")) {
                builder.withName((String)value);
            } else if (key.equals("phone")) {
                builder.withPhone((String)value);
            } else if (key.equals("languageOverride")) {
                builder.withLanguageOverride((String)value);
            } else if (key.equals("signedUpAt")) {
                Date dateSignedUpAt = new Date(((Number)value).longValue());
                builder.withSignedUpAt(dateSignedUpAt);
            } else if (key.equals("unsubscribedFromEmails")) {
                builder.withUnsubscribedFromEmails((Boolean)value);
            } else if (key.equals("custom_attributes")) {
                // value should be a Map here
                builder.withCustomAttributes((Map)value);
            } else if (key.equals("companies")) {
                Log.w(TAG, "Not implemented yet");
                // Note that this parameter is companies for iOS and company for Android
            }
        }
        return builder.build();
    }


    private Map<String, Object> recursivelyDeconstructReadableMap(ReadableMap readableMap) {
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        Map<String, Object> deconstructedMap = new HashMap<>();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            ReadableType type = readableMap.getType(key);
            switch (type) {
                case Null:
                    deconstructedMap.put(key, null);
                    break;
                case Boolean:
                    deconstructedMap.put(key, readableMap.getBoolean(key));
                    break;
                case Number:
                    deconstructedMap.put(key, readableMap.getDouble(key));
                    break;
                case String:
                    deconstructedMap.put(key, readableMap.getString(key));
                    break;
                case Map:
                    deconstructedMap.put(key, recursivelyDeconstructReadableMap(readableMap.getMap(key)));
                    break;
                case Array:
                    deconstructedMap.put(key, recursivelyDeconstructReadableArray(readableMap.getArray(key)));
                    break;
                default:
                    throw new IllegalArgumentException("Could not convert object with key: " + key + ".");
            }

        }
        return deconstructedMap;
    }

    private List<Object> recursivelyDeconstructReadableArray(ReadableArray readableArray) {
        List<Object> deconstructedList = new ArrayList<>(readableArray.size());
        for (int i = 0; i < readableArray.size(); i++) {
            ReadableType indexType = readableArray.getType(i);
            switch(indexType) {
                case Null:
                    deconstructedList.add(i, null);
                    break;
                case Boolean:
                    deconstructedList.add(i, readableArray.getBoolean(i));
                    break;
                case Number:
                    deconstructedList.add(i, readableArray.getDouble(i));
                    break;
                case String:
                    deconstructedList.add(i, readableArray.getString(i));
                    break;
                case Map:
                    deconstructedList.add(i, recursivelyDeconstructReadableMap(readableArray.getMap(i)));
                    break;
                case Array:
                    deconstructedList.add(i, recursivelyDeconstructReadableArray(readableArray.getArray(i)));
                    break;
                default:
                    throw new IllegalArgumentException("Could not convert object at index " + i + ".");
            }
        }
        return deconstructedList;
    }
}


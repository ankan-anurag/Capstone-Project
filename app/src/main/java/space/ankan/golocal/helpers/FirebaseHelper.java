package space.ankan.golocal.helpers;

import android.net.Uri;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import space.ankan.golocal.core.AppConstants;
import space.ankan.golocal.model.kitchens.Dish;
import space.ankan.golocal.model.kitchens.Kitchen;
import space.ankan.golocal.model.users.User;

/**
 * Created by ankan.
 * TODO: Add a class comment
 */

public class FirebaseHelper implements AppConstants {

    private FirebaseApp app;
    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private static boolean isPersistent;

    private DatabaseReference usersRef;
    private DatabaseReference channelsRef;
    private DatabaseReference kitchensRef;
    private DatabaseReference geoFireRef;
    private GeoFire geoFire;

    private StorageReference kitchenImagesRef;
    private StorageReference chatImagesRef;

    public FirebaseHelper() {
        if (!isPersistent) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            isPersistent = true;
        }
        app = FirebaseApp.getInstance();
        database = FirebaseDatabase.getInstance(app);
        auth = FirebaseAuth.getInstance(app);
        storage = FirebaseStorage.getInstance(app);

        usersRef = database.getReference(FIREBASE_DB_USERS);
        channelsRef = database.getReference(FIREBASE_DB_CHANNELS);
        kitchensRef = database.getReference(FIREBASE_DB_KITCHENS);
        geoFireRef = database.getReference(FIREBASE_DB_GEOFIRE);
        geoFire = new GeoFire(geoFireRef);

        kitchenImagesRef = storage.getReference(FIREBASE_STORAGE_KITCHEN_ROOT);
        chatImagesRef = storage.getReference(FIREBASE_STORAGE_CHAT_IMAGES);


    }

    public DatabaseReference getUsersReference() {
        return usersRef;
    }

    public DatabaseReference getChannelsReference() {
        return channelsRef;
    }

    public DatabaseReference getKitchensReference() {
        return kitchensRef;
    }

    public StorageReference getDishImagesReference() {
        return kitchenImagesRef;
    }

    public StorageReference getChatImagesReference() {
        return chatImagesRef;
    }

    public DatabaseReference getUserReference(String user) {
        return database.getReference(FIREBASE_DB_USERS + FOLDER_SEPARATOR + user);
    }

    public DatabaseReference getCurrentUserReference() {
        String user = auth.getCurrentUser().getUid();
        return database.getReference(FIREBASE_DB_USERS + FOLDER_SEPARATOR + user);
    }

    public DatabaseReference getChannelReference(String channel) {
        return database.getReference(FIREBASE_DB_CHANNELS + FOLDER_SEPARATOR + channel);
    }

    public DatabaseReference getKitchenReference(String kitchen) {
        return database.getReference(FIREBASE_DB_KITCHENS + FOLDER_SEPARATOR + kitchen);
    }

    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public FirebaseAuth getFirebaseAuth() {
        return auth;
    }

    public void enrollNewUser() {
        getCurrentUserReference().setValue(new User(null, auth.getCurrentUser().getDisplayName()));
    }

    public String getCurrentUserUid() {
        return auth.getCurrentUser().getUid();
    }

    public void push(Dish dish, String kitchenId) {
        getKitchenReference(kitchenId).child(FIREBASE_DB_KITCHENS_DISHES).push().setValue(dish);

    }

    public String push(Kitchen kitchen) {
        DatabaseReference kitchenRef = getKitchensReference().push();
        kitchenRef.setValue(kitchen);
        Map<String, Object> map = new HashMap<>();
        map.put(User.KITCHEN, kitchenRef.getKey());
        getCurrentUserReference().updateChildren(map);
        String key = kitchenRef.getKey();
        geoFire.setLocation(key, new GeoLocation(kitchen.latitude, kitchen.longitude));
        return key;
    }

    public GeoQuery buildQueryForKitchens(GeoLocation geo, double rangeInKms) {
        return geoFire.queryAtLocation(geo, rangeInKms);
    }

    public DatabaseReference getDishesReference(String kitchenId) {
        return getKitchenReference(kitchenId).child(FIREBASE_DB_KITCHENS_DISHES);
    }

    public DatabaseReference getUserChannels() {
        return getCurrentUserReference().child(FIREBASE_DB_USERS_CHANNELS);
    }

    public DatabaseReference getUserChannels(String userId) {
        return getUserReference(userId).child(FIREBASE_DB_USERS_CHANNELS);
    }

    public UploadTask pushImage(Uri imageUri) {
        // Get a reference to the location where we'll store our photos
        // Get a reference to store file at chat_photos/<FILENAME>
        final StorageReference photoRef = chatImagesRef.child(imageUri.getLastPathSegment());
        // Upload file to Firebase Storage
        return photoRef.putFile(imageUri);


    }

    public UploadTask pushProfileImage(Uri imageUri, String userId) {
        // Get a reference to the location where we'll store our photos
        // Get a reference to store file at chat_photos/<FILENAME>
        final StorageReference photoRef = kitchenImagesRef.child(userId);
        // Upload file to Firebase Storage
        return photoRef.putFile(imageUri);


    }


}

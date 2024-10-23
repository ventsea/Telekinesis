package com.st.letter.lib.media;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

public class LocalContacts {

    private static final String TAG = "LocalContacts";

    private static final Uri URI = ContactsContract.Contacts.CONTENT_URI;

    private static final Uri DATA_URI = ContactsContract.Data.CONTENT_URI;

    private static final String[] INFO = new String[]{
            String.valueOf(ContactsContract.Contacts._ID)
    };

    private static final String[] DATA1_INFO = new String[]{
            ContactsContract.Data.DATA1
    };

    private static final Uri FAST_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

    private static final String[] FAST_INFO = new String[]{
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
    };


    public static List<Contact> fastScanAllContacts(Context context) {
        LinkedHashMap<String, Contact> cache = new LinkedHashMap<>();
        try {
            ContentResolver resolver = context.getApplicationContext().getContentResolver();
            Cursor cursor = resolver.query(FAST_URI, FAST_INFO, null, null, "sort_key");
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        String id = cursor.getString(0);
                        if (id == null) continue;

                        Contact contact = cache.get(id);
                        if (contact == null) {
                            contact = new Contact();
                            contact.phone = new ArrayList<>();
                        }
                        contact.id = id;

                        contact.name = cursor.getString(1);

                        contact.phone.add(cursor.getString(2));

                        cache.put(id, contact);
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.d(TAG, "fastScanAllContacts error");
        }
        return new ArrayList<>(cache.values());
    }

    public static void insertAllContacts(Context context, List<Contact> contacts, ContactsInsertListener listener) {
        if (contacts == null || contacts.size() == 0) {
            return;
        }
        try {
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();
            int rawContactInsertIndex;
            for (int i = 0; i < contacts.size(); i++) {
                Contact contact = contacts.get(i);
                if (contact == null) continue;
                rawContactInsertIndex = ops.size();

                ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                        .withYieldAllowed(true)
                        .build());

                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.name)
                        .withYieldAllowed(true)
                        .build());

                if (contact.phone == null || contact.phone.size() == 0) continue;

                for (String number : contact.phone) {
                    ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                            .withYieldAllowed(true)
                            .build());
                }
            }
            try {
                context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                if (listener != null) listener.onInsertFinish();
            } catch (RemoteException e) {
                Log.e(TAG, "insertAllContacts RemoteException " + e.getMessage());
                if (listener != null) listener.onInsertError();
            } catch (OperationApplicationException e) {
                Log.e(TAG, "insertAllContacts OperationApplicationException " + e.getMessage());
                if (listener != null) listener.onInsertError();
            }
        } catch (Exception e) {
            if (listener != null) listener.onInsertError();
        }
    }

    public static List<Contact> scanAllContacts(Context context) {
        List<Contact> contacts = new ArrayList<>();
        ContentResolver resolver = context.getApplicationContext().getContentResolver();
        Cursor cursor = resolver.query(URI, INFO, null, null, "sort_key");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);//获取 id 所在列的索引
                do {
                    Contact contact = new Contact();
                    contact.id = cursor.getString(idIndex);
                    String name = getData1(resolver, contact.id, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
                    if (!TextUtils.isEmpty(name)) {
                        String[] split = name.split("\\|");
                        contact.name = split[0];
                    } else {
                        continue;
                    }

                    String phone = getData1(resolver, contact.id, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                    if (!TextUtils.isEmpty(phone)) {
                        String[] split = phone.split("\\|");
                        contact.phone = Arrays.asList(split);
                    }

                    String email = getData1(resolver, contact.id, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
                    if (!TextUtils.isEmpty(email)) {
                        String[] split = email.split("\\|");
                        contact.email = Arrays.asList(split);
                    }

                    contacts.add(contact);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return contacts;
    }

    private static String getData1(ContentResolver resolver, String contactId, String mimeType) {
        StringBuilder builder = new StringBuilder();
        Cursor cursor = resolver.query(DATA_URI, DATA1_INFO,
                ContactsContract.Data.CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + "='" + mimeType + "'",
                new String[]{contactId},
                null);
        if (cursor != null) {
            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                int dataIndex = cursor.getColumnIndex(ContactsContract.Data.DATA1);
                do {
                    builder.append(cursor.getString(dataIndex));
                    builder.append("|");//多个值,之间的分隔符.自定义;
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return builder.toString();
    }

    public static class Contact implements LocalBean {
        public String id;
        public String name;
        public List<String> phone;
        public List<String> email;

        public Contact() {

        }

        @Override
        public String toString() {
            return "Contact{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", phone=" + phone +
                    ", email=" + email +
                    '}';
        }

        @Override
        public void buildCorrectFileBean(String ip, int port) {

        }
    }

    public interface ContactsInsertListener {
        void onInsertFinish();

        void onInsertError();
    }
}
